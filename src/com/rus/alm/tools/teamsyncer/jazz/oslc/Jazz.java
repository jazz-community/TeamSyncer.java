package com.rus.alm.tools.teamsyncer.jazz.oslc;

import java.util.*;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.rus.alm.tools.teamsyncer.jazz.IJazz;
import com.rus.alm.tools.teamsyncer.jazz.IJazzMember;
import com.rus.alm.tools.teamsyncer.jazz.IJazzProjectArea;
import com.rus.alm.tools.teamsyncer.jazz.IJazzTeamArea;

public class Jazz implements IJazz
{
	private JazzServerController _jServ;
	private JazzNamespaceContext _jnsCtxt;
	private String _projAreasURL;
	public final String getProjectAreasURL()
	{
		return _projAreasURL;
	}
	private String _userrepoURL;
	public final String getUserRepoURL()
	{
		return _userrepoURL;
	}

	private ArrayList<IJazzProjectArea> _pa_list = null;
	public enum repositoryType
	{
		ccm,
		rm,
		qm;

		public static final int SIZE = java.lang.Integer.SIZE;

		public int getValue()
		{
			return this.ordinal();
		}

		public static repositoryType forValue(int value)
		{
			return values()[value];
		}
	}

	private JazzRoleCache _jazzRoleCache;
	private JazzUserRepository _jazzUserRepo;

	public final JazzServerController GetJazzServerCtrl()
	{
		return _jServ;
	}

	public final JazzNamespaceContext GetJazzNamespaceContext()
	{
		return _jnsCtxt;
	}

	public final JazzRoleCache GetJazzRoleCache()
	{
		return _jazzRoleCache;
	}

	public final JazzUserRepository GetJazzUserRepository()
	{
		return _jazzUserRepo;
	}

	public Jazz(String repositoryURL, repositoryType rType, String username, String password)
	{
		String jazzAppl;
		if (rType == repositoryType.ccm) 		jazzAppl = "ccm";
		else if (rType == repositoryType.rm)    jazzAppl = "rm";
		else									jazzAppl = "qm";

		_jServ = new JazzServerController(repositoryURL, jazzAppl, username, password);
		_jnsCtxt = new JazzNamespaceContext();
		_jazzRoleCache = new JazzRoleCache(this);
		_jazzUserRepo = new JazzUserRepository(this);

		Document xml;
		xml = _jServ.getXml(repositoryURL + "/" + jazzAppl + "/rootservices");

		XPath xpath;
		Node node;
		try {
			xpath = XPathFactory.newInstance().newXPath();
            xpath.setNamespaceContext(_jnsCtxt);

            String currXPath = "/rdf:Description/" + JazzConstants.jazzProcNum + ":projectAreas/@rdf:resource";
            node = (Node) xpath.evaluate(currXPath, xml, XPathConstants.NODE);
			_projAreasURL = node.getNodeValue();

			currXPath = "/rdf:Description/jfs:users/@rdf:resource";
			node = (Node) xpath.evaluate(currXPath, xml, XPathConstants.NODE);
			_userrepoURL = node.getNodeValue();

		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Jazz(String repositoryExtendedURL, String username, String password)
	{
		String repositoryURL = repositoryExtendedURL.substring(0, repositoryExtendedURL.lastIndexOf('/'));
		String jazzAppl = repositoryExtendedURL.substring(repositoryExtendedURL.lastIndexOf('/')+1);
		_jServ = new JazzServerController(repositoryURL, jazzAppl, username, password);
		_jnsCtxt = new JazzNamespaceContext();
		_jazzRoleCache = new JazzRoleCache(this);
		_jazzUserRepo = new JazzUserRepository(this);

		Document xml;
		xml = _jServ.getXml(repositoryExtendedURL + "/rootservices");

		XPath xpath;
		Node node;
		try {
			xpath = XPathFactory.newInstance().newXPath();
            xpath.setNamespaceContext(_jnsCtxt);

            String currXPath = "/rdf:Description/" + JazzConstants.jazzProcNum + ":projectAreas/@rdf:resource";
            node = (Node) xpath.evaluate(currXPath, xml, XPathConstants.NODE);
			_projAreasURL = node.getNodeValue();

			currXPath = "/rdf:Description/jfs:users/@rdf:resource";
			node = (Node) xpath.evaluate(currXPath, xml, XPathConstants.NODE);
			_userrepoURL = node.getNodeValue();

		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public final Iterable<IJazzProjectArea> JazzProjectAreas()
	{
		if (_pa_list == null)
		{
			_pa_list = new ArrayList<IJazzProjectArea>();

			Document xml = _jServ.getXml(getProjectAreasURL() + "?includeArchived=true");			
			try {
				XPath xpath = XPathFactory.newInstance().newXPath();
	            xpath.setNamespaceContext(_jnsCtxt);

				String currXPath = "/" + JazzConstants.jazzProcNum + ":project-areas/" + JazzConstants.jazzProcNum + ":project-area";
	            NodeList nodeList = (NodeList) xpath.evaluate(currXPath, xml, XPathConstants.NODESET);
	            for (int i = 0; i < nodeList.getLength(); i++) {
					_pa_list.add(new JazzProjectArea(nodeList.item(i), this));	            	
	            }
			} catch (XPathExpressionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (_pa_list == null)
			return Collections.emptyList();
		else
			return _pa_list;
	}

	/* (non-Javadoc)
	 * @see com.rus.alm.tools.teamsyncer.jazz.IJazz#GetJazzProjectAreaByName(java.lang.String)
	 */
	@Override
	public final IJazzProjectArea GetJazzProjectAreaByName(String name)
	{
		if (_pa_list == null)
		{
			Document xml = _jServ.getXml(getProjectAreasURL() + "?includeArchived=true");
			try {
				XPath xpath = XPathFactory.newInstance().newXPath();
				xpath.setNamespaceContext(_jnsCtxt);

				String currXPath = "/" + JazzConstants.jazzProcNum + ":project-areas/" + JazzConstants.jazzProcNum + ":project-area[@" + JazzConstants.jazzProcNum + ":name='" + name + "']";
				Node n = (Node) xpath.evaluate(currXPath, xml, XPathConstants.NODE);
				if (n != null)
				{
					return new JazzProjectArea(n, this);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
		{
			for (int i = 0; i < _pa_list.size(); i++)
			{
				if (name.equalsIgnoreCase(_pa_list.get(i).getName()))
				{
					return _pa_list.get(i);
				}
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.rus.alm.tools.teamsyncer.jazz.IJazz#GetJazzProjectAreaByOid(java.lang.String)
	 */
	@Override
	public final IJazzProjectArea GetJazzProjectAreaByOid(String oid)
	{
		if (_pa_list == null)
		{
			Document xml = _jServ.getXml(getProjectAreasURL() + "/" + oid);

			XPath xpath = XPathFactory.newInstance().newXPath();
			xpath.setNamespaceContext(_jnsCtxt);

			try {
				String currXPath = "/" + JazzConstants.jazzProcNum + ":project-area";
				Node n = (Node) xpath.evaluate(currXPath, xml, XPathConstants.NODE);
				if (n != null)
				{
					return new JazzProjectArea(n, this);
				}
			} catch (XPathExpressionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
		{
			for (int i = 0; i < _pa_list.size(); i++)
			{
				String pa_oid = ((JazzProjectArea) _pa_list.get(i)).getURL().substring(((JazzProjectArea) _pa_list.get(i)).getURL().lastIndexOf('/') + 1);
				if (oid.equalsIgnoreCase(pa_oid))
				{
					return _pa_list.get(i);
				}
			}
		}
		return null;
	}

	@Override
	public IJazzMember GetNewJazzMember(String userName) {
		return new JazzMember(userName, this);
	}

	@Override
	public IJazzTeamArea GetJazzTeamAreaByOid(String oid) {
		// Only PlainJavaAPI version will find a team area based on one single oid (without parent oids)
		return null;
	}
	
	@Override
	public void close() {
		// OSLC version needs not to do anything
	}
}