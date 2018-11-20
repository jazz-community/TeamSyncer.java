package com.rus.alm.tools.teamsyncer.jazz.oslc;

import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.rus.alm.tools.teamsyncer.jazz.IJazzMember;
import com.rus.alm.tools.teamsyncer.jazz.IJazzProjectArea;
import com.rus.alm.tools.teamsyncer.jazz.IJazzRole;
import com.rus.alm.tools.teamsyncer.jazz.IJazzTeamArea;

public class JazzProjectArea implements IJazzProjectArea
{
	private String _name;
	/* (non-Javadoc)
	 * @see com.rus.alm.tools.teamsyncer.jazz.IJazzProjectArea#getName()
	 */
	@Override
	public final String getName()
	{
		return _name;
	}
	private String _url;
	public final String getURL()
	{
		return _url;
	}
	private String _summary;
	public final String getSummary()
	{
		return _summary;
	}
	private String _description;
	public final String getDescription()
	{
		return _description;
	}
	private String _team_areas_url;
	//public string TeamAreaListURL
	//{
	//    get { return _team_areas_url; }
	//}
	private String _admins_url;
	private String _members_url;
	private String _roles_url;
	private boolean _archived = true;
	public final boolean getisArchived()
	{
		return _archived;
	}

	private ArrayList<IJazzTeamArea> _ta_list = null;
	private ArrayList<JazzUser> _adm_list = null;
	private ArrayList<IJazzMember> _mem_list = null;
	private ArrayList<IJazzRole> _role_list = null;
	private JazzServerController _jServ = null;
	private JazzNamespaceContext _jnsCtxt = null;
	private Jazz _owner = null;

	public JazzProjectArea(String name, String url)
	{
		_name = name;
		_url = url;
	}

	public JazzProjectArea(String name, String url, Jazz owner)
	{
		_name = name;
		_url = url;
		_owner = owner;
		_jServ = owner.GetJazzServerCtrl();
		_jnsCtxt = owner.GetJazzNamespaceContext();

		Document xml = _jServ.getXml(_url + "?includeArchived=true");
		
		XPath xpath = XPathFactory.newInstance().newXPath();
		xpath.setNamespaceContext(_jnsCtxt);

		try {
			String currXPath = "/" + JazzConstants.jazzProcNum + ":project-area/" + JazzConstants.jazzProcNum + ":summary";
			Node node = (Node) xpath.evaluate(currXPath, xml, XPathConstants.NODE);
			_summary = node.getTextContent();
			
			currXPath = "/" + JazzConstants.jazzProcNum + ":project-area/" + JazzConstants.jazzProcNum + ":description";
			node = (Node) xpath.evaluate(currXPath, xml, XPathConstants.NODE);
			_description = node.getTextContent();

			currXPath = "/" + JazzConstants.jazzProcNum + ":project-area/" + JazzConstants.jazzProcNum + ":team-areas-url";
			node = (Node) xpath.evaluate(currXPath, xml, XPathConstants.NODE);
			_team_areas_url = node.getTextContent();

			currXPath = "/" + JazzConstants.jazzProcNum + ":project-area/" + JazzConstants.jazzProc + ":admins-url";
			node = (Node) xpath.evaluate(currXPath, xml, XPathConstants.NODE);
			_admins_url = node.getTextContent();

			currXPath = "/" + JazzConstants.jazzProcNum + ":project-area/" + JazzConstants.jazzProcNum + ":members-url";
			node = (Node) xpath.evaluate(currXPath, xml, XPathConstants.NODE);
			_members_url = node.getTextContent();

			currXPath = "/" + JazzConstants.jazzProcNum + ":project-area/" + JazzConstants.jazzProcNum + ":roles-url";
			node = (Node) xpath.evaluate(currXPath, xml, XPathConstants.NODE);
			_roles_url = node.getTextContent();

			currXPath = "/" + JazzConstants.jazzProcNum + ":project-area/" + JazzConstants.jazzProcNum + ":archived";
			node = (Node) xpath.evaluate(currXPath, xml, XPathConstants.NODE);
			if (node.getTextContent().equals("false"))
			{
				_archived = false;
			}
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public JazzProjectArea(Node paNode, Jazz owner)
	{
		_owner = owner;
		_jServ = owner.GetJazzServerCtrl();
		_jnsCtxt = owner.GetJazzNamespaceContext();

		XPath xpath = XPathFactory.newInstance().newXPath();
		xpath.setNamespaceContext(_jnsCtxt);

		try {
			String currXPath = "./@" + JazzConstants.jazzProcNum + ":name";
			Node node = (Node) xpath.evaluate(currXPath, paNode, XPathConstants.NODE);
			_name = node.getTextContent();

			currXPath = "./" + JazzConstants.jazzProcNum + ":url";
			node = (Node) xpath.evaluate(currXPath, paNode, XPathConstants.NODE);
			_url = node.getTextContent();

			currXPath = "./" + JazzConstants.jazzProcNum + ":summary";
			node = (Node) xpath.evaluate(currXPath, paNode, XPathConstants.NODE);
			_summary = node.getTextContent();

			currXPath = "./" + JazzConstants.jazzProcNum + ":description";
			node = (Node) xpath.evaluate(currXPath, paNode, XPathConstants.NODE);
			_description = node.getTextContent();

			currXPath = "./" + JazzConstants.jazzProcNum + ":team-areas-url";
			node = (Node) xpath.evaluate(currXPath, paNode, XPathConstants.NODE);
			_team_areas_url = node.getTextContent();

			currXPath = "./" + JazzConstants.jazzProc + ":admins-url";
			node = (Node) xpath.evaluate(currXPath, paNode, XPathConstants.NODE);
			_admins_url = node.getTextContent();

			currXPath = "./" + JazzConstants.jazzProcNum + ":members-url";
			node = (Node) xpath.evaluate(currXPath, paNode, XPathConstants.NODE);
			_members_url = node.getTextContent();

			currXPath = "./" + JazzConstants.jazzProcNum + ":roles-url";
			node = (Node) xpath.evaluate(currXPath, paNode, XPathConstants.NODE);
			_roles_url = node.getTextContent();

			currXPath = "./" + JazzConstants.jazzProcNum + ":archived";
			node = (Node) xpath.evaluate(currXPath, paNode, XPathConstants.NODE);
			if (node.getTextContent().equals("false"))
			{
				_archived = false;
			}
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public final Iterable<IJazzTeamArea> JazzTeamAreas()
	{
		if (_ta_list == null)
		{
			_ta_list = new ArrayList<IJazzTeamArea>();

			Document xml = _jServ.getXml(_team_areas_url + "?includeArchived=true");			
			try {
				XPath xpath = XPathFactory.newInstance().newXPath();
	            xpath.setNamespaceContext(_jnsCtxt);

				String currXPath = "/" + JazzConstants.jazzProcNum + ":team-areas/" + JazzConstants.jazzProcNum + ":team-area";
	            NodeList nodeList = (NodeList) xpath.evaluate(currXPath, xml, XPathConstants.NODESET);
	            for (int i = 0; i < nodeList.getLength(); i++) {
					_ta_list.add(new JazzTeamArea(nodeList.item(i), _owner));	            	
	            }
			} catch (XPathExpressionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (_ta_list == null)
			return Collections.emptyList();
		else
			return _ta_list;
	}

	/* (non-Javadoc)
	 * @see com.rus.alm.tools.teamsyncer.jazz.IJazzProjectArea#GetJazzTeamAreaByName(java.lang.String)
	 */
	@Override
	public final IJazzTeamArea GetJazzTeamAreaByName(String name)
	{
		if (_ta_list == null)
		{
			Document xml = _jServ.getXml(_team_areas_url + "?includeArchived=true");
			//nsMgr = _accServ.buildNamespaceManager(xml, new string[] { "rdf", "oslc", "dcterms" });

			XPath xpath;
			try {
				xpath = XPathFactory.newInstance().newXPath();
				xpath.setNamespaceContext(_jnsCtxt);

				//currXPath = "//oslc:ServiceProvider";
				String currXPath = "/" + JazzConstants.jazzProcNum + ":team-areas/" + JazzConstants.jazzProcNum + ":team-area[@" + JazzConstants.jazzProcNum + ":name='" + name + "']";
				NodeList nList = (NodeList) xpath.evaluate(currXPath, xml, XPathConstants.NODESET);
				for (int i = 0; i < nList.getLength(); i++) {
					currXPath = "./" + JazzConstants.jazzProcNum + ":parent-url";
					Node parentNode = (Node) xpath.evaluate(currXPath, nList.item(i), XPathConstants.NODE);
					if (parentNode == null)
					{
						return new JazzTeamArea(nList.item(i), _owner);
					}
					
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
		{
			for (int i = 0; i < _ta_list.size(); i++)
			{
				if (name.equalsIgnoreCase(_ta_list.get(i).getName()))
				{
					return _ta_list.get(i);
				}
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.rus.alm.tools.teamsyncer.jazz.IJazzProjectArea#GetJazzTeamAreaByOid(java.lang.String)
	 */
	@Override
	public final IJazzTeamArea GetJazzTeamAreaByOid(String oid)
	{
		if (_ta_list == null)
		{
			Document xml = _jServ.getXml(_team_areas_url + "/" + oid);

			XPath xpath = XPathFactory.newInstance().newXPath();
			xpath.setNamespaceContext(_jnsCtxt);

			try {
				String currXPath = "/" + JazzConstants.jazzProcNum + ":team-area";
				Node n = (Node) xpath.evaluate(currXPath, xml, XPathConstants.NODE);
				if (n != null)
				{
					return new JazzTeamArea(n, _owner);
				}
			} catch (XPathExpressionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
		{
			for (int i = 0; i < _ta_list.size(); i++)
			{
				String ta_oid = ((JazzTeamArea) _ta_list.get(i)).getURL().substring(((JazzTeamArea) _ta_list.get(i)).getURL().lastIndexOf('/') + 1);
				if (oid.equalsIgnoreCase(ta_oid))
				{
					return _ta_list.get(i);
				}
			}
		}
		return null;
	}

	public final Iterable<JazzUser> JazzProjectAdmins()
	{
		if (_adm_list == null)
		{
			_adm_list = new ArrayList<JazzUser>();

			Document xml = _jServ.getXml(_admins_url);

			XPath xpath = XPathFactory.newInstance().newXPath();
			xpath.setNamespaceContext(_jnsCtxt);
			
			try {
				String teamAreaXPath = "//" + JazzConstants.jazzProc + ":admin";
				NodeList nodeList = (NodeList) xpath.evaluate(teamAreaXPath, xml, XPathConstants.NODESET);
				for (int i = 0; i < nodeList.getLength(); i++) {
					String currXPath = "./" + JazzConstants.jazzProc + ":user-url";
					Node currNode = (Node) xpath.evaluate(currXPath, nodeList.item(i), XPathConstants.NODE);
					_adm_list.add(_owner.GetJazzUserRepository().GetOrAddUser(currNode.getTextContent(), _owner));
				}
				
			} catch (XPathExpressionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (_adm_list == null)
			return Collections.emptyList();
		else
			return _adm_list;
	}

	private void GetMembers()
	{
		_mem_list = new ArrayList<IJazzMember>();

		Document xml = _jServ.getXml(_members_url + "?includeArchived=true");

		XPath xpath = XPathFactory.newInstance().newXPath();
		xpath.setNamespaceContext(_jnsCtxt);
		
		try {
			String projAreaXPath = "//" + JazzConstants.jazzProcNum + ":member";
			NodeList nodeList = (NodeList) xpath.evaluate(projAreaXPath, xml, XPathConstants.NODESET);
			for (int i = 0; i < nodeList.getLength(); i++) {
				String currXPath = "./" + JazzConstants.jazzProcNum + ":url";
				Node currNode = (Node) xpath.evaluate(currXPath, nodeList.item(i), XPathConstants.NODE);
				_mem_list.add(new JazzMember(_owner, currNode.getTextContent()));
			}
			
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see com.rus.alm.tools.teamsyncer.jazz.IJazzProjectArea#JazzProjectMembers()
	 */
	@Override
	public final Iterable<IJazzMember> JazzProjectMembers()
	{
		if (_mem_list == null)
		{
			GetMembers();
		}
		if (_mem_list == null)
			return Collections.emptyList();
		else
			return _mem_list;
	}

	public final boolean HasMemberWithName(String MemberName)
	{
		if (_mem_list == null)
		{
			GetMembers();
		}

		if (_mem_list != null)
		{
			for (int i = 0; i < _mem_list.size(); i++)
			{
				if (MemberName.equalsIgnoreCase(_mem_list.get(i).getJazzMemberUser().getName()))
				{
					return true;
				}
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see com.rus.alm.tools.teamsyncer.jazz.IJazzProjectArea#GetMemberWithName(java.lang.String)
	 */
	@Override
	public final IJazzMember GetMemberWithName(String MemberName)
	{
		if (_mem_list == null)
		{
			GetMembers();
		}

		if (_mem_list != null)
		{
			for (int i = 0; i < _mem_list.size(); i++)
			{
				if (MemberName.equalsIgnoreCase(_mem_list.get(i).getJazzMemberUser().getName()))
				{
					return _mem_list.get(i);
				}
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.rus.alm.tools.teamsyncer.jazz.IJazzProjectArea#DeleteMemberWithName(java.lang.String)
	 */
	@Override
	public final void DeleteMemberWithName(String MemberName)
	{
		if (_mem_list == null)
		{
			GetMembers();
		}

		if (_mem_list != null)
		{
			for (int i = 0; i < _mem_list.size(); i++)
			{
				if (MemberName.equalsIgnoreCase(_mem_list.get(i).getJazzMemberUser().getName()))
				{
					if (_jServ.delete(((JazzMember) _mem_list.get(i)).getURL()) == 200)
					{
						_mem_list.remove(i);
					}
					return;
				}
			}
		}
	}

	public final void AddMemberPending(JazzMember member)
	{
		member.setPostIsPending(true);
		_mem_list.add(member);
	}

	public final void PostNewMembers()
	{
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		    Document px = docBuilder.newDocument();
//		    px.InsertBefore(px.CreateXmlDeclaration("1.0", "UTF-8", null), px.DocumentElement);
		    Element rootNode = px.createElementNS(JazzConstants.jazzProcNsUrl06, JazzConstants.jazzProcNum + ":members");
		    px.appendChild(rootNode);
		    int memPostCount = 0;
		    for (IJazzMember jm : JazzProjectMembers())
		    {
		    	if (jm.getPostIsPending())
		    	{
		    		memPostCount++;
		    		Element mem = px.createElementNS(JazzConstants.jazzProcNsUrl06, JazzConstants.jazzProcNum + ":member");
		    		rootNode.appendChild(mem);
		    		Text usrUrlCont = px.createTextNode(((JazzUser) jm.getJazzMemberUser()).getURL());
		    		Element usrUrl = px.createElementNS(JazzConstants.jazzProcNsUrl06, JazzConstants.jazzProcNum + ":user-url");
		    		mem.appendChild(usrUrl);
		    		usrUrl.appendChild(usrUrlCont);
		    		int memRoleCount = 0;
		    		Element roleAss = null;
		    		for (IJazzRole jr : jm.JazzMemberRoles())
		    		{
		    			if (memRoleCount == 0)
		    			{
		    				roleAss = px.createElementNS(JazzConstants.jazzProcNsUrl06, JazzConstants.jazzProcNum + ":role-assignments");
		    				mem.appendChild(roleAss);
		    			}
		    			Element roleAs = px.createElementNS(JazzConstants.jazzProcNsUrl06, JazzConstants.jazzProcNum + ":role-assignment");
		    			Text roleUrlCont = px.createTextNode(((JazzRole) jr).getURL());
		    			Element roleUrl = px.createElementNS(JazzConstants.jazzProcNsUrl06, JazzConstants.jazzProcNum + ":role-url");
		    			roleAss.appendChild(roleAs);
		    			roleAs.appendChild(roleUrl);
		    			roleUrl.appendChild(roleUrlCont);
		    			memRoleCount++;
		    		}
		    		jm.setPostIsPending(false);
		    	}
		    }
		    if (memPostCount > 0)
		    {
		    	_jServ.postXml(_url + "/members", px);
		    }

		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see com.rus.alm.tools.teamsyncer.jazz.IJazzProjectArea#AddMember(com.rus.alm.tools.teamsyncer.jazz.JazzMember)
	 */
	@Override
	public final void AddMember(IJazzMember member)
	{
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		    Document px = docBuilder.newDocument();

		    //px.InsertBefore(px.CreateXmlDeclaration("1.0", "UTF-8", null), px.DocumentElement);
			Element rootNode = px.createElementNS(JazzConstants.jazzProcNsUrl06, JazzConstants.jazzProcNum + ":members");
			px.appendChild(rootNode);

			Element mem = px.createElementNS(JazzConstants.jazzProcNsUrl06, JazzConstants.jazzProcNum + ":member");
			rootNode.appendChild(mem);

			Text usrUrlCont = px.createTextNode(((JazzUser) member.getJazzMemberUser()).getURL());
			Element usrUrl = px.createElementNS(JazzConstants.jazzProcNsUrl06, JazzConstants.jazzProcNum + ":user-url");
			mem.appendChild(usrUrl);
			usrUrl.appendChild(usrUrlCont);

			String mem_url = _jServ.postXml(_url + "/members", px);
			((JazzMember) member).SetMemberURL(mem_url);

			_mem_list.add(member);
			
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public final Iterable<IJazzRole> JazzProjectRoles()
	{
		if (_role_list == null)
		{
			_role_list = new ArrayList<IJazzRole>();

			Document xml = _jServ.getXml(_roles_url);

			XPath xpath = XPathFactory.newInstance().newXPath();
			xpath.setNamespaceContext(_jnsCtxt);

			try {
				String teamAreaXPath = "//" + JazzConstants.jazzProcNum + ":role";
				NodeList nodeList = (NodeList) xpath.evaluate(teamAreaXPath, xml, XPathConstants.NODESET);
				for (int i = 0; i < nodeList.getLength(); i++) {
					String currXPath = "./" + JazzConstants.jazzProcNum + ":url";
					Node currNode = (Node) xpath.evaluate(currXPath, nodeList.item(i), XPathConstants.NODE);
					_role_list.add(_owner.GetJazzRoleCache().GetOrAddRole(currNode.getTextContent()));
				}

			} catch (XPathExpressionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (_role_list == null)
			return Collections.emptyList();
		else
			return _role_list;
	}

	/* (non-Javadoc)
	 * @see com.rus.alm.tools.teamsyncer.jazz.IJazzProjectArea#GetJazzRoleByLabel(java.lang.String)
	 */
	@Override
	public final IJazzRole GetJazzRoleByLabel(String role)
	{
		for (IJazzRole jr : this.JazzProjectRoles())
		{
			if (jr.getLabel().equals(role))
			{
				return jr;
			}
		}
		return null;
	}
}