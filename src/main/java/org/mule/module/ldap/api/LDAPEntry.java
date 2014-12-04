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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * This class is the abstraction
 * 
 * @author mariano
 */
public class LDAPEntry implements Serializable, Map<String, Object>
{
    public static final String MAP_DN_KEY = "dn";
    private static final long serialVersionUID = 194192089581212405L;

    private String dn = null;
    private LDAPEntryAttributes attributes = new LDAPEntryAttributes();

    /**
	 * 
	 */
    public LDAPEntry()
    {
        this((String) null);
    }

    /**
     * @param dn
     */
    public LDAPEntry(String dn)
    {
        super();
        setDn(dn);
    }
    
    /**
     * @param dn
     */
    public LDAPEntry(String dn, Map<String, ?> attributes)
    {
        if(dn == null)
        {
            throw new IllegalArgumentException("Entry DN cannot be null.");
        }
        
        Map<String, Object> entry = new HashMap<String, Object>(attributes);
        entry.put(MAP_DN_KEY, dn);
        try
        {
            fromMap(entry);
        }
        catch(LDAPException ex)
        {
            // Should never happen as he only exception fromMap throws
            // is when the DN is not present in the map
        }
    }
    
    /**
     * 
     * @param entry
     */
    public LDAPEntry(Map<String, Object> entry) throws LDAPException
    {
        fromMap(entry);
    }

    /**
     * @return Returns the dn.
     */
    public String getDn()
    {
        return dn;
    }

    /**
     * @param dn The dn to set.
     */
    public void setDn(String dn)
    {
        this.dn = dn;
    }

    /**
     * @param attribute
     */
    public void addAttribute(LDAPEntryAttribute attribute)
    {
        this.attributes.addAttribute(attribute);
    }

    /**
     * 
     * @param attributeName
     * @param attributeValue
     */
    @SuppressWarnings("unchecked")
    public void addAttribute(String attributeName, Object attributeValue)
    {
        if(attributeValue instanceof Collection)
        {
            addAttribute(new LDAPMultiValueEntryAttribute(attributeName, (Collection<Object>) attributeValue));
        }
        else if(attributeValue instanceof Object[])
        {
            addAttribute(new LDAPMultiValueEntryAttribute(attributeName, (Object[]) attributeValue));
        }
        else
        {
            addAttribute(new LDAPSingleValueEntryAttribute(attributeName, attributeValue));
        }
    }
    
    /**
     * @param name
     * @return
     */
    public LDAPEntryAttribute getAttribute(String name)
    {
        if (name != null)
        {
            return getAttributes().getAttribute(name);
        }
        else
        {
            return null;
        }
    }

    /**
	 * 
	 *
	 */
    public void resetAttributes()
    {
        this.attributes.resetAttributes();
    }

    /**
     * @return
     */
    public int getAttributeCount()
    {
        return this.attributes.getCount();
    }

    /**
     * @return
     */
    public Iterator<LDAPEntryAttribute> attributes()
    {
        return this.attributes.attributes();
    }

    /**
     * @return
     */
    public String toLDIFString()
    {

        StringBuilder entryLdif = new StringBuilder();

        entryLdif.append("dn: " + getDn() + LDAPUtils.NEW_LINE);

        LDAPEntryAttribute anAttr;
        for (Iterator<LDAPEntryAttribute> it = attributes(); it.hasNext();)
        {
            anAttr = it.next();
            entryLdif.append(anAttr.toLDIFString());
        }
        entryLdif.append(LDAPUtils.NEW_LINE);

        return entryLdif.toString();
    }

    private void fromMap(Map<String, ?> entry) throws LDAPException
    {
        Object entryDN = entry.remove(MAP_DN_KEY);
        if(entryDN != null && entryDN instanceof String)
        {
            setDn((String) entryDN);
        }
        else
        {
            throw new LDAPException("The map representing the LDAP entry should contain the key '" + MAP_DN_KEY + "' with the string value of the DN (Distinguished Name) of the entry.");
        }
        
        String attr;
        Object value;
        for(Iterator<String> attrsIt = entry.keySet().iterator(); attrsIt.hasNext(); )
        {
            attr = attrsIt.next();
            value = entry.get(attr);
            addAttribute(attr, value);
        }
    }
    
    /**
     * 
     * @return
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return toLDIFString();
    }
    
    /**
     * @return Returns the attributes.
     */
    public LDAPEntryAttributes getAttributes()
    {
        return attributes;
    }

    /**
     * @param attributes The attributes to set.
     */
    public void setAttributes(LDAPEntryAttributes attributes)
    {
        this.attributes = attributes;
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj == this)
        {
            return true;
        }
        
        if(obj == null || !(obj instanceof LDAPEntry))
        {
            return false;
        }
        LDAPEntry entry = (LDAPEntry) obj;
        
        return getDn() != null && getDn().equals(entry.getDn());
        
    }

    @Override
    public int hashCode()
    {
        return getDn() != null ? getDn().hashCode() : super.hashCode();
    }

    /*
     * Map implementation methods! 
     */
    
    @Override
    public void clear()
    {
        getAttributes().resetAttributes();
        setDn(null);
    }

    private String getKeyAsString(Object key)
    {
        if(key == null || key instanceof String)
        {
            return (String) key;
        }
        else
        {
            throw new IllegalArgumentException("Key should be a string. " + key.getClass() + " is not supported");
        }
    }
    
    @Override
    public boolean containsKey(Object key)
    {
        if(MAP_DN_KEY.equals(key)) {
            return getDn() != null;
        } else {
            return get(key) != null;
        }
    }

    @Override
    public boolean containsValue(Object value)
    {
        return false;
    }

    @Override
    public Set<java.util.Map.Entry<String, Object>> entrySet()
    {
        return null;
    }

    @Override
    public Object get(Object key)
    {
        LDAPEntryAttribute attr = getAttribute(getKeyAsString(key));
        return attr.isMultiValued() ? attr.getValues() : attr.getValue();
    }

    @Override
    public boolean isEmpty()
    {
        return getDn() == null && getAttributeCount() <= 0;
    }

    @Override
    public Set<String> keySet()
    {
        // Just in case DN + attributes
        Set<String> keys = new HashSet<String>(attributes.getCount() + 1);
        
        if(getDn() != null)
        {
            keys.add(MAP_DN_KEY);
        }
        
        LDAPEntryAttribute anAttr;
        for (Iterator<LDAPEntryAttribute> it = attributes(); it.hasNext();)
        {
            anAttr = it.next();
            keys.add(anAttr.getName());
        } 
        
        return keys;
    }

    @Override
    public Object put(String key, Object value)
    {
        if(MAP_DN_KEY.equals(key))
        {
            if(value == null || value instanceof String)
            {
                setDn((String) value);
            }
            else
            {
                throw new IllegalArgumentException("DN should be a string. " + key.getClass() + " is not supported");
            }
        }
        else 
        {
            addAttribute(key, value);
        }
        return value;
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> map)
    {
        for(java.util.Map.Entry<? extends String, ? extends Object> entry : map.entrySet())
        {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public Object remove(Object key)
    {
        if(MAP_DN_KEY.equals(key)) {
            String dn = getDn();
            setDn(null);
            return dn;
        } else {
            return this.attributes.removeAttribute(getKeyAsString(key));
        }
    }

    @Override
    public int size()
    {
        return getDn() != null ? 1 : 0 + getAttributeCount();
    }

    @Override
    public Collection<Object> values()
    {
        // Just in case DN + attributes
        Collection<Object> values = new ArrayList<Object>(attributes.getCount() + 1);
        
        if(getDn() != null)
        {
            values.add(getDn());
        }
        
        LDAPEntryAttribute anAttr;
        for (Iterator<LDAPEntryAttribute> it = attributes(); it.hasNext();)
        {
            anAttr = it.next();
            if(!anAttr.isMultiValued())
            {
                values.add(anAttr.getValue());
            }
            else
            {
                values.add(anAttr.getValues());
            }
        }        
        
        return values;
    }
}
