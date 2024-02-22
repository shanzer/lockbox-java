package com.foobar.tools.lockbox;

public class ConfigConstants {

	public static final String SECURE_PROPS_CONFIG_PREFIX = "com.foobar.secureProperties.";
	public static final String SECURE_ENABLE_PROPS = SECURE_PROPS_CONFIG_PREFIX + "state";
	public static final String SECURE_PROPS_VERSION = SECURE_PROPS_CONFIG_PREFIX + "version";
	public static final String SECURE_RECOVER_KEY = SECURE_PROPS_CONFIG_PREFIX + "recoveryKey";
	public static final String SECURE_RECOVER_KEY_IV = SECURE_PROPS_CONFIG_PREFIX + "recoveryIV";
	public static final String SECURE_RECOVER_PASSWD = SECURE_PROPS_CONFIG_PREFIX + "recoveryPassword";
	public static final String SECURE_PROPS_SECURED_PROPERTY_PREFIX = SECURE_PROPS_CONFIG_PREFIX + "property.";
	public static final String SECURE_PROPS_PARAMS = SECURE_PROPS_CONFIG_PREFIX + "params.";
	public static final String SECURE_PROPERTY_PREFIX = "PROTECT.";
	public static final String MASTER_KEY_HASH = SECURE_PROPS_CONFIG_PREFIX + "keyhash";
	public static final String SECURE_PROPS_PROPERTY_IV = SECURE_PROPS_CONFIG_PREFIX + "iv";
	public static final String SECURE_VAULT = SECURE_PROPS_CONFIG_PREFIX + "vault.data";
	public static final String SECURE_VAULT_IV = SECURE_PROPS_CONFIG_PREFIX + "vault.iv";
	public static final String SECURE_VAULT_M = SECURE_PROPS_CONFIG_PREFIX + "vault.m";
	public static final String SECURE_VAULT_N = SECURE_PROPS_CONFIG_PREFIX + "vault.n";
}
