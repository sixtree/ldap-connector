/**
 * Copyright (c) MuleSoft, Inc. All rights reserved. http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.md file.
 */
package org.mule.module.ldap.api;

import java.util.List;

public class LDAPEntryObjectClassDefinition extends AbstractLDAPEntryDefinition
{

    private static final long serialVersionUID = 3350557026278577962L;

    public static final String ABSTRACT = "ABSTRACT";
    public static final String STRUCTURAL = "STRUCTURAL";
    public static final String AUXILIARY = "AUXILIARY";
    public static final String MUST = "MUST";
    public static final String MAY = "MAY";
    
    /**
     * 
     */
    public LDAPEntryObjectClassDefinition()
    {
        super();
    }

    public boolean isAbstract()
    {
        return getAttributeAsBoolean(ABSTRACT);
    }

    public void setAbstract(boolean isAbstract)
    {
        this.attributes.put(ABSTRACT, String.valueOf(isAbstract));
    }    

    public boolean isStructural()
    {
        return getAttributeAsBoolean(STRUCTURAL);
    }

    public void setStructural(boolean isStructural)
    {
        this.attributes.put(STRUCTURAL, String.valueOf(isStructural));
    }    
    
    public boolean isAuxiliary()
    {
        return getAttributeAsBoolean(AUXILIARY);
    }

    public void setAuxiliary(boolean isAuxiliary)
    {
        this.attributes.put(AUXILIARY, String.valueOf(isAuxiliary));
    }   

    @SuppressWarnings("unchecked")
    public List<String> getMust()
    {
        return (List<String>) this.attributes.get(MUST);
    }
    
    public void setMust(List<String> must)
    {
        this.attributes.put(MUST, must);
    }
  
    @SuppressWarnings("unchecked")
    public List<String> getMay()
    {
        return (List<String>) this.attributes.get(MAY);
    }
    
    public void setMay(List<String> may)
    {
        this.attributes.put(MAY, may);
    }    
}


