/**
 * Copyright (c) MuleSoft, Inc. All rights reserved. http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.md file.
 */

package org.mule.module.ldap.api.jndi;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.SizeLimitExceededException;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.module.ldap.api.LDAPConnection;
import org.mule.module.ldap.api.LDAPEntry;
import org.mule.module.ldap.api.LDAPException;
import org.mule.module.ldap.api.LDAPResultSet;
import org.mule.module.ldap.api.LDAPSchemaAware;
import org.mule.module.ldap.api.LDAPSearchControls;

public class SimpleLDAPResultSet implements LDAPResultSet
{
    protected final Log logger = LogFactory.getLog(getClass());

    private NamingEnumeration<SearchResult> entries = null;
    private String baseDn = null;
    private LDAPSearchControls controls = null;
    private LDAPConnection schemaCache = null;
    
    /**
     * 
     */
    public SimpleLDAPResultSet(String baseDn, LdapContext conn, LDAPSearchControls controls, NamingEnumeration<SearchResult> entries, LDAPSchemaAware schemaCache)
    {
        this.entries = entries;
        this.baseDn = baseDn;
        this.controls = controls;
        this.schemaCache = null;
    }

    /**
     * 
     * @return
     * @see org.mule.module.ldap.api.LDAPResultSet#hasNext()
     */
    @Override
    public boolean hasNext() throws LDAPException
    {
        try
        {
            return this.entries != null ? this.entries.hasMore() : false;
        }
        catch(SizeLimitExceededException slee)
        {
            logger.warn("Size limit exceeded. Max results is: " + this.controls.getMaxResults(), slee);
            return false;
        }
        catch(NamingException nex)
        {
            throw LDAPException.create(nex);
        }
    }

    /**
     * 
     * @return
     * @throws LDAPException
     * @see org.mule.module.ldap.api.LDAPResultSet#next()
     */
    @Override
    public LDAPEntry next() throws LDAPException
    {
        SearchResult searchResult = (SearchResult) this.entries.nextElement();
        String entryDn;
        if (searchResult != null)
        {
            entryDn = searchResult.getName();
            if (searchResult.isRelative())
            {
                entryDn += "," + baseDn;
            }
            return LDAPJNDIUtils.buildEntry(entryDn, searchResult.getAttributes(), schemaCache);
        }
        else
        {
            throw new NoSuchElementException("End of result set");
        }
    }


    /**
     * @throws LDAPException
     * @see org.mule.module.ldap.api.LDAPResultSet#close()
     */
    @Override
    public void close() throws LDAPException
    {
        try
        {
            if(this.entries != null)
            {
                this.entries.close();
            }
        }
        catch(NamingException nex)
        {
            throw LDAPException.create(nex);
        }
        finally
        {
            this.entries = null;
            this.schemaCache = null; // Only used in order to retrieve schema cache
        }
    }

    @Override
    public List<LDAPEntry> getAllEntries() throws LDAPException
    {
        List<LDAPEntry> allEntries = new ArrayList<LDAPEntry>();
        
        while(hasNext())
        {
            LDAPEntry entry = next();
            if(entry != null)
            {
                allEntries.add(entry);
            }
        }
        
        return allEntries;
    }

}


