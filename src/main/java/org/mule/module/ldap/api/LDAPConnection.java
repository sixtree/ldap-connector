/**
 * Copyright (c) MuleSoft, Inc. All rights reserved. http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.md file.
 */

package org.mule.module.ldap.api;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.module.ldap.api.jndi.LDAPJNDIConnection;

/**
 * This class is the abstraction
 * 
 * @author mariano
 */
public abstract class LDAPConnection implements LDAPSchemaAware
{

    protected final Log logger = LogFactory.getLog(getClass());

    public static final boolean DEFAULT_SCHEMA_ENABLED = false;
    public static final boolean DEFAULT_TLS_ENABLED = false;
    
    protected static final Map<String, Class<?>> CONNECTION_IMPLEMENTATIONS = new HashMap<String, Class<?>>();

    private boolean schemaEnabled = DEFAULT_SCHEMA_ENABLED;
    private boolean tlsEnabled = DEFAULT_TLS_ENABLED;

    static
    {
        CONNECTION_IMPLEMENTATIONS.put("jndi", LDAPJNDIConnection.class);
    }

    public static final String NO_AUTHENTICATION = "none";
    public static final String SIMPLE_AUTHENTICATION = "simple";
    public static final String CONNECTION_TYPE_ATTR = "type";
    public static final String LDAP_URL_ATTR = "url";
    public static final String AUTHENTICATION_ATTR = "authentication";
    public static final String INITIAL_POOL_CONNECTIONS_ATTR = "initialPoolSize";
    public static final String MAX_POOL_CONNECTIONS_ATTR = "maxPoolSize";
    public static final String POOL_TIMEOUT_ATTR = "poolTimeout";
    public static final String REFERRAL_ATTR = "referral";
    public static final String SCHEMA_ENABLED = "schema-enabled";
    public static final String TLS_ENABLED = "tls-enabled";
    
    /**
	 * 
	 */
    public LDAPConnection()
    {
        super();
    }

    public static LDAPConnection getConnection(String type, String url, String authentication, Map<String, String> extendedConf) throws LDAPException
    {
        Map<String, String> conf = extendedConf != null ? new HashMap<String, String>(extendedConf) : new HashMap<String, String>();
        conf.put(CONNECTION_TYPE_ATTR, type);
        conf.put(LDAP_URL_ATTR, url);
        conf.put(AUTHENTICATION_ATTR, authentication);
        return getConnection(conf);
    }
    
    public static LDAPConnection getConnection(String type, String url, String authentication) throws LDAPException
    {
        return getConnection(type, url, authentication, new HashMap<String, String>());
    }

    public static LDAPConnection getConnection(String type, String url, String authentication, int initialPoolSize, int maxPoolSize, long poolTimeout, String referral, Map<String, String> extendedConf) throws LDAPException
    {
        return getConnection(type, url, authentication, initialPoolSize, maxPoolSize, poolTimeout, referral, extendedConf, DEFAULT_SCHEMA_ENABLED, DEFAULT_TLS_ENABLED);        
    }    

    public static LDAPConnection getConnection(String type, String url, String authentication, int initialPoolSize, int maxPoolSize, long poolTimeout, String referral, Map<String, String> extendedConf, boolean schemaEnabled) throws LDAPException
    {
        return getConnection(type, url, authentication, initialPoolSize, maxPoolSize, poolTimeout, referral, extendedConf, schemaEnabled, DEFAULT_TLS_ENABLED);        
    }    

    public static LDAPConnection getConnection(String type, String url, String authentication, int initialPoolSize, int maxPoolSize, long poolTimeout, String referral, Map<String, String> extendedConf, boolean schemaEnabled, boolean tlsEnabled) throws LDAPException
    {
        Map<String, String> conf = extendedConf != null ? new HashMap<String, String>(extendedConf) : new HashMap<String, String>();
        conf.put(CONNECTION_TYPE_ATTR, type);
        conf.put(LDAP_URL_ATTR, url);
        conf.put(REFERRAL_ATTR, referral);
        conf.put(AUTHENTICATION_ATTR, authentication);
        conf.put(INITIAL_POOL_CONNECTIONS_ATTR, String.valueOf(initialPoolSize));
        conf.put(MAX_POOL_CONNECTIONS_ATTR, String.valueOf(maxPoolSize));
        conf.put(POOL_TIMEOUT_ATTR, String.valueOf(poolTimeout));
        conf.put(SCHEMA_ENABLED, String.valueOf(schemaEnabled));
        conf.put(TLS_ENABLED, String.valueOf(tlsEnabled));
        
        return getConnection(conf);        
    }    
    
    public static LDAPConnection getConnection(String type, String url, String authentication, int initialPoolSize, int maxPoolSize, long poolTimeout, String referral) throws LDAPException
    {
        return getConnection(type, url, authentication, initialPoolSize, maxPoolSize, poolTimeout, referral, new HashMap<String, String>());
    }
    
    /**
     * @return
     * @throws LDAPException
     */
    public static LDAPConnection getConnection(Map<String, String> conf) throws LDAPException
    {
        try
        {
            if (!conf.containsKey(CONNECTION_TYPE_ATTR))
            {
                throw new LDAPException("Could not instantiate connection as configuration is missing parameter [" + CONNECTION_TYPE_ATTR + "]");
            }
            else if (CONNECTION_IMPLEMENTATIONS.get(conf.get(CONNECTION_TYPE_ATTR)) == null)
            {
                throw new LDAPException("Could not instantiate connection as " + CONNECTION_TYPE_ATTR + " [" + conf.get(CONNECTION_TYPE_ATTR) + "] is not supported");
            }

            LDAPConnection conn = (LDAPConnection) CONNECTION_IMPLEMENTATIONS.get(conf.get(CONNECTION_TYPE_ATTR)).newInstance();
            conn.initialize(conf);

            return conn;
        }
        catch (InstantiationException ex)
        {
            throw new LDAPException("Could not instantiate connection from configuration " + conf, ex);
        }
        catch (IllegalAccessException ex)
        {
            throw new LDAPException("Could not instantiate connection from configuration " + conf, ex);
        }
        catch (Throwable ex)
        {
            throw new LDAPException("Could not obtain connection from configuration " + conf, ex);
        }
    }

    /**
     * @param conf
     * @throws LDAPException
     */
    protected abstract void initialize(Map<String, String> conf) throws LDAPException;

    /**
     * @param dn
     * @param password
     * @throws LDAPException
     */
    public abstract void bind(String dn, String password) throws LDAPException;

    /**
     * @throws LDAPException
     */
    public abstract void rebind() throws LDAPException;
    
    /**
     * @param baseDn
     * @param filter
     * @param filterArgs
     * @param controls
     * @return
     * @throws LDAPException
     */
    public abstract LDAPResultSet search(String baseDn,
                                         String filter,
                                         Object[] filterArgs,
                                         LDAPSearchControls controls) throws LDAPException;

    /**
     * 
     * @return
     * @throws LDAPException
     */
    public abstract String getBindedUserDn() throws LDAPException;
    
    /**
     * @param baseDn
     * @param filter
     * @param controls
     * @return
     * @throws LDAPException
     */
    public abstract LDAPResultSet search(String baseDn, String filter, LDAPSearchControls controls)
        throws LDAPException;

    /**
     * @param dn
     * @return
     * @throws LDAPException
     */
    public abstract LDAPEntry lookup(String dn) throws LDAPException;

    /**
     * 
     * @param oldDn
     * @param newDn
     * @throws LDAPException
     */
    public abstract void renameEntry(String oldDn, String newDn) throws LDAPException;
    
    /**
     * Returns the LDAP entry matching the given dn. The entry will contain only the
     * defined attributes.
     * 
     * @param dn
     * @param attributes Attributes names to fetch.
     * @return
     * @throws LDAPException
     */
    public abstract LDAPEntry lookup(String dn, String attributes[]) throws LDAPException;

    /**
     * @param entry
     * @throws LDAPException
     */
    public abstract void addEntry(LDAPEntry entry) throws LDAPException;

    /**
     * @param entry
     * @throws LDAPException
     */
    public abstract void updateEntry(LDAPEntry entry) throws LDAPException;

    /**
     * @param entry
     * @throws LDAPException
     */
    public abstract void deleteEntry(LDAPEntry entry) throws LDAPException;

    /**
     * @param dn
     * @throws LDAPException
     */
    public abstract void deleteEntry(String dn) throws LDAPException;

    /**
     * @param dn
     * @param attribute
     * @throws LDAPException
     */
    public abstract void addAttribute(String dn, LDAPEntryAttribute attribute) throws LDAPException;

    /**
     * @param dn
     * @param attribute
     * @throws LDAPException
     */
    public abstract void updateAttribute(String dn, LDAPEntryAttribute attribute) throws LDAPException;

    /**
     * Deletes the attribute or the value of the attribute.
     * 
     * @param dn
     * @param attribute
     * @throws LDAPException
     */
    public abstract void deleteAttribute(String dn, LDAPEntryAttribute attribute) throws LDAPException;

    /**
     * @throws LDAPException
     */
    public abstract void close() throws LDAPException;

    /**
     * @return
     * @throws LDAPException
     */
    public abstract boolean isClosed() throws LDAPException;
    
    @Override
    public boolean isSchemaEnabled()
    {
        return schemaEnabled;
    }

    public void setSchemaEnabled(boolean schemaEnabled)
    {
        this.schemaEnabled = schemaEnabled;
    }

	public boolean isTlsEnabled() {
		return tlsEnabled;
	}

	public void setTlsEnabled(boolean tlsEnabled) {
		this.tlsEnabled = tlsEnabled;
	}    
}
