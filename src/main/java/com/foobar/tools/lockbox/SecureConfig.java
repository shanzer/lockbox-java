
package com.foobar.tools.lockbox;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.codahale.shamir.Scheme;


class SecureConfig {
    public static final long serialVersionUID = 0xcafebaba;
    private Properties	standardProps;
    private Properties	unlockedProps;
    private String		propsFile;
    private boolean		protectedProps;
    private byte[]		aKey;

    public SecureConfig() {
    	standardProps = null;
    	unlockedProps = null;
    	protectedProps = false;
    }
   
    public SecureConfig(String configFile, byte applicationKey[]) throws SecureConfigException {
        this.Initialize(configFile, applicationKey);
    }

    public void Initialize(String configFile, byte applicationKey[]) throws SecureConfigException {
    	this.propsFile = configFile;
        InputStream input = null;
        protectedProps = false;
        standardProps = new Properties();

        try {
			this.aKey = CryptoUtils.SHA256(applicationKey);
		} catch (Exception e) {
			throw new SecureConfigException(e);
		}

        try {
        	input = new FileInputStream(propsFile);
        	standardProps.load(input);
        } catch (IOException ex) {
                throw new SecureConfigException(ex);
        } finally {
        	if (input != null) {
        		try {
        			input.close();
        		} catch (IOException e) {
        		}
        	}
        }
        String configState = standardProps.getProperty(ConfigConstants.SECURE_ENABLE_PROPS);
        if (configState == null) {
        	protectedProps = false;
        } else if (configState.compareToIgnoreCase("initialize") == 0) {
        	// initalize lockbox save new props file and return props
        	lockConfig(standardProps.getProperty(ConfigConstants.SECURE_RECOVER_PASSWD), standardProps);
        	protectedProps = true;
        } else if (configState.compareToIgnoreCase("true") == 0) {
        	// Read in props. Unlock secure props and put them in properties object
        	protectedProps = true;
        } else if (configState.compareToIgnoreCase("reset") == 0) {
        	relockConfig(standardProps.getProperty(ConfigConstants.SECURE_RECOVER_PASSWD));
        	protectedProps = true;
        } else {
        	protectedProps = false;
        }
        return;
    }

    public boolean isProtected() {
    	return this.protectedProps;
    }
    
    private void relockConfig(String password) throws SecureConfigException {
    	byte [] iv = null;
    	byte [] cipher = null;
    	unlockedProps = new Properties();
    	Set<String> propKeys = standardProps.stringPropertyNames();
    	for(String pk : propKeys) {
    		if (pk.compareTo(ConfigConstants.SECURE_VAULT) == 0) {
    			cipher = Base64.getDecoder().decode(standardProps.getProperty(ConfigConstants.SECURE_VAULT));
    		} else if (pk.compareTo(ConfigConstants.SECURE_VAULT_IV) == 0) {
    			iv = Base64.getDecoder().decode(standardProps.getProperty(ConfigConstants.SECURE_VAULT_IV));
    		} else {
    			unlockedProps.setProperty(pk, standardProps.getProperty(pk));
    		}
    	}
    	ByteArrayInputStream bis = null;
    	try {
    		byte clearProps[] = CryptoUtils.Decrypt(cipher, iv, aKey);
    		bis = new ByteArrayInputStream(clearProps);
    		Properties lockProps = new Properties();
    		lockProps.load(bis);
    		bis.close();
    		lockProps.list(System.out);
    		byte [] encryptedMK = Base64.getDecoder().decode(lockProps.getProperty(ConfigConstants.SECURE_RECOVER_KEY));
    		byte [] recoveryIV = Base64.getDecoder().decode(lockProps.getProperty(ConfigConstants.SECURE_RECOVER_KEY_IV));
    		byte [] masterKey = CryptoUtils.Decrypt(encryptedMK, recoveryIV, CryptoUtils.ValueToKeybyte(password.getBytes(), recoveryIV, 1024));

    		String keyHash = lockProps.getProperty(ConfigConstants.MASTER_KEY_HASH);
    		if (keyHash.compareTo(Base64.getEncoder().encodeToString(CryptoUtils.SHA256(masterKey))) != 0) {
    			throw new SecureConfigException("Cannot recustruct config encryption key");
    		}
    		// OK now that we have the master key, we need to decrypt the sensistive properties and put the clear values
    		// in the property object we are returning.
    		byte [] propertyIV = Base64.getDecoder().decode(lockProps.getProperty(ConfigConstants.SECURE_PROPS_PROPERTY_IV));
    		propKeys = lockProps.stringPropertyNames();
    		for(String pk: propKeys) {
    			if (pk.startsWith(ConfigConstants.SECURE_PROPS_SECURED_PROPERTY_PREFIX) == true) {
    				String propName = pk.substring(ConfigConstants.SECURE_PROPS_SECURED_PROPERTY_PREFIX.length());
    	    		unlockedProps.setProperty( ConfigConstants.SECURE_PROPERTY_PREFIX + propName, new String(CryptoUtils.Decrypt(Base64.getDecoder().decode(lockProps.getProperty(pk)), propertyIV, masterKey)));
    			}
    		}
    		unlockedProps.setProperty(ConfigConstants.SECURE_RECOVER_PASSWD, password);
    		lockConfig(password, unlockedProps);
    		unlockedProps = null;
    	} catch (Exception e) {
    		throw new SecureConfigException(e);
    	} finally {
    		try { if (bis != null) bis.close(); } catch(Exception e1) {}
    	}
    }
    
    private void lockConfig(String password, Properties lp) throws SecureConfigException {
    	Properties lockProps = new Properties();
    	if (password == null || password.length() == 0) {
    		throw new IllegalArgumentException("Recovery password must be set");
    	}
    	try {    	
    		// Generate 256bit AES key for the master key for the lockbox
    		byte [] masterKey = CryptoUtils.GenerateRandomKey(32);
    		lockProps.setProperty(ConfigConstants.MASTER_KEY_HASH, Base64.getEncoder().encodeToString(CryptoUtils.SHA256(masterKey)));
    	
    		// Generate a 128 bit IV to encrypt the recovery password.
    		byte [] pwIV = CryptoUtils.GenerateRandomData(16);

    		byte [] encryptedPW = CryptoUtils.Encrypt(masterKey, pwIV, CryptoUtils.ValueToKeybyte(password.getBytes(), pwIV, 1024));
    		lockProps.setProperty(ConfigConstants.SECURE_RECOVER_KEY, Base64.getEncoder().encodeToString(encryptedPW));
    		lockProps.setProperty(ConfigConstants.SECURE_RECOVER_KEY_IV, Base64.getEncoder().encodeToString(pwIV));
    		lockProps.setProperty(ConfigConstants.SECURE_PROPS_VERSION, "1.0");
    	
    		// We now get the system values.
    		// The system values come back as byte arrays suitable for use as keys.
    		// This is done via a PBKDF function which uses the "application" key as a salt and 
    		// a hard coded iteration count
    		byte [][] systemValues = SystemValues.getValues(this.aKey);
    		// We are going to need 70% of the system values to be the same in order for the configuration to be unlocked.
    		// So we create the splits based on that.
    		int numParts = systemValues.length;
    		int minParts = (int) Math.ceil(numParts * .70);
    		Scheme bloomScheme  = Scheme.of(numParts, minParts);    
    		lockProps.setProperty(ConfigConstants.SECURE_VAULT_M, Integer.toString(minParts));
    		lockProps.setProperty(ConfigConstants.SECURE_VAULT_N, Integer.toString(numParts));
    		
    		Map<Integer, byte[]> parts = bloomScheme.split(masterKey);    	

    		// We are now going to take each part from the master key split and encrypt it with a key derived from the system values.
    		for(int partCount = 0; partCount < numParts; partCount++) {
    			byte iv[] = CryptoUtils.GenerateRandomData(16);
    			// The bloom-shamir code has the part map start at 1 instead of 0.
    			byte cipher[] = CryptoUtils.Encrypt(parts.get(partCount+1), iv, systemValues[partCount]);
    			byte hash[] = CryptoUtils.SHA256(parts.get(partCount+1));
    			lockProps.setProperty(ConfigConstants.SECURE_PROPS_PARAMS+ partCount + ".cipher", Base64.getEncoder().encodeToString(cipher));
    			lockProps.setProperty(ConfigConstants.SECURE_PROPS_PARAMS + partCount + ".iv", Base64.getEncoder().encodeToString(iv));
    			lockProps.setProperty(ConfigConstants.SECURE_PROPS_PARAMS + partCount + ".hash", Base64.getEncoder().encodeToString(hash));
    		}
    		// OK we now are going to parse through the properties file provided. Pass on the non-protected values
    		// and encrypt / remove the protected values.
    		byte propertyIV[] = CryptoUtils.GenerateRandomData(16);
    		lockProps.setProperty(ConfigConstants.SECURE_PROPS_PROPERTY_IV, Base64.getEncoder().encodeToString(propertyIV));
    		Set<String> propKeys = lp.stringPropertyNames();
    	    for (String pk : propKeys) {
    	    	if (pk.startsWith(ConfigConstants.SECURE_PROPERTY_PREFIX) == true) {
    	    		String propName = ConfigConstants.SECURE_PROPS_SECURED_PROPERTY_PREFIX + (pk.substring(ConfigConstants.SECURE_PROPERTY_PREFIX.length()));
    	    		lockProps.setProperty(propName, Base64.getEncoder().encodeToString(CryptoUtils.Encrypt(lp.getProperty(pk).getBytes(), propertyIV, masterKey)));
    	    		lp.remove(pk);
    	    	}
    	    }
    	    // We stored the recovery password protected, so now we can remove it from the properties
    	    lp.remove(ConfigConstants.SECURE_RECOVER_PASSWD);
    	    // Set the enabled flag to true now that we are all done
    	    lp.setProperty(ConfigConstants.SECURE_ENABLE_PROPS, "true");
    	    
    	    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    	    lockProps.store(bos,null);
    	    // Just for some more obfucation we are going to encrypt the entire secure config part of the properties file 
    	    // using the application key and a hash of the application key as the IV. Not the best practice, but it works
    	    // for what we need.
    	    byte [] secureIV = CryptoUtils.GenerateRandomData(16);
    	    byte secureConfig[] = CryptoUtils.Encrypt(bos.toByteArray(), secureIV, aKey);
    	    bos.close();
    	    lp.setProperty(ConfigConstants.SECURE_VAULT, Base64.getEncoder().encodeToString(secureConfig));
    	    lp.setProperty(ConfigConstants.SECURE_VAULT_IV, Base64.getEncoder().encodeToString(secureIV));
    	    FileOutputStream fos = null;
    	    try {
    	    	fos = new FileOutputStream(new File(this.propsFile));
    	    	lp.store(fos, null);
    	    	
    	    } catch(Exception e1) {
    	    	
    	    } finally {
    	    	if (fos != null) { try { fos.close(); } catch(Exception e2) {}}
    	    }
    	} catch(Exception e) {
    		throw new SecureConfigException(e);
    	}
    	
    }

    public void resetProtection(String password) throws SecureConfigException {

    }

    public Properties getProperties(boolean allowRefresh) throws SecureConfigException {
    	if (protectedProps == false) {
    		return standardProps;
    	}
    	byte [] iv = null;
    	byte [] cipher = null;
    	unlockedProps = new Properties();
    	Set<String> propKeys = standardProps.stringPropertyNames();
    	for(String pk : propKeys) {
    		if (pk.compareTo(ConfigConstants.SECURE_VAULT) == 0) {
    			cipher = Base64.getDecoder().decode(standardProps.getProperty(ConfigConstants.SECURE_VAULT));
    		} else if (pk.compareTo(ConfigConstants.SECURE_VAULT_IV) == 0) {
    			iv = Base64.getDecoder().decode(standardProps.getProperty(ConfigConstants.SECURE_VAULT_IV));
    		} else {
    			unlockedProps.setProperty(pk, standardProps.getProperty(pk));
    		}
    	}
    	ByteArrayInputStream bis = null;
    	try {
    		byte clearProps[] = CryptoUtils.Decrypt(cipher, iv, aKey);
    		bis = new ByteArrayInputStream(clearProps);
    		Properties lockProps = new Properties();
    		lockProps.load(bis);
    		bis.close();
    		byte [][] systemParams = SystemValues.getValues(this.aKey);
    		Map<Integer, byte[]> parts = new HashMap<Integer, byte[]>();
    		// Now we are going to iterate through the system params and unlock all the parts of the keys.
    		// If the hash does not match The key part must be different So we skip it.
    		for(int x = 0; x < systemParams.length; x++) {
    			String cipherPart = lockProps.getProperty(ConfigConstants.SECURE_PROPS_PARAMS + x + ".cipher");
    			String cipherIV = lockProps.getProperty(ConfigConstants.SECURE_PROPS_PARAMS + x + ".iv");
    			String partHash = lockProps.getProperty(ConfigConstants.SECURE_PROPS_PARAMS + x + ".hash");
    			if (cipherPart == null || cipherIV == null) continue;
    			byte decryptedPart[] = CryptoUtils.Decrypt(Base64.getDecoder().decode(cipherPart), Base64.getDecoder().decode(cipherIV), systemParams[x]);
    			if (decryptedPart != null && partHash.compareTo(Base64.getEncoder().encodeToString(CryptoUtils.SHA256(decryptedPart))) == 0) {
    				parts.put(x+1, decryptedPart);
    			}
    		}
    		// We now re-assemble the master key and check it with our hash value to make sure it matches.
    		int minParts = Integer.parseInt(lockProps.getProperty(ConfigConstants.SECURE_VAULT_M));
    		int numParts = Integer.parseInt(lockProps.getProperty(ConfigConstants.SECURE_VAULT_N));
    		Scheme bloomScheme  = Scheme.of(numParts, minParts);    	
    		byte[] masterKey = bloomScheme.join(parts);
    		String keyHash = lockProps.getProperty(ConfigConstants.MASTER_KEY_HASH);
    		if (keyHash.compareTo(Base64.getEncoder().encodeToString(CryptoUtils.SHA256(masterKey))) != 0) {
    			throw new SecureConfigException("Cannot recustruct config encryption key");
    		}
    		// OK now that we have the master key, we need to decrypt the sensistive properties and put the clear values
    		// in the property object we are returning.
    		byte [] propertyIV = Base64.getDecoder().decode(lockProps.getProperty(ConfigConstants.SECURE_PROPS_PROPERTY_IV));
    		propKeys = lockProps.stringPropertyNames();
    		for(String pk: propKeys) {
    			if (pk.startsWith(ConfigConstants.SECURE_PROPS_SECURED_PROPERTY_PREFIX) == true) {
    				String propName = pk.substring(ConfigConstants.SECURE_PROPS_SECURED_PROPERTY_PREFIX.length());
    	    		unlockedProps.setProperty(propName, new String(CryptoUtils.Decrypt(Base64.getDecoder().decode(lockProps.getProperty(pk)), propertyIV, masterKey)));
    			}
    		}
    	} catch (Exception e) {
    		throw new SecureConfigException(e);
    	} finally {
    		try { if (bis != null) bis.close(); } catch(Exception e1) {}
    	}
        return unlockedProps;
    }

}