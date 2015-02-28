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
public class InvalidAttributeException extends LDAPException
{

    /**
	 * 
	 */
    private static final long serialVersionUID = 1608293876428310202L;

    public InvalidAttributeException()
    {
        super();
    }

    public InvalidAttributeException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public InvalidAttributeException(String message)
    {
        super(message);
    }

    public InvalidAttributeException(Throwable cause)
    {
        super(cause);
    }
}
