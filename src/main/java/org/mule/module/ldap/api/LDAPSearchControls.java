/**
 * Copyright (c) MuleSoft, Inc. All rights reserved. http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.md file.
 */

package org.mule.module.ldap.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is the abstraction
 * 
 * @author mariano
 */
public class LDAPSearchControls implements Serializable
{
    /**
	 * 
	 */
    private static final long serialVersionUID = -5307515256650204659L;

    public final static int OBJECT_SCOPE = 0;
    public final static int ONELEVEL_SCOPE = 1;
    public final static int SUBTREE_SCOPE = 2;

    private int scope = ONELEVEL_SCOPE;

    /**
     * Timeout in millisecons. (0 is no limit)
     */
    private int timeout = 0;

    /**
     * Maximun amount of entries to return (0 is no limit)
     */
    private long maxResults = 0;

    /**
     * Attributes to be returned for each entry. (null means all)
     */
    private String[] attributesToReturn = null;

    /**
	  * 
	  */
    private boolean returnObject = false;
    
    /**
     * Page size to retrieve results from LDAP
     */
    private int pageSize = 0;

    /**
     * Sort keys (Determines the attributes to sort by (ascending or not)
     */
    private List<LDAPSortKey> sortKeys = new ArrayList<LDAPSortKey>();
    
    /**
	 * 
	 */
    public LDAPSearchControls()
    {
        super();
    }

    /**
     * @return Returns the attributesToReturn.
     */
    public String[] getAttributesToReturn()
    {
        return attributesToReturn;
    }

    /**
     * @param attributesToReturn The attributesToReturn to set.
     */
    public void setAttributesToReturn(String[] attributesToReturn)
    {
        this.attributesToReturn = attributesToReturn;
    }

    /**
     * @return Returns the maxResults.
     */
    public long getMaxResults()
    {
        return maxResults;
    }

    /**
     * @param maxResults The maxResults to set.
     */
    public void setMaxResults(long maxResults)
    {
        this.maxResults = maxResults;
    }

    /**
     * @return Returns the scope.
     */
    public int getScope()
    {
        return scope;
    }

    /**
     * @param scope The scope to set.
     */
    public void setScope(int scope)
    {
        this.scope = scope;
    }

    /**
     * @return Returns the timeout.
     */
    public int getTimeout()
    {
        return timeout;
    }

    /**
     * @param timeout The timeout to set.
     */
    public void setTimeout(int timeout)
    {
        this.timeout = timeout;
    }

    /**
     * @return Returns the returnObject.
     */
    public boolean isReturnObject()
    {
        return returnObject;
    }

    /**
     * @param returnObject The returnObject to set.
     */
    public void setReturnObject(boolean returnObject)
    {
        this.returnObject = returnObject;
    }

    /**
     * 
     * @return
     */
    public int getPageSize()
    {
        return pageSize;
    }

    /**
     * 
     * @param pageSize
     */
    public void setPageSize(int pageSize)
    {
        this.pageSize = pageSize;
    }

    /**
     * 
     * @return
     */
    public boolean isPagingEnabled()
    {
        return getPageSize() > 0;
    }

    /**
     * 
     * @return
     */
    public boolean isSortEnabled()
    {
        return getSortKeys() != null && getSortKeys().size() > 0;
    }

    /**
     * 
     * @return
     */
    public List<LDAPSortKey> getSortKeys()
    {
        return sortKeys;
    }
}
