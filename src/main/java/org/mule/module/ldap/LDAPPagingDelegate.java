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

import org.mule.api.MuleException;
import org.mule.module.ldap.api.LDAPEntry;
import org.mule.module.ldap.api.LDAPException;
import org.mule.module.ldap.api.LDAPResultSet;
import org.mule.streaming.PagingConfiguration;
import org.mule.streaming.ProviderAwarePagingDelegate;

public class LDAPPagingDelegate extends ProviderAwarePagingDelegate<LDAPEntry, LDAPConnector>
{
    private LDAPResultSet rs = null;
    private PagingConfiguration pagingConfiguration;
    
    /**
     * 
     */
    public LDAPPagingDelegate(LDAPResultSet rs, PagingConfiguration pagingConfiguration)
    {
        this.rs = rs;
        this.pagingConfiguration = pagingConfiguration;
    }

    @Override
    public void close() throws MuleException
    {
        if(this.rs != null) 
        {
            try
            {
                this.rs.close();
            }
            catch(LDAPException ex)
            {
                throw new RuntimeException(ex);
            }
        }
    }

	@Override
	public List<LDAPEntry> getPage(LDAPConnector ldapConnector) throws Exception {
        try
        {
            // No more elements!
            if(!this.rs.hasNext())
            {
                return null;
            }
            
            List<LDAPEntry> page = new ArrayList<LDAPEntry>(this.pagingConfiguration.getFetchSize());
            int count = 0;
            
            while(this.rs.hasNext() && count < this.pagingConfiguration.getFetchSize())
            {
                page.add(this.rs.next());
                count++;
            }
            
            return page;
        }
        catch(LDAPException ex)
        {
            throw new RuntimeException(ex);
        }
	}

	@Override
	public int getTotalResults(LDAPConnector ldapConnector) throws Exception {
		return this.rs.getResultSize();
	}
}