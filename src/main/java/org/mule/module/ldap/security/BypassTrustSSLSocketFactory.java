/**
 * 
 */
package org.mule.module.ldap.security;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

/**
 * @author mariano
 *
 */
public class BypassTrustSSLSocketFactory extends SSLSocketFactory {
	  private SSLSocketFactory socketFactory;
	  
	  public BypassTrustSSLSocketFactory()
	  {
		   try {
			      SSLContext ctx = SSLContext.getInstance("TLS");
			      ctx.init(null, new TrustManager[]{ new BypassTrustManager()}, new SecureRandom());
			      socketFactory = ctx.getSocketFactory();
			    } catch ( Exception ex ){ ex.printStackTrace(System.err);  /* handle exception */ }
	  }
	  public static SocketFactory getDefault(){
	    return new BypassTrustSSLSocketFactory();
	  }
	  @Override
	  public String[] getDefaultCipherSuites()
	  {
	    return socketFactory.getDefaultCipherSuites();
	  }
	  @Override
	  public String[] getSupportedCipherSuites()
	  {
	    return socketFactory.getSupportedCipherSuites();
	  }
	  @Override
	  public Socket createSocket(Socket socket, String string, int i, boolean bln) throws IOException
	  {
	    return socketFactory.createSocket(socket, string, i, bln);
	  }
	  @Override
	  public Socket createSocket(String string, int i) throws IOException, UnknownHostException
	  {
	    return socketFactory.createSocket(string, i);
	  }
	  @Override
	  public Socket createSocket(String string, int i, InetAddress ia, int i1) throws IOException, UnknownHostException
	  {
	    return socketFactory.createSocket(string, i, ia, i1);
	  }
	  @Override
	  public Socket createSocket(InetAddress ia, int i) throws IOException
	  {
	    return socketFactory.createSocket(ia, i);
	  }
	  @Override
	  public Socket createSocket(InetAddress ia, int i, InetAddress ia1, int i1) throws IOException
	  {
		  return socketFactory.createSocket(ia, i, ia1, i1);
	  }
}
