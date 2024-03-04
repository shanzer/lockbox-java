package com.foobar.tools.lockbox;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

public class SecureConfigTest {
    @SuppressWarnings("unused")
    private static final String PROTECTED_PROPS = "sample-protected-file.props";
    private static final String UNPROTECTED_PROPS = "sample-unprotected-file.props";
    @SuppressWarnings("unused")
    private static final String PREVIOUSLY_PROTECTED_PROPS = "previous-protected-file.props";
    private static final String TEST_PROP_CLEAR = "test.property.one";
    private static final String TEST_PROP_CLEAR_VALUE = "This is in the clear";
    private static final String PROTECTED_FILE_NOPROPS = "sample-protected-noprotprops.props";

    @TempDir
    static File testFolder;

    @BeforeAll
    public static void setupEnvironment() throws IOException {
        System.out.println("TestFolder: " + testFolder.getAbsolutePath());
        System.out.println(("user.dir: " + System.getProperty("user.dir")));        
    }

    @AfterAll
    public static void teardownEnvironment() throws IOException {

    }

    /**
     * Before we run each test we are going to create a temporary dicrectory
     * with the "sample" config files and set up a standard environment for each test
     * the directory is going to have:
     *  - a good props file that is going to be protected
     *  - a good props file that is not going to be protected
     *  - a file which is not a props file
     */
    @BeforeEach
    
    public void setupTest() throws IOException {
        FileUtils.cleanDirectory(testFolder);
        File srcDir = new File(System.getProperty("user.dir") + "/src/test/resources");
        FileUtils.copyDirectory(srcDir, testFolder);
    }

    /**
     * When we are done, we are going to clean out our test directory.
     * We are going to do this before each test too, but you cannot be to careful.
     * 
     * @throws IOException
     */
    @AfterEach
    public void teardownTest() throws IOException {
        File srcDir = new File(System.getProperty("user.dir"));        
        FileUtils.copyDirectory(srcDir, testFolder);
    }

    @Test
    @DisplayName("Null File")
    public void testInitializeNullFile() {
        SecureConfig sc = new SecureConfig();
        Assertions.assertThrows(NullPointerException.class, () -> {
            sc.Initialize(null, "This is a test"); }, "Should have gotten null pointer exception");
    }

    @Test
    @DisplayName("Null Application Name")
    public void testInitializeNullApplication() {
        SecureConfig sc = new SecureConfig();
        Assertions.assertThrows(NullPointerException.class, () -> {
            sc.Initialize("test.cfg", (String) null); }, "Should have gotten null pointer exception");
          
    }

    @Test
    @DisplayName("Non-Existant File")
    public void testInitializeNonExistantFile() {
        SecureConfig sc = new SecureConfig();
        Assertions.assertThrows(FileNotFoundException.class, () -> {
            sc.Initialize("thereisnosuchfilebythis.name", "testapplication"); }, "Should have gotten null pointer exception");
    }

    @Test
    @DisplayName("Unprotected Properties File")
    public void testUnprotectedPropetiesFile() {
        SecureConfig sc = new SecureConfig();
        try {
            sc.Initialize(testFolder.getAbsolutePath() + "/" + UNPROTECTED_PROPS, "testApplication");
        } catch (Exception e) {
            assertNull(e);
        }
        Properties props = null;
        try {
            props = sc.getProperties();
        } catch (Exception e) {
            assertNull(e);
        }
        assertNotEquals(props.getProperty(TEST_PROP_CLEAR), TEST_PROP_CLEAR_VALUE);;
    }
    @Test
    @DisplayName("No protected Properties in protected File")
    public void testNoProtectedProps() {
        SecureConfig sc = new SecureConfig();
        try {
            sc.Initialize(testFolder.getAbsolutePath() + "/" + PROTECTED_FILE_NOPROPS, "testApplication");
        } catch (Exception e) {
            assertNull(e);
        }
        Properties props = null;
        try {
            props = sc.getProperties();
        } catch (Exception e) {
            assertNull(e);
        }
        assertNotEquals(props.getProperty(TEST_PROP_CLEAR), TEST_PROP_CLEAR_VALUE);;
    }

/*
    @Test
    public void testInitialize() {
        // no protected parmeters
        // missing required parameters
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
    */
}
