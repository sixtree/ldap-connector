/**
 * Copyright (c) MuleSoft, Inc. All rights reserved. http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.md file.
 */

package org.mule.module.ldap.api.jndi;

import javax.naming.NamingEnumeration;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;

import org.mule.module.ldap.api.LDAPResultSet;
import org.mule.module.ldap.api.LDAPSchemaAware;
import org.mule.module.ldap.api.LDAPSearchControls;

public class LDAPResultSetFactory
{

    /**
     * 
     */
    public LDAPResultSetFactory()
    {
    }

    /**
     * 
     * @param baseDn
     * @param filter
     * @param filterArgs
     * @param conn
     * @param controls
     * @param entries
     * @return
     */
    public static LDAPResultSet create(String baseDn, String filter, Object[] filterArgs, LdapContext conn, LDAPSearchControls controls, NamingEnumeration<SearchResult> entries, LDAPSchemaAware connection)
    {
        if(controls.isPagingEnabled())
        {
            return new PagedLDAPResultSet(baseDn, filter, filterArgs, conn, controls, entries, connection);
        }
        else
        {
            return new SimpleLDAPResultSet(baseDn, conn, controls, entries, connection);
        }
    }
    
    public static LDAPResultSet create(String baseDn, NamingEnumeration<SearchResult> entries, LDAPSchemaAware connection)
    {
        return new SimpleLDAPResultSet(baseDn, null, new LDAPSearchControls(), entries, connection);
    }
}


