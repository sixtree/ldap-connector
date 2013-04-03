/**
 * Copyright (c) MuleSoft, Inc. All rights reserved. http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.md file.
 */

package org.mule.module.ldap;

import java.io.File;

import org.apache.directory.server.core.schema.SchemaInterceptor;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.mule.util.FileUtils;
import org.springframework.security.ldap.server.ApacheDSContainer;

public abstract class AbstractLDAPConnectorEmbeddedLDAPTest extends AbstractLDAPConnectorTest
{
    private static ApacheDSContainer ldapServer;
    public static int LDAP_PORT = 10389;
    public static File WORKING_DIRECTORY = new File(System.getProperty("java.io.tmpdir") + File.separator + "ldap-connector-junit-server");
    
    @BeforeClass
    public static void startLdapServer() throws Exception {
        FileUtils.deleteDirectory(WORKING_DIRECTORY);
        
        ldapServer = new ApacheDSContainer("dc=mulesoft,dc=org", "classpath:test-server.ldif");
        ldapServer.setWorkingDirectory(WORKING_DIRECTORY);
        ldapServer.setPort(LDAP_PORT);
        ldapServer.getService().setAllowAnonymousAccess(true);
        ldapServer.getService().setAccessControlEnabled(true);
        ldapServer.getService().setShutdownHookEnabled(true);
        
        ldapServer.getService().getInterceptors().add(new SchemaInterceptor());
        ldapServer.afterPropertiesSet(); // This method calls start
        ldapServer.getService().getSchemaService().getRegistries();
    }

    @AfterClass
    public static void stopLdapServer() throws Exception {
        if (ldapServer != null) {
            ldapServer.stop();
        }
    }    
}
