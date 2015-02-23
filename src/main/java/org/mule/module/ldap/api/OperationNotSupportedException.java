/**
 * Copyright (c) MuleSoft, Inc. All rights reserved. http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.md file.
 */
package org.mule.module.ldap.api;

import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.SortControl;

/**
 * This class is the abstraction
 * 
 * @author mariano
 */
public class OperationNotSupportedException extends LDAPException
{

    /**
	 * 
	 */
    private static final long serialVersionUID = 1608293876428310202L;

    public OperationNotSupportedException()
    {
        super();
    }

    public OperationNotSupportedException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public OperationNotSupportedException(String message)
    {
        super(message);
    }

    public OperationNotSupportedException(Throwable cause)
    {
        super(cause);
    }
    
    @Override
    public String getMessage() 
    {
    	String originalMessage = super.getMessage();
    	if (originalMessage != null && originalMessage.contains(PagedResultsControl.OID))
    	{
    		return "The LDAP server does not support paging results. " + originalMessage;
    	}
    	else if (originalMessage != null && originalMessage.contains(SortControl.OID))
    	{
    		return "The LDAP server does not support sorting results. " + originalMessage;
    	} else {
    		return originalMessage;
    	}
    }
}
