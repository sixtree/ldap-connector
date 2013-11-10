/**
 * Copyright (c) MuleSoft, Inc. All rights reserved. http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.md file.
 */

package org.mule.module.ldap.api.jndi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.InvalidNameException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.ldap.Control;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.SortControl;
import javax.naming.ldap.SortKey;

import org.mule.module.ldap.api.LDAPEntry;
import org.mule.module.ldap.api.LDAPEntryAttribute;
import org.mule.module.ldap.api.LDAPEntryAttributeTypeDefinition;
import org.mule.module.ldap.api.LDAPException;
import org.mule.module.ldap.api.LDAPMultiValueEntryAttribute;
import org.mule.module.ldap.api.LDAPSearchControls;
import org.mule.module.ldap.api.LDAPSingleValueEntryAttribute;
import org.mule.module.ldap.api.LDAPSortKey;

public class LDAPJNDIUtils
{
    
    /**
     * 
     */
    public LDAPJNDIUtils()
    {
    }

    /**
     * @param entryDN
     * @param attributes
     * @return
     * @throws LDAPException
     */
    public static LDAPEntry buildEntry(String entryDN, Attributes attributes) throws LDAPException
    {
        return buildEntry(entryDN, attributes, false);
    }
    
    /**
     * @param entryDN
     * @param attributes
     * @param retrieveAttributeTypeDefinition
     * @return
     * @throws LDAPException
     */
    public static LDAPEntry buildEntry(String entryDN, Attributes attributes, boolean retrieveAttributeTypeDefinition) throws LDAPException
    {
        LDAPEntry anEntry = new LDAPEntry(entryDN);
        if (attributes != null)
        {
            try
            {
                for (NamingEnumeration<?> attrs = attributes.getAll(); attrs.hasMore();)
                {
                    anEntry.addAttribute(buildAttribute((Attribute) attrs.nextElement(), retrieveAttributeTypeDefinition));
                }
            }
            catch (NamingException nex)
            {
                throw LDAPException.create(nex);
            }
        }
        return anEntry;
    }    
    
    /**
     * @param attribute
     * @return
     * @throws LDAPException
     */
    protected static LDAPEntryAttribute buildAttribute(Attribute attribute, boolean retrieveAttributeTypeDefinition) throws LDAPException
    {
        if (attribute != null)
        {
            LDAPEntryAttributeTypeDefinition typeDefinition = retrieveAttributeTypeDefinition ? buildAttributeTypeDefinition(attribute) : null;
            
            try
            {
                if (attribute.size() > 1)
                {
                    LDAPMultiValueEntryAttribute newAttribute = new LDAPMultiValueEntryAttribute();
                    newAttribute.setName(attribute.getID());
                    NamingEnumeration<?> values = attribute.getAll();
                    while (values.hasMore())
                    {
                        newAttribute.addValue(values.next());
                    }
                    newAttribute.setTypeDefinition(typeDefinition);
                    return newAttribute;
                }
                else
                {
                    LDAPSingleValueEntryAttribute newAttribute = new LDAPSingleValueEntryAttribute();
                    newAttribute.setName(attribute.getID());
                    newAttribute.setValue(attribute.get());
                    newAttribute.setTypeDefinition(typeDefinition);
                    return newAttribute;
                }
            }
            catch (NamingException nex)
            {
                throw LDAPException.create(nex);
            }
        }
        else
        {
            return null;
        }
    }   
    
    protected static LDAPEntryAttributeTypeDefinition buildAttributeTypeDefinition(Attribute attribute) throws LDAPException
    {
        if(attribute != null)
        {
            try
            {
                DirContext attrSchema = attribute.getAttributeDefinition();
                
                LDAPEntryAttributeTypeDefinition typeDefinition = new LDAPEntryAttributeTypeDefinition();
                
                return typeDefinition;
            }
            catch (NamingException nex)
            {
                throw LDAPException.create(nex);
            }
        }
        else
        {
            return null;
        }
    }
    
    /**
     * 
     * @param controls
     * @param cookie
     * @return
     * @throws LDAPException
     */
    public static Control[] buildRequestControls(LDAPSearchControls controls, byte[] cookie) throws LDAPException
    {
        List<Control> requestControls = new ArrayList<Control>();
        try
        {
            if(controls.isPagingEnabled())
            {
                if(cookie != null)
                {
                    requestControls.add(new PagedResultsControl(controls.getPageSize(), cookie, Control.CRITICAL));
                }
                else
                {
                    requestControls.add(new PagedResultsControl(controls.getPageSize(), Control.CRITICAL));
                }
            }
            
            if(controls.isSortEnabled())
            {
                requestControls.add(new SortControl(buildSortKeyArray(controls.getSortKeys()), Control.CRITICAL));
            }

            return requestControls.toArray(new Control[0]);
        }
        catch(IOException ex)
        {
            throw new LDAPException("Could not create request paging and/or sort controls", ex);
        }
    }    
    
    private static SortKey[] buildSortKeyArray(List<LDAPSortKey> sortKeys)
    {
        SortKey keys[] = new SortKey[sortKeys.size()];
        int i = 0;
        
        for(LDAPSortKey key : sortKeys)
        {
            keys[i++] = new SortKey(key.getAttributeName(), key.isAscending(), key.getMatchingRuleID()); 
        }
        
        return keys;
    }
    
    /**
     * @param controls
     * @return
     */
    public static SearchControls buildSearchControls(LDAPSearchControls controls)
    {
        SearchControls ctrls = new SearchControls();
        ctrls.setCountLimit(controls.getMaxResults());
        ctrls.setReturningAttributes(controls.getAttributesToReturn());
        ctrls.setReturningObjFlag(controls.isReturnObject());
        ctrls.setSearchScope(transformScope(controls.getScope()));
        ctrls.setTimeLimit(controls.getTimeout());
        return ctrls;
    }    
    
    
    /**
     * @param scope
     * @return
     */
    private static int transformScope(int scope)
    {
        switch (scope)
        {
            case LDAPSearchControls.OBJECT_SCOPE :
                return SearchControls.OBJECT_SCOPE;
            case LDAPSearchControls.ONELEVEL_SCOPE :
                return SearchControls.ONELEVEL_SCOPE;
            case LDAPSearchControls.SUBTREE_SCOPE :
                return SearchControls.SUBTREE_SCOPE;
            default :
                return SearchControls.ONELEVEL_SCOPE;
        }
    } 
    
    /**
     * Whether the list of values contains a given DN. You can use this
     * method to evaluate if a multi value attribute that holds DNs contains
     * a given DN. 
     * @param dn
     * @param values
     * @return
     */
    public static boolean containsDnValue(String dn, List<Object> values)
    {
        LdapName normalizedDn = toLdapName(dn);
        if(normalizedDn != null && values != null && values.size() > 0)
        {
            for(Object value : values)
            {
                if(value instanceof String)
                {
                    if(normalizedDn.equals(toLdapName((String) value)))
                    {
                        return true;
                    }
                }
            }
            return false;
        }
        else
        {
            return false;
        }
    }
    
    private static LdapName toLdapName(String dn) {
        try
        {
            return new LdapName(dn);
        }
        catch (InvalidNameException e)
        {
            return null;
        }         
    }
}


