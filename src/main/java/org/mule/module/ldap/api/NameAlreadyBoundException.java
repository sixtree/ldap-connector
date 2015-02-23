/**
 * Copyright (c) MuleSoft, Inc. All rights reserved. http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.md file.
 */
package org.mule.module.ldap.api;

/**
 * This class is the abstraction
 * 
 * @author mariano
 */
public class NameAlreadyBoundException extends LDAPException
{

    /**
	 * 
	 */
    private static final long serialVersionUID = 3869273274579892302L;

    public NameAlreadyBoundException()
    {
        super();
    }

    public NameAlreadyBoundException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public NameAlreadyBoundException(String message)
    {
        super(message);
    }

    public NameAlreadyBoundException(Throwable cause)
    {
        super(cause);
    }
}
