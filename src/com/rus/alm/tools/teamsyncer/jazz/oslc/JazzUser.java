package com.rus.alm.tools.teamsyncer.jazz.oslc;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.rus.alm.tools.teamsyncer.jazz.IJazzUser;

public class JazzUser implements IJazzUser
{
	private String _name;
	public final String getName()
	{
		return _name;
	}
	private String _nick;
	public final String getNick()
	{
		return _nick;
	}
	private String _mail;
	public final String getMailAddress()
	{
		return _mail;
	}
	private String _url;
	public final String getURL()
	{
		return _url;
	}
	private boolean _archived = false;
	public final boolean isArchived()
	{
		return _archived;
	}

	public JazzUser(String name, String url)
	{
		_name = name;
		_url = url;
	}

	public JazzUser(String url, Jazz owner)
	{
		_url = url;
//		AccessRTCServer accServ = owner.GetAccessRTCServer();
		JazzServerController jServ = owner.GetJazzServerCtrl();
		JazzNamespaceContext jnsCtxt = owner.GetJazzNamespaceContext();

		Document xml = jServ.getXml(_url, true);
		if (xml == null) {
			throw new RuntimeException("Jazz user not found");
		}
//		//XmlNamespaceManager nsMgr = accServ.buildNamespaceManager(xml, new string[] { "rdf", "j.0", "j.1" });
//		XmlNamespaceManager nsMgr = accServ.buildNamespaceManager(xml, new String[] {"rdf", "foaf", "jfs"});
		
		XPath xpath = XPathFactory.newInstance().newXPath();
		xpath.setNamespaceContext(jnsCtxt);
		
		try {
			//string currXPath = "//j.0:name";
			String currXPath = "//foaf:name";
			Node node = (Node) xpath.evaluate(currXPath, xml, XPathConstants.NODE);
			_name = node.getTextContent();
			
			//currXPath = "//j.0:nick";
			currXPath = "//foaf:nick";
			node = (Node) xpath.evaluate(currXPath, xml, XPathConstants.NODE);
			_nick = node.getTextContent();
	
			//currXPath = "//j.0:mbox/@rdf:resource";
			currXPath = "//foaf:mbox/@rdf:resource";
			node = (Node) xpath.evaluate(currXPath, xml, XPathConstants.NODE);
			_mail = node.getTextContent().replace("mailto:", "");
	
			//currXPath = "//j.1:archived";
			currXPath = "//jfs:archived";
			node = (Node) xpath.evaluate(currXPath, xml, XPathConstants.NODE);
			if (node.getTextContent().equals("true"))
			{
				_archived = true;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}