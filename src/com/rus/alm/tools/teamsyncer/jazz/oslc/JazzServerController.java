package com.rus.alm.tools.teamsyncer.jazz.oslc;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class JazzServerController {

	private String jazzServerUrl;
	private String jazzApplication;
	private String jazzUserName;
	private String jazzUserPassword;
	
	private CloseableHttpClient httpClient;
	private DocumentBuilderFactory documentBuilderFactory;
	private DocumentBuilder documentBuilder;
    private TransformerFactory transformerFactory;
    private Transformer transformer;
	private HttpClientContext context;
	
	public JazzServerController(String serverUrl, String application, String userName, String userPassword) {
		this.jazzServerUrl = serverUrl;
		this.jazzApplication = application;
		this.jazzUserName = userName;
		this.jazzUserPassword = userPassword;
		
		this.httpClient = HttpClients.createDefault();
		this.context = HttpClientContext.create();
		
		try {
			this.documentBuilderFactory = DocumentBuilderFactory.newInstance();
			this.documentBuilderFactory.setNamespaceAware(true);			
			this.documentBuilder = this.documentBuilderFactory.newDocumentBuilder();
		    this.transformerFactory = TransformerFactory.newInstance();
		    this.transformer = this.transformerFactory.newTransformer();
		    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void authenticate() {
		
		HttpPost _formPost = new HttpPost(jazzServerUrl + "/" + jazzApplication + "/j_security_check");
		_formPost.setHeader("Accept", "text/xml");
		_formPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair("j_username", jazzUserName));
		formparams.add(new BasicNameValuePair("j_password", jazzUserPassword));
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, Consts.UTF_8);
		_formPost.setEntity(entity);
		CloseableHttpResponse _formResponse;
		try {
			_formResponse = httpClient.execute(_formPost, context);
			Header _rtcAuthHeader = _formResponse.getFirstHeader("X-com-ibm-team-repository-web-auth-msg");
			if (_rtcAuthHeader != null && _rtcAuthHeader.getValue().equalsIgnoreCase("authfailed")) {
				throw new AuthenticationException("Authentication failure");
			}
			_formResponse.close();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public final Document getXml(String requestUrl)
	{
		return getXml(requestUrl, false);
	}

	public final Document getXml(String requestUrl, boolean acceptRdf) {
		HttpGet httpGet = new HttpGet(requestUrl);

		if (acceptRdf) 	httpGet.setHeader("Accept", "application/rdf+xml");
		else 			httpGet.setHeader("Accept", "text/xml");
		httpGet.setHeader("OSLC-Core-Version", "2.0");

		Document doc = null;
		try {
			CloseableHttpResponse response = httpClient.execute(httpGet);
			int retCode = response.getStatusLine().getStatusCode();
			if (retCode != 200) {
				httpGet.releaseConnection();
				if (retCode != 404) {
					System.out.println("GetXml returned " + retCode + ", status line:");
					System.out.println(response.getStatusLine());
				}
				response.close();
				return null;
			}
			Header _rtcAuthHeader = response.getFirstHeader("X-com-ibm-team-repository-web-auth-msg");
			if (_rtcAuthHeader != null && _rtcAuthHeader.getValue().equalsIgnoreCase("authrequired")) {
				response.close();
				httpGet.releaseConnection();
				authenticate();
				response = httpClient.execute(httpGet);
				retCode = response.getStatusLine().getStatusCode();
				if (retCode != 200) {
					httpGet.releaseConnection();
					if (retCode != 404) {
						System.out.println("GetXml returned " + retCode + ", status line:");
						System.out.println(response.getStatusLine());
					}
					response.close();
					return null;
				}
				_rtcAuthHeader = response.getFirstHeader("X-com-ibm-team-repository-web-auth-msg");
				if (_rtcAuthHeader != null && _rtcAuthHeader.getValue().equalsIgnoreCase("authrequired")) {
					throw new AuthenticationException("Authentication failure");
				}
			}
			HttpEntity respEnt = response.getEntity();
		    doc = documentBuilder.parse(respEnt.getContent());
		    response.close();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return doc;
	}
	
	private String getSessionIdFromCookies() {
		CookieStore cookieStore = context.getCookieStore();
	    List<Cookie> cookies = cookieStore.getCookies();
		String s_id = "";
		for (Cookie c : cookies)
		{
			if (c.getName().equals("JSESSIONID"))
			{
				s_id = c.getValue();
				break;
			}
		}
		return s_id;
	}

	public final String postXml(String requestUrl, Document xmlToPost) {
		HttpPost httpPost = new HttpPost(requestUrl);
		//httpPost.setHeader("Timeout", "30000");
		httpPost.setHeader("Accept", "text/xml");
		httpPost.setHeader("OSLC-Core-Version", "2.0");
		String s_id = getSessionIdFromCookies();
		httpPost.setHeader("X-Jazz-CSRF-Prevent", s_id);
		String location = "";

		try {
		    StringWriter writer = new StringWriter();
			transformer.transform(new DOMSource(xmlToPost), new StreamResult(writer));
	        StringEntity entity = new StringEntity(writer.toString(), Consts.UTF_8);
			httpPost.setEntity(entity);
			CloseableHttpResponse response;
			response = httpClient.execute(httpPost);
			int retCode = response.getStatusLine().getStatusCode();
			if (retCode == 403) {
				// we probably had a rm forbidden return and got a new session id so repeat with new id
				httpPost.releaseConnection();
				httpPost.setHeader("X-Jazz-CSRF-Prevent", getSessionIdFromCookies());
				response = httpClient.execute(httpPost);
				retCode = response.getStatusLine().getStatusCode();				
			}
			if (retCode != 201) {
				httpPost.releaseConnection();
				if (retCode != 404) {
					System.out.println("PostXml returned " + retCode + ", status line:");
					System.out.println(response.getStatusLine());
				}
			}
			location = response.getFirstHeader("Location").getValue();
		    response.close();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    return location;
	}

	public final void putXml(String requestUrl, Document xmlToPut) {
		HttpPut httpPut = new HttpPut(requestUrl);
		httpPut.setHeader("Accept", "text/xml");	
		httpPut.setHeader("OSLC-Core-Version", "2.0");
		String s_id = getSessionIdFromCookies();
		httpPut.setHeader("X-Jazz-CSRF-Prevent", s_id);

		try {
		    StringWriter writer = new StringWriter();
			transformer.transform(new DOMSource(xmlToPut), new StreamResult(writer));
	        StringEntity entity = new StringEntity(writer.toString(), Consts.UTF_8);
			httpPut.setEntity(entity);
			CloseableHttpResponse response;
			response = httpClient.execute(httpPut);
			int retCode = response.getStatusLine().getStatusCode();
			if (retCode != 201) {
				httpPut.releaseConnection();
				System.out.println("PutXml returned " + retCode + ", status line:");
				System.out.println(response.getStatusLine());
			}
		    response.close();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public final int delete(String requestURI)
	{
		HttpDelete httpDelete = new HttpDelete(requestURI);
		httpDelete.setHeader("Accept", "text/xml");
		httpDelete.setHeader("OSLC-Core-Version", "2.0");
		String s_id = getSessionIdFromCookies();
		httpDelete.setHeader("X-Jazz-CSRF-Prevent", s_id);

		CloseableHttpResponse response;
		int retCode = 0;
		try {
			response = httpClient.execute(httpDelete);
			retCode = response.getStatusLine().getStatusCode();
			if (retCode == 403) {
				// we probably had a rm forbidden return and got a new session id so repeat with new id
				httpDelete.releaseConnection();
				httpDelete.setHeader("X-Jazz-CSRF-Prevent", getSessionIdFromCookies());
				response = httpClient.execute(httpDelete);
				retCode = response.getStatusLine().getStatusCode();				
			}
			if (retCode != 200) {
				httpDelete.releaseConnection();
				System.out.println("Delete returned " + retCode + ", status line:");
				System.out.println(response.getStatusLine());
			}
		    response.close();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return retCode;
	}
}
