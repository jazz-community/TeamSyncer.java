package com.rus.alm.tools.teamsyncer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;

public class LdapController {

	private DirContext ctx;

	public LdapController() throws NamingException {

		System.setProperty("com.sun.jndi.ldap.object.disableEndpointIdentification","True");
        String userName = "<LDAP access user>";
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        // user-specific
        env.put(Context.PROVIDER_URL, "ldaps://dcs-mu.rsint.net");
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        // user-specific
        env.put(Context.SECURITY_PRINCIPAL, new String("RSINT" + "\\" + userName));
        env.put(Context.SECURITY_CREDENTIALS, "<password>");
		this.ctx = new InitialDirContext(env);
	}

	public LdapController(String userName, String userPwd) throws NamingException {

		System.setProperty("com.sun.jndi.ldap.object.disableEndpointIdentification","True");
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        // user-specific
        env.put(Context.PROVIDER_URL, "ldaps://dcs-mu.rsint.net");
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, new String("RSINT" + "\\" + userName));
        env.put(Context.SECURITY_CREDENTIALS, userPwd);
		this.ctx = new InitialDirContext(env);
	}

//	public List<String> getArtifactoryManagerGroups() throws NamingException {
//
//        SearchControls controls = new SearchControls();
//        controls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
//        NamingEnumeration<?> results = ctx.search("OU=ROLES,OU=SWDEV,OU=MGMT,dc=rsint,dc=net", "(&(objectCategory=group)(cn=artifactory*_manager))", controls);
//        List<String> groups = new ArrayList<String>();
//        while (results.hasMore()) {
//            SearchResult searchResult = (SearchResult) results.next();
//            Attributes attributes = searchResult.getAttributes();
//            Attribute attr = attributes.get("cn");
//            String cn = (String) attr.get();
//            groups.add(cn.toLowerCase());
//        }
//        return groups;
//	}

	public boolean groupExists(String groupName, String searchScope) throws NamingException {
        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
        NamingEnumeration<?> results = ctx.search(searchScope, "(&(objectCategory=group)(cn=" + groupName + "))", controls);
		return results.hasMore();
	}

	public Map<String,String> getGroupMembers(String groupName, String searchScope) throws NamingException {

		Map<String,String> memList = new HashMap<String, String>();
        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.OBJECT_SCOPE);
        controls.setReturningAttributes(new String[] {"member"});
        NamingEnumeration<?> results = ctx.search("CN=" + groupName + "," + searchScope, "(objectClass=*)", controls);
        while (results.hasMore()) {
            SearchResult searchResult = (SearchResult) results.next();
            Attributes attributes = searchResult.getAttributes();
            Attribute attr = attributes.get("member");
            if (attr != null) {
            	NamingEnumeration<?> attrEnum = attr.getAll();
            	while (attrEnum.hasMore()) {
            		String dnName = attrEnum.next().toString();
            		String simpleName = dnName.split(",")[0].substring(3);
            		// System.out.println(simpleName);
           			memList.put(simpleName, dnName);
            	}
            }
        }
		return memList;
	}

	public ArrayList<String> getGroupMemberList(String groupName, String searchScope) throws NamingException {

		ArrayList<String> memList = new ArrayList<String>();
        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        controls.setReturningAttributes(new String[] {"member"});
        NamingEnumeration<?> results = ctx.search(searchScope, "(&(objectClass=group)(cn=" + groupName + "))", controls);
        try {
			while (results.hasMore()) {
			    SearchResult searchResult = (SearchResult) results.next();
			    Attributes attributes = searchResult.getAttributes();
			    Attribute attr = attributes.get("member");
			    if (attr != null) {
			    	NamingEnumeration<?> attrEnum = attr.getAll();
			    	while (attrEnum.hasMore()) {
			    		String dnName = attrEnum.next().toString();
			    		String simpleName = dnName.split(",")[0].substring(3);
			    		// System.out.println(simpleName);
			   			memList.add(simpleName);
			    	}
			    }
			}
		}
        catch (javax.naming.PartialResultException ex) {
        }
        results.close();
		return memList;
	}

	public Map<String,String> getGroupMembersExt(String groupName, String searchScope) throws NamingException {

		Map<String,String> memList = new HashMap<String, String>();
        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.OBJECT_SCOPE);

        int rangeStep = 1000;
        int rangeLow = 0;
        int rangeHigh = rangeLow + (rangeStep - 1);
        boolean lastQuery = false;
        boolean quitLoop = false;

        do
        {
            String attributeWithRange;
            if(!lastQuery)
            {
                attributeWithRange = String.format("member;range=%d-%d", rangeLow, rangeHigh);
            }
            else
            {
                attributeWithRange = String.format("member;range=%d-*", rangeLow);
            }
            controls.setReturningAttributes(new String[] { attributeWithRange });
            NamingEnumeration<?> results = ctx.search("CN=" + groupName + "," + searchScope, "(objectClass=*)", controls);
            if (results.hasMore()) {
                SearchResult searchResult = (SearchResult) results.next();
                Attributes attributes = searchResult.getAttributes();
                Attribute attr = attributes.get(attributeWithRange);
                if (attr != null) {
                	NamingEnumeration<?> attrEnum = attr.getAll();
                	while (attrEnum.hasMore()) {
                		String dnName = attrEnum.next().toString();
                		String simpleName = dnName.split(",")[0].substring(3);
                		// System.out.println(simpleName);
               			memList.put(simpleName, dnName);
                	}
                	if(lastQuery)
                	{
                		quitLoop = true;
                	}
                }
                else
                {
                	if (lastQuery == false)
                	{
                		lastQuery = true;
                	}
                	else
                	{
                		quitLoop = true;
                	}
                }
                if (!lastQuery)
                {
                	rangeLow = rangeHigh + 1;
                	rangeHigh = rangeLow + (rangeStep - 1);
                }
            }
        }
        while(!quitLoop);
		return memList;
	}

	public ArrayList<String> getGroupMemberListExt(String groupName, String searchScope) throws NamingException {

		ArrayList<String> memList = new ArrayList<String>();
        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        int rangeStep = 1000;
        int rangeLow = 0;
        int rangeHigh = rangeLow + (rangeStep - 1);
        boolean lastQuery = false;
        boolean quitLoop = false;

        try {
			do
			{
			    String attributeWithRange;
			    if(!lastQuery)
			    {
			        attributeWithRange = String.format("member;range=%d-%d", rangeLow, rangeHigh);
			    }
			    else
			    {
			        attributeWithRange = String.format("member;range=%d-*", rangeLow);
			    }
			    controls.setReturningAttributes(new String[] { attributeWithRange });
			    NamingEnumeration<?> results = ctx.search(searchScope, "(&(objectClass=group)(cn=" + groupName + "))", controls);
			    if (results.hasMore()) {
			        SearchResult searchResult = (SearchResult) results.next();
			        Attributes attributes = searchResult.getAttributes();
			        Attribute attr = attributes.get(attributeWithRange);
			        if (attr != null) {
			        	NamingEnumeration<?> attrEnum = attr.getAll();
			        	while (attrEnum.hasMore()) {
			        		String dnName = attrEnum.next().toString();
			        		String simpleName = dnName.split(",")[0].substring(3);
			        		// System.out.println(simpleName);
			       			memList.add(simpleName);
			        	}
			        	if(lastQuery)
			        	{
			        		quitLoop = true;
			        	}
			        }
			        else
			        {
			        	if (lastQuery == false)
			        	{
			        		lastQuery = true;
			        	}
			        	else
			        	{
			        		quitLoop = true;
			        	}
			        }
			        if (!lastQuery)
			        {
			        	rangeLow = rangeHigh + 1;
			        	rangeHigh = rangeLow + (rangeStep - 1);
			        }
			    }
			}
			while(!quitLoop);
		}
        catch (javax.naming.PartialResultException ex) {
		}
		return memList;
	}

	public void addGroupMember (String groupName, String searchScope, String userDn) throws NamingException {
		BasicAttribute member = new BasicAttribute("member", userDn);
		Attributes atts = new BasicAttributes();
		atts.put(member);
		ctx.modifyAttributes("CN=" + groupName + "," + searchScope, LdapContext.ADD_ATTRIBUTE, atts);
	}

	public void addGroupMember (String groupDN, String userDn) throws NamingException {
		BasicAttribute member = new BasicAttribute("member", userDn);
		Attributes atts = new BasicAttributes();
		atts.put(member);
		ctx.modifyAttributes(groupDN, LdapContext.ADD_ATTRIBUTE, atts);
	}

	public void removeGroupMember (String groupName, String searchScope, String userDn) throws NamingException {
		BasicAttribute member = new BasicAttribute("member", userDn);
		Attributes atts = new BasicAttributes();
		atts.put(member);
		ctx.modifyAttributes("CN=" + groupName + "," + searchScope, LdapContext.REMOVE_ATTRIBUTE, atts);
	}

	public void removeGroupMember (String groupDN, String userDn) throws NamingException {
		BasicAttribute member = new BasicAttribute("member", userDn);
		Attributes atts = new BasicAttributes();
		atts.put(member);
		ctx.modifyAttributes(groupDN, LdapContext.REMOVE_ATTRIBUTE, atts);
	}

	public String getGroupDN(String groupName, String searchScope) throws NamingException {
        String groupDN = "";
        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        NamingEnumeration<?> results = ctx.search(searchScope, "(&(objectClass=group)(cn=" + groupName + "))", controls);
        try {
        	while (results.hasMore()) {
        		SearchResult searchResult = (SearchResult) results.next();
        		Attributes attributes = searchResult.getAttributes();
        		Attribute attr = attributes.get("distinguishedName");
        		groupDN = (String) attr.get();
        	}
        }
        catch (javax.naming.PartialResultException ex) {
        }
		return groupDN;
	}

	public String getUserDN(String userName, String searchScope) throws NamingException {
        String userDN = "";
        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        NamingEnumeration<?> results = ctx.search(searchScope, "(&(objectClass=user)(!(objectClass=computer))(cn=" + userName + "))", controls);
        try {
        	while (results.hasMore()) {
        		SearchResult searchResult = (SearchResult) results.next();
        		Attributes attributes = searchResult.getAttributes();
        		Attribute attr = attributes.get("distinguishedName");
        		userDN = (String) attr.get();
        	}
        }
        catch (javax.naming.PartialResultException ex) {
        }
		return userDN;
	}

	public String GUIDToOctetString(String guidStr) {
		StringBuilder octet = new StringBuilder();
		octet.append("\\");
		octet.append(guidStr.substring(7,9)).append("\\");
		octet.append(guidStr.substring(5,7)).append("\\");
		octet.append(guidStr.substring(3,5)).append("\\");
		octet.append(guidStr.substring(1,3)).append("\\");

		octet.append(guidStr.substring(12,14)).append("\\");
		octet.append(guidStr.substring(10,12)).append("\\");

		octet.append(guidStr.substring(17,19)).append("\\");
		octet.append(guidStr.substring(15,17)).append("\\");

		octet.append(guidStr.substring(20,22)).append("\\");
		octet.append(guidStr.substring(22,24)).append("\\");

		octet.append(guidStr.substring(25,27)).append("\\");
		octet.append(guidStr.substring(27,29)).append("\\");
		octet.append(guidStr.substring(29,31)).append("\\");
		octet.append(guidStr.substring(31,33)).append("\\");
		octet.append(guidStr.substring(33,35)).append("\\");
		octet.append(guidStr.substring(35,37));

		return octet.toString();
	}

	public String getGroupDNbyGUID(String groupGUID, String searchScope) throws NamingException {
        String groupDN = "";
        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        NamingEnumeration<?> results = ctx.search(searchScope, "(&(objectClass=group)(objectGUID=" + GUIDToOctetString(groupGUID) + "))", controls);
        try {
        	while (results.hasMore()) {
        		SearchResult searchResult = (SearchResult) results.next();
        		Attributes attributes = searchResult.getAttributes();
        		Attribute attr = attributes.get("distinguishedName");
        		groupDN = (String) attr.get();
        	}
        }
        catch (javax.naming.PartialResultException ex) {
        }
		return groupDN;
	}

	public String getExactAccountName (String userName) throws NamingException {
        String sAMAccountName = "";
        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        NamingEnumeration<?> results = ctx.search("dc=rsint,dc=net", "(&(objectClass=user)(!(objectClass=computer))(|(cn=" + userName + ")(sAMAccountName=" + userName + ")))", controls);
        try {
        	while (results.hasMore()) {
        		SearchResult searchResult = (SearchResult) results.next();
        		Attributes attributes = searchResult.getAttributes();
        		Attribute attr = attributes.get("cn");
        		sAMAccountName = (String) attr.get();
        	}
        }
        catch (javax.naming.PartialResultException ex) {
        }
		return sAMAccountName;
	}

	public String getFullName (String userName) throws NamingException {
        String fullName = "";
        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        NamingEnumeration<?> results = ctx.search("dc=rsint,dc=net", "(&(objectClass=user)(!(objectClass=computer))(|(cn=" + userName + ")(sAMAccountName=" + userName + ")))", controls);
        try {
        	while (results.hasMore()) {
        		SearchResult searchResult = (SearchResult) results.next();
        		Attributes attributes = searchResult.getAttributes();
        		Attribute attr = attributes.get("displayName");
        		fullName = (String) attr.get();
        	}
        }
        catch (javax.naming.PartialResultException ex) {
        }
		return fullName;
	}

	public String getEmailAddress (String userName) throws NamingException {
        String emailAddr = "";
        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        NamingEnumeration<?> results = ctx.search("dc=rsint,dc=net", "(&(objectClass=user)(!(objectClass=computer))(|(cn=" + userName + ")(sAMAccountName=" + userName + ")))", controls);
        try {
        	while (results.hasMore()) {
        		SearchResult searchResult = (SearchResult) results.next();
        		Attributes attributes = searchResult.getAttributes();
        		Attribute attr = attributes.get("mail");
        		if (attr != null)
        			emailAddr = (String) attr.get();
        	}
        }
        catch (javax.naming.PartialResultException ex) {
        }
		return emailAddr;
	}

	public void close() throws NamingException {
		this.ctx.close();
	}
}
