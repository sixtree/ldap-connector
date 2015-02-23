/**
 * Copyright (c) MuleSoft, Inc. All rights reserved. http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.md file.
 */

package org.mule.module.ldap.functional.openldap;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mule.common.metadata.ConnectorMetaDataEnabled;
import org.mule.common.metadata.DefaultMetaDataKey;
import org.mule.common.metadata.MetaData;
import org.mule.common.metadata.MetaDataKey;
import org.mule.module.ldap.AbstractLDAPConnectorEmbeddedLDAPTest;

public class OpenLDAPDataSenseTest extends AbstractLDAPConnectorEmbeddedLDAPTest
{

    /**
     * 
     */
    public OpenLDAPDataSenseTest()
    {
    }

    /**
     * @return
     * @see org.mule.tck.junit4.FunctionalTestCase#getConfigResources()
     */
    @Override
    protected String getConfigResources()
    {
        return "functional/openldap/openldap-config.xml";
    }
        
    @Test
    public void testRetrieveObjectClasses()
    {
    	ConnectorMetaDataEnabled connector = (ConnectorMetaDataEnabled) muleContext.getRegistry().get("adminConf");
        
    	List<MetaDataKey> keys = connector.getMetaDataKeys().get();
    	List<String> objectClasses = new ArrayList<String>(keys.size());
    	
    	for (MetaDataKey key : keys)
    	{
    		objectClasses.add(key.getId().toLowerCase());
    	}
    	
        assertTrue("person", objectClasses.contains("person"));
        assertTrue("inetOrgPerson", objectClasses.contains("inetorgperson"));
        
        assertTrue("organizationalUnit", objectClasses.contains("organizationalunit"));
    }
    
    @Test
    public void testRetrieveObjectClassesDefinition()
    {
    	ConnectorMetaDataEnabled connector = (ConnectorMetaDataEnabled) muleContext.getRegistry().get("adminConf");
        
    	MetaData metaData = connector.getMetaData(new DefaultMetaDataKey("inetOrgPerson", "inetOrgPerson")).get();
    	
    	assertNotNull(metaData);
    }    
}


