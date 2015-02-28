package org.mule.module.ldap;

import org.mule.module.ldap.api.LDAPConnection;

public interface LDAPConnectionStrategy
{
	LDAPConnection getConnection();
	
	void disconnect();
}
