/**
 * Copyright (c) MuleSoft, Inc. All rights reserved. http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.md file.
 */

package org.mule.module.ldap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.mule.api.ConnectionException;
import org.mule.api.annotations.ConnectionStrategy;
import org.mule.api.annotations.Connector;
import org.mule.api.annotations.MetaDataKeyRetriever;
import org.mule.api.annotations.MetaDataRetriever;
import org.mule.api.annotations.Paged;
import org.mule.api.annotations.Processor;
import org.mule.api.annotations.ReconnectOn;
import org.mule.api.annotations.Transformer;
import org.mule.api.annotations.display.FriendlyName;
import org.mule.api.annotations.display.Placement;
import org.mule.api.annotations.param.Default;
import org.mule.api.annotations.param.MetaDataKeyParam;
import org.mule.api.annotations.param.MetaDataKeyParamAffectsType;
import org.mule.api.annotations.param.Optional;
import org.mule.common.metadata.DefaultMetaData;
import org.mule.common.metadata.DefaultMetaDataKey;
import org.mule.common.metadata.MetaData;
import org.mule.common.metadata.MetaDataKey;
import org.mule.common.metadata.MetaDataModel;
import org.mule.common.metadata.builder.DefaultMetaDataBuilder;
import org.mule.common.metadata.builder.DynamicObjectBuilder;
import org.mule.module.ldap.api.CommunicationException;
import org.mule.module.ldap.api.ContextNotEmptyException;
import org.mule.module.ldap.api.InvalidAttributeException;
import org.mule.module.ldap.api.LDAPConnection;
import org.mule.module.ldap.api.LDAPEntry;
import org.mule.module.ldap.api.LDAPException;
import org.mule.module.ldap.api.LDAPMultiValueEntryAttribute;
import org.mule.module.ldap.api.LDAPResultSet;
import org.mule.module.ldap.api.LDAPSearchControls;
import org.mule.module.ldap.api.LDAPSingleValueEntryAttribute;
import org.mule.module.ldap.api.LDAPSortKey;
import org.mule.module.ldap.api.NameNotFoundException;
import org.mule.streaming.PagingConfiguration;
import org.mule.streaming.ProviderAwarePagingDelegate;
import org.mule.util.StringUtils;

/**
 * 
 * The LDAP Connector will allow to connect to any LDAP server and perform every LDAP operation:
 * <ul>
 *  <li><a href="#bind"><b>bind</b></a>: Authenticate against the LDAP server. This occurs automatically before each operation but can also be performed on request</li>
 *  <li><a href="#search"><b>search</b></a>: Perform a LDAP search in a base DN with a given filter</li>
 *  <li><a href="#lookup"><b>lookup</b></a>: Retrieve a unique LDAP entry</li>
 *  <li><a href="#add"><b>add</b></a>: Creates a new LDAP entry</li>
 *  <li><a href="#add-single-value-attribute"><b>add attribute/s</b></a>: Add specific attributes to an existing LDAP entry</li>
 *  <li><a href="#modify"><b>modify</b></a>: Update an existing LDAP entry</li>
 *  <li><a href="#modify-single-value-attribute"><b>modify attribute/s</b></a>: Update specific attributes of an existing LDAP entry</li>
 *  <li><a href="#delete"><b>delete</b></a>: Delete an existing LDAP entry</li>
 *  <li><a href="#delete-single-value-attribute"><b>delete attribute/s</b></a>: Delete specific attributes of an existing LDAP entry</li>
 * </ul>
 * <p/>
 * In order to be able to use any of the operations listed before, you must define a <a href="#config"><b>config</b></a> element with the LDAP connection parameters:
 * 
 * <table>
 *  <tr>
 *  <td><b>URL</b></td>
 *  <td>
 * The connection URL to the LDAP server. LDAP connection URLs have the following syntax: <code>ldap[s]://hostname:port/base_dn</code>
 * <p/>
 * <ul>
 *    <li><b>hostname</b>: Name (or IP address in dotted format) of the LDAP server. For example, ldap.example.com or 192.202.185.90.</li>
 *    <li><b>port</b>: Port number of the LDAP server (for example, 696). If no port is specified, the standard LDAP port (389) or LDAPS port (636) is used.</li>
 *    <li><b>base_dn</b>: distinguished name (DN) of an entry in the directory. This DN identifies the entry that is the starting point of the search. If no base DN is specified, the search starts at the root of the directory tree.</li>
 * </ul>
 * 
 * Some examples are:
 * <ul>
 *    <li>ldap://localhost:389/</i>
 *    <li>ldap://localhost:389/dc=mulesoft,dc=org</i>
 *    <li>ldaps://localhost:636/dc=mulesoft,dc=org</i>
 *    <li>ldaps://ldap.mulesoft.org/</i>
 * </ul>
 *  </td>
 *  </tr>
 *  <tr>
 *  <td><b>Type</b></td>
 *  <td>
 * The implementation of the connection to be used. Right now the only available implementation is JNDI, though any other
 * implementation can be used (For example using Novell libraries). If you want to create your own implementation you should
 * extend the class {@link LDAPConnection}
 * <ul>
 *    <li><b>JNDI</b>: Implementation that uses the JNDI interfaces provided in the standard JRE.</i>
 * </ul>
 *  </td>
 *  </tr>
 *  <tr>
 *  <td><b>Initial Pool Size</b></td>
 *  <td>
 * The string representation of an integer that represents the number of connections per connection identity to create when initially
 * creating a connection for the identity. To disable pooling, just set this value to 0 (zero).
 *  </td>
 *  </tr>
 *  <tr>
 *  <td><b>Max Pool Size</b></td>
 *  <td>
 * The string representation of an integer that represents the maximum number of connections per connection identity that can be maintained
 * concurrently.
 *  </td>
 *  </tr>
 *  <tr>
 *  <td><b>Pool Timeout</b></td>
 *  <td>
 * The string representation of an integer that represents the number of milliseconds that an idle connection may remain in the pool without
 * being closed and removed from the pool. 
 *  </td>
 *  </tr>
 *  <tr>
 *  <td><b>Referral</b></td>
 *  <td>
 * Constant that holds the name of the environment property for specifying how referrals encountered by the service provider are to be processed.
 * The value of the property is one of the following strings:
 * <ul>
 *    <li><b>follow</b>: Follow referrals automatically</li>
 *    <li><b>ignore</b>: Ignore referrals</li>
 *    <li><b>throw</b>: Throw ReferralException when a referral is encountered.</li>
 * </ul>
 *  </td>
 *  </tr>
 *  <tr>
 *  <td><b>Extended Configuration</b></td>
 *  <td>
 * This is a {@link Map} instance holding extended configuration attributes that will be used in the Context environment.
 * Values configured here have less precedence than the other values that are allowed
 * in the module configuration.
 * Some examples of extended properties (key: value) are:
 * <ul>
 *    <li><b>java.naming.language</b>: Constant that holds the name of the environment
 *                property for specifying the preferred language to use with the service.
 *                The value of the property is a colon-separated list of language tags as
 *                defined in RFC 1766.</li>
 *    <li><b>java.naming.security.authentication</b>: Constant that holds the name of the environment
 *                property for specifying the security level to use. Its value is one of the following
 *                strings: "none", "simple", "strong".</li>
 *    <li><b>java.naming.security.protocol</b>: Constant that holds the name of the environment property for specifying
 *                the security protocol to use. Its value is a string determined by the service provider (e.g. "ssl").</li>
 *    <li><b>com.sun.jndi.ldap.connect.pool.authentication</b>: A list of space-separated authentication types of connections that may be
 *                pooled. Valid types are "none", "simple", and "DIGEST-MD5".</li>
 *    <li><b>com.sun.jndi.ldap.connect.pool.debug</b>: A string that indicates the level of debug output to produce. Valid values are "fine"
 *                (trace connection creation and removal) and "all" (all debugging information).</li>
 *    <li><b>com.sun.jndi.ldap.connect.pool.prefsize</b>: The string representation of an integer that represents the preferred number of
 *                connections per connection identity that should be maintained concurrently.</li>
 *    <li><b>com.sun.jndi.ldap.connect.pool.protocol</b>: A list of space-separated protocol types of connections that may be pooled. Valid types are "plain" and "ssl".</li>
 * </ul>
 *  </td>
 *  </tr>
 *  <tr>
 *  <td><b>Use Schema</b></td>
 *  <td>
 * If set to true, the LDAP connector will use the LDAP schema (only works for LDAP v3) to define the structure of the LDAP entry (or map). This needs to be 'true'
 * in order to use DataSense.
 * <br/>
 * If useSchema is true, then the LDAP server schema will be used to determine if attributes of the {@link LDAPEntry} will be Multi Valued ({@link LDAPMultiValueEntryAttribute}) or Single Value {@link LDAPSingleValueEntryAttribute}. This translates if the
 * value will be a {@link java.util.List} or a single Object (String, byte[], etc.). In the past, attributes were Multi Valued only when the retrieved LDAP entry had more
 * than one value.
 * <br/><br/>
 * <b>Example:</b><br/>
 * <i>Sample LDAP server entry</i>:<br/>
 * <code>
 * dn: attr1=Value2,ou=group,dc=company,dc=org<br/>
 * attr1: Value1<br/>
 * attr2: Value2<br/>
 * multi1: Value3<br/>
 * multi1: Value4<br/>
 * objectclass: top<br/>
 * objectclass: myentry
 * </code>
 * <br/><br/>
 * <i>Schema for objectClass myentry</i>:<br/>
 * <code>
 * attr1: {SINGLE-VALUE=true}<br/>
 * attr2: {SINGLE-VALUE=false}<br/>
 * multi1: {SINGLE-VALUE=false}<br/>
 * </code>
 * <br/><br/>
 * <i>If useSchema is <b>false</b> then the resulting {@link LDAPEntry} representing the payload will return</i>:<br/>
 * <code>
 * // Using LDAPEntry methods<br/>
 * payload.getAttribute("attr1")  returns {@link LDAPSingleValueEntryAttribute}<br/>
 * payload.getAttribute("attr2")  returns {@link LDAPSingleValueEntryAttribute} (<u>The attribute has only one value</u>)<br/>
 * payload.getAttribute("multi1") returns  {@link LDAPMultiValueEntryAttribute}<br/>
 * <br/><br/>
 * // Using {@link java.util.Map} methods<br/>
 * payload.get("attr1")  returns {@link java.lang.String}<br/>
 * payload.get("attr2")  returns {@link java.lang.String} (<u>The attribute has only one value</u>)<br/>
 * payload.get("multi1") returns {@link java.util.List}<br/>
 * </code>
 * <br/><br/>
 * <i>If useSchema is <b>true</b> then the resulting {@link LDAPEntry} representing the payload will return</i>:<br/>
 * <code>
 * // Using LDAPEntry methods<br/>
 * payload.getAttribute("attr1")  returns {@link LDAPSingleValueEntryAttribute}<br/>
 * payload.getAttribute("attr2")  returns {@link LDAPMultiValueEntryAttribute} (<u>The attribute is multi value</u>)<br/>
 * payload.getAttribute("multi1") returns  {@link LDAPMultiValueEntryAttribute}<br/>
 * <br/><br/>
 * // Using {@link java.util.Map} methods<br/>
 * payload.get("attr1")  returns {@link java.lang.String}<br/>
 * payload.get("attr2")  returns {@link java.util.List} (<u>The attribute is multi value</u>)<br/>
 * payload.get("multi1") returns {@link java.util.List}<br/>
 * </code>
 * </td>
 * </tr>
 * </table>
 * <p/>
 * {@sample.config ../../../doc/mule-module-ldap.xml.sample ldap:config-1}
 * <p/>
 * {@sample.config ../../../doc/mule-module-ldap.xml.sample ldap:config-2}
 * <p/>
 * {@sample.config ../../../doc/mule-module-ldap.xml.sample ldap:config-3}
 *
 * @author Mariano Capurro (MuleSoft, Inc.)
 */
@Connector(name = "ldap", schemaVersion = "3.6", friendlyName="LDAP", minMuleVersion="3.6", description="LDAP Connector that allows you to connect to any LDAP server and perform every LDAP operation")
public class LDAPConnector
{
    protected final Logger logger = Logger.getLogger(getClass());

    @ConnectionStrategy
    private LDAPCacheConnection connectionStrategy;
    
    // Operations
    /**
     * Performs an LDAP bind (login) operation. After login there will be a LDAP connection pool
     * ready to use for other operations using the authenticated user.
     * If no values are provided to override <i>authDn</i> and <i>authPassword</i> then using
     * this operation will just re-bind (re-authenticate) the user/password defined in the <i>config</i>
     * element. If new values are provided for <i>authDn</i> and <i>authPassword</i>, then authentication
     * will be performed.
     * <p/>
     * <h4>Re-authenticating and returning the LDAP entry using <i>config</i> level credentials (authDn & authPassword)</h4>
     * {@sample.xml ../../../doc/mule-module-ldap.xml.sample ldap:bind-1}
     * <h4>Authenticating and returning the LDAP entry using new credentials (authDn & authPassword)</h4>
     * {@sample.xml ../../../doc/mule-module-ldap.xml.sample ldap:bind-2}
     * <h4>Authenticating as anonymous user (returns always null)</h4>
     * {@sample.xml ../../../doc/mule-module-ldap.xml.sample ldap:bind-3}
     * <h4>Authenticating and returning the LDAP entry using credentials (authDn & authPassword) from Mule Expression</h4>
     * {@sample.xml ../../../doc/mule-module-ldap.xml.sample ldap:bind-4}
     * 
     * @return The {@link LDAPEntry} of the authenticated user.
     * @throws org.mule.module.ldap.api.NoPermissionException If the current binded user has no permissions to perform the lookup for its own LDAP entry.
     * @throws org.mule.module.ldap.api.NameNotFoundException If base DN is invalid (for example it doesn't exist)
     * @throws org.mule.module.ldap.api.LDAPException In case there is any other exception, mainly related to connectivity problems or referrals.
     * @throws Exception In case there is any other error performing the login and posterior lookup.
     *  
     */
    @Processor
    @ReconnectOn(exceptions = CommunicationException.class)
    public LDAPEntry bind() throws Exception
    {
        /*
         * Force the login. By the time the connection makes it here it is already handled by the Connection Manager.
         * :TODO: Check when the connection was binded and avoid rebinding a newly created connection by the connection manager.
         */
        getConnectionStrategy().getConnection().rebind();
        
        String dn = getConnectionStrategy().getConnection().getBindedUserDn();
        
        if(logger.isInfoEnabled())
        {
            logger.info("Bind was successful for user: " + (dn != null ? dn : "Anonymous"));
        }        
        
        LDAPEntry entry = null;
        
        if(dn != null)
        {
            if(logger.isDebugEnabled())
            {
                logger.debug("About to retrieve authenticated user entry for: " + dn);
            }
            
            try
            {
                entry = getConnectionStrategy().getConnection().lookup(dn);
            }
            catch(LDAPException ex)
            {
                // In some cases like Active Directory the DN will be an email address: username@domain
                logger.warn("Cannot retrieve entry for dn: " + dn, ex);
            }

            if(logger.isDebugEnabled())
            {
                logger.debug("Retrieved entry: " + entry);
            }
        }
        else
        {
            if(logger.isDebugEnabled())
            {
                logger.debug("Anoymous user returns no entry (null)");
            }            
        }
        return entry;
    }
    
    /**
     * Closes the current connection, forcing the login operation (bind) the next time it is used.
     * 
     *  {@sample.xml ../../../doc/mule-module-ldap.xml.sample ldap:unbind}
     *  
     *  @since 1.3.1
     */
    @Processor
    public void unbind()
    {
        getConnectionStrategy().disconnect();
    }
    
    /**
     * Retrieves an entry from the LDAP server base on its distinguished name (DN). DNs are the unique identifiers
     * of an LDAP entry, so this method will perform a search based on this ID and so return a single entry as result
     * or throw an exception if the DN is invalid or inexistent.
     * <p/>
     * Use this operation over {@link LDAPConnector#searchOne(String, String, List, SearchScope, int, long, boolean)} when you know the DN of the object you want to
     * retrieve.
     * <p/>
     * <h4>Lookup returning all attributes for the entry</h4>
     * {@sample.xml ../../../doc/mule-module-ldap.xml.sample ldap:lookup-1}
     * <h4>Lookup returning the attributes in the list obtained by expression</h4>
     * {@sample.xml ../../../doc/mule-module-ldap.xml.sample ldap:lookup-2}
     * <h4>Lookup returning the attributes defined in the XML config file</h4>
     * {@sample.xml ../../../doc/mule-module-ldap.xml.sample ldap:lookup-3}
     * 
     * @param dn The DN of the LDAP entry that will be retrieved.
     * @param attributes A list of the attributes that should be returned in the result. If the attributes list is empty or null, then by default all
     *        LDAP entry attributes are returned.
     * @param structuralObjectClass The type of entry that will be returned. Only for DataSense purposes to be used in Anypoint Studio IDE. Has no impact on runtime, that's why it is optional.
     * @return The {@link LDAPEntry} for the given <code>dn</code> parameter.
     * @throws org.mule.module.ldap.api.NoPermissionException If the current binded user has no permissions to perform the lookup for the given DN.
     * @throws org.mule.module.ldap.api.NameNotFoundException If base DN is invalid (for example it doesn't exist)
     * @throws org.mule.module.ldap.api.LDAPException In case there is any other exception, mainly related to connectivity problems or referrals.
     * @throws Exception In case there is any other error performing the lookup.
     */
    @Processor
    @ReconnectOn(exceptions = CommunicationException.class)
    public LDAPEntry lookup(@FriendlyName("DN") String dn, @Optional List<String> attributes, @Optional @MetaDataKeyParam(affects=MetaDataKeyParamAffectsType.OUTPUT) String structuralObjectClass) throws Exception
    {
        if(logger.isDebugEnabled())
        {
            logger.debug("About to retrieve LDAP entry: " + dn);
        }
        
        LDAPEntry entry = null;
        if(attributes != null && attributes.size() > 0)
        {
            entry = getConnectionStrategy().getConnection().lookup(dn, attributes.toArray(new String[0]));
        }
        else
        {
            entry = getConnectionStrategy().getConnection().lookup(dn);
        }
        
        if(logger.isDebugEnabled())
        {
            logger.debug("Retrieved entry: " + entry);
        }
        
        return entry;
    }

    /**
     * Checks whether a LDAP entry exists in the LDAP server or not.
     * <h4>Check if LDAP entry exists</h4>
     * {@sample.xml ../../../doc/mule-module-ldap.xml.sample ldap:exists-1}
     * @param dn The DN of the LDAP entry that will be retrieved.
     * @return true if the LDAP entry represented by <code>dn</code> exists or false if not
     * @throws org.mule.module.ldap.api.NoPermissionException If the current binded user has no permissions to perform the lookup for the given DN.
     * @throws org.mule.module.ldap.api.LDAPException In case there is any other exception, mainly related to connectivity problems or referrals.
     * @throws org.mule.module.ldap.api.NameNotFoundException Only in case throwException is true and the LDAP entry does not exist.
     * @throws Exception In case there is any other error checking for entry existence.
     */
    @Processor
    @ReconnectOn(exceptions = CommunicationException.class)
    public boolean exists(@FriendlyName("DN") String dn) throws Exception
    {
        try
        {
            return lookup(dn, null, null) != null;
        }
        catch(NameNotFoundException nnfe)
        {
            return false;
        }
    }
    
    /**
     * Performs a LDAP search returning a list with all the resulting LDAP entries.
     * <p/>
     * For queries returning large results it is recommended to use pagination (not all LDAP servers support this or are configured to support it).
     * For that you need to provide a page size value that should be less or equal than max results (count limit). If you are getting a
     * Sizelimit Exceeded exception then you should check that the authenticated user has enough privileges or the LDAP server is not
     * limited by configuration.
     * <p/>
     * <h4>Returning all attributes for all persons that have Doe as surname</h4>
     * {@sample.xml ../../../doc/mule-module-ldap.xml.sample ldap:search-1}
     * <p/>
     * <h4>Returning username and fullname for the first 100 person entries</h4>
     * {@sample.xml ../../../doc/mule-module-ldap.xml.sample ldap:search-2}
     * <p/>
     * <h4>Search that receives all configuration attributes using Mule Expressions</h4>
     * {@sample.xml ../../../doc/mule-module-ldap.xml.sample ldap:search-3}
     * 
     * @param baseDn The base DN of the LDAP search.
     * @param filter A valid LDAP filter. The LDAP connector supports LDAP search filters as defined in RFC 2254. Some examples are:
     *               <ul>
     *                  <li>(objectClass=*): All objects.</li>
     *                  <li>(&(objectClass=person)(!cn=andy)): All persons except for the one with common name (cn) "andy".</li>
     *                  <li>(sn=sm*): All objects with a surname that starts with "sm".</li>
     *                  <li>(&(objectClass=person)(|(sn=Smith)(sn=Johnson))): All persons with a surname equal to "Smith" or "Johnson".</li>
     *               </ul>
     * @param attributes A list of the attributes that should be returned in the result. If the attributes list is empty or null, then by default all
     *        LDAP entry attributes are returned.
     * @param scope The scope of the search. Valid attributes are:
     *              <ul>
     *                 <li><b>OBJECT</b>: This value is used to indicate searching only the entry at the base DN, resulting in only that entry
     *                               being returned (keeping in mind that it also has to meet the search filter criteria!)</li>
     *                 <li><b>ONE_LEVEL</b>: This value is used to indicate searching all entries one level under the base DN - but not including
     *                               the base DN and not including any entries under that one level under the base DN. </li>
     *                 <li><b>SUB_TREE</b>: This value is used to indicate searching of all entries at all levels under and including the specified base DN.</li>
     *              </ul>
     * @param timeout Search timeout in milliseconds. If the value is 0, this means to wait indefinitely. 
     * @param maxResults The maximum number of entries that will be returned as a result of the search. 0 indicates that all entries will be returned. 
     * @param returnObject Enables/disables returning objects returned as part of the result. If disabled, only the name and class of the object is returned.
     *                     If enabled, the object will be returned. 
     * @param pageSize If the LDAP server supports paging results set in this attribute the size of the page. If the pageSize is less or equals than 0, then paging will be disabled.
     * @param structuralObjectClass The type of entry that will be returned. Only for DataSense purposes to be used in Anypoint Studio IDE. Has no impact on runtime, that's why it is optional.
     * 
     * @return A {@link java.util.List} of {@link LDAPEntry} objects with the results of the search. If the search throws no results, then this is an empty list.
     * @throws org.mule.module.ldap.api.NoPermissionException If the current binded user has no permissions to perform the search under the given base DN.
     * @throws org.mule.module.ldap.api.NameNotFoundException If base DN is invalid (for example it doesn't exist)
     * @throws org.mule.module.ldap.api.LDAPException In case there is any other exception, mainly related to connectivity problems or referrals.
     * @throws Exception In case there is any other error performing the search.
     */
    @Processor
    @ReconnectOn(exceptions = CommunicationException.class)
    public List<LDAPEntry> search(@FriendlyName("Base DN") String baseDn, String filter, @Optional List<String> attributes,
                                  @Default("ONE_LEVEL") SearchScope scope, @Default("0") @Placement(group = "Search Controls") int timeout,
                                  @Default("0") @Placement(group = "Search Controls") long maxResults,
                                  @Default("false") @Placement(group = "Search Controls") boolean returnObject,
                                  @Default("0") @Placement(group = "Search Controls") int pageSize,
                                  @Optional @MetaDataKeyParam(affects=MetaDataKeyParamAffectsType.OUTPUT) String structuralObjectClass) throws Exception
    {
        LDAPResultSet result = null;
        try
        {
            if(logger.isDebugEnabled())
            {
                logger.debug("About to search LDAP entries matching " + filter + " under: " + baseDn);
            }
            
            LDAPSearchControls controls = new LDAPSearchControls();
            if(attributes != null && attributes.size() > 0)
            {
                controls.setAttributesToReturn(attributes.toArray(new String[0]));
            }
            controls.setMaxResults(maxResults);
            controls.setTimeout(timeout);
            controls.setScope(scope.getValue());
            controls.setReturnObject(returnObject);
            controls.setPageSize(pageSize);
            
            result = getConnectionStrategy().getConnection().search(baseDn, filter, controls);
            
            List<LDAPEntry> allEntries = result.getAllEntries();
            
            if(logger.isDebugEnabled())
            {
                logger.debug("Retrieved " + allEntries.size() + " entries");
            }
            
            return allEntries;        
        }
        finally
        {
            if(result != null)
            {
                result.close();
            }
        }
    }
    
    /**
     * Performs a LDAP search and streams result to the rest of the flow. This means that instead of returning a list with all results it partitions the LDAP
     * search result into pages (individual entry if <i>resultPageSize</i> is 1) or lists of size <i>resultPageSize</i>.
     * <p/>
     * This is an <b>intercepting</b> operation what means that for each result (individual entry if <i>resultPageSize</i> is 1 or {@link List} of <i>resultPageSize</i> size)
     * the rest of the flow will be executed. Each of these executions will return a result that will be aggregated into a {@link List} of results.
     * 
     * <p/>
     * For queries returning large results it is recommended to use pagination (not all LDAP servers support this or are configured to support it).
     * For that you need to provide a page size value that should be less or equal than max results (count limit). If you are getting a
     * Size Limit Exceeded exception message then you should check that the authenticated user has enough privileges or the LDAP server is not
     * limited by configuration.
     * <p/>
     * 
     * <h4>Returning all persons one LDAP entry at a time</h4>
     * {@sample.xml ../../../doc/mule-module-ldap.xml.sample ldap:paged-result-search-1}
     * <p/>
     * <h4>Returning all persons in lists of 100 LDAP entries</h4>
     * {@sample.xml ../../../doc/mule-module-ldap.xml.sample ldap:paged-result-search-2}
     * 
     * @param baseDn The base DN of the LDAP search.
     * @param filter A valid LDAP filter. The LDAP connector supports LDAP search filters as defined in RFC 2254. Some examples are:
     *               <ul>
     *                  <li>(objectClass=*): All objects.</li>
     *                  <li>(&(objectClass=person)(!cn=andy)): All persons except for the one with common name (cn) "andy".</li>
     *                  <li>(sn=sm*): All objects with a surname that starts with "sm".</li>
     *                  <li>(&(objectClass=person)(|(sn=Smith)(sn=Johnson))): All persons with a surname equal to "Smith" or "Johnson".</li>
     *               </ul>
     * @param attributes A list of the attributes that should be returned in the result. If the attributes list is empty or null, then by default all
     *        LDAP entry attributes are returned.
     * @param scope The scope of the search. Valid attributes are:
     *              <ul>
     *                 <li><b>OBJECT</b>: This value is used to indicate searching only the entry at the base DN, resulting in only that entry
     *                               being returned (keeping in mind that it also has to meet the search filter criteria!)</li>
     *                 <li><b>ONE_LEVEL</b>: This value is used to indicate searching all entries one level under the base DN - but not including
     *                               the base DN and not including any entries under that one level under the base DN. </li>
     *                 <li><b>SUB_TREE</b>: This value is used to indicate searching of all entries at all levels under and including the specified base DN.</li>
     *              </ul>
     * @param timeout Search timeout in milliseconds. If the value is 0, this means to wait indefinitely. 
     * @param maxResults The maximum number of entries that will be returned as a result of the search. 0 indicates that all entries will be returned. 
     * @param returnObject Enables/disables returning objects returned as part of the result. If disabled, only the name and class of the object is returned.
     *                     If enabled, the object will be returned. 
     * @param pageSize If the LDAP server supports paging results set in this attribute the size of the page. If the pageSize is less or equals than 0, then paging will be disabled.
     * @param orderBy Name of the LDAP attribute used to sort results.
     * @param ascending If <i>orderBy</i> was set, whether to sort in ascending or descending order.
     * @param structuralObjectClass The type of entry that will be returned. Only for DataSense purposes to be used in Anypoint Studio IDE. Has no impact on runtime, that's why it is optional.
     * @param pagingConfiguration Paging configuration. If the LDAP server supports paging results set in the fetchSize field of this object the size of the page. If the fetchSize is less or equals than 0, then paging will be disabled.
     * @return A list with individual results of executing the rest of flow with each results page.
     * @throws org.mule.module.ldap.api.NoPermissionException If the current binded user has no permissions to perform the search under the given base DN.
     * @throws org.mule.module.ldap.api.NameNotFoundException If base DN is invalid (for example it doesn't exist)
     * @throws org.mule.module.ldap.api.LDAPException In case there is any other exception, mainly related to connectivity problems or referrals.
     * @throws Exception In case there is any other error performing the search.
     */
    @Processor
    @ReconnectOn(exceptions = CommunicationException.class)
    @Paged(defaultFetchSize=200)
    public ProviderAwarePagingDelegate<LDAPEntry, LDAPConnector> pagedResultSearch(@FriendlyName("Base DN") String baseDn, String filter, @Optional List<String> attributes,
                                          @Default("ONE_LEVEL") SearchScope scope, @Default("0") @Placement(group = "Search Controls") int timeout,
                                          @Default("0") @Placement(group = "Search Controls") long maxResults,
                                          @Default("false") @Placement(group = "Search Controls") boolean returnObject,
                                          @FriendlyName("Order by attribute") @Optional @Placement(group = "Search Controls", order = 1) String orderBy,
                                          @FriendlyName("Ascending order?") @Default("true") @Placement(group = "Search Controls", order = 2) boolean ascending,
                                          @Optional @MetaDataKeyParam(affects=MetaDataKeyParamAffectsType.OUTPUT) String structuralObjectClass,
                                          PagingConfiguration pagingConfiguration) throws Exception
    {
        LDAPResultSet result = null;

        if(logger.isDebugEnabled())
        {
            logger.debug("About to search LDAP entries matching " + filter + " under: " + baseDn + ". Returning results in pages of " + pagingConfiguration.getFetchSize() + " entries.");
        }
        
        LDAPSearchControls controls = new LDAPSearchControls();
        if(attributes != null && attributes.size() > 0)
        {
            controls.setAttributesToReturn(attributes.toArray(new String[0]));
        }
        controls.setMaxResults(maxResults);
        controls.setTimeout(timeout);
        controls.setScope(scope.getValue());
        controls.setReturnObject(returnObject);
        controls.setPageSize(pagingConfiguration.getFetchSize());
        if(StringUtils.isNotBlank(orderBy))
        {
            controls.getSortKeys().add(new LDAPSortKey(orderBy, ascending, null));
        }
        
        result = getConnectionStrategy().getConnection().search(baseDn, filter, controls);
        
        return new LDAPPagingDelegate(result, pagingConfiguration);
    }

    /**
     * Performs a LDAP search that is supposed to return a unique result. If the search returns more than one result, then a
     * warn log message is generated and the first element of the result is returned.
     * <p/>
     * Use this operation over {@link LDAPConnector#lookup(String, List)} when you know don't know the DN of the entry you need
     * to retrieve but you have a set of attributes that you know should return a single entry (for example an email address)
     * 
     * {@sample.xml ../../../doc/mule-module-ldap.xml.sample ldap:search-one-1}
     * 
     * @param baseDn The base DN of the LDAP search.
     * @param filter A valid LDAP filter. The LDAP connector supports LDAP search filters as defined in RFC 2254. Some examples are:
     *               <ul>
     *                  <li>(objectClass=*): All objects.</li>
     *                  <li>(&(objectClass=person)(!cn=andy)): All persons except for the one with common name (cn) "andy".</li>
     *                  <li>(sn=sm*): All objects with a surname that starts with "sm".</li>
     *                  <li>(&(objectClass=person)(|(sn=Smith)(sn=Johnson))): All persons with a surname equal to "Smith" or "Johnson".</li>
     *               </ul>
     * @param attributes A list of the attributes that should be returned in the result. If the attributes list is empty or null, then by default all
     *        LDAP entry attributes are returned.
     * @param scope The scope of the search. Valid attributes are:
     *              <ul>
     *                 <li><b>OBJECT</b>: This value is used to indicate searching only the entry at the base DN, resulting in only that entry
     *                               being returned (keeping in mind that it also has to meet the search filter criteria!)</li>
     *                 <li><b>ONE_LEVEL</b>: This value is used to indicate searching all entries one level under the base DN - but not including
     *                               the base DN and not including any entries under that one level under the base DN. </li>
     *                 <li><b>SUB_TREE</b>: This value is used to indicate searching of all entries at all levels under and including the specified base DN.</li>
     *              </ul>
     * @param timeout Search timeout in milliseconds. If the value is 0, this means to wait indefinitely. 
     * @param maxResults The maximum number of entries that will be returned as a result of the search. 0 indicates that all entries will be returned. 
     * @param returnObject Enables/disables returning objects returned as part of the result. If disabled, only the name and class of the object is returned.
     *                     If enabled, the object will be returned. 
     * @param structuralObjectClass The type of entry that will be returned. Only for DataSense purposes to be used in Anypoint Studio IDE. Has no impact on runtime, that's why it is optional.
     * @return A {@link LDAPEntry} with the first element of the search result or null if there are no results.
     * @throws org.mule.module.ldap.api.NoPermissionException If the current binded user has no permissions to perform the search under the given base DN.
     * @throws org.mule.module.ldap.api.NameNotFoundException If base DN is invalid (for example it doesn't exist)
     * @throws org.mule.module.ldap.api.LDAPException In case there is any other exception, mainly related to connectivity problems or referrals.
     * @throws Exception In case there is any other error performing the search.
     */
    @Processor
    @ReconnectOn(exceptions = CommunicationException.class)
    public LDAPEntry searchOne(@FriendlyName("Base DN") String baseDn, String filter, @Optional List<String> attributes, @Default("ONE_LEVEL") SearchScope scope, @Default("0") @Placement(group = "Search Controls") int timeout, @Default("0") @Placement(group = "Search Controls") long maxResults, @Default("false") @Placement(group = "Search Controls") boolean returnObject, @Optional @MetaDataKeyParam(affects=MetaDataKeyParamAffectsType.OUTPUT) String structuralObjectClass) throws Exception
    {
        if(logger.isDebugEnabled())
        {
            logger.debug("Searching entries under " + baseDn + " with filter " + filter);
        }
        
        List<LDAPEntry> results = search(baseDn, filter, attributes, scope, timeout, maxResults, returnObject, 0, structuralObjectClass);
        
        if(results != null && results.size() > 1)
        {
            logger.warn("Search returned more than one result. Total results matching filter [" + filter + "]: " + results.size());
        }
        
        return results != null && results.size() > 0 ? results.get(0) : null;
    }

    private LDAPEntry mapToLDAPEntry(Map<String, Object> entryMap) throws LDAPException
    {
        return entryMap instanceof LDAPEntry ? (LDAPEntry) entryMap : new LDAPEntry(entryMap);        
    }
    
    /**
     * Creates a new {@link LDAPEntry} in the LDAP server. The entry should contain the distinguished name (DN), the <i>objectClass</i>
     * attributes that define its structure and at least a value for all the required attributes (required attributes depend on the
     * <i>object classes</i> assigned to the entry. You can refer to RFC 4519 for standard object classes and attributes.
     * 
     * <h4>LDAPEntry object provided with expression</h4>
     * {@sample.xml ../../../doc/mule-module-ldap.xml.sample ldap:add-1}
     * <h4>LDAPEntry object provided in payload</h4>
     * {@sample.xml ../../../doc/mule-module-ldap.xml.sample ldap:add-2}
     * 
     * @param entry The {@link LDAPEntry} that should be added.
     * @param structuralObjectClass The type of entry that will be added. Only for DataSense purposes to be used in Anypoint Studio IDE. Has no impact on runtime, that's why it is optional.
     * @throws org.mule.module.ldap.api.NoPermissionException If the current binded user has no permissions to add entries under any of the RDN (relative DN) that compose the entry DN.
     * @throws org.mule.module.ldap.api.InvalidAttributeException If the structure of the entry is invalid (for example there are missing required attributes or it has attributes that
     *         are not part of any of the defined object classes)
     * @throws org.mule.module.ldap.api.NameAlreadyBoundException If there is already an existing entry with the same DN in the LDAP server tree.
     * @throws org.mule.module.ldap.api.LDAPException In case there is any other exception, mainly related to connectivity problems or referrals.
     * @throws Exception In case there is any other error creating the entry.
     */
    @Processor
    @ReconnectOn(exceptions = CommunicationException.class)
    public void add(@Default("#[payload:]") Map<String, Object> entry, @Optional @MetaDataKeyParam(affects=MetaDataKeyParamAffectsType.INPUT) String structuralObjectClass) throws Exception
    {
        LDAPEntry ldapEntry = mapToLDAPEntry(entry);
        
        if(logger.isDebugEnabled())
        {
            logger.debug("About to add entry " + ldapEntry.getDn() + ": " + entry);
        }        
        
        getConnectionStrategy().getConnection().addEntry(ldapEntry);
        
        if(logger.isInfoEnabled())
        {
            logger.info("Added entry " + ldapEntry.getDn());
        }
    }
    
    /**
     * Creates a new entry in the LDAP server from a {@link Map} representation. The distinguished name (DN) of the
     * entry is first obtained from the optional parameter <i>dn</i> and if this value is blank (null, empty string
     * or string with only space chars) then the DN should be a present in the entry map as a {@link String} value
     * under the key "<b>dn</b>" (see {@link LDAPEntry#MAP_DN_KEY}).
     * <p/>
     * In order to represent a LDAP entry as a map, you should consider the following rules for the map key/value pair:
     * <ul>
     *    <li><b>Single Value Attributes</b>: The key should be the name of the single value attribute (for example uid, cn, ...) as a
     *                 {@link String} and the value is just the value of the attribute (most of the times represented by a {@link String}.</li>
     *    <li><b>Multi-value Attributes</b>: The key should be the name of the multiple values attribute (for example objectClass, memberOf, mail, ...)
     *                 as a {@link String} and the value should be a {@link List} holding the multiple values of the attribute. Usually it will be
     *                 a list of strings.</li>
     *    <li><b>Distinguished name</b>: As an alternative to passing the DN as a separate argument of the operation, you can include it in
     *                 the entry map. In this case, the key should be the string <code>"dn"</code> (see {@link LDAPEntry#MAP_DN_KEY}) and the value
     *                 a {@link String} representing the distinguished name (for example <code>cn=andy,ou=people,dc=mulesoft,dc=org</code>).</li>
     * </ul>
     * 
     * <h4>The map is provided as a reference and the DN is not in the map</h4>
     * {@sample.xml ../../../doc/mule-module-ldap.xml.sample ldap:add-from-map-1}
     * <h4>The map is created in the XML file and the DN is another map entry</h4>
     * {@sample.xml ../../../doc/mule-module-ldap.xml.sample ldap:add-from-map-2}
     * <h4>The map is provided in the payload and the DN is another map entry</h4>
     * {@sample.xml ../../../doc/mule-module-ldap.xml.sample ldap:add-from-map-3}
     *  
     * @param dn The primary value to use as DN of the entry. If not set, then the DN will be retrieved from the map representing the entry under the key <b>dn</b>.
     * @param entry {@link Map} representation of the LDAP entry.
     * @throws org.mule.module.ldap.api.NoPermissionException If the current binded user has no permissions to add entries under any of the RDN (relative DN) that compose the entry DN.
     * @throws org.mule.module.ldap.api.InvalidAttributeException If the structure of the entry is invalid (for example there are missing required attributes or it has attributes that
     *         are not part of any of the defined object classes)
     * @throws org.mule.module.ldap.api.NameAlreadyBoundException If there is already an existing entry with the same DN in the LDAP server tree.
     * @throws org.mule.module.ldap.api.LDAPException In case there is any other exception, mainly related to connectivity problems or referrals.
     * @throws Exception In case there is any other error creating the entry (for example if the DN is not passed as an argument nor in the entry map).
     * @deprecated LDAPEntry is now a Map<String, Object> so it makes no sense using this operation.
     */
    @Processor
    @ReconnectOn(exceptions = CommunicationException.class)
    @Deprecated
    public void addFromMap(@Optional @FriendlyName("DN") String dn, @Default("#[payload:]") Map<String, Object> entry) throws Exception
    {
        // Need to remove the DN from the map, so that it only contains attributes
        String entryDn = (String) entry.remove(LDAPEntry.MAP_DN_KEY);
        
        if(!StringUtils.isBlank(dn))
        {
            // DN attribute has precedence over entry DN at map level
            entryDn = dn;
        }
        else
        {
            logger.debug("DN is blank. Retrieved DN from entry map (key = " + LDAPEntry.MAP_DN_KEY + "): " + entryDn);
        }

        if(logger.isDebugEnabled())
        {
            logger.debug("About to add entry " + entryDn + ": " + entry);
        }
        
        getConnectionStrategy().getConnection().addEntry(new LDAPEntry(entryDn, entry));
        
        if(logger.isInfoEnabled())
        {
            logger.info("Added entry " + entryDn);
        }
    }
    
    /**
     * Updates an existing {@link LDAPEntry} in the LDAP server. The entry should contain an existing distinguished name (DN), the <i>objectClass</i>
     * attributes that define its structure and at least a value for all the required attributes (required attributes depend on the
     * <i>object classes</i> assigned to the entry. You can refer to RFC 4519 for standard object classes and attributes.
     * <p/>
     * When updating a LDAP entry, only the attributes in the entry passed as parameter are updated or added. If you
     * need to delete an attribute, you should use the delete attribute operation.
     * <p/>
     * <b>Example:</b> Updating one attributes and adding one.<br/><br/>
     * <i>Original LDAP server entry</i>:<br/>
     * <code>
     * dn: cn=entry,ou=group,dc=company,dc=org<br/>
     * cn: entry<br/>
     * attr1: Value1<br/>
     * attr2: Value2<br/>
     * multi1: Value3<br/>
     * multi1: Value4<br/>
     * objectclass: top<br/>
     * objectclass: myentry
     * </code>
     * <br/><br/>
     * <i>Entry map passed as parameter:</i><br/> 
     * <code>
     * dn: cn=entry,ou=group,dc=company,dc=org<br/>
     * attr1: NewValue<br/>
     * attr3: NewAttributeValue
     * </code>
     * <br/><br/>
     * <i>Resulting  LDAP server entry:</i><br/> 
     * <code>
     * dn: cn=entry,ou=group,dc=company,dc=org<br/>
     * cn: entry<br/>
     * attr1: NewValue<br/>
     * attr2: Value2<br/>
     * multi1: Value3<br/>
     * multi1: Value4<br/>
     * attr3: NewAttributeValue<br/>
     * objectclass: top<br/>
     * objectclass: myentry
     * </code>
     * <p/><p/>
     * <h4>The LDAP entry is in the payload</h4> 
     * {@sample.xml ../../../doc/mule-module-ldap.xml.sample ldap:modify-1}
     * <p/>
     * <h4>The LDAP entry is in a session variable</h4> 
     * {@sample.xml ../../../doc/mule-module-ldap.xml.sample ldap:modify-2}

     * @param entry The {@link LDAPEntry} that should be updated.
     * @param structuralObjectClass The type of entry that will be updated. Only for DataSense purposes to be used in Anypoint Studio IDE. Has no impact on runtime, that's why it is optional.
     * @throws org.mule.module.ldap.api.NoPermissionException If the current binded user has no permissions to update entries under any of the RDN (relative DN) that compose the entry DN.
     * @throws org.mule.module.ldap.api.InvalidAttributeException If the structure of the entry is invalid (for example there are missing required attributes or it has attributes that
     *         are not part of any of the defined object classes)
     * @throws org.mule.module.ldap.api.NameNotFoundException If there is no existing entry with the same DN in the LDAP server tree.
     * @throws org.mule.module.ldap.api.LDAPException In case there is any other exception, mainly related to connectivity problems or referrals.
     * @throws Exception In case there is any other error updating the entry.
     */
    @Processor
    @ReconnectOn(exceptions = CommunicationException.class)
    public void modify(@Default("#[payload:]") LDAPEntry entry, @Optional @MetaDataKeyParam(affects=MetaDataKeyParamAffectsType.INPUT) String structuralObjectClass) throws Exception
    {
        if(logger.isDebugEnabled())
        {
            logger.debug("About to modify entry " + entry.getDn() + ": " + entry);
        }        
        
        getConnectionStrategy().getConnection().updateEntry(entry);
        
        if(logger.isInfoEnabled())
        {
            logger.info("Modified entry " + entry.getDn());
        }
    }
    
    /**
     * Updates an existing entry in the LDAP server from a {@link Map} representation. The distinguished name (DN) of the
     * entry is first obtained from the optional parameter <i>dn</i> and if this value is blank (null, empty string
     * or string with only space chars) then the DN should be a present in the entry map as a {@link String} value
     * under the key "<b>dn</b>" (see {@link LDAPEntry#MAP_DN_KEY}).
     * <p/>
     * When updating a LDAP entry, only the attributes in the entry passed as parameter are updated or added. If you
     * need to delete an attribute, you should use the delete attribute operation.
     * <p/>
     * <b>Example:</b> Updating one attributes and adding one.<br/><br/>
     * <i>Original LDAP server entry</i>:<br/>
     * <code>
     * dn: cn=entry,ou=group,dc=company,dc=org<br/>
     * cn: entry<br/>
     * attr1: Value1<br/>
     * attr2: Value2<br/>
     * multi1: Value3<br/>
     * multi1: Value4<br/>
     * objectclass: top<br/>
     * objectclass: myentry
     * </code>
     * <br/><br/>
     * <i>Entry map passed as parameter:</i><br/>
     * <code>
     * dn: cn=entry,ou=group,dc=company,dc=org<br/>
     * attr1: NewValue<br/>
     * attr3: NewAttributeValue
     * </code>
     * <br/><br/>
     * <i>Resulting  LDAP server entry:</i><br/> 
     * <code>
     * dn: cn=entry,ou=group,dc=company,dc=org<br/>
     * cn: entry<br/>
     * attr1: NewValue<br/>
     * attr2: Value2<br/>
     * multi1: Value3<br/>
     * multi1: Value4<br/>
     * attr3: NewAttributeValue<br/>
     * objectclass: top<br/>
     * objectclass: myentry
     * </code>
     * <p/><p/>
     * In order to represent a LDAP entry as a map, you should consider the following rules for the map key/value pair:
     * <ul>
     *    <li><b>Single Value Attributes</b>: The key should be the name of the single value attribute (for example uid, cn, ...) as a
     *                 {@link String} and the value is just the value of the attribute (most of the times represented by a {@link String}.</li>
     *    <li><b>Multi-value Attributes</b>: The key should be the name of the multiple values attribute (for example objectClass, memberOf, mail, ...)
     *                 as a {@link String} and the value should be a {@link List} holding the multiple values of the attribute. Usually it will be
     *                 a list of strings.</li>
     *    <li><b>Distinguished name</b>: As an alternative to passing the DN as a separate argument of the operation, you can include it in
     *                 the entry map. In this case, the key should be the string <code>"dn"</code> (see {@link LDAPEntry#MAP_DN_KEY}) and the value
     *                 a {@link String} representing the distinguished name (for example <code>cn=andy,ou=people,dc=mulesoft,dc=org</code>).</li>
     * </ul>
     * <p/><p/>
     * <h4>The map is provided as a reference and the DN is not in the map</h4>
     * {@sample.xml ../../../doc/mule-module-ldap.xml.sample ldap:modify-from-map-1}
     * <h4>The map is created in the XML file and the DN is another map entry</h4>
     * {@sample.xml ../../../doc/mule-module-ldap.xml.sample ldap:modify-from-map-2}
     * <h4>The map is provided in the payload and the DN is another map entry</h4>
     * {@sample.xml ../../../doc/mule-module-ldap.xml.sample ldap:modify-from-map-3}
     * 
     * @param dn The primary value to use as DN of the entry. If not set, then the DN will be retrieved from the map representing the entry under the key <b>dn</b>.
     * @param entry {@link Map} representation of the LDAP entry.
     * @throws org.mule.module.ldap.api.NoPermissionException If the current binded user has no permissions to update entries under any of the RDN (relative DN) that compose the entry DN.
     * @throws org.mule.module.ldap.api.InvalidAttributeException If the structure of the entry is invalid (for example there are missing required attributes or it has attributes that
     *         are not part of any of the defined object classes)
     * @throws org.mule.module.ldap.api.NameNotFoundException If there is no existing entry with the same DN in the LDAP server tree.
     * @throws org.mule.module.ldap.api.LDAPException In case there is any other exception, mainly related to connectivity problems or referrals.
     * @throws Exception In case there is any other error updating the entry (for example if the DN is not passed as an argument nor in the entry map).
     * @deprecated LDAPEntry is now a Map<String, Object> so it makes no sense using this operation.
     */
    @Processor
    @ReconnectOn(exceptions = CommunicationException.class)
    @Deprecated
    public void modifyFromMap(@Optional @FriendlyName("DN") String dn, @Default("#[payload:]") Map<String, Object> entry) throws Exception
    {
        // Need to remove the DN from the map, so that it only contains attributes
        String entryDn = (String) entry.remove(LDAPEntry.MAP_DN_KEY);;
        
        if(!StringUtils.isBlank(dn))
        {
            // DN attribute has precedence over entry DN at map level
            entryDn = dn;
        }
        else
        {
            logger.debug("DN is blank. Retrieved DN from entry map (key = " + LDAPEntry.MAP_DN_KEY + "): " + entryDn);
        }

        if(logger.isDebugEnabled())
        {
            logger.debug("About to update entry " + entryDn + ": " + entry);
        }
        
        getConnectionStrategy().getConnection().updateEntry(new LDAPEntry(entryDn, entry));
        
        if(logger.isInfoEnabled())
        {
            logger.info("Updated entry " + entryDn);
        }
    }

    /**
     * Deletes the LDAP entry represented by the provided distinguished name. The entry should not have child entries, in which case a
     * {@link ContextNotEmptyException} is thrown.
     * <p/>
     * This operation is idempotent. It succeeds even if the terminal atomic name is not bound in the target context, but throws
     * {@link NameNotFoundException} if any of the intermediate contexts do not exist.
     * 
     * {@sample.xml ../../../doc/mule-module-ldap.xml.sample ldap:delete}
     * 
     * @param dn The DN of the LDAP entry to delete
     * @throws org.mule.module.ldap.api.NoPermissionException If the current binded user has no permissions to delete the entry.
     * @throws org.mule.module.ldap.api.NameNotFoundException If an intermediate context does not exist.
     * @throws org.mule.module.ldap.api.ContextNotEmptyException If the entry to delete has child entries.
     * @throws org.mule.module.ldap.api.LDAPException In case there is any other exception, mainly related to connectivity problems or referrals.
     * @throws Exception In case there is any other error deleting the entry.
     */
    @Processor
    @ReconnectOn(exceptions = CommunicationException.class)
    public void delete(@Default("#[payload:]") @FriendlyName("DN") String dn) throws Exception
    {
        if(logger.isDebugEnabled())
        {
            logger.debug("About to delete entry " + dn);
        }
        
        getConnectionStrategy().getConnection().deleteEntry(dn);
        
        if(logger.isInfoEnabled())
        {
            logger.info("Deleted entry " + dn);
        }        
    }
    
    /**
     * Renames and existing LDAP entry (moves and entry from a DN to another one).
     * 
     * {@sample.xml ../../../doc/mule-module-ldap.xml.sample ldap:rename}
     * 
     * @param oldDn DN of the existing entry that will be renamed.
     * @param newDn Destination DN
     * @throws org.mule.module.ldap.api.NameAlreadyBoundException If there is already an existing entry with the same DN as <i>newDn</i>.
     * @throws org.mule.module.ldap.api.LDAPException In case there is any other exception, mainly related to connectivity problems or referrals.
     * @throws Exception In case there is any other error deleting the entry.
     */
    @Processor
    @ReconnectOn(exceptions = CommunicationException.class)
    public void rename(@Placement(order = 1) @FriendlyName("Current DN") String oldDn, @Placement(order = 2) @FriendlyName("New DN") String newDn) throws Exception
    {
        if(logger.isDebugEnabled())
        {
            logger.debug("About to rename entry " + oldDn + " to " + newDn);
        }
        
        getConnectionStrategy().getConnection().renameEntry(oldDn, newDn);
        
        if(logger.isInfoEnabled())
        {
            logger.info("Renamed entry " + oldDn + " to " + newDn);
        }          
    }
    
    /**
     * Adds a value for an attribute in an existing LDAP entry. If the entry already contained a value for the given
     * <i>attributeName</i> then this value will be added (only if the attribute is multi value and there entry didn't
     * have the value already).
     * <p/>
     * If you want to add a value with a type different than {@link String}, then you can use the add-multi-value-attribute
     * operation and define a one element list with the value.
     * 
     * {@sample.xml ../../../doc/mule-module-ldap.xml.sample ldap:add-single-value-attribute}
     *  
     * @param dn The DN of the LDAP entry to modify
     * @param attributeName The name of the attribute to add a value to.
     * @param attributeValue The value for the attribute
     * @param ignoreInvalidAttribute If the attribute value to add is already present, then don't throw {@link org.mule.module.ldap.api.InvalidAttributeException}
     * @throws org.mule.module.ldap.api.NoPermissionException If the current binded user has no permissions to update the entry.
     * @throws org.mule.module.ldap.api.InvalidAttributeException If the attribute value is invalid or the entry already has the provided value.
     * @throws org.mule.module.ldap.api.NameNotFoundException If there is no existing entry for the given DN.
     * @throws org.mule.module.ldap.api.InvalidAttributeException If the entry does have the attribute value that should be added. Ignored if ignoreInvalidAttribute is true. <i>Note</i>: Not every LDAP server will through this exception.
     * @throws org.mule.module.ldap.api.LDAPException In case there is any other exception, mainly related to connectivity problems or referrals.
     * @throws Exception In case there is any other error updating the entry.
     */
    @Processor
    @ReconnectOn(exceptions = CommunicationException.class)
    public void addSingleValueAttribute(@FriendlyName("DN") String dn, String attributeName, String attributeValue, @Default("false") boolean ignoreInvalidAttribute) throws Exception
    {
        if(logger.isDebugEnabled())
        {
            logger.debug("About to add attribute " + attributeName + " with value " + attributeValue + " to entry " + dn);
        }
        
        try
        {
            getConnectionStrategy().getConnection().addAttribute(dn, new LDAPSingleValueEntryAttribute(attributeName, attributeValue));
        }
        catch(InvalidAttributeException iaex)
        {
            if(!ignoreInvalidAttribute)
            {
                throw iaex;
            }
            else
            {
                logger.info("Ignoring attribute addition. " + iaex.getMessage());
            }
        }
        
        if(logger.isInfoEnabled())
        {
            logger.info("Added attribute " + attributeName + " with value " + attributeValue + " to entry " + dn);
        }           
    }
    
    /**
     * Adds all the values for an attribute in an existing LDAP entry. If the entry already contained a value (or values)
     * for the given <i>attributeName</i> then these values will be added. The attribute should allow multiple values or
     * an exception will be raised.
     * 
     * {@sample.xml ../../../doc/mule-module-ldap.xml.sample ldap:add-multi-value-attribute}
     * 
     * @param dn The DN of the LDAP entry to modify
     * @param attributeName The name of the attribute to add values to.
     * @param attributeValues The values for the attribute
     * @param ignoreInvalidAttribute If the attribute value to add is already present, then don't throw {@link org.mule.module.ldap.api.InvalidAttributeException}
     * @throws org.mule.module.ldap.api.NoPermissionException If the current binded user has no permissions to update the entry.
     * @throws org.mule.module.ldap.api.NameNotFoundException If there is no existing entry for the given DN.
     * @throws org.mule.module.ldap.api.InvalidAttributeException If the attribute value is invalid or the entry already has the provided value.
     * @throws org.mule.module.ldap.api.InvalidAttributeException If the entry does have the attribute value that should be added. Ignored if ignoreInvalidAttribute is true. <i>Note</i>: Not every LDAP server will through this exception.
     * @throws org.mule.module.ldap.api.LDAPException In case there is any other exception, mainly related to connectivity problems or referrals.
     * @throws Exception In case there is any other error updating the entry.
     */
    @Processor
    @ReconnectOn(exceptions = CommunicationException.class)
    public void addMultiValueAttribute(@FriendlyName("DN") String dn, String attributeName, List<Object> attributeValues, @Default("false") boolean ignoreInvalidAttribute) throws Exception
    {
        if(logger.isDebugEnabled())
        {
            logger.debug("About to add attribute " + attributeName + " with values " + attributeValues + " to entry " + dn);
        }
        
        try
        {
            getConnectionStrategy().getConnection().addAttribute(dn, new LDAPMultiValueEntryAttribute(attributeName, attributeValues));
        }
        catch(InvalidAttributeException iaex)
        {
            if(!ignoreInvalidAttribute)
            {
                throw iaex;
            }
            else
            {
                logger.info("Ignoring attribute addition. " + iaex.getMessage());
            }
        }
    
        if(logger.isInfoEnabled())
        {
            logger.info("Added attribute " + attributeName + " with values " + attributeValues + " to entry " + dn);
        }         
    }

    /**
     * Updates (replaces) the value or values of the attribute defined by <i>attributeName</i> with the new value defined by
     * <i>attributeValue</i>. If the attribute was not present in the entry, then the value is added.
     * <p/>
     * If you want to update a value with a type different than {@link String}, then you can use the update-multi-value-attribute
     * operation and define a one element list with the value.
     * 
     * {@sample.xml ../../../doc/mule-module-ldap.xml.sample ldap:modify-single-value-attribute}
     * 
     * @param dn The DN of the LDAP entry to modify
     * @param attributeName The name of the attribute to update its value.
     * @param attributeValue The new value for the attribute
     * @param ignoreInvalidAttribute If the attribute value to modify is already present, then don't throw {@link org.mule.module.ldap.api.InvalidAttributeException}
     * @throws org.mule.module.ldap.api.NoPermissionException If the current binded user has no permissions to update the entry.
     * @throws org.mule.module.ldap.api.NameNotFoundException If there is no existing entry for the given DN.
     * @throws org.mule.module.ldap.api.InvalidAttributeException If the entry does have the attribute value that should be modified. Ignored if ignoreInvalidAttribute is true. <i>Note</i>: Not every LDAP server will through this exception.
     * @throws org.mule.module.ldap.api.LDAPException In case there is any other exception, mainly related to connectivity problems or referrals.
     * @throws Exception In case there is any other error updating the entry.
     */
    @Processor
    @ReconnectOn(exceptions = CommunicationException.class)
    public void modifySingleValueAttribute(@FriendlyName("DN") String dn, String attributeName, String attributeValue, @Default("false") boolean ignoreInvalidAttribute) throws Exception
    {
        if(logger.isDebugEnabled())
        {
            logger.debug("About to update attribute " + attributeName + " with value " + attributeValue + " to entry " + dn);
        }
        
        try
        {
            getConnectionStrategy().getConnection().updateAttribute(dn, new LDAPSingleValueEntryAttribute(attributeName, attributeValue));
        }
        catch(InvalidAttributeException iaex)
        {
            if(!ignoreInvalidAttribute)
            {
                throw iaex;
            }
            else
            {
                logger.info("Ignoring attribute modification. " + iaex.getMessage());
            }
        }
        
        if(logger.isInfoEnabled())
        {
            logger.info("Updated attribute " + attributeName + " with value " + attributeValue + " to entry " + dn);
        }         
    }
    
    /**
     * Updates (replaces) the value or values of the attribute defined by <i>attributeName</i> with the new values defined by
     * <i>attributeValues</i>.  If the attribute was not present in the entry, then the value is added.
     * 
     * {@sample.xml ../../../doc/mule-module-ldap.xml.sample ldap:modify-multi-value-attribute}
     * 
     * @param dn The DN of the LDAP entry to modify
     * @param attributeName The name of the attribute to update its values.
     * @param attributeValues The new values for the attribute
     * @param ignoreInvalidAttribute If the attribute value to modify is already present, then don't throw {@link org.mule.module.ldap.api.InvalidAttributeException}
     * @throws org.mule.module.ldap.api.NoPermissionException If the current binded user has no permissions to update the entry.
     * @throws org.mule.module.ldap.api.NameNotFoundException If there is no existing entry for the given DN.
     * @throws org.mule.module.ldap.api.InvalidAttributeException If the entry does have the attribute value that should be modified. Ignored if ignoreInvalidAttribute is true. <i>Note</i>: Not every LDAP server will through this exception.
     * @throws org.mule.module.ldap.api.LDAPException In case there is any other exception, mainly related to connectivity problems or referrals.
     * @throws Exception In case there is any other error updating the entry.
     */
    @Processor
    @ReconnectOn(exceptions = CommunicationException.class)
    public void modifyMultiValueAttribute(@FriendlyName("DN") String dn, String attributeName, List<Object> attributeValues, @Default("false") boolean ignoreInvalidAttribute) throws Exception
    {
        if(logger.isDebugEnabled())
        {
            logger.debug("About to modify attribute " + attributeName + " with values " + attributeValues + " to entry " + dn);
        }
        
        try
        {
            getConnectionStrategy().getConnection().updateAttribute(dn, new LDAPMultiValueEntryAttribute(attributeName, attributeValues));
        }
        catch(InvalidAttributeException iaex)
        {
            if(!ignoreInvalidAttribute)
            {
                throw iaex;
            }
            else
            {
                logger.info("Ignoring attribute modification. " + iaex.getMessage());
            }
        }
        
        if(logger.isInfoEnabled())
        {
            logger.info("Modified attribute " + attributeName + " with values " + attributeValues + " to entry " + dn);
        }          
    }
    
    /**
     * Deletes the value matching <i>attributeValue</i> of the attribute defined by <i>attributeName</i>. If the entry didn't have
     * the value, then the entry stays the same. If no value is specified, then the whole attribute is deleted from the entry.
     * <p/>
     * If you want to delete a value with a type different than {@link String}, then you can use the delete-multi-value-attribute
     * operation and define a one element list with the value.
     * 
     * {@sample.xml ../../../doc/mule-module-ldap.xml.sample ldap:delete-single-value-attribute}
     * 
     * @param dn The DN of the LDAP entry to modify
     * @param attributeName The name of the attribute to delete its value.
     * @param attributeValue The value that should be deleted.
     * @param ignoreInvalidAttribute If the attribute or value to delete is no present, then don't throw {@link org.mule.module.ldap.api.InvalidAttributeException}
     * @throws org.mule.module.ldap.api.NoPermissionException If the current binded user has no permissions to update the entry.
     * @throws org.mule.module.ldap.api.NameNotFoundException If there is no existing entry for the given DN.
     * @throws org.mule.module.ldap.api.InvalidAttributeException If the entry doesn't have the attribute or value that should be deleted. Ignored if ignoreInvalidAttribute is true. <i>Note</i>: Not every LDAP server will through this exception.
     * @throws org.mule.module.ldap.api.LDAPException In case there is any other exception, mainly related to connectivity problems or referrals.
     * @throws Exception In case there is any other error updating the entry.
     */
    @Processor
    @ReconnectOn(exceptions = CommunicationException.class)
    public void deleteSingleValueAttribute(@FriendlyName("DN") String dn, String attributeName, @Optional String attributeValue, @Default("false") boolean ignoreInvalidAttribute) throws Exception
    {
        if(logger.isDebugEnabled())
        {
            logger.debug("About to delete value " + attributeValue + " from attribute " + attributeName + " on entry " + dn);
        }
        
        try
        {
            getConnectionStrategy().getConnection().deleteAttribute(dn, new LDAPSingleValueEntryAttribute(attributeName, attributeValue));
        }
        catch(InvalidAttributeException iaex)
        {
            if(!ignoreInvalidAttribute)
            {
                throw iaex;
            }
            else
            {
                logger.info("Ignoring attribute deletion. " + iaex.getMessage());
            }
        }        
        if(logger.isInfoEnabled())
        {
            if(attributeValue != null && attributeValue.length() > 0)
            {
                logger.info("Deleted value " + attributeValue + " from attribute " + attributeName + " from entry " + dn);
            }
            else
            {
                logger.info("Deleted attribute " + attributeName + " from entry " + dn);
            }
        }          
        
    }

    /**
     * Deletes all the values matching <i>attributeValues</i> of the attribute defined by <i>attributeName</i>. Values that are
     * not present in the entry are ignored. If no values are specified, then the whole attribute is deleted from the entry.
     * 
     * {@sample.xml ../../../doc/mule-module-ldap.xml.sample ldap:delete-multi-value-attribute}
     * 
     * @param dn The DN of the LDAP entry to modify
     * @param attributeName The name of the attribute to delete its values.
     * @param attributeValues The values that should be deleted.
     * @param ignoreInvalidAttribute If the attribute or value to delete is no present, then don't throw {@link org.mule.module.ldap.api.InvalidAttributeException}
     * @throws org.mule.module.ldap.api.NoPermissionException If the current binded user has no permissions to update the entry.
     * @throws org.mule.module.ldap.api.NameNotFoundException If there is no existing entry for the given DN.
     * @throws org.mule.module.ldap.api.InvalidAttributeException If the entry doesn't have the attribute or value that should be deleted. Ignored if ignoreInvalidAttribute is true. <i>Note</i>: Not every LDAP server will through this exception.
     * @throws org.mule.module.ldap.api.LDAPException In case there is any other exception, mainly related to connectivity problems or referrals.
     * @throws Exception In case there is any other error updating the entry.
     */
    @Processor
    @ReconnectOn(exceptions = CommunicationException.class)
    public void deleteMultiValueAttribute(@FriendlyName("DN") String dn, String attributeName, @Optional List<Object> attributeValues, @Default("false") boolean ignoreInvalidAttribute) throws Exception
    {
        if(logger.isDebugEnabled())
        {
            logger.debug("About to delete values " + attributeValues + " from attribute " + attributeName + " on entry " + dn);
        }
        
        try
        {
            getConnectionStrategy().getConnection().deleteAttribute(dn, new LDAPMultiValueEntryAttribute(attributeName, attributeValues));
        }
        catch(InvalidAttributeException iaex)
        {
            if(!ignoreInvalidAttribute)
            {
                throw iaex;
            }
            else
            {
                logger.info("Ignoring attribute deletion. " + iaex.getMessage());
            }
        }
        
        if(logger.isInfoEnabled())
        {
            logger.info("Deleted values " + attributeValues + " from attribute " + attributeName + " on entry " + dn);
        }          
    }
    
    // Transformers
    
    /**
     * Creates a {@link LDAPEntry} from its {@link Map} representation. This transformer won't check that the entry is valid. The only validation
     * that is performed is the presence of the distinguished name.
     * <p/>
     * In order to represent a LDAP entry as a map, you should consider the following rules for the map key/value pair:
     * <ul>
     *    <li><b>Distinguished name</b>: The map should have a special attribute for the distinguish name. In this case, the key should be the string
     *                 <code>"dn"</code> (see {@link LDAPEntry#MAP_DN_KEY}) and the value a {@link String} representing the distinguished name
     *                 (for example <code>cn=andy,ou=people,dc=mulesoft,dc=org</code>).</li>
     *    <li><b>Single Value Attributes</b>: The key should be the name of the single value attribute (for example uid, cn, ...) as a
     *                 {@link String} and the value is just the value of the attribute (most of the times represented by a {@link String}.</li>
     *    <li><b>Multi-value Attributes</b>: The key should be the name of the multiple values attribute (for example objectClass, memberOf, mail, ...)
     *                 as a {@link String} and the value should be a {@link List} holding the multiple values of the attribute. Usually it will be
     *                 a list of strings.</li>
     * </ul>
     * 
     * {@sample.xml ../../../doc/mule-module-ldap.xml.sample ldap:map-to-ldap-entry}
     * 
     * @param entry {@link Map} representation of the LDAP entry.
     * @return {@link LDAPEntry} object.
     * @throws Exception If the map entry is invalid (for example, it doesn't contain the DN)
     */
    @Transformer(sourceTypes = {Map.class})
    public static LDAPEntry mapToLdapEntry(Map<String, Object> entry) throws Exception
    {
        return new LDAPEntry(entry);
    }

    /**
     * Creates the {@link Map} representation of an {@link LDAPEntry}.
     * <p/>
     * The resulting map has the name of the attributes as {@link String} keys and the values for these keys are:
     * <ul>
     *    <li><b>Distinguished name</b>: The DN is the value of the special string key <code>"dn"</code> (see {@link LDAPEntry#MAP_DN_KEY}).</li>
     *    <li><b>Single Value Attributes</b>: The value is an Object (most of the cases a {@link String} and in some cases, like userPassword, a <code>byte[]</code>).</li>
     *    <li><b>Multi-value Attributes</b>: The value is a {@link List} containing all the values for the attribute (most of the cases a {@link String} and in some cases, like userPassword, a <code>byte[]</code>).</li>
     * </ul>
     *  
     * {@sample.xml ../../../doc/mule-module-ldap.xml.sample ldap:ldap-entry-to-map}
     *  
     * @param entry The {@link LDAPEntry} to transform to map.
     * @return The {@link Map} representation of the entry.
     * @deprecated LDAPEntry is now a Map<String, Object> so it makes no sense using this transformer.
     */
    @Transformer(sourceTypes = {LDAPEntry.class})
    @Deprecated
    public static Map<String, Object> ldapEntryToMap(LDAPEntry entry)
    {
        // Now that entry is a map, just return the entry.
        // Leaving this for backwards compatibility
        return entry;
    }
    
    /**
     * Transforms a {@link LDAPEntry} to a {@link String} in LDIF representation (RFC 2849).
     * 
     * {@sample.xml ../../../doc/mule-module-ldap.xml.sample ldap:ldap-entry-to-ldif}
     * 
     * @param entry The {@link LDAPEntry} to transform to LDIF.
     * @return The LDIF representation of the entry.
     */
    @Transformer(sourceTypes = {LDAPEntry.class})
    public static String ldapEntryToLdif(LDAPEntry entry)
    {
        return entry != null ? entry.toLDIFString() : null;
    }
    
    // DataSense methods
    
    @MetaDataKeyRetriever
    public List<MetaDataKey> getMetaDataKeys() throws ConnectionException
    {
        List<MetaDataKey> keys = new ArrayList<MetaDataKey>();
        
        
        keys.add(new DefaultMetaDataKey("person", "person"));
        keys.add(new DefaultMetaDataKey("organizationalUnit", "organizationalUnit"));
        keys.add(new DefaultMetaDataKey("person", "person"));
        return keys;
    }

    @MetaDataRetriever
    public MetaData getMetaData(MetaDataKey key) throws ConnectionException
    {
        MetaData metaData = null;
        DynamicObjectBuilder<?> dynamicObject = new DefaultMetaDataBuilder().createDynamicObject(key.getId());
        MetaDataModel model = dynamicObject.build();
        metaData = new DefaultMetaData(model);
        return metaData;
    }

    public LDAPCacheConnection getConnectionStrategy()
    {
        return connectionStrategy;
    }

    public void setConnectionStrategy(LDAPCacheConnection connectionStrategy)
    {
        this.connectionStrategy = connectionStrategy;
    }
}
