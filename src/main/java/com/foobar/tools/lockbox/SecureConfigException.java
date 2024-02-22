package com.foobar.tools.lockbox;

public class SecureConfigException extends Exception {

        public static final long serialVersionUID = 1234567;

        public SecureConfigException() {
                super();
        }
        public SecureConfigException(String message) {
                super(message);
        }
        public SecureConfigException(Throwable cause) {
                super(cause);
        }

        public SecureConfigException(String message, Throwable cause) {
            super(message, cause);
        }
}
