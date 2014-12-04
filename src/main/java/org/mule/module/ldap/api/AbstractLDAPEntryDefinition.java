/**
 * Copyright (c) MuleSoft, Inc. All rights reserved. http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.md file.
 */
package org.mule.module.ldap.api;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractLDAPEntryDefinition implements Serializable
{
    private static final long serialVersionUID = -7441401978161427169L;
    
    public static final String NUMERICOID = "NUMERICOID";
    public static final String NAME = "NAME";
    public static final String DESC = "DESC";
    public static final String OBSOLETE = "OBSOLETE";
    public static final String SUP = "SUP";
    
    protected Map<String, Object> attributes = new HashMap<String, Object>();
    
    /**
     * 
     */
    public AbstractLDAPEntryDefinition()
    {
        super();
    }

    protected String getAttributeAsString(String attrName)
    {
        Object attr = this.attributes.get(attrName);
        
        if (attr != null)
        {
            return attr instanceof String ? (String) attr : String.valueOf(attr);
        }
        return null;
    }
    
    protected boolean getAttributeAsBoolean(String attrName)
    {
        String attr = getAttributeAsString(attrName);
        
        return attr != null && "true".equalsIgnoreCase(attr);
    }
    
    /**
     * 
     * @param attributeName
     * @param attributeValue
     */
    public void set(String attributeName, Object attributeValue)
    {
        this.attributes.put(attributeName.toUpperCase(), attributeValue);
    }
    
    public Object get(String attributeName)
    {
        return this.attributes.get(attributeName.toUpperCase());
    }
    
    public String getNumericOid()
    {
        return getAttributeAsString(NUMERICOID);
    }

    public void setNumericOid(String numericOid)
    {
        this.attributes.put(NUMERICOID, numericOid);
    }

    public String getName()
    {
        return getAttributeAsString(NAME);
    }

    public void setName(String name)
    {
        this.attributes.put(NAME, name);
    }
    
    public String getDescription()
    {
        return getAttributeAsString(DESC);
    }

    public void setDescription(String description)
    {
        this.attributes.put(DESC, description);
    }
    
    public boolean isObsolete()
    {
        return getAttributeAsBoolean(OBSOLETE);
    }

    public void setObsolete(boolean obsolete)
    {
        this.attributes.put(OBSOLETE, String.valueOf(obsolete));
    }

    public String getSupName()
    {
        return getAttributeAsString(SUP);
    }

    public void setSupName(String supName)
    {
        this.attributes.put(SUP, supName);
    }
    
    @Override
    public int hashCode()
    {
        return getNumericOid().hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        else if (obj == this)
        {
            return true;
        }
        else if (!getClass().equals(obj.getClass()))
        {
            return false;
        }
        else if (obj instanceof AbstractLDAPEntryDefinition)
        {
            return getNumericOid().equals(((AbstractLDAPEntryDefinition) obj).getNumericOid());
        }
        else
        {
            return false;
        }
    }

    @Override
    public String toString()
    {
        return getName() != null ? getName() : getNumericOid();
    }    
}


