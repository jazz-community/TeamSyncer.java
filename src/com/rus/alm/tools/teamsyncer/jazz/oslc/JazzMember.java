package com.rus.alm.tools.teamsyncer.jazz.oslc;

import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.rus.alm.tools.teamsyncer.jazz.IJazzMember;
import com.rus.alm.tools.teamsyncer.jazz.IJazzRole;
import com.rus.alm.tools.teamsyncer.jazz.IJazzUser;

public class JazzMember implements IJazzMember
{
	private String _url;
	public final String getURL()
	{
		return _url;
	}
	private JazzUser _user;
	/* (non-Javadoc)
	 * @see com.rus.alm.tools.teamsyncer.jazz.IJazzMember#getJazzMemberUser()
	 */
	@Override
	public final IJazzUser getJazzMemberUser()
	{
		return _user;
	}

	private boolean PostIsPending;
	public final boolean getPostIsPending()
	{
		return PostIsPending;
	}
	public final void setPostIsPending(boolean value)
	{
		PostIsPending = value;
	}

	private ArrayList<IJazzRole> _roles;
	private JazzServerController _jServ = null;
	private JazzNamespaceContext _jnsCtxt = null;
	private Jazz _owner = null;

	public JazzMember(Jazz owner, String url)
	{
		_url = url;
		_owner = owner;
		_jServ = owner.GetJazzServerCtrl();
		_jnsCtxt = owner.GetJazzNamespaceContext();
		setPostIsPending(false);

		Document xml = _jServ.getXml(url);

		XPath xpath = XPathFactory.newInstance().newXPath();
		xpath.setNamespaceContext(_jnsCtxt);
		
		try {
			String currXPath = "/" + JazzConstants.jazzProcNum + ":member/" + JazzConstants.jazzProcNum + ":user-url";
			Node node = (Node) xpath.evaluate(currXPath, xml, XPathConstants.NODE);
			_user = owner.GetJazzUserRepository().GetOrAddUser(node.getTextContent(), owner);
			
			currXPath = "/" + JazzConstants.jazzProcNum + ":member/" + JazzConstants.jazzProcNum + ":role-assignments/" + JazzConstants.jazzProcNum + ":role-assignment";
			NodeList nList = (NodeList) xpath.evaluate(currXPath, xml, XPathConstants.NODESET);
			if (nList.getLength() > 0) {
				
				_roles = new ArrayList<IJazzRole>();

				for (int i = 0; i < nList.getLength(); i++) {
					currXPath = "./" + JazzConstants.jazzProcNum + ":role-url";
					Node n = (Node) xpath.evaluate(currXPath, nList.item(i), XPathConstants.NODE);
					_roles.add(owner.GetJazzRoleCache().GetOrAddRole(n.getTextContent()));
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public JazzMember(String userName, Jazz owner)
	{
		_owner = owner;
		_jServ = _owner.GetJazzServerCtrl();
		_jnsCtxt = _owner.GetJazzNamespaceContext();
		_user = _owner.GetJazzUserRepository().GetUserByName(userName);
		if (_user == null)
		{
			_user = _owner.GetJazzUserRepository().GetOrAddUser(_owner.getUserRepoURL() + "/" + userName, _owner);
		}
	}

	public final void SetMemberURL(String memberURL)
	{
		_url = memberURL;
	}

	/* (non-Javadoc)
	 * @see com.rus.alm.tools.teamsyncer.jazz.IJazzMember#JazzMemberRoles()
	 */
	@Override
	public final Iterable<IJazzRole> JazzMemberRoles()
	{
		return _roles;
	}

	/* (non-Javadoc)
	 * @see com.rus.alm.tools.teamsyncer.jazz.IJazzMember#JazzMemberHasRoleWithLabel(java.lang.String)
	 */
	@Override
	public final boolean JazzMemberHasRoleWithLabel(String role)
	{
		if (_roles != null)
		{
			for (int i = 0; i < _roles.size(); i++)
			{
				if (role.equalsIgnoreCase(_roles.get(i).getLabel()))
				{
					return true;
				}
			}
		}
		return false;
	}

	public final boolean JazzMemberHasDefaultRoleOnly()
	{
		if (_roles == null)
		{
			return true;
		}
		else if (_roles.size() < 2)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see com.rus.alm.tools.teamsyncer.jazz.IJazzMember#AssignRole(com.rus.alm.tools.teamsyncer.jazz.JazzRole)
	 */
	@Override
	public final void AssignRole(IJazzRole jazzRole)
	{
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		    Document px = docBuilder.newDocument();

			Element rootNode = px.createElementNS(JazzConstants.jazzProcNsUrl06, JazzConstants.jazzProcNum + ":role-assignments");
			px.appendChild(rootNode);

			Element roleAs = px.createElementNS(JazzConstants.jazzProcNsUrl06, JazzConstants.jazzProcNum + ":role-assignment");
			rootNode.appendChild(roleAs);

			Text roleUrlCont = px.createTextNode(((JazzRole) jazzRole).getURL());
			Element roleUrl = px.createElementNS(JazzConstants.jazzProcNsUrl06, JazzConstants.jazzProcNum + ":role-url");
			roleAs.appendChild(roleUrl);
			roleUrl.appendChild(roleUrlCont);

			_jServ.postXml(_url + "/role-assignments", px);

			_roles.add(jazzRole);
			
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see com.rus.alm.tools.teamsyncer.jazz.IJazzMember#UnassignRole(java.lang.String)
	 */
	@Override
	public final void UnassignRole(String role)
	{
		if (_roles != null)
		{
			for (int i = 0; i < _roles.size(); i++)
			{
				if (role.equalsIgnoreCase(_roles.get(i).getLabel()))
				{
					String roleAssignmentURL = _url + "/role-assignments/" + ((JazzRole) _roles.get(i)).getURL().substring(((JazzRole) _roles.get(i)).getURL().lastIndexOf('/') + 1);
					if (_jServ.delete(roleAssignmentURL) == 200)
					{
						_roles.remove(i);
					}
					return;
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.rus.alm.tools.teamsyncer.jazz.IJazzMember#AddRole(com.rus.alm.tools.teamsyncer.jazz.JazzRole)
	 */
	@Override
	public final void AddRole(IJazzRole jazzRole)
	{
		if (_roles == null)
		{
			_roles = new ArrayList<IJazzRole>();
		}
		if (!JazzMemberHasRoleWithLabel(jazzRole.getLabel()))
		{
			_roles.add(jazzRole);
		}
	}

	/* (non-Javadoc)
	 * @see com.rus.alm.tools.teamsyncer.jazz.IJazzMember#SetRoles()
	 */
	@Override
	public final void SetRoles()
	{
		if ((_roles != null) && (!_roles.isEmpty()))
		{
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			try {
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			    Document px = docBuilder.newDocument();

				Element rootNode = px.createElementNS(JazzConstants.jazzProcNsUrl06, JazzConstants.jazzProcNum + ":role-assignments");
				px.appendChild(rootNode);

				for (IJazzRole jr : _roles)
				{
					Element roleAs = px.createElementNS(JazzConstants.jazzProcNsUrl06, JazzConstants.jazzProcNum + ":role-assignment");
					rootNode.appendChild(roleAs);

					Text roleUrlCont = px.createTextNode(((JazzRole) jr).getURL());
					Element roleUrl = px.createElementNS(JazzConstants.jazzProcNsUrl06, JazzConstants.jazzProcNum + ":role-url");
					roleAs.appendChild(roleUrl);
					roleUrl.appendChild(roleUrlCont);
				}
				
				_jServ.putXml(_url + "/role-assignments", px);

			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if (_url != null)
		{
			_jServ.delete(_url + "/role-assignments");
		}
	}
}