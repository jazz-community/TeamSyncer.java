package com.rus.alm.tools.teamsyncer.jazz.oslc;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.rus.alm.tools.teamsyncer.jazz.IJazzRole;

public class JazzRole implements IJazzRole
{
	private String _id;
	public final String getId()
	{
		return _id;
	}
	private String _label;
	/* (non-Javadoc)
	 * @see com.rus.alm.tools.teamsyncer.jazz.IJazzRole#getLabel()
	 */
	@Override
	public final String getLabel()
	{
		return _label;
	}
	private String _url;
	public final String getURL()
	{
		return _url;
	}
	private String _description;
	public final String getDescription()
	{
		return _description;
	}

	public JazzRole(String url, Jazz owner)
	{
		_url = url;
		JazzServerController jServ = owner.GetJazzServerCtrl();
		JazzNamespaceContext jnsCtxt = owner.GetJazzNamespaceContext();

		Document xml = jServ.getXml(_url);

		XPath xpath = XPathFactory.newInstance().newXPath();
		xpath.setNamespaceContext(jnsCtxt);
		
		try {
			String currXPath = "//" + JazzConstants.jazzProcNum + ":id";
			Node node = (Node) xpath.evaluate(currXPath, xml, XPathConstants.NODE);
			_id = node.getTextContent();

			currXPath = "//" + JazzConstants.jazzProcNum + ":label";
			node = (Node) xpath.evaluate(currXPath, xml, XPathConstants.NODE);
			_label = node.getTextContent();

			currXPath = "//" + JazzConstants.jazzProcNum + ":description";
			node = (Node) xpath.evaluate(currXPath, xml, XPathConstants.NODE);
			_description = node.getTextContent();
			
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}