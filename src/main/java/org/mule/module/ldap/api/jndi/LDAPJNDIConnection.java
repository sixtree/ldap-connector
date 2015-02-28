/**
 * Copyright (c) MuleSoft, Inc. All rights reserved. http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.md file.
 */

package org.mule.module.ldap.api.jndi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.StartTlsRequest;
import javax.naming.ldap.StartTlsResponse;
import javax.net.ssl.SSLSession;

import org.apache.commons.lang.StringUtils;
import org.mule.module.ldap.api.LDAPConnection;
import org.mule.module.ldap.api.LDAPEntry;
import org.mule.module.ldap.api.LDAPEntryAttribute;
import org.mule.module.ldap.api.LDAPEntryAttributeTypeDefinition;
import org.mule.module.ldap.api.LDAPEntryAttributes;
import org.mule.module.ldap.api.LDAPEntryObjectClassDefinition;
import org.mule.module.ldap.api.LDAPException;
import org.mule.module.ldap.api.LDAPResultSet;
import org.mule.module.ldap.api.LDAPSearchControls;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * This class is the abstraction
 * 
 * @author mariano
 */
public class LDAPJNDIConnection extends LDAPConnection
{
    public static final int DEFAULT_MAX_POOL_CONNECTIONS = 0;
    public static final int DEFAULT_INITIAL_POOL_CONNECTIONS = 0;
    public static final long DEFAULT_POOL_TIMEOUT = 0L;
    public static final String DEFAULT_INITIAL_CONTEXT_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";
    public static final String DEFAULT_REFERRAL = "ignore";

    private static final boolean IGNORE_CASE = true;

    private static final String INITIAL_CONTEXT_FACTORY_ATTR = "initialContextFactory";
    
    /**
     * Final constants for managing JNDI Pool connections.
     */
    private static final String POOL_ENABLED_ENV_PARAM =  "com.sun.jndi.ldap.connect.pool";
    private static final String MAX_POOL_SIZE_ENV_PARAM = "com.sun.jndi.ldap.connect.pool.maxsize";
    private static final String INIT_POOL_SIZE_ENV_PARAM = "com.sun.jndi.ldap.connect.pool.initsize";
    private static final String TIME_OUT_ENV_PARAM = "com.sun.jndi.ldap.connect.pool.timeout";
    private static final String AUTHENTICATION_ENV_PARAM = "com.sun.jndi.ldap.pool.authentication";

    private String providerUrl = null;
    private int maxPoolConnections = DEFAULT_MAX_POOL_CONNECTIONS;
    
    private int initialPoolSizeConnections = DEFAULT_INITIAL_POOL_CONNECTIONS;
    private long poolTimeout = DEFAULT_POOL_TIMEOUT;
    private String authentication = NO_AUTHENTICATION;
    private String initialContextFactory = DEFAULT_INITIAL_CONTEXT_FACTORY;
    private String referral = DEFAULT_REFERRAL;
    private Map<String, String> extendedEnvironment = null;

    private LoadingCache<String, LDAPEntryAttributeTypeDefinition> schemaCache = null;
    
    private LdapContext conn = null;
    private StartTlsResponse tls = null;

    /**
	 * 
	 */
    public LDAPJNDIConnection()
    {
        super();
    }

    /**
     * @param providerUrl
     * @throws LDAPException
     */
    public LDAPJNDIConnection(String providerUrl) throws LDAPException
    {
        this(providerUrl, DEFAULT_INITIAL_CONTEXT_FACTORY);
    }

    /**
     * @param providerUrl
     * @param initialContextFactory
     * @throws LDAPException
     */
    public LDAPJNDIConnection(String providerUrl, String initialContextFactory) throws LDAPException
    {
        this(providerUrl, initialContextFactory, NO_AUTHENTICATION);
    }

    /**
     * @param providerUrl
     * @param initialContextFactory
     * @param authentication
     * @param maxPoolConnections
     * @param initialPoolSizeConnections
     * @param poolTimeout
     * @throws LDAPException
     */
    public LDAPJNDIConnection(String providerUrl,
                              String initialContextFactory,
                              String authentication,
                              int maxPoolConnections,
                              int initialPoolSizeConnections,
                              long poolTimeout) throws LDAPException
    {
        this(providerUrl, initialContextFactory, authentication, maxPoolConnections, initialPoolSizeConnections, poolTimeout, false);
    }

    /**
     * @param providerUrl
     * @param initialContextFactory
     * @param authentication
     * @throws LDAPException
     */
    public LDAPJNDIConnection(String providerUrl, String initialContextFactory, String authentication)
        throws LDAPException
    {
        this(providerUrl, initialContextFactory, authentication, DEFAULT_MAX_POOL_CONNECTIONS,
            DEFAULT_INITIAL_POOL_CONNECTIONS, DEFAULT_POOL_TIMEOUT);
    }

    /**
     * 
     * @param providerUrl
     * @param initialContextFactory
     * @param authentication
     * @param maxPoolConnections
     * @param initialPoolSizeConnections
     * @param poolTimeout
     * @param useSchema
     * @throws LDAPException
     */
    public LDAPJNDIConnection(String providerUrl,
                              String initialContextFactory,
                              String authentication,
                              int maxPoolConnections,
                              int initialPoolSizeConnections,
                              long poolTimeout,
                              boolean schemaEnabled) throws LDAPException
    {
        this();
        setProviderUrl(providerUrl);
        setInitialContextFactory(initialContextFactory);
        setAuthentication(authentication);
        setMaxPoolConnections(maxPoolConnections);
        setInitialPoolSizeConnections(initialPoolSizeConnections);
        setPoolTimeout(poolTimeout);
        setSchemaEnabled(schemaEnabled);
        
        initializeCache();
    }    
    
    private synchronized void initializeCache()
    {
        if (isSchemaEnabled() && this.schemaCache == null)
        {
            this.schemaCache = CacheBuilder.newBuilder()
                .maximumSize(1000)
                .build(new CacheLoader<String, LDAPEntryAttributeTypeDefinition>()
                {
                    public LDAPEntryAttributeTypeDefinition load(String attributeName) throws LDAPException
                    {
                        return retrieveAttributeTypeDefinition(attributeName);
                    }
                });
        }        
    }
    
    /**
     * @param conf
     * @throws LDAPException
     * @see org.mule.module.ldap.api.LDAPConnection#initialize(java.util.Map)
     */
    @Override
    protected void initialize(Map<String, String> conf) throws LDAPException
    {
        if (conf != null)
        {
            extendedEnvironment = new HashMap<String, String>(conf);
            extendedEnvironment.remove(CONNECTION_TYPE_ATTR);
            
            setAuthentication(getConfValue(conf, AUTHENTICATION_ATTR, NO_AUTHENTICATION));
            extendedEnvironment.remove(AUTHENTICATION_ATTR);
            
            setInitialContextFactory(getConfValue(conf, INITIAL_CONTEXT_FACTORY_ATTR, DEFAULT_INITIAL_CONTEXT_FACTORY));
            extendedEnvironment.remove(INITIAL_CONTEXT_FACTORY_ATTR);
            
            setInitialPoolSizeConnections(getConfValue(conf, INITIAL_POOL_CONNECTIONS_ATTR, DEFAULT_INITIAL_POOL_CONNECTIONS));
            extendedEnvironment.remove(INITIAL_POOL_CONNECTIONS_ATTR);

            setMaxPoolConnections(getConfValue(conf, MAX_POOL_CONNECTIONS_ATTR, DEFAULT_MAX_POOL_CONNECTIONS));
            extendedEnvironment.remove(MAX_POOL_CONNECTIONS_ATTR);

            setPoolTimeout(getConfValue(conf, POOL_TIMEOUT_ATTR, DEFAULT_POOL_TIMEOUT));
            extendedEnvironment.remove(POOL_TIMEOUT_ATTR);
            
            setProviderUrl(getConfValue(conf, LDAP_URL_ATTR, null));
            extendedEnvironment.remove(LDAP_URL_ATTR);
            
            setReferral(getConfValue(conf, REFERRAL_ATTR, DEFAULT_REFERRAL));
            extendedEnvironment.remove(REFERRAL_ATTR);
            
            setSchemaEnabled("true".equals(getConfValue(conf, SCHEMA_ENABLED, String.valueOf(DEFAULT_SCHEMA_ENABLED))));
            initializeCache();
            extendedEnvironment.remove(SCHEMA_ENABLED);

            setTlsEnabled("true".equals(getConfValue(conf, TLS_ENABLED, String.valueOf(DEFAULT_TLS_ENABLED))));
            extendedEnvironment.remove(TLS_ENABLED);
            
        }
    }

    private String getConfValue(Map<String, String> conf, String key, String defaultValue)
    {
        String value = conf.get(key);
        
        return StringUtils.isNotEmpty(value) ? value : defaultValue;
    }
    
    private int getConfValue(Map<String, String> conf, String key, int defaultValue)
    {
        String value = conf.get(key);
        
        return StringUtils.isNotEmpty(value) ? Integer.parseInt(value) : defaultValue;        
    }

    private long getConfValue(Map<String, String> conf, String key, long defaultValue)
    {
        String value = conf.get(key);
        
        return StringUtils.isNotEmpty(value) ? Long.parseLong(value) : defaultValue;        
    }
    
    /**
     * @return
     */
    private void logConfiguration(String bindDn, String bindPassword)
    {
        StringBuilder conf = new StringBuilder();

        conf.append("{");
        conf.append("tls: " + isTlsEnabled() + ", ");
        conf.append("url: " + getProviderUrl() + ", ");
        conf.append("authentication: " + getAuthentication() + ", ");
        if (!isNoAuthentication() && StringUtils.isNotEmpty(bindDn))
        {
            conf.append("authDn: " + bindDn + ", ");
        }
        else
        {
            conf.append("authDn: {anonymous}, ");
        }
        
        conf.append("initialContextFactory: " + getInitialContextFactory() + ", ");
        conf.append("referral: " + getReferral() + ", ");

        if (isConnectionPoolEnabled())
        {
            conf.append("initialPoolSize: " + getInitialPoolSizeConnections() + ", ");
            conf.append("maxPoolSize: " + getMaxPoolConnections() + ", ");
            conf.append("poolTimeout: " + getPoolTimeout());
        }
        else
        {
            conf.append("pool: disabled");
        }
        if(extendedEnvironment != null && extendedEnvironment.size() > 0)
        {
            conf.append(", extended: " + extendedEnvironment);
        }
        conf.append("}");
        logger.debug(conf.toString());
    }

    /**
     * @return
     */
    public boolean isNoAuthentication()
    {
        return NO_AUTHENTICATION.equalsIgnoreCase(getAuthentication());
    }

    /**
     * @return
     * @see org.mule.module.ldap.api.LDAPConnection#isClosed()
     */
    @Override
    public boolean isClosed()
    {
        return this.conn == null;
    }

    /**
     * @throws LDAPException
     * @see org.mule.module.ldap.api.LDAPConnection#close()
     */
    @Override
    public void close() throws LDAPException
    {
        if (!isClosed())
        {
            String connectionId = toString();
            try
            {
            	if (isTlsEnabled() && this.tls != null)
            	{
            		tls.close();
                    logger.info("Connection " + connectionId + " closed.");
            	}
            }
            catch(IOException ex)
            {
                throw handleException(ex, "Close TLS for connection " + connectionId + " failed.");
            }
            finally
            {
                try
                {
                    getConn().close();
                    logger.info("Connection " + connectionId + " closed.");
                }
                catch (NamingException nex)
                {
                    throw handleNamingException(nex, "Close connection " + connectionId + " failed.");
                } 
                finally
                {
                    setConn(null);
                }
            }
        } 
        else
        {
            logger.warn("Connection already closed.");
        }
    }

    /**
     * @param dn
     * @param password
     * @return
     * @throws LDAPException
     */
    private Hashtable<String, String> buildEnvironment(String dn, String password) throws LDAPException
    {
        Hashtable<String, String> env = new Hashtable<String, String>();
        
        if (getReferral() != null)
        {
            env.put(Context.REFERRAL, getReferral().toLowerCase());
        }
        env.put(Context.SECURITY_AUTHENTICATION, getAuthentication());
        if (!isNoAuthentication())
        {
            if (dn == null || password == null)
            {
                throw new LDAPException("Bind DN and/or password cannot be null when authentication is required. Used authentication = 'none' for anonymous bind.");
            }
            env.put(Context.SECURITY_PRINCIPAL, dn);
            env.put(Context.SECURITY_CREDENTIALS, password);
        }
        env.put(Context.INITIAL_CONTEXT_FACTORY, getInitialContextFactory());
        env.put(Context.PROVIDER_URL, getProviderUrl());


        if (isConnectionPoolEnabled())
        {
            env.put(POOL_ENABLED_ENV_PARAM, "true");
            
            env.put(AUTHENTICATION_ENV_PARAM, getAuthentication());

            if (getMaxPoolConnections() > 0)
            {
                env.put(MAX_POOL_SIZE_ENV_PARAM, String.valueOf(getMaxPoolConnections()));
            }

            if (getInitialPoolSizeConnections() > 0)
            {
                env.put(INIT_POOL_SIZE_ENV_PARAM, String.valueOf(getInitialPoolSizeConnections()));
            }

            if (getPoolTimeout() > 0)
            {
                env.put(TIME_OUT_ENV_PARAM, String.valueOf(getPoolTimeout()));
            }
        }
        else
        {
            env.put(POOL_ENABLED_ENV_PARAM, "false");
        }
        
        if(extendedEnvironment != null && extendedEnvironment.size() > 0)
        {
            env.putAll(extendedEnvironment);
        }
        
        if(logger.isDebugEnabled())
        {
            logger.debug("Created environment without authentication credentials: " + env);
        }
        
        return env;

    }

    /**
     * 
     * @throws LDAPException
     * @see org.mule.module.ldap.api.LDAPConnection#rebind()
     */
    @Override
    public void rebind() throws LDAPException
    {
        if(isClosed())
        {
            throw new LDAPException("Cannot rebind a close connection. You must first bind.");
        }
        else
        {
            String dn = getBindedUserDn();
            String password = getBindedUserPassword();
            bind(dn, password);
        }
    }
    
    /**
     * @param dn
     * @param password
     * @throws LDAPException
     * @see org.mule.module.ldap.api.LDAPConnection#bind(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public void bind(String dn, String password) throws LDAPException
    {
    	LdapContext conn = null;
        try
        {
            if(!isClosed())
            {
                String currentUrl = (String) getConn().getEnvironment().get(Context.PROVIDER_URL);
                String currentAuth = (String) getConn().getEnvironment().get(Context.SECURITY_AUTHENTICATION);
                String currentDn = getBindedUserDn();
                
                logger.info("Already binded to " + currentUrl + " with " + currentAuth + " authentication as " + (currentDn != null ? currentDn : "anonymous") + ". Closing connection first.");
                
                close();
                
                logger.info("Re-binding to " + getProviderUrl() + " with " + getAuthentication() + " authentication as " + (dn != null ? dn : "anonymous"));
            }
            
            logConfiguration(dn, password);
            
            

            if (isTlsEnabled())
            {
            	Hashtable<String, String> env = buildEnvironment(dn, password);
            	
            	Map<String, String> postEncryptEnv = removeAuthenticationConfigurationFromEnvironment(env);
            	
            	conn = new InitialLdapContext(env, null);
            	initTls();
            	
            	/*
            	 * Note that the username and cleartext password are now encrypted because the authentication
            	 * is being performed after establishment of the TLS session
            	 */
            	applyAuthenticationConfiguration(postEncryptEnv, conn);
            }
            else
            {
            	conn = new InitialLdapContext(buildEnvironment(dn, password), null);
            }
            
            setConn(conn);
            logger.info("Binded to " + getProviderUrl() + " with " + getAuthentication() + " authentication as " + (dn != null ? dn : "anonymous"));
        }
        catch (NamingException nex)
        {
        	silentyCloseDirContext(conn);
            throw handleNamingException(nex, "Bind failed.");
        }
        catch (Throwable ex)
        {
        	silentyCloseDirContext(conn);
        	throw ex;
        }
    }

    private void applyAuthenticationConfiguration(Map<String, String> postEncryptEnv, LdapContext conn) throws NamingException
    {
    	for (String key : postEncryptEnv.keySet())
    	{
    		conn.addToEnvironment(key, postEncryptEnv.get(key));
    	}
    }
    
    private Map<String, String> removeAuthenticationConfigurationFromEnvironment(Hashtable<String, String> env)
    {
		Map<String, String> authEnv = new HashMap<String, String>();
		
		for (String key : Arrays.asList(Context.SECURITY_AUTHENTICATION, Context.SECURITY_CREDENTIALS, Context.SECURITY_PRINCIPAL, Context.SECURITY_PROTOCOL))
		{
			String value = env.remove(key);
			if (value != null)
			{
				authEnv.put(key, value);
			}
		}  
		
		return authEnv;
    }
    
	private void initTls() throws LDAPException
	{
		try
		{
			logger.debug("Enabling TLS");
			this.tls = (StartTlsResponse) conn.extendedOperation(new StartTlsRequest());
			final SSLSession negotiate = this.tls.negotiate();
			logger.info("TLS enabled successfully using protocol " + negotiate.getProtocol());
		}
		catch(NamingException nex)
		{
			throw handleNamingException(nex, "TLS initialization failed.");
		}
		catch(IOException ex)
		{
			throw handleException(ex, "TLS negotiation failed.");
		}
	}

    private String getBindedUserPassword() throws LDAPException
    {
        try
        {
            return (String) getConn().getEnvironment().get(Context.SECURITY_CREDENTIALS);
        }
        catch (NamingException nex)
        {
            throw handleNamingException(nex, "Cannot get binded user password.");
        }
    }
    
    /**
     * 
     * @return
     * @throws LDAPException
     * @see org.mule.module.ldap.api.LDAPConnection#getBindedUserDn()
     */
    @Override
    public String getBindedUserDn() throws LDAPException
    {
        if(!isClosed())
        {
            try
            {
                return (String) getConn().getEnvironment().get(Context.SECURITY_PRINCIPAL);
            }
            catch (NamingException nex)
            {
                throw handleNamingException(nex, "Cannot get binded user DN.");
            }
        }
        else
        {
            return null;
        }
    }
    
    /**
     * @param baseDn
     * @param filter
     * @param controls
     * @return
     * @throws LDAPException
     * @see org.mule.module.ldap.api.LDAPConnection#search(java.lang.String,
     *      java.lang.String, org.mule.module.ldap.api.LDAPSearchControls)
     */
    @Override
    public LDAPResultSet search(String baseDn, String filter, LDAPSearchControls controls)
        throws LDAPException
    {
        return doSearch(baseDn, filter, null, controls);
    }

    /**
     * @param baseDn
     * @param filter
     * @param filterArgs
     * @param controls
     * @return
     * @throws LDAPException
     * @see org.mule.module.ldap.api.LDAPConnection#search(java.lang.String,
     *      java.lang.String, java.lang.Object[],
     *      org.mule.module.ldap.api.LDAPSearchControls)
     */
    @Override
    public LDAPResultSet search(String baseDn, String filter, Object[] filterArgs, LDAPSearchControls controls)
        throws LDAPException
    {
        return doSearch(baseDn, filter, filterArgs, controls);
    }

    private LDAPResultSet doSearch(String baseDn, String filter, Object[] filterArgs, LDAPSearchControls controls) throws LDAPException
    {
        LdapContext searchConn = null;
        try
        {
            searchConn = controls.isPagingEnabled() ? getConn().newInstance(LDAPJNDIUtils.buildRequestControls(controls, null)) : getConn();
            
            NamingEnumeration<SearchResult> entries;
            if(filterArgs != null && filterArgs.length > 0)
            {
                entries = searchConn.search(baseDn, filter, filterArgs, LDAPJNDIUtils.buildSearchControls(controls));
            }
            else
            {
                entries = searchConn.search(baseDn, filter, LDAPJNDIUtils.buildSearchControls(controls));
            }
            
            return LDAPResultSetFactory.create(baseDn, filter, filterArgs, searchConn, controls, entries, isSchemaEnabled() ? this : null);
        }
        catch (NamingException nex)
        {
            throw handleNamingException(nex, "Search failed.");
        }
    }
    
    /**
     * @param dn
     * @return
     * @throws LDAPException
     * @see org.mule.module.ldap.api.LDAPConnection#lookup(java.lang.String)
     */
    @Override
    public LDAPEntry lookup(String dn) throws LDAPException
    {
        try
        {
            return LDAPJNDIUtils.buildEntry(dn, getConn().getAttributes(dn), isSchemaEnabled() ? this : null);
        }
        catch (NamingException nex)
        {
            throw handleNamingException(nex, "Lookup of entry " + dn + " failed.");
        }
    }

    /**
     * @param dn
     * @param attributes
     * @return
     * @throws LDAPException
     * @see org.mule.module.ldap.api.LDAPConnection#lookup(java.lang.String,
     *      java.lang.String[])
     */
    @Override
    public LDAPEntry lookup(String dn, String[] attributes) throws LDAPException
    {
        try
        {
            return LDAPJNDIUtils.buildEntry(dn, getConn().getAttributes(dn, attributes), isSchemaEnabled() ? this : null);
        }
        catch (NamingException nex)
        {
            throw handleNamingException(nex, "Lookup of entry " + dn + " failed.");
        }
    }

    /**
     * @param entry
     * @throws LDAPException
     * @see org.mule.module.ldap.api.LDAPConnection#addEntry(org.mule.module.ldap.api.LDAPEntry)
     */
    @Override
    public void addEntry(LDAPEntry entry) throws LDAPException
    {
        try
        {
            getConn().bind(entry.getDn(), null, buildAttributes(entry));
        }
        catch (NamingException nex)
        {
            throw handleNamingException(nex, "Add entry " + (entry != null ? entry.getDn() : "null") + " failed.");
        }
    }

    
    private LDAPException handleNamingException(NamingException nex, String logMessage)
    {
        if(logger.isDebugEnabled())
        {
            logger.debug(logMessage, nex);
        }
        else
        {
            logger.warn(logMessage);
        }
        
        return LDAPException.create(nex);
    }

    private LDAPException handleException(Throwable ex, String logMessage)
    {
        if(logger.isDebugEnabled())
        {
            logger.debug(logMessage, ex);
        }
        else
        {
            logger.warn(logMessage);
        }
        
        return new LDAPException(ex.getMessage(), ex);
    }
    
    /**
     * @param entry
     * @throws LDAPException
     * @see org.mule.module.ldap.api.LDAPConnection#updateEntry(org.mule.module.ldap.api.LDAPEntry)
     */
    @Override
    public void updateEntry(LDAPEntry entry) throws LDAPException
    {
        try
        {
            ModificationItem[] mods = new ModificationItem[entry.getAttributeCount()];
            Iterator<LDAPEntryAttribute> it = entry.attributes();
            for (int i = 0; it.hasNext() && i < mods.length; i++)
            {
                mods[i] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                    buildBasicAttribute(((LDAPEntryAttribute) it.next())));
            }
            getConn().modifyAttributes(entry.getDn(), mods);
        }
        catch (NamingException nex)
        {
            throw handleNamingException(nex, "Update entry " + (entry != null ? entry.getDn() : "null") + " failed.");
        }
    }

    /**
     * @param entry
     * @throws LDAPException
     * @see org.mule.module.ldap.api.LDAPConnection#deleteEntry(org.mule.module.ldap.api.LDAPEntry)
     */
    @Override
    public void deleteEntry(LDAPEntry entry) throws LDAPException
    {
        deleteEntry(entry.getDn());
    }

    /**
     * @param dn
     * @throws LDAPException
     * @see org.mule.module.ldap.api.LDAPConnection#deleteEntry(java.lang.String)
     */
    @Override
    public void deleteEntry(String dn) throws LDAPException
    {
        try
        {
            if(logger.isDebugEnabled())
            {
                logger.debug("About to delete entry " + dn );
            } 
            
            getConn().unbind(dn);
            
            if(logger.isInfoEnabled())
            {
                logger.info("Deleted entry " + dn);
            }             
        }
        catch (NamingException nex)
        {
            throw handleNamingException(nex, "Delete entry failed.");
        }
    }

    /**
     * 
     * @param oldDn
     * @param newDn
     * @throws LDAPException
     * @see org.mule.module.ldap.api.LDAPConnection#renameEntry(java.lang.String, java.lang.String)
     */
    @Override
    public void renameEntry(String oldDn, String newDn) throws LDAPException
    {
        try
        {
            if(logger.isDebugEnabled())
            {
                logger.debug("About to rename entry " + oldDn + " to " + newDn);
            }
            
            getConn().rename(oldDn, newDn);
            
            if(logger.isInfoEnabled())
            {
                logger.info("Renamed entry " + oldDn + " to " + newDn);
            }            
        }
        catch (NamingException nex)
        {
            throw handleNamingException(nex, "Rename entry " + oldDn + " to " + newDn + " failed.");
        }
    }
    
    /**
     * @param dn
     * @param attribute
     * @throws LDAPException
     * @see org.mule.module.ldap.api.LDAPConnection#addAttribute(java.lang.String,
     *      org.mule.module.ldap.api.LDAPEntryAttribute)
     */
    @Override
    public void addAttribute(String dn, LDAPEntryAttribute attribute) throws LDAPException
    {
        try
        {
            ModificationItem[] mods = new ModificationItem[1];
            mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE, buildBasicAttribute(attribute));
            getConn().modifyAttributes(dn, mods);
        }
        catch (NamingException nex)
        {
            throw handleNamingException(nex, "Add attribute " + (attribute != null ? attribute.getName() : "null") + " to entry " + dn + " failed.");
        }
    }

    /**
     * @param dn
     * @param attribute
     * @throws LDAPException
     * @see org.mule.module.ldap.api.LDAPConnection#updateAttribute(java.lang.String,
     *      org.mule.module.ldap.api.LDAPEntryAttribute)
     */
    @Override
    public void updateAttribute(String dn, LDAPEntryAttribute attribute) throws LDAPException
    {
        try
        {
            ModificationItem[] mods = new ModificationItem[1];
            mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, buildBasicAttribute(attribute));
            getConn().modifyAttributes(dn, mods);
        }
        catch (NamingException nex)
        {
            throw handleNamingException(nex, "Update attribute " + (attribute != null ? attribute.getName() : "null") + " from entry " + dn + " failed.");
        }
    }

    /**
     * @param dn
     * @param attribute
     * @throws LDAPException
     * @see org.mule.module.ldap.api.LDAPConnection#deleteAttribute(java.lang.String,
     *      org.mule.module.ldap.api.LDAPEntryAttribute)
     */
    @Override
    public void deleteAttribute(String dn, LDAPEntryAttribute attribute) throws LDAPException
    {
        try
        {
            ModificationItem[] mods = new ModificationItem[1];
            mods[0] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE, buildBasicAttribute(attribute));
            getConn().modifyAttributes(dn, mods);
        }
        catch (NamingException nex)
        {
            throw handleNamingException(nex, "Delete attribute " + (attribute != null ? attribute.getName() : "null") + " from entry " + dn + " failed.");
        }
    }

    private void silentyCloseDirContext(DirContext ctx)
    {
        if (ctx != null)
        {
            try {
                ctx.close();
            }
            catch (NamingException nex)
            {
                logger.warn("Cannot close directory context", nex);
            }
        }
    }
    
    @Override
    public LDAPEntryAttributeTypeDefinition getAttributeTypeDefinition(String attributeName) throws LDAPException
    {
        if (this.schemaCache != null)
        {
            try
            {
                return this.schemaCache.get(attributeName);
            }
            catch (ExecutionException ex)
            {
                logger.error("Could not retrieve attribute type definition for attribute " + attributeName + " from cache. Trying to retrieve directly.", ex);
                return retrieveAttributeTypeDefinition(attributeName);
            }
        }
        else
        {
            logger.info("Schema cache disabled. Retriving attribute type definition directly.");
            return retrieveAttributeTypeDefinition(attributeName);
        }
    }

    /**
     * 
     * @param attributeName
     * @return
     * @throws LDAPException
     */
    protected LDAPEntryAttributeTypeDefinition retrieveAttributeTypeDefinition(String attributeName) throws LDAPException
    {
        DirContext schema = null;
        DirContext attrSchema = null;
        try
        {
            // Get the schema tree root
            schema = getConn().getSchema("");

            if (logger.isDebugEnabled())
            {
                logger.debug("About to retrieve attribute definition for attribute " + attributeName);
            }
            
            // Get the schema object for "attributeName"
            attrSchema = (DirContext) schema.lookup("AttributeDefinition/" + attributeName);   
            
            return LDAPJNDIUtils.buildAttributeTypeDefinition(attrSchema.getAttributes(""));
        }
        catch(NamingException nex)
        {
            throw handleNamingException(nex, "Get attribute type definition for attribute " + attributeName + " failed.");
        }
        finally
        {
            silentyCloseDirContext(attrSchema);
            silentyCloseDirContext(schema);
        }
    }
    
    @Override
    public List<String> getAllObjectClasses() throws LDAPException
    {
        DirContext schema = null;
        DirContext attrSchema = null;
        try
        {
            // Get the schema tree root
            schema = getConn().getSchema("");

            if (logger.isDebugEnabled())
            {
                logger.debug("About to retrieve all object classes");
            }
            
            NamingEnumeration<Binding> bindings = schema.listBindings("ClassDefinition");
            List<String> objectClasses = new ArrayList<String>(200);
            while (bindings.hasMore())
            {
                Binding binding = bindings.next();
                objectClasses.add(binding.getName());
            }
            
            return objectClasses;
        }
        catch(NamingException nex)
        {
            throw handleNamingException(nex, "Get all object classes failed.");
        }
        finally
        {
            silentyCloseDirContext(attrSchema);
            silentyCloseDirContext(schema);
        }    	
    }
    
    @Override
    public LDAPEntryObjectClassDefinition getObjectClassDefinition(String objectClassName) throws LDAPException
    {
        DirContext schema = null;
        DirContext attrSchema = null;
        try
        {
            // Get the schema tree root
            schema = getConn().getSchema("");

            if (logger.isDebugEnabled())
            {
                logger.debug("About to retrieve class definition for objectClass " + objectClassName);
            }
            
            // Get the schema object for "objectClassName"
            attrSchema = (DirContext) schema.lookup("ClassDefinition/" + objectClassName);   
            
            return LDAPJNDIUtils.buildObjectClassDefinition(attrSchema.getAttributes(""));
        }
        catch(NamingException nex)
        {
            throw handleNamingException(nex, "Get class definition for objectClass " + objectClassName + " failed.");
        }
        finally
        {
            silentyCloseDirContext(attrSchema);
            silentyCloseDirContext(schema);
        }
    }
    
    /**
     * @return Returns the authentication.
     */
    public String getAuthentication()
    {
        return authentication;
    }

    /**
     * @param authentication The authentication to set.
     */
    public void setAuthentication(String authentication)
    {
        this.authentication = authentication;
    }

    /**
     * @return Returns the initialPoolSizeConnections.
     */
    public int getInitialPoolSizeConnections()
    {
        return initialPoolSizeConnections;
    }

    /**
     * @param initialPoolSizeConnections The initialPoolSizeConnections to set.
     */
    public void setInitialPoolSizeConnections(int initialPoolSizeConnections)
    {
        this.initialPoolSizeConnections = initialPoolSizeConnections;
    }

    /**
     * @return Returns the maxPoolConnections.
     */
    public int getMaxPoolConnections()
    {
        return maxPoolConnections;
    }

    /**
     * @param maxPoolConnections The maxPoolConnections to set.
     */
    public void setMaxPoolConnections(int maxPoolConnections)
    {
        this.maxPoolConnections = maxPoolConnections;
    }

    /**
     * @return Returns the poolTimeout.
     */
    public long getPoolTimeout()
    {
        return poolTimeout;
    }

    /**
     * @param poolTimeout The poolTimeout to set.
     */
    public void setPoolTimeout(long poolTimeout)
    {
        this.poolTimeout = poolTimeout;
    }

    /**
     * @return Returns the providerUrl.
     */
    public String getProviderUrl()
    {
        return providerUrl;
    }

    /**
     * @param providerUrl The providerUrl to set.
     */
    public void setProviderUrl(String provider)
    {
        this.providerUrl = provider;
    }

    /**
     * 
     * @return
     */
    public boolean isConnectionPoolEnabled()
    {
        return getInitialPoolSizeConnections() > 0;
    }

    /**
     * @return Returns the initialContextFactory.
     */
    public String getInitialContextFactory()
    {
        return initialContextFactory;
    }

    /**
     * @param initialContextFactory The initialContextFactory to set.
     */
    public void setInitialContextFactory(String initialContextFactory)
    {
        this.initialContextFactory = initialContextFactory;
    }
    
    /**
     * @return Returns the conn.
     */
    private LdapContext getConn() throws LDAPException
    {
        if (conn == null)
        {
            throw new LDAPException("Connection is closed. Call bind method first.");
        }
        return conn;
    }

    /**
     * @param conn The conn to set.
     */
    private void setConn(LdapContext conn)
    {
        this.conn = conn;
    }



    /**
     * @param attrs
     * @return
     * @throws LDAPException
     */
    private Attributes buildAttributes(LDAPEntryAttributes attrs) throws LDAPException
    {
        Attributes attributes = new BasicAttributes(IGNORE_CASE);

        for (Iterator<LDAPEntryAttribute> it = attrs.attributes(); it.hasNext();)
        {
            attributes.put(buildBasicAttribute((LDAPEntryAttribute) it.next()));
        }

        return attributes;
    }

    /**
     * @param entry
     * @return
     * @throws LDAPException
     */
    private Attributes buildAttributes(LDAPEntry entry) throws LDAPException
    {
        return buildAttributes(entry.getAttributes());
    }

    /**
     * @param attribute
     * @return
     * @throws LDAPException
     */
    private BasicAttribute buildBasicAttribute(LDAPEntryAttribute attribute) throws LDAPException
    {
        if (attribute != null)
        {
            if (attribute.isMultiValued())
            {
                BasicAttribute basicAttribute = new BasicAttribute(attribute.getName());
                for (Iterator<Object> it = attribute.getValues().iterator(); it.hasNext();)
                {
                    basicAttribute.add(it.next());
                }
                return basicAttribute;
            }
            else
            {
                return new BasicAttribute(attribute.getName(), attribute.getValue());
            }
        }
        else
        {
            return null;
        }
    }

    public String getReferral()
    {
        return referral;
    }

    public void setReferral(String referral)
    {
        this.referral = referral;
    }
    
    @Override
    public String toString()
    {
        try
        {
            String user = getBindedUserDn();
            return (user != null ? user : "anonymous") + "@" + getProviderUrl();
        }
        catch(Throwable ex)
        {
            return "{unknown}@" + getProviderUrl();
        }
    }
}
