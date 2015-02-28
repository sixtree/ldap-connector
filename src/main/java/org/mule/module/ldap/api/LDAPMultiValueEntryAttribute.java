/**
 * Copyright (c) MuleSoft, Inc. All rights reserved. http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.md file.
 */

package org.mule.module.ldap.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class is the abstraction
 * 
 * @author mariano
 */
public class LDAPMultiValueEntryAttribute extends LDAPEntryAttribute
{
    /**
	 * 
	 */
    private static final long serialVersionUID = 2022378719197583814L;

    private List<Object> values = null;

    /**
	 * 
	 */
    public LDAPMultiValueEntryAttribute()
    {
        super();
    }

    /**
     * @param name
     * @param values
     */
    public LDAPMultiValueEntryAttribute(String name, Object values[])
    {
        this(name);
        addValues(values);
    }

    /**
     * @param name
     * @param values
     */
    public LDAPMultiValueEntryAttribute(String name, Collection<Object> values)
    {
        this(name);
        addValues(values != null ? values.toArray() : null);
    }

    /**
     * @param name
     */
    public LDAPMultiValueEntryAttribute(String name)
    {
        super(name);
    }

    /**
     * @return
     * @see leonards.common.ldap.LDAPEntryAttribute#getValue()
     */
    public Object getValue()
    {
        return !values.isEmpty() ? values.get(0) : null;
    }

    /**
     * @return
     * @see leonards.common.ldap.LDAPEntryAttribute#getValues()
     */
    public List<Object> getValues()
    {
        if (values == null)
        {
            values = new ArrayList<Object>();
        }
        return values;
    }

    /**
     * @return
     * @see leonards.common.ldap.LDAPEntryAttribute#isMultiValued()
     */
    public boolean isMultiValued()
    {
        return true;
    }

    /**
     * @param value
     */
    public void addValue(Object value)
    {
        getValues().add(value);
    }

    /**
     * @param values
     */
    public void addValues(Object values[])
    {
        if (values != null)
        {
            for (int i = 0; i < values.length; i++)
            {
                getValues().add(values[i]);
            }
        }

    }
}
