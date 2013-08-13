/**
 * Copyright (c) MuleSoft, Inc. All rights reserved. http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.md file.
 */

package org.mule.module.ldap.api;

public class LDAPSortKey
{
    /**
     * The non-null Attribute ID of the sort key.
     */
    private String attributeName;
    /**
     * Determines the sort order: true if the sort order is ascending, false if descending.
     */
    private boolean ascending;
    /**
     * The possibly null matching rule ID. If null then the ordering matching rule defined for the sort key attribute is used.
     */
    private String matchingRuleID;
    
    /**
     * 
     */
    public LDAPSortKey()
    {
        this(null, false, null);
    }

    /**
     * 
     * @param attributeName
     * @param ascending
     * @param matchingRuleID
     */
    public LDAPSortKey(String attributeName, boolean ascending, String matchingRuleID)
    {
        super();
        setAttributeName(attributeName);
        setAscending(ascending);
        setMatchingRuleID(matchingRuleID);
    }

    public String getAttributeName()
    {
        return attributeName;
    }

    public void setAttributeName(String attributeName)
    {
        this.attributeName = attributeName;
    }

    public boolean isAscending()
    {
        return ascending;
    }

    public void setAscending(boolean ascending)
    {
        this.ascending = ascending;
    }

    public String getMatchingRuleID()
    {
        return matchingRuleID;
    }

    public void setMatchingRuleID(String matchingRuleID)
    {
        this.matchingRuleID = matchingRuleID;
    }

}


