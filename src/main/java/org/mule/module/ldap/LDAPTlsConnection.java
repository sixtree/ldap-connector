/**
 * Copyright (c) MuleSoft, Inc. All rights reserved. http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.md file.
 */
package org.mule.module.ldap;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;
import org.mule.api.ConnectionException;
import org.mule.api.ConnectionExceptionCode;
import org.mule.api.annotations.Configurable;
import org.mule.api.annotations.Connect;
import org.mule.api.annotations.ConnectStrategy;
import org.mule.api.annotations.ConnectionIdentifier;
import org.mule.api.annotations.Disconnect;
import org.mule.api.annotations.TestConnectivity;
import org.mule.api.annotations.ValidateConnection;
import org.mule.api.annotations.components.ConnectionManagement;
import org.mule.api.annotations.display.FriendlyName;
import org.mule.api.annotations.display.Password;
import org.mule.api.annotations.display.Placement;
import org.mule.api.annotations.param.ConnectionKey;
import org.mule.api.annotations.param.Default;
import org.mule.api.annotations.param.Optional;
import org.mule.module.ldap.api.LDAPConnection;
import org.mule.module.ldap.api.LDAPEntry;
import org.mule.module.ldap.api.LDAPException;

@ConnectionManagement(friendlyName="TLS Config", configElementName="tls-config")
public class LDAPTlsConnection implements LDAPConnectionStrategy
{
    protected final Logger logger = Logger.getLogger(getClass());

    protected final static boolean USE_TLS = true;
    protected final static int DISABLE_POOLING = 0;
    protected final static int NO_POOLING_TIMEOUT = 0;
    
    
    /**
     * The connection URL to the LDAP server with the following syntax: <code>ldap[s]://hostname:port/base_dn</code>.
     */
    @Configurable
    @Placement(group = "Connection", order = 0)
    @FriendlyName("URL")
    private String url;

    /**
     * The implementation of the connection to be used. 
     */
    @Configurable
    @Default(value = "JNDI")
    private Type type;

    /**
     * Constant that holds the name of the environment property for specifying how referrals encountered by the service provider are to be processed (follow, ignore, throw).
     */
    @Configurable
    @Default(value = "IGNORE")
    @Placement(group = "Advanced")
    private Referral referral;
    
    /**
     * This is a {@link Map} instance holding extended configuration attributes that will be used in the Context environment.
     * When working with TLS connections you need to make sure that the native LDAP pooling functionality is turned off. For example if
     * using JNDI do not use attributes such as 'com.sun.jndi.ldap.connect.pool=true' will cause problems when using TLS.
     */
    @Configurable
    @Optional
    @Placement(group = "Advanced")
    private Map<String, String> extendedConfiguration;
    
    /**
     * If set to true, the LDAP connector will use the LDAP schema (only works for LDAP v3) to define the structure of the LDAP entry (or map). This needs to be 'true'
     * in order to use DataSense as it will affect the implementing class of {@link LDAPEntry} attributes.
     * @since 2.0.0
     */
    @Configurable
    @Default(value = "false")
    @Placement(group = "General")
    private boolean schemaEnabled;
    
	/**
     * 
     */
    public LDAPTlsConnection()
    {
    }

    /*
     * LDAP client
     */
    private LDAPConnection connection = null;
    
    private final String connectionIdPrefix = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    
    // Connection Management
    /**
     * Establish the connection to the LDAP server and using TLS. As native LDAP pooling is not supported with TLS, pooling capabilities will be
     * offered by the connector itself and as the TLS channel negotiation process is quite expensive, great performance benefits will be gained by
     * doing so.
     * 
     * @param authDn The DN (distinguished name) of the user (for example: uid=user,ou=people,dc=mulesoft,dc=org).
     *               If using Microsoft Active Directory, instead of the DN, you can provide the user@domain (for example: user@mulesoft.org)
     * @param authPassword The password of the user
     * @param authentication Specifies the authentication mechanism to use. For the Sun LDAP service provider, this can be one of the following strings:
     * <ul>
     *    <li><b>simple</b> (DEFAULT): Used for user/password authentication.</li>
     *    <li><b>none</b>: Used for anonymous authentication.</li>
     *    <li><b>sasl_mech</b> (UNSUPPORTED): Where sasl_mech is a space-separated list of SASL mechanism names.
     *             SASL is the Simple Authentication and Security Layer (RFC 2222). It specifies a challenge-response protocol in which
     *             data is exchanged between the client and the server for the purposes of authentication and establishment of a security
     *             layer on which to carry out subsequent communication. By using SASL, the LDAP can support any type of authentication
     *             agreed upon by the LDAP client and server.</li>
     * </ul>
     * @throws ConnectionException Holding one of the possible values in {@link ConnectionExceptionCode}.
     */
    @Connect(strategy=ConnectStrategy.MULTIPLE_INSTANCES)
    @TestConnectivity
    public void connect(@ConnectionKey @FriendlyName("Principal DN") String authDn, @Optional @FriendlyName("Password") @Password String authPassword, @Optional String authentication) throws ConnectionException
    {
        
        authentication = authentication == null ? LDAPConnection.SIMPLE_AUTHENTICATION : authentication;
        /*
         * DevKit doesn't support null values for the @Connect parameters. In order to have an anonymous bind, the
         * authentication parameter should be "none" and a default value should be provided as value for "authDn".
         */
        try
        {
            if(this.connection == null)
            {
                this.connection = LDAPConnection.getConnection(type.toString(), getUrl(), authentication, DISABLE_POOLING, DISABLE_POOLING, NO_POOLING_TIMEOUT, getReferral().toString(), getExtendedConfiguration(), isSchemaEnabled(), USE_TLS);
            }
            
            if(LDAPConnection.NO_AUTHENTICATION.equals(authentication))
            {
                // Anonymous -> Ignoring authDn and authPassword
                // For DevKit connection Management to work, authDn should be set to a value (like ANONYMOUS)
                this.connection.bind(null, null);
            }
            else
            {
                this.connection.bind(authDn, authPassword);
            }
        }
        catch(Throwable ex)
        {
            throw LDAPConnector.toConnectionException(ex);
        }
    }
    
    /**
     * Disconnect the current connection
     */
    @Override
    @Disconnect
    public void disconnect()
    {
        String id = connectionId();
        if(logger.isDebugEnabled())
        {
            logger.debug("About to disconnect " + id);
        }
        if (this.connection != null)
        {
            try
            {
                this.connection.close();
            }
            catch (LDAPException ex)
            {
                logger.error("Unable to close connection " + id + ". Forcing close anyway.", ex);
            }
            finally
            {
                this.connection = null;
            }
        }
    }

    /**
     * Are we connected?
     * 
     * @return boolean <i>true</i> if the connection is still valid or <i>false</i> otherwise.
     */
    @ValidateConnection
    public boolean isConnected()
    {
        try
        {
            return this.connection != null && !this.connection.isClosed();
        }
        catch (Exception ex)
        {
            logger.error("Unable to validate LDAP connection. Returning that LDAP is not connected.", ex);
            return false;
        }        
    }

    /**
     * Returns the connection ID
     * 
     * @return String with the connection Id
     */
    @ConnectionIdentifier
    public String connectionId()
    {
        return "[" + connectionIdPrefix + "]:" + (this.connection != null ? this.connection.toString() : "{null connection}");
    }    
    
    // Getters and Setters of @Configurable elements
    
    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public Type getType()
    {
        return type;
    }

    public void setType(Type type)
    {
        this.type = type;
    }

    public Referral getReferral()
    {
        return referral;
    }

    public void setReferral(Referral referral)
    {
        this.referral = referral;
    }

    public Map<String, String> getExtendedConfiguration()
    {
        return extendedConfiguration;
    }

    public void setExtendedConfiguration(Map<String, String> extendedConfiguration)
    {
        this.extendedConfiguration = extendedConfiguration;
    }

    public boolean isSchemaEnabled()
    {
        return schemaEnabled;
    }

    public void setSchemaEnabled(boolean schemaEnabled)
    {
        this.schemaEnabled = schemaEnabled;
    }

    @Override
    public LDAPConnection getConnection()
    {
        return connection;
    }    
}


