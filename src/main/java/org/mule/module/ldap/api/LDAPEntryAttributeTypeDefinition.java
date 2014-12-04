/**
 * Copyright (c) MuleSoft, Inc. All rights reserved. http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.md file.
 */
package org.mule.module.ldap.api;


public class LDAPEntryAttributeTypeDefinition extends AbstractLDAPEntryDefinition
{
    private static final long serialVersionUID = -7441401978161427169L;
    
    public static final String EQUALITY = "EQUALITY";
    public static final String ORDERING = "ORDERING";
    public static final String SUBSTRING = "SUBSTRING";
    public static final String SYNTAX = "SYNTAX";
    public static final String SINGLE_VALUE = "SINGLE-VALUE";
    public static final String COLLECTIVE = "COLLECTIVE";
    public static final String NO_USER_MODIFICATION = "NO-USER-MODIFICATION";
    public static final String USAGE = "USAGE";
    
    /**
     * 
     */
    public LDAPEntryAttributeTypeDefinition()
    {
        super();
    }



    public String getSyntax()
    {
        return getAttributeAsString(SYNTAX);
    }

    public void setSyntax(String syntax)
    {
        this.attributes.put(SYNTAX, syntax);
    }

    public boolean isSingleValue()
    {
        return getAttributeAsBoolean(SINGLE_VALUE);
    }

    public void setSingleValue(boolean singleValue)
    {
        this.attributes.put(SINGLE_VALUE, String.valueOf(singleValue));
    }

    public String getEquality()
    {
        return getAttributeAsString(EQUALITY);
    }

    public void setEquality(String equality)
    {
        this.attributes.put(EQUALITY, equality);
    }

    public String getOrdering()
    {
        return getAttributeAsString(ORDERING);
    }

    public void setOrdering(String ordering)
    {
        this.attributes.put(ORDERING, ordering);
    }

    public String getSubstring()
    {
        return getAttributeAsString(SUBSTRING);
    }

    public void setSubstring(String substring)
    {
        this.attributes.put(SUBSTRING, substring);
    }

    public boolean isCollective()
    {
        return getAttributeAsBoolean(COLLECTIVE);
    }

    public void setCollective(boolean collective)
    {
        this.attributes.put(COLLECTIVE, String.valueOf(collective));
    }

    public boolean isNoUserModification()
    {
        return getAttributeAsBoolean(NO_USER_MODIFICATION);
    }

    public void setNoUserModification(boolean noUserModification)
    {
        this.attributes.put(NO_USER_MODIFICATION, String.valueOf(noUserModification));
    }

    public String getUsage()
    {
        return getAttributeAsString(USAGE);
    }

    public void setUsage(String usage)
    {
        this.attributes.put(USAGE, usage);
    }
}


