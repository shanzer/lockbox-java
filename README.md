# Lockbox / Secure Configuration #

This repository contains java code to create a "secure" storage container for properties. It encrypts the property file using various system parameters, and if enough of those system parameters change the secure contents of the file cannot be decrypted. If an attacker has access to the system and the encrypted file they can decrypt the file. This prevents configuration files from being copied and used on other systems (or somehow extracted off the system)

## Usage ##

To use this toolkit you need to have a java properties file which contains information that you want to proctect.
The first time the application runs it'll use the information in the properties file and system information to
obscure / encrypt the sensitive information in the properties file. A sample initial properties file would look like:
```
com.foobar.secureProperties.state=initialize
com.foobar.secureProperties.recoverPassword=SetupPassword
test.property.one="This is in the clear"
PROTECT.test.property.two="This is encrypted"
```
The first two properties are the secure config/lockbox parameters which tell it that we want to protect some data in this
properties file. Setting the `com.foobar.secureProperties.state` to initialize says that when we run for the first time we need 
to initialize and set up the lockbox. The `com.foobar.secureProperties.recoverPassword` is used to recover the encrypted properties in case of emergency (discussed later). The properties that start with the "PROTECT" string are the properties that are to be encrypted/protected. 

From an application perspective there are really only two calls that are needed. Below is a sample java program that does the basic functions:
```java
public static void main(String args[]) throws Exception {
    	SecureConfig sc = new SecureConfig();
    	sc.Initialize("c:/temp/foo.properties", "This is a test".getBytes());
    	Properties p = sc.getProperties(true);
		p.list(System.out);
		return;
	}
```

Running the above code snippet with the sample properties file would produce the following output:

```
-- listing properties --
com.foobar.secureProperties.state=true
test.property.one="This is in the clear"
test,property.two="This is encrypted"`
```

The 'initialize' function is what sets up the object to be used. If when it reads the properties file if the state is set to "initialize"
it then does the work of encrypting the file and re-writting out the properties file encrypted. The `getProperties` function returns a 
java properties object with all the properties in clear text (without the protected prefix) for the application to use as it normally would.

The second argument to initialize is an  "application key". This prevents a different application from opening up and decrypting the secure configuration
information for your application. It is used as a seed for the encryption operations. It can be hard coded in your application but it should be somewhat protected.

Once the application runs for the first time the properties file is going to get re-written with the encrypted information. The new sample properties
is going to look something like:
```
com.foobar.secureProperties.vault.iv=ByWriy6ZoR09fo6mSLhKnA\=\=
com.foobar.secureProperties.state=true
test.property.one="This is in the clear"
com.foobar.secureProperties.vault.data=XUbfk2nSj9uXg9l3rKDxaQy1Icqn5V4CZdXg5WoB+lHKf/ch9L4sjj3iKu14Xtxf0eoIn5XmN+OAgtVfoJu+0ho2KuLRKIJ6KNWM+2RogVR3BDRgMoHwXJcs...
```
As you can see there are 2 new, 1 update and 1 unchanged property.
The unchanged propeties are the properties that were not protected.
The `com.foobar.secureProperties.state` property has been changed to true and we added
two new properties: 
* com.foobar.secureProperties.vault.iv 
is the initialiation vectore used for the encryption operation of the protected properties.
* com.foobar.securePropeties.vault.data 
is the base64 encoded encrypted vault which contains the encrypted properties and the the meta data used for the operation.

Modifying any of these new properties would invalidate the vault and make it un-recoverable.

### Recovery
If you move the properties file to a new machine, or enough of the machine's configuration changes to invalidate the encrypted contents
you can recover and re-encrypt the configuration file by changing the `com.foobar.secureProperties.state` property to 'reset' and adding back
in the `com.foobar.secureProperties.recoverPassword` using the same password used when the file was encrypted. This is going to re-encrypt the
file with new system parameters.  The application would continue to run as normal. The `getProperties` method still return the un-encrypted 
properties.  

If the encrypted data or IV have been modified or the recovery password is lost, the lockbox needs to be re-initialized from scratch. There is no way to recover.
