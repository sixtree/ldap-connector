/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.ldap.functional.openldap;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.mule.module.ldap.api.LDAPConnection;

public class SchemaTest
{

    @Test
    public void testSchema() throws Exception {
        Map<String, String> extendedConf = new HashMap<String, String>();
        extendedConf.put("java.naming.ldap.factory.socket", "org.mule.module.ldap.security.BypassTrustSSLSocketFactory");
        LDAPConnection connection = LDAPConnection.getConnection("jndi", "ldaps://ldap-qa.cloudhub.io:3636/", "simple", 1, 5, 60000, "ignore", extendedConf);
        
        connection.bind("uid=emmet.brown,ou=people,dc=muleforge,dc=org", "mule1234");
        
        connection.lookup("uid=emmet.brown,ou=people,dc=muleforge,dc=org");
        
        connection.close();
    }
}


