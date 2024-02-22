package com.foobar.tools.lockbox;

import java.util.ArrayList;

class SystemValues {
	public static byte [][] getValues(byte key[]) throws Exception {
		OSHIValues ov = new OSHIValues();
		byte [][] ovValues = ov.getValues();
    	ArrayList<byte []> returnValues = new ArrayList<byte[]>();
		
    	// hard coding iteration count here. Should be configurable, but if it changes that could screw up an existing system
    	// so lets just hard code it to a semi-random value.
    	
    	for(int x = 0; x < ovValues.length; x++) {
    		returnValues.add(CryptoUtils.ValueToKeybyte(ovValues[x], key, 117));
    	}
    	return returnValues.toArray(new byte[returnValues.size()][]);
    }
}
    