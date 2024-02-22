package com.foobar.tools.lockbox;

import org.junit.Test;

public class SecureConfigTest {
    @Test
    public void testInitialize() {
        // null file name
        // non existant file name
        // bad / garbage file
        // no protected parmeters
        // missing required parameters
        // null application name
        // different application key
        // good locked config file (right system)
        // bad locked config file (copied from another system)
    }

    @Test
    public void testGetProperties() {
        // Get protected parameter
        // get clear parameter
        // get non-existant parameter
    }

    @Test
    public void testIsProtected() {
        // protected file
        // unprotected file
        // newly protected file
        // just resteted file
    }

    @Test
    public void testResetProtection() {

    }
}
