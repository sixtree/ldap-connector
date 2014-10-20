/**
 * Copyright (c) MuleSoft, Inc. All rights reserved. http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.md file.
 */
package org.mule.module.ldap.api;

import java.io.Serializable;

public class LDAPEntryAttributeTypeDefinition implements Serializable
{
    private static final long serialVersionUID = -7441401978161427169L;
    
    private String numericOid = null;
    private String name = null;
    private String syntax = null;
    private String description = null;
    private boolean singleValue = false;
    private boolean obsolete = false;
    private String supName = null;
    private String equality = null;
    private String ordering = null;
    private String substring = null;
    private boolean collective = false;
    private boolean noUserModification = false;
    private String usage = null;

    /**
     * 
     */
    public LDAPEntryAttributeTypeDefinition()
    {
    }

    public String getNumericOid()
    {
        return numericOid;
    }

    public void setNumericOid(String numericOid)
    {
        this.numericOid = numericOid;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getSyntax()
    {
        return syntax;
    }

    public void setSyntax(String syntax)
    {
        this.syntax = syntax;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public boolean isSingleValue()
    {
        return singleValue;
    }

    public void setSingleValue(boolean singleValue)
    {
        this.singleValue = singleValue;
    }

    public boolean isObsolete()
    {
        return obsolete;
    }

    public void setObsolete(boolean obsolete)
    {
        this.obsolete = obsolete;
    }

    public String getSupName()
    {
        return supName;
    }

    public void setSupName(String supName)
    {
        this.supName = supName;
    }

    public String getEquality()
    {
        return equality;
    }

    public void setEquality(String equality)
    {
        this.equality = equality;
    }

    public String getOrdering()
    {
        return ordering;
    }

    public void setOrdering(String ordering)
    {
        this.ordering = ordering;
    }

    public String getSubstring()
    {
        return substring;
    }

    public void setSubstring(String substring)
    {
        this.substring = substring;
    }

    public boolean isCollective()
    {
        return collective;
    }

    public void setCollective(boolean collective)
    {
        this.collective = collective;
    }

    public boolean isNoUserModification()
    {
        return noUserModification;
    }

    public void setNoUserModification(boolean noUserModification)
    {
        this.noUserModification = noUserModification;
    }

    public String getUsage()
    {
        return usage;
    }

    public void setUsage(String usage)
    {
        this.usage = usage;
    }

}


