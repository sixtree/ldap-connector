/**
 * Copyright (c) MuleSoft, Inc. All rights reserved. http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.md file.
 */

package org.mule.module.ldap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.mule.api.MessagingException;
import org.mule.api.MuleEvent;
import org.mule.construct.Flow;
import org.mule.tck.junit4.FunctionalTestCase;

public abstract class AbstractLDAPConnectorTest extends FunctionalTestCase
{
    /**
     * Run the flow specified by name using the specified payload and assert
     * equality on the expected output
     *
     * @param flowName The name of the flow to run
     * @param payload The payload of the input event
     */
     protected <U> Object runFlow(String flowName, U payload) throws Exception
     {
         Flow flow = lookupFlowConstruct(flowName);
         MuleEvent event = getTestEvent(payload);
         MuleEvent responseEvent = flow.process(event);
         
         return responseEvent.getMessage().getPayload();
     }
     
     /**
      * Run the flow specified by name using the specified payload and assert
      * equality on the expected exception
      *
      * @param flowName The name of the flow to run
      * @param expect The expected exception
      * @param payload The payload of the input event
      */
      protected <T, U> Throwable runFlowWithPayloadAndExpectException(String flowName, Class<T> expect, U payload) throws Exception
      {
          try
          {
              Flow flow = lookupFlowConstruct(flowName);
              MuleEvent event = getTestEvent(payload);
              MuleEvent responseEvent = flow.process(event);

              // Support for mule 3.2.x and previous
              assertNotNull(responseEvent.getMessage().getExceptionPayload());
              assertEquals(expect, responseEvent.getMessage().getExceptionPayload().getException().getCause().getClass());
              
              return responseEvent.getMessage().getExceptionPayload().getException().getCause();
          }
          catch(MessagingException ex)
          {
              // Support for mule 3.3.x
              assertEquals(expect, ex.getCause().getClass());
              return ex.getCause();
          }
      }
      
      /**
       * Run the flow specified by name using the specified payload, expect an exception
       * and return that exception
       *
       * @param flowName The name of the flow to run
       * @param payload The payload of the input event
       * @return The exception
       */
       protected <U> Throwable runFlowWithPayloadAndReturnException(String flowName, U payload) throws Exception
       {
           try
           {
               Flow flow = lookupFlowConstruct(flowName);
               MuleEvent event = getTestEvent(payload);
               MuleEvent responseEvent = flow.process(event);

               // Support for mule 3.2.x and previous
               assertNotNull(responseEvent.getMessage().getExceptionPayload());
               
               return responseEvent.getMessage().getExceptionPayload().getException().getCause();
           }
           catch(MessagingException ex)
           {
               // Support for mule 3.3.x
               return ex.getCause();
           }
       }
       
    /**
    * Run the flow specified by name and assert equality on the expected output
    *
    * @param flowName The name of the flow to run
    * @param expect The expected output
    */
    protected <T> void runFlowAndExpect(String flowName, T expect) throws Exception
    {
        assertEquals(expect, runFlow(flowName, null));
    }

    /**
    * Run the flow specified by name using the specified payload and assert
    * equality on the expected output
    *
    * @param flowName The name of the flow to run
    * @param expect The expected output
    * @param payload The payload of the input event
    */
    protected <T, U> void runFlowWithPayloadAndExpect(String flowName, T expect, U payload) throws Exception
    {
        assertEquals(expect, runFlow(flowName, payload));
    }

    /**
     * Retrieve a flow by name from the registry
     *
     * @param name Name of the flow to retrieve
     */
    protected Flow lookupFlowConstruct(String name) throws Exception
    {
        Flow flow = (Flow) muleContext.getRegistry().lookupFlowConstruct(name);
        if(flow == null)
        {
            throw new Exception("Flow " + name + " is not present in configuration " + getConfigResources());
        }        
        return flow;
    }
}
