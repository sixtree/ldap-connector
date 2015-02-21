/**
 * Copyright (c) MuleSoft, Inc. All rights reserved. http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.md file.
 */

package org.mule.module.ldap.api;

import java.util.List;

public interface LDAPResultSet
{
    void close() throws LDAPException;

    LDAPEntry next() throws LDAPException;
    
    boolean hasNext() throws LDAPException;
    
    List<LDAPEntry> getAllEntries() throws LDAPException;
    
    /**
     * Retrieves (an estimate of) the number of entries in the search result.
     * @return The number of entries in the search result, or -1 if unknown.
     * @throws LDAPException
     */
    int getResultSize() throws LDAPException;
}


