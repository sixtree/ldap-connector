/**
 * Copyright (c) MuleSoft, Inc. All rights reserved. http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.md file.
 */

package org.mule.module.ldap;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.mule.module.ldap.api.LDAPEntry;

public class LDAPMapAddTest extends AbstractLDAPConnectorEmbeddedLDAPTest
{

    /**
     * 
     */
    public LDAPMapAddTest()
    {
    }

    @Override
    protected String getConfigResources()
    {
        return "add-map-mule-config.xml";
    }
    
    @Test
    public void testAddNewValidEntry() throws Exception
    {
        Map<String, Object> entryToAdd = new HashMap<String, Object>();
        entryToAdd.put("dn", "uid=testuser,ou=people,dc=mulesoft,dc=org");
        entryToAdd.put("uid", "testuser");
        entryToAdd.put("cn", "Test User");
        entryToAdd.put("sn", "User");
        entryToAdd.put("userPassword", "test1234");
        entryToAdd.put("objectClass", new String[] {"top", "person", "organizationalPerson", "inetOrgPerson"});
        
        LDAPEntry result = (LDAPEntry) runFlow("testAddEntryFlow", entryToAdd);
        
        assertEquals(entryToAdd.get("uid"), result.getAttribute("uid").getValue());
        assertEquals(entryToAdd.get("cn"), result.getAttribute("cn").getValue());
        assertEquals(entryToAdd.get("sn"), result.getAttribute("sn").getValue());
    }
}


