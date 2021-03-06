/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.ldap.api;

import java.util.List;

public interface LDAPSchemaAware
{
    /**
     * 
     * @return true if the schema is enabled and should be used
     */
    boolean isSchemaEnabled();
    
    /**
     * 
     * @param attributeName
     * @return
     * @throws LDAPException
     */
    LDAPEntryAttributeTypeDefinition getAttributeTypeDefinition(String attributeName) throws LDAPException;
    
    /**
     * 
     * @param objectClassName
     * @return
     * @throws LDAPException
     */
    LDAPEntryObjectClassDefinition getObjectClassDefinition(String objectClassName) throws LDAPException;

    /**
     * 
     * @return
     * @throws LDAPException
     */
    List<String> getAllObjectClasses() throws LDAPException;
}


