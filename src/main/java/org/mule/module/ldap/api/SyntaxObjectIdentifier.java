/**
 * Copyright (c) MuleSoft, Inc. All rights reserved. http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.md file.
 */
package org.mule.module.ldap.api;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * @author mariano
 *
 */
public class SyntaxObjectIdentifier
{
	private String oid = null;
	private boolean humanReadable = true;
	private String description = null;
	
	private final static Map<String, SyntaxObjectIdentifier> SYNTAX_OBJECT_IDENTIFIERS = new HashMap<String, SyntaxObjectIdentifier>(60);

	static
	{
		addSyntaxObjectIdentifier(new SyntaxObjectIdentifier("ACI Item", false, "1.3.6.1.4.1.1466.115.121.1.1"));
		addSyntaxObjectIdentifier(new SyntaxObjectIdentifier("Access Point", true, "1.3.6.1.4.1.1466.115.121.1.2"));
		addSyntaxObjectIdentifier(new SyntaxObjectIdentifier("Attribute Type Description", true, "1.3.6.1.4.1.1466.115.121.1.3"));
		addSyntaxObjectIdentifier(new SyntaxObjectIdentifier("Audio", false, "1.3.6.1.4.1.1466.115.121.1.4"));
		addSyntaxObjectIdentifier(new SyntaxObjectIdentifier("Binary", false, "1.3.6.1.4.1.1466.115.121.1.5"));
		addSyntaxObjectIdentifier(new SyntaxObjectIdentifier("Bit String", true, "1.3.6.1.4.1.1466.115.121.1.6"));
		addSyntaxObjectIdentifier(new SyntaxObjectIdentifier("Boolean", true, "1.3.6.1.4.1.1466.115.121.1.7")); 
		addSyntaxObjectIdentifier(new SyntaxObjectIdentifier("Certificate", false, "1.3.6.1.4.1.1466.115.121.1.8"));
		addSyntaxObjectIdentifier(new SyntaxObjectIdentifier("Certificate List", false, "1.3.6.1.4.1.1466.115.121.1.9"));
		addSyntaxObjectIdentifier(new SyntaxObjectIdentifier("Certificate Pair", false, "1.3.6.1.4.1.1466.115.121.1.10"));
		addSyntaxObjectIdentifier(new SyntaxObjectIdentifier("Country String", true, "1.3.6.1.4.1.1466.115.121.1.11"));
		addSyntaxObjectIdentifier(new SyntaxObjectIdentifier("DN", true, "1.3.6.1.4.1.1466.115.121.1.12"));
		addSyntaxObjectIdentifier(new SyntaxObjectIdentifier("Data Quality Syntax", true, "1.3.6.1.4.1.1466.115.121.1.13"));
		addSyntaxObjectIdentifier(new SyntaxObjectIdentifier("Delivery Method", true, "1.3.6.1.4.1.1466.115.121.1.14"));
		addSyntaxObjectIdentifier(new SyntaxObjectIdentifier("Directory String", true, "1.3.6.1.4.1.1466.115.121.1.15"));
		addSyntaxObjectIdentifier(new SyntaxObjectIdentifier("DIT Content Rule Description", true, "1.3.6.1.4.1.1466.115.121.1.16"));
		addSyntaxObjectIdentifier(new SyntaxObjectIdentifier("DIT Structure Rule Description", true, "1.3.6.1.4.1.1466.115.121.1.17"));
		addSyntaxObjectIdentifier(new SyntaxObjectIdentifier("DL Submit Permission", true, "1.3.6.1.4.1.1466.115.121.1.18"));
		addSyntaxObjectIdentifier(new SyntaxObjectIdentifier("DSA Quality Syntax", true, "1.3.6.1.4.1.1466.115.121.1.19"));
		addSyntaxObjectIdentifier(new SyntaxObjectIdentifier("DSE Type", true, "1.3.6.1.4.1.1466.115.121.1.20"));
		addSyntaxObjectIdentifier(new SyntaxObjectIdentifier("Enhanced Guide", true, "1.3.6.1.4.1.1466.115.121.1.21"));
		addSyntaxObjectIdentifier(new SyntaxObjectIdentifier("Facsimile Telephone Number", true, "1.3.6.1.4.1.1466.115.121.1.22"));
		addSyntaxObjectIdentifier(new SyntaxObjectIdentifier("Fax", false, "1.3.6.1.4.1.1466.115.121.1.23"));
		addSyntaxObjectIdentifier(new SyntaxObjectIdentifier("Generalized Time", true, "1.3.6.1.4.1.1466.115.121.1.24"));
		addSyntaxObjectIdentifier(new SyntaxObjectIdentifier("Guide", true, "1.3.6.1.4.1.1466.115.121.1.25"));
		addSyntaxObjectIdentifier(new SyntaxObjectIdentifier("IA5 String", true, "1.3.6.1.4.1.1466.115.121.1.26"));
		addSyntaxObjectIdentifier(new SyntaxObjectIdentifier("INTEGER", true, "1.3.6.1.4.1.1466.115.121.1.27"));
		addSyntaxObjectIdentifier(new SyntaxObjectIdentifier("JPEG", false, "1.3.6.1.4.1.1466.115.121.1.28"));
		addSyntaxObjectIdentifier(new SyntaxObjectIdentifier("LDAP Syntax Description", true, "1.3.6.1.4.1.1466.115.121.1.54"));
		addSyntaxObjectIdentifier(new SyntaxObjectIdentifier("LDAP Schema Definition", true, "1.3.6.1.4.1.1466.115.121.1.56"));
		addSyntaxObjectIdentifier(new SyntaxObjectIdentifier("LDAP Schema Description", true, "1.3.6.1.4.1.1466.115.121.1.57"));
		addSyntaxObjectIdentifier(new SyntaxObjectIdentifier("Master And Shadow Access Points", true, "1.3.6.1.4.1.1466.115.121.1.29"));
		addSyntaxObjectIdentifier(new SyntaxObjectIdentifier("Matching Rule Description", true, "1.3.6.1.4.1.1466.115.121.1.30"));
		addSyntaxObjectIdentifier(new SyntaxObjectIdentifier("Matching Rule Use Description", true, "1.3.6.1.4.1.1466.115.121.1.31"));
		addSyntaxObjectIdentifier(new SyntaxObjectIdentifier("Mail Preference", true, "1.3.6.1.4.1.1466.115.121.1.32"));
		addSyntaxObjectIdentifier(new SyntaxObjectIdentifier("MHS OR Address", true, "1.3.6.1.4.1.1466.115.121.1.33"));
		addSyntaxObjectIdentifier(new SyntaxObjectIdentifier("Modify Rights", true, "1.3.6.1.4.1.1466.115.121.1.55"));
		addSyntaxObjectIdentifier(new SyntaxObjectIdentifier("Name And Optional UID", true, "1.3.6.1.4.1.1466.115.121.1.34"));
		addSyntaxObjectIdentifier(new SyntaxObjectIdentifier("Name Form Description", true, "1.3.6.1.4.1.1466.115.121.1.35"));
		addSyntaxObjectIdentifier(new SyntaxObjectIdentifier("Numeric String", true, "1.3.6.1.4.1.1466.115.121.1.36"));
		addSyntaxObjectIdentifier(new SyntaxObjectIdentifier("Object Class Description", true, "1.3.6.1.4.1.1466.115.121.1.37"));
		addSyntaxObjectIdentifier(new SyntaxObjectIdentifier("Octet String", true, "1.3.6.1.4.1.1466.115.121.1.40"));
		addSyntaxObjectIdentifier(new SyntaxObjectIdentifier("OID", true, "1.3.6.1.4.1.1466.115.121.1.38"));
		addSyntaxObjectIdentifier(new SyntaxObjectIdentifier("Other Mailbox", true, "1.3.6.1.4.1.1466.115.121.1.39"));
		addSyntaxObjectIdentifier(new SyntaxObjectIdentifier("Postal Address", true, "1.3.6.1.4.1.1466.115.121.1.41"));
		addSyntaxObjectIdentifier(new SyntaxObjectIdentifier("Protocol Information", true, "1.3.6.1.4.1.1466.115.121.1.42"));
		addSyntaxObjectIdentifier(new SyntaxObjectIdentifier("Presentation Address", true, "1.3.6.1.4.1.1466.115.121.1.43"));
		addSyntaxObjectIdentifier(new SyntaxObjectIdentifier("Printable String", true, "1.3.6.1.4.1.1466.115.121.1.44"));
		addSyntaxObjectIdentifier(new SyntaxObjectIdentifier("Substring Assertion", true, "1.3.6.1.4.1.1466.115.121.1.58"));
		addSyntaxObjectIdentifier(new SyntaxObjectIdentifier("Subtree Specification", true, "1.3.6.1.4.1.1466.115.121.1.45"));
		addSyntaxObjectIdentifier(new SyntaxObjectIdentifier("Supplier Information", true, "1.3.6.1.4.1.1466.115.121.1.46"));
		addSyntaxObjectIdentifier(new SyntaxObjectIdentifier("Supplier Or Consumer", true, "1.3.6.1.4.1.1466.115.121.1.47"));
		addSyntaxObjectIdentifier(new SyntaxObjectIdentifier("Supplier And Consumer", true, "1.3.6.1.4.1.1466.115.121.1.48"));
		addSyntaxObjectIdentifier(new SyntaxObjectIdentifier("Supported Algorithm", false, "1.3.6.1.4.1.1466.115.121.1.49"));
		addSyntaxObjectIdentifier(new SyntaxObjectIdentifier("Telephone Number", true, "1.3.6.1.4.1.1466.115.121.1.50"));
		addSyntaxObjectIdentifier(new SyntaxObjectIdentifier("Teletex Terminal Identifier", true, "1.3.6.1.4.1.1466.115.121.1.51"));
		addSyntaxObjectIdentifier(new SyntaxObjectIdentifier("Telex Number", true, "1.3.6.1.4.1.1466.115.121.1.52"));
		addSyntaxObjectIdentifier(new SyntaxObjectIdentifier("UTC Time", true, "1.3.6.1.4.1.1466.115.121.1.53"));
	};
	
	public SyntaxObjectIdentifier(String oid)
	{
		super();
		if (oid == null)
		{
			throw new IllegalArgumentException("oid cannot be null");
		}
		setOid(oid);		
	}
	
	/**
	 * 
	 */
	public SyntaxObjectIdentifier(String description, boolean humanReadable, String oid)
	{
		this(oid);
		setHumanReadable(humanReadable);
		setDescription(description);
	}

	public String getOid()
	{
		return oid;
	}

	private void setOid(String oid)
	{
		this.oid = oid;
	}

	public boolean isHumanReadable()
	{
		return humanReadable;
	}

	private void setHumanReadable(boolean humanReadable)
	{
		this.humanReadable = humanReadable;
	}

	public String getDescription() {
		return description;
	}

	private void setDescription(String description)
	{
		this.description = description;
	}

	@Override
	public int hashCode() {
		return getOid().hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		
		if (!(obj instanceof SyntaxObjectIdentifier))
		{
			return false;
		}
		
		SyntaxObjectIdentifier other = (SyntaxObjectIdentifier) obj;
		
		return getOid().equals(other.getOid());
	}

	@Override
	public String toString()
	{
		return getDescription() != null ? getDescription() + " (" + getOid() + ")" : getOid();
	}
	
	private static void addSyntaxObjectIdentifier(SyntaxObjectIdentifier syntaxObjectIdentifier)
	{
		SYNTAX_OBJECT_IDENTIFIERS.put(syntaxObjectIdentifier.getOid(), syntaxObjectIdentifier);
	}
	
	public static SyntaxObjectIdentifier getSyntaxObjectIdentifier(String oid)
	{
		SyntaxObjectIdentifier syntaxObjectIdentifier = SYNTAX_OBJECT_IDENTIFIERS.get(oid);

		if (syntaxObjectIdentifier == null)
		{
			throw new NoSuchElementException("There is no Syntax Object Identifier for OID " + oid);
		}
		
		return syntaxObjectIdentifier;
	}
	
	public static boolean isHumanReadable(String oid)
	{
		return getSyntaxObjectIdentifier(oid).isHumanReadable();
	}
}
