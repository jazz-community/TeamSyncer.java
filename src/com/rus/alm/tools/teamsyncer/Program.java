package com.rus.alm.tools.teamsyncer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;

import javax.naming.NamingException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.rus.alm.tools.teamsyncer.artifactory.ArtifactoryGroupHandler;
import com.rus.alm.tools.teamsyncer.artifactory.ArtifactoryGroupHandlerController;
import com.rus.alm.tools.teamsyncer.gitlab.GitLabAccessObject;
import com.rus.alm.tools.teamsyncer.gitlab.GitLabAccessObject.AccessObjectNotFoundException;
import com.rus.alm.tools.teamsyncer.jazz.IJazz;
import com.rus.alm.tools.teamsyncer.jazz.IJazzMember;
import com.rus.alm.tools.teamsyncer.jazz.IJazzProjectArea;
import com.rus.alm.tools.teamsyncer.jazz.IJazzRole;
import com.rus.alm.tools.teamsyncer.jazz.IJazzTeamArea;

public class Program
{
	private static Options rtOptions;
	private static Scanner scanner = new Scanner(System.in);

	@SuppressWarnings("serial")
	public static class ArtifactoryAccessException extends RuntimeException
	{
		public ArtifactoryAccessException()
		{
		}

		public ArtifactoryAccessException(String message)
		{
			super(message);
		}

		public ArtifactoryAccessException(String message, RuntimeException inner)
		{
			super(message, inner);
		}
	}

	@SuppressWarnings("serial")
	public static class NotFoundException extends RuntimeException
	{
		public NotFoundException()
		{
		}

		public NotFoundException(String message)
		{
			super(message);
		}

		public NotFoundException(String message, RuntimeException inner)
		{
			super(message, inner);
		}
	}

	private static String getUsername()
	{
		String usr = "";
		System.out.print("Username: ");
		try {
			usr = System.console().readLine();
		} catch (Exception e) {
			usr = scanner.nextLine();
		}
		return usr;
	}

	private static String getPassword()
	{
		String pwd = "";
		System.out.print("Password: ");
		try {
			pwd = System.console().readPassword().toString();
		} catch (Exception e) {
			pwd = scanner.nextLine();
		}
		return pwd;
	}

	private static String getAttributeOrEmpty(Node n, String attrName) {

		try {
			return n.getAttributes().getNamedItem(attrName).getNodeValue();
		} catch (Exception e) {
			return "";
		}
	}

	public static void main(String[] args) throws IOException
	{
		final String idstring = "@(#)RTC TeamSyncer 4.1 2018/10/12 R&S 6DSE";
		boolean write_id = true;
		int exitCode = 0;
		PrintWriter s_log = null;
		boolean additional_debug_log = false;

		try
		{
			rtOptions = ArgumentsHandling.getOptionsFromArguments(args);
			if (rtOptions.getopt_log())
			{
				String p = (new java.io.File(rtOptions.getlogdir())).getAbsolutePath();
				if (!(new java.io.File(p)).isDirectory())
				{
					(new java.io.File(p)).mkdirs();
				}
				//String jsrv = rtOptions.getjazzRepository().split(new String[] {"//", "."}, StringSplitOptions.RemoveEmptyEntries)[1];
				// He 2018-09-18: jazz repository as command line parameter no longer mandatory
				String defaultUrl = rtOptions.getjazzRepository();
				String jsrv;
				if (defaultUrl == null)
					jsrv = "";
				else
					jsrv = "_" + rtOptions.getjazzRepository().split("[/\\.]")[2];
				String l = "TeamSyncer_log" + jsrv + "_" + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm")) + ".txt";
				try {
					s_log = new PrintWriter(p + "\\" + l);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				s_log.println(idstring);
				s_log.println();
				if (defaultUrl == null)
					s_log.println("No default repository url for this syncer run specified, assuming 'https://jazz.rsint.net'");
				else
					s_log.println("Repository url for this syncer run: '" + defaultUrl + "'");
				s_log.println();
			}
			if (rtOptions.getopt_verbose())
			{
				System.out.println(idstring);
				System.out.println();
				//System.out.println("Repository url for this syncer run: '" + rtOptions.getjazzRepository() + "'");
				//System.out.println();
				write_id = false;
			}

			if (!rtOptions.getopt_check_map())
			{
				if ((rtOptions.getuser() == null) || (rtOptions.getpasswd() == null))
				{
					if (write_id == true)
					{
						System.out.println(idstring);
						System.out.println();
						write_id = false;
					}
					if (rtOptions.getuser() == null)
					{
						rtOptions.setuser(getUsername());
					}
					if (rtOptions.getpasswd() == null)
					{
						rtOptions.setpasswd(getPassword());
					}
					System.out.println();
				}
			}

			File mappingFile = new File(rtOptions.getmappingfile());
			if (!mappingFile.exists()) {
				throw new NotFoundException("Could not find mapping file '" + rtOptions.getmappingfile() + "'.");
			}

			if (rtOptions.getopt_check_map())
			{
				if (write_id == true)
				{
					System.out.println(idstring);
					System.out.println();
					write_id = false;
				}
				System.out.printf("Checking mapping file '%1$s' for appropriate targetArea succession.\n" + System.lineSeparator(), rtOptions.getmappingfile());
				if (rtOptions.getopt_log())
				{
					s_log.println("Checking mapping file '" + rtOptions.getmappingfile() + "' for appropriate targetArea succession.");
				}
				System.exit(CheckSyncerMappings.CheckSyncerMapping(rtOptions.getmappingfile(), s_log));
			}

		    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		    Document doc = docBuilder.parse (new File(rtOptions.getmappingfile()));
	        XPathFactory xPathFactory = XPathFactory.newInstance();
	        XPath xPathBuilder = xPathFactory.newXPath();
	        XPathExpression xPath = xPathBuilder.compile("/mapping/*");
	        NodeList nodes = (NodeList) xPath.evaluate(doc, XPathConstants.NODESET);

			//Jazz.repositoryType jtype;
			//if (rtOptions.opt_rm)
			//    jtype = Jazz.repositoryType.rm;
			//else
			//    jtype = Jazz.repositoryType.ccm;

			//Jazz jazz = new Jazz(rtOptions.jazzRepository, jtype, rtOptions.user, rtOptions.passwd);

			RepoManager theRM = new RepoManager(rtOptions.getuser(), rtOptions.getpasswd());
			String defaultJazzUrl = rtOptions.getjazzRepository();
			// He 2018-09-18: jazz repository as command line parameter no longer mandatory
			//  ... but we must assume s.th. to avoid large changes of the inner workings
			//  --> jazz.rsint.net assumed (probably not used at all)
			if (defaultJazzUrl == null)
				defaultJazzUrl = "https://jazz.rsint.net"; // user-specific
			if (rtOptions.getopt_rm())
			{
				defaultJazzUrl += "/rm";
			}
			else
			{
				defaultJazzUrl += "/ccm";
			}
			IJazz jazz = theRM.GetRepository(defaultJazzUrl);

			for (int tn = 0; tn < nodes.getLength(); tn++)
			{
				if (nodes.item(tn).getNodeName().equals("targetArea"))
				{
                    write_id = processTargetArea(idstring, write_id, s_log,
							additional_debug_log, nodes, theRM, defaultJazzUrl,
							jazz, tn);
				}
				else if (nodes.item(tn).getNodeName().equals("targetLDAPGroup"))
				{
					write_id = processTargetLDAPGroup(idstring, write_id,
							s_log, additional_debug_log, nodes, theRM,
							defaultJazzUrl, jazz, tn);
				}
				else if (nodes.item(tn).getNodeName().equals("targetGitLab"))
				{
					write_id = processTargetGitLab(idstring, write_id,
							s_log, additional_debug_log, nodes, theRM,
							defaultJazzUrl, jazz, tn);
				}
				else if (nodes.item(tn).getNodeName().equals("targetArtifactory"))
				{
					write_id = processTargetArtifactory(idstring, write_id,
							s_log, additional_debug_log, nodes, theRM,
							defaultJazzUrl, jazz, tn);
				}
			}
			jazz.close();
		}
//		catch (RuS.Jazz.AuthenticationException ex)
//		{
//			if (write_id && !rtOptions.getopt_verbose())
//			{
//				System.out.println(idstring);
//				System.out.println();
//			}
//			System.out.printf("Exception occurred, message: %1$s\n" + System.lineSeparator(), ex.getMessage());
//			if (rtOptions.getopt_log())
//			{
//				s_log.WriteLine("Exception occurred, message: {0}\n", ex.getMessage());
//			}
//			exitCode = 3;
//		}
		catch (NotFoundException ex)
		{
			if (write_id && !rtOptions.getopt_verbose())
			{
				System.out.println(idstring);
				System.out.println();
			}
			System.out.printf("Exception occurred, message: %1$s\n" + System.lineSeparator(), ex.getMessage());
			if (rtOptions.getopt_log())
			{
				s_log.printf("Exception occurred, message:  %1$s\n" + System.lineSeparator(), ex.getMessage());
			}
			exitCode = 4;
		}
		catch (NoArgumentsException e7)
		{
			System.out.println(idstring);
			System.out.println();
			System.out.println(ArgumentsHandling.getUsageMessage("TeamSyncer"));
			exitCode = 1;
		}
		catch (ArgumentsParseException ex)
		{
			System.out.println(idstring);
			System.out.println();
			System.out.println(ex.getMessage());
			System.out.println();
			System.out.println(ArgumentsHandling.getUsageMessage("TeamSyncer"));
			exitCode = 1;
		}
		catch (Exception ex)
		{
			if (write_id && !rtOptions.getopt_verbose())
			{
				System.out.println(idstring);
				System.out.println();
			}
			System.out.printf("Exception occurred, message: %1$s\n" + System.lineSeparator(), ex.getMessage());
			if (s_log != null)
			{
				s_log.printf("Exception occurred, message: %1$s\n" + System.lineSeparator(), ex.getMessage());
			}
			exitCode = 9;
		}
		finally
		{
			if (s_log != null)
			{
				s_log.println();
				s_log.println("TeamSyncer finished.");
				s_log.close();
			}
			scanner.close();
		}
		if (exitCode == 0)
			return;
		else
			System.exit(exitCode);
	}

	private static boolean processTargetArtifactory(String idstring,
			boolean write_id, PrintWriter s_log, boolean additional_debug_log,
			NodeList nodes, RepoManager theRM, String defaultJazzUrl,
			IJazz jazz, int tn) throws XPathExpressionException, NamingException {

		String targetGroup = getAttributeOrEmpty(nodes.item(tn), "name");
		if (targetGroup.equals(""))
		{
			if (rtOptions.getopt_verbose())
			{
				System.out.println("No name attribute in targetArtifactory, skipping this.");
			}
			if (rtOptions.getopt_log())
			{
				s_log.write("No name attribute in targetArtifactory, skipping this." + System.lineSeparator());
			}
			return write_id;
		}

		String targetUrl = getAttributeOrEmpty(nodes.item(tn), "url");
		if (targetUrl.equals(""))
		{
			if (rtOptions.getopt_verbose())
			{
				System.out.println("No url attribute in targetArtifactory '" + targetGroup + "', skipping this.");
			}
			if (rtOptions.getopt_log())
			{
				s_log.write("No url attribute in targetArtifactory '" + targetGroup + "', skipping this." + System.lineSeparator());
			}
			return write_id;
		}
		if (rtOptions.getopt_log() && additional_debug_log)
		{
			s_log.write("targetArtifactory:" + System.lineSeparator());
			s_log.write("  url  = '" + targetUrl + "'" + System.lineSeparator());
			s_log.write("  name = '" + targetGroup + "'" + System.lineSeparator());
		}
		boolean mergeMode = (getAttributeOrEmpty(nodes.item(tn), "mode").equals("merge"));

		ArtifactoryGroupHandler agh = ArtifactoryGroupHandlerController.getInstance(targetUrl);
		if (!agh.isGroupExisting(targetGroup)) {
			if (rtOptions.getopt_verbose())
			{
				System.out.println("Artifactory group '" + targetGroup + "' not found.");
			}
			if (rtOptions.getopt_log())
			{
				s_log.write("Artifactory group '" + targetGroup + "' not found." + System.lineSeparator());
			}
			return write_id;
		}

		MemberRoleAssignments target_as_is = new MemberRoleAssignments();
		ArrayList<String> targetMemberList = agh.getGroupMemberList(targetGroup);
		if (rtOptions.getopt_log() && additional_debug_log)
		{
			s_log.write(" target artifactory group contains " + targetMemberList.size() + " members" + System.lineSeparator());
		}

		for (String member : targetMemberList)
		{
			target_as_is.addMember(member, false);
		}

		MemberRoleAssignments target_as_should_be;
		if (mergeMode)
		{
			target_as_should_be = new MemberRoleAssignments(target_as_is);
		}
		else
		{
			target_as_should_be = new MemberRoleAssignments();
		}

		XPath xpath = XPathFactory.newInstance().newXPath();
		NodeList s_nodes = (NodeList) xpath.evaluate("./sourceArea", nodes.item(tn), XPathConstants.NODESET);
		for (int sn = 0; sn < s_nodes.getLength(); sn++)
		{
			processReadSourceArea(s_log, additional_debug_log, theRM,
					defaultJazzUrl, jazz, xpath, null, 0,
					null, 0, target_as_should_be, s_nodes, sn, false);
		}

		s_nodes = (NodeList) xpath.evaluate("./sourceLDAPGroup", nodes.item(tn), XPathConstants.NODESET);
		for (int sn = 0; sn < s_nodes.getLength(); sn++)
		{
			processReadSourceLDAPGroup(s_log, additional_debug_log,
					xpath, null, 0, target_as_should_be, s_nodes, sn);
		}
		s_nodes = (NodeList) xpath.evaluate("./sourceGitLab", nodes.item(tn), XPathConstants.NODESET);
		for (int sn = 0; sn < s_nodes.getLength(); sn++)
		{
			processReadSourceGitLab(s_log, additional_debug_log,
					xpath, null, 0, null, 0, target_as_should_be, s_nodes, sn, false);
		}
		s_nodes = (NodeList) xpath.evaluate("./sourceArtifactory", nodes.item(tn), XPathConstants.NODESET);
		for (int sn = 0; sn < s_nodes.getLength(); sn++)
		{
			processReadSourceArtifactory(s_log, additional_debug_log,
					xpath, null, 0, null, 0, target_as_should_be, s_nodes, sn, false);
		}

		write_id = performChangesTargetArtifactory(idstring,
				write_id, s_log, targetGroup, agh, target_as_is, target_as_should_be);

		return write_id;
	}

	private static void processReadSourceArtifactory(PrintWriter s_log,
			boolean additional_debug_log, XPath xpath,
			ArrayList<String> assign, int t_assigned,
			ArrayList<String> excludeAssign, int t_excluded,
			MemberRoleAssignments target_as_should_be, NodeList s_nodes,
			int sn, boolean targetHasRoles) throws XPathExpressionException {


		String sourceGroup = getAttributeOrEmpty(s_nodes.item(sn), "name");
		if (sourceGroup.equals(""))
		{
			if (rtOptions.getopt_verbose())
			{
				System.out.println("No name attribute in sourceArtifactory, skipping this.");
			}
			if (rtOptions.getopt_log())
			{
				s_log.write("No name attribute in sourceArtifactory, skipping this." + System.lineSeparator());
			}
			return;
		}

		String sourceUrl = getAttributeOrEmpty(s_nodes.item(sn), "url");
		if (sourceUrl.equals(""))
		{
			if (rtOptions.getopt_verbose())
			{
				System.out.println("No url attribute in sourceArtifactory '" + sourceGroup + "', skipping this.");
			}
			if (rtOptions.getopt_log())
			{
				s_log.write("No url attribute in sourceArtifactory '" + sourceGroup + "', skipping this." + System.lineSeparator());
			}
			return;
		}
		if (rtOptions.getopt_log() && additional_debug_log)
		{
			s_log.write("sourceArtifactory:" + System.lineSeparator());
			s_log.write("  url  = '" + sourceUrl + "'" + System.lineSeparator());
			s_log.write("  name = '" + sourceGroup + "'" + System.lineSeparator());
		}
		NodeList sel_s_nodes = (NodeList) xpath.evaluate("./select", s_nodes.item(sn), XPathConstants.NODESET);
		ArrayList<String> select = new ArrayList<String>();
		for (int ssn = 0; ssn < sel_s_nodes.getLength(); ssn++)
		{
			select.add(sel_s_nodes.item(ssn).getTextContent());
		}

		if (targetHasRoles) {
			// Set assigns back to contain only (global) targetArea assigns
			if (assign.size() > t_assigned)
			{
				assign = new ArrayList<String>(assign.subList(0, t_assigned));
			}
			NodeList ass_s_nodes = (NodeList) xpath.evaluate("./assign", s_nodes.item(sn), XPathConstants.NODESET);
			for (int asn = 0; asn < ass_s_nodes.getLength(); asn++)
			{
				assign.add(ass_s_nodes.item(asn).getTextContent());
			}

			// Set excludeAssigns back to contain only (global) targetArea excludeAssigns
			if (excludeAssign.size() > t_excluded)
			{
				excludeAssign = new ArrayList<String>(excludeAssign.subList(0, t_excluded));
			}
			NodeList excl_s_nodes = (NodeList) xpath.evaluate("./excludeAssign", s_nodes.item(sn), XPathConstants.NODESET);
			for (int esn = 0; esn < excl_s_nodes.getLength(); esn++)
			{
				excludeAssign.add(excl_s_nodes.item(esn).getTextContent());
			}
		}

		ArtifactoryGroupHandler agh = ArtifactoryGroupHandlerController.getInstance(sourceUrl);
		if (!agh.isGroupExisting(sourceGroup)) {
			if (rtOptions.getopt_verbose())
			{
				System.out.println("Artifactory group '" + sourceGroup + "' not found.");
			}
			if (rtOptions.getopt_log())
			{
				s_log.write("Artifactory group '" + sourceGroup + "' not found." + System.lineSeparator());
			}
			return;
		}

		MemberRoleAssignments source_as_is = new MemberRoleAssignments();
		ArrayList<String> sourceMemberList = agh.getGroupMemberList(sourceGroup);
		for (String member : sourceMemberList)
		{
			source_as_is.addMember(member, false);
		}

		int srcCount = 0;
		for (MemberRoleAssignment mra: source_as_is.AllMembers())
		{
			srcCount++;
			boolean is_included = false;
			if (select.isEmpty())
			{
				is_included = true;
			}
			else
			{
				for (String role : select)
				{
					if (mra.hasRole(role))
					{
						is_included = true;
						break;
					}
				}
			}

			if (is_included)
			{
				if (targetHasRoles) {
					if (assign.isEmpty())
					{
						for (String role: mra.AllRoles())
						{
							if (!excludeAssign.contains(role))
							{
								target_as_should_be.addMemberRole(mra.getMember(), mra.isArchived(), role);
							}
						}
					}
					else
					{
						for (String targetRole : assign)
						{
							target_as_should_be.addMemberRole(mra.getMember(), mra.isArchived(), targetRole);
						}
					}
				}
				else
				{
					target_as_should_be.addMember(mra.getMember(), mra.isArchived());
				}
			}
		}
		if (rtOptions.getopt_log() && additional_debug_log)
		{
			s_log.write(" source artifactory group contains " + srcCount + " members" + System.lineSeparator());
		}
	}

	private static boolean performChangesTargetArtifactory(String idstring,
			boolean write_id, PrintWriter s_log, String targetGroup,
			ArtifactoryGroupHandler agh, MemberRoleAssignments target_as_is,
			MemberRoleAssignments target_as_should_be) {

		//Perform required changes
		if (rtOptions.getopt_verbose())
		{
			System.out.printf("Processing target Artifactory group '%1$s'" + System.lineSeparator(), targetGroup);
		}
		if (rtOptions.getopt_log())
		{
			s_log.println("Processing target Artifactory group '" + targetGroup + "'");
		}

		target_as_should_be.removeArchivedMembers();
		ArrayList<String> deFactoList = new ArrayList<String>();
		for (MemberRoleAssignment mra: target_as_is.AllMembers()) {
			deFactoList.add(mra.getMember());
		}

		for (MemberRoleAssignment mra : target_as_should_be.AllMembers())
		{
			MemberRoleAssignment t_mra = target_as_is.getMember(mra.getMember());
			if (t_mra == null)
			{
				// member must be added
				try
				{
					String userAccountName = agh.getExactAccountName(mra.getMember());
					if (userAccountName.isEmpty())
						throw new NotFoundException("User with name '" + mra.getMember() + "' not found in Artifactory");
					else
					{
						if (rtOptions.getopt_verbose())
						{
							System.out.printf("  Adding member '%1$s'" + System.lineSeparator(), userAccountName);
						}
						if (rtOptions.getopt_log())
						{
							s_log.println("  Adding member '" + userAccountName + "'");
						}

						//String userDn = ac.GetObjectDistinguishedName(ADControl.objectClass.user, ADControl.returnType.distinguishedName, mra.getMember(), targetDomain);
						//String userDn = lctrl.getUserDN(mra.getMember(), searchScope);
						//ac.AddToGroup(userDn, targetGroup);

						//if (!agh.addGroupMember(targetGroup, userAccountName))
						//	throw new ArtifactoryAccessException();
						deFactoList.add(userAccountName);
					}
				}
				catch (NotFoundException ex)
				{
					if (write_id)
					{
						System.out.println(idstring);
						System.out.println();
						write_id = false;
					}
					//Console.WriteLine("Exception occurred, message: {0}\n", ex.Message);
					System.out.printf(ex.getMessage() + System.lineSeparator());
					System.out.printf("Failed to add member '%1$s'" + System.lineSeparator(), mra.getMember());
					if (rtOptions.getopt_log())
					{
						s_log.println(ex.getMessage());
						s_log.println("Failed to add member '" + mra.getMember() + "'");
					}
				}
				catch (RuntimeException ex)
				{
					if (write_id)
					{
						System.out.println(idstring);
						System.out.println();
						write_id = false;
					}
					//Console.WriteLine("Exception occurred, message: {0}\n", ex.Message);
					System.out.printf("Failed to add member '%1$s'" + System.lineSeparator(), mra.getMember());
					if (rtOptions.getopt_log())
					{
						s_log.println("Failed to add member '" + mra.getMember() + "'");
					}
				}
			}
		}

		for (MemberRoleAssignment mra : target_as_is.AllMembers())
		{
			MemberRoleAssignment t_mra = target_as_should_be.getMember(mra.getMember());
			if (t_mra == null)
			{
				// member must be deleted
				if (rtOptions.getopt_verbose())
				{
					System.out.printf("  Deleting member '%1$s'" + System.lineSeparator(), mra.getMember());
				}
				if (rtOptions.getopt_log())
				{
					s_log.println("  Deleting member '" + mra.getMember() + "'");
				}
				try
				{
					//String userDn = ac.GetObjectDistinguishedName(ADControl.objectClass.user, ADControl.returnType.distinguishedName, mra.getMember(), targetDomain);
					//String userDn = lctrl.getUserDN(mra.getMember(), searchScope);
					//ac.RemoveUserFromGroup(userDn, targetGroup);

					//if (!agh.removeGroupMember(targetGroup, mra.getMember()))
					//	throw new ArtifactoryAccessException();
					String userAccountName = agh.getExactAccountName(mra.getMember());
					if (!userAccountName.isEmpty())
						deFactoList.remove(userAccountName);
				}
				catch (RuntimeException ex)
				{
					if (write_id)
					{
						System.out.println(idstring);
						System.out.println();
						write_id = false;
					}
					//Console.WriteLine("Exception occurred, message: {0}\n", ex.Message);
					System.out.printf("Failed to delete member '%1$s' from Artifactory group '%2$s'" + System.lineSeparator(), mra.getMember(), targetGroup);
					if (rtOptions.getopt_log())
					{
						s_log.println("Failed to delete member '" + mra.getMember() + "' from Artifactory group '" + targetGroup + "'");
					}
				}
			}
		}
		if (!agh.setGroupMembers(targetGroup, deFactoList)) {

			if (write_id)
			{
				System.out.println(idstring);
				System.out.println();
				write_id = false;
			}
			//Console.WriteLine("Exception occurred, message: {0}\n", ex.Message);
			System.out.printf("Failed to set member list for Artifactory group '%2$s'" + System.lineSeparator(), targetGroup);
			if (rtOptions.getopt_log())
			{
				s_log.println("Failed to set member list for Artifactory group '" + targetGroup + "'");
			}
		}
		return write_id;
	}

	private static boolean processTargetGitLab(String idstring,
			boolean write_id, PrintWriter s_log, boolean additional_debug_log,
			NodeList nodes, RepoManager theRM, String defaultJazzUrl,
			IJazz jazz, int tn) throws XPathExpressionException, NamingException {

		String targetPath = getAttributeOrEmpty(nodes.item(tn), "name");
		if (targetPath.equals(""))
		{
			if (rtOptions.getopt_verbose())
			{
				System.out.println("No name attribute in targetGitLab, skipping this.");
			}
			if (rtOptions.getopt_log())
			{
				s_log.write("No name attribute in targetGitLab, skipping this." + System.lineSeparator());
			}
			return write_id;
		}

		String targetUrl = getAttributeOrEmpty(nodes.item(tn), "url");
		if (targetUrl.equals(""))
		{
			if (rtOptions.getopt_verbose())
			{
				System.out.println("No url attribute in targetGitLab '" + targetPath + "', skipping this.");
			}
			if (rtOptions.getopt_log())
			{
				s_log.write("No url attribute in targetGitLab '" + targetPath + "', skipping this." + System.lineSeparator());
			}
			return write_id;
		}
		if (rtOptions.getopt_log() && additional_debug_log)
		{
			s_log.write("targetGitLabGroup:" + System.lineSeparator());
			s_log.write("  url  = '" + targetUrl + "'" + System.lineSeparator());
			s_log.write("  name = '" + targetPath + "'" + System.lineSeparator());
		}
		boolean mergeMode = (getAttributeOrEmpty(nodes.item(tn), "mode").equals("merge"));

		GitLabAccessObject glc;
		try {
			glc = new GitLabAccessObject(targetUrl, targetPath);

		} catch (AccessObjectNotFoundException e) {

			if (rtOptions.getopt_verbose())
			{
				System.out.println("GitLab group or project '" + targetPath + "' not found.");
			}
			if (rtOptions.getopt_log())
			{
				s_log.write("GitLab group or project'" + targetPath + "' not found." + System.lineSeparator());
			}
			return write_id;
		}
		MemberRoleAssignments target_as_is = glc.getMemberRoleAssignementsForAccessObject();
		if (rtOptions.getopt_log() && additional_debug_log)
		{
			s_log.write(" target gitlab group/project contains " + target_as_is.getMemberCount() + " members" + System.lineSeparator());
		}

		MemberRoleAssignments target_as_should_be;
		if (mergeMode)
		{
			target_as_should_be = new MemberRoleAssignments(target_as_is);
		}
		else
		{
			target_as_should_be = new MemberRoleAssignments();
		}
		// target i.e. "global" assign
		XPath xpath = XPathFactory.newInstance().newXPath();
		NodeList ass_nodes = (NodeList) xpath.evaluate("./assign", nodes.item(tn), XPathConstants.NODESET);
		ArrayList<String> assign = new ArrayList<String>();
		for (int an = 0; an < ass_nodes.getLength(); an++)
		{
			assign.add(ass_nodes.item(an).getTextContent());
		}
		int t_assigned = assign.size();
		// target i.e. "global" excludeAssign
		NodeList excl_nodes = (NodeList) xpath.evaluate("./excludeAssign", nodes.item(tn), XPathConstants.NODESET);
		ArrayList<String> excludeAssign = new ArrayList<String>();
		for (int en = 0; en < excl_nodes.getLength(); en++)
		{
			excludeAssign.add(excl_nodes.item(en).getTextContent());
		}
		int t_excluded = excludeAssign.size();

		xpath = XPathFactory.newInstance().newXPath();
		NodeList s_nodes = (NodeList) xpath.evaluate("./sourceArea", nodes.item(tn), XPathConstants.NODESET);
		for (int sn = 0; sn < s_nodes.getLength(); sn++)
		{
			processReadSourceArea(s_log, additional_debug_log, theRM,
					defaultJazzUrl, jazz, xpath, assign, t_assigned,
					excludeAssign, t_excluded, target_as_should_be, s_nodes, sn, true);
		}

		s_nodes = (NodeList) xpath.evaluate("./sourceLDAPGroup", nodes.item(tn), XPathConstants.NODESET);
		for (int sn = 0; sn < s_nodes.getLength(); sn++)
		{
			processReadSourceLDAPGroup(s_log, additional_debug_log,
					xpath, assign, t_assigned, target_as_should_be, s_nodes, sn);
		}

		s_nodes = (NodeList) xpath.evaluate("./sourceGitLab", nodes.item(tn), XPathConstants.NODESET);
		for (int sn = 0; sn < s_nodes.getLength(); sn++)
		{
			processReadSourceGitLab(s_log, additional_debug_log,
					xpath, assign, t_assigned, excludeAssign, t_excluded, target_as_should_be, s_nodes, sn, true);
		}
		s_nodes = (NodeList) xpath.evaluate("./sourceArtifactory", nodes.item(tn), XPathConstants.NODESET);
		for (int sn = 0; sn < s_nodes.getLength(); sn++)
		{
			processReadSourceArtifactory(s_log, additional_debug_log,
					xpath, assign, t_assigned, excludeAssign, t_excluded, target_as_should_be, s_nodes, sn, true);
		}

		write_id = performChangesTargetGitLab(idstring, write_id, s_log, targetPath, targetUrl,
				                                     glc, target_as_is, target_as_should_be);

		return write_id;
	}

	private static void processReadSourceGitLab(PrintWriter s_log,
			boolean additional_debug_log, XPath xpath,
			ArrayList<String> assign, int t_assigned,
			ArrayList<String> excludeAssign, int t_excluded,
			MemberRoleAssignments target_as_should_be, NodeList s_nodes,
			int sn, boolean targetHasRoles) throws XPathExpressionException {

		String sourcePath = getAttributeOrEmpty(s_nodes.item(sn), "name");
		if (sourcePath.equals(""))
		{
			if (rtOptions.getopt_verbose())
			{
				System.out.println("No name attribute in sourceGitLab, skipping this.");
			}
			if (rtOptions.getopt_log())
			{
				s_log.write("No name attribute in sourceGitLab, skipping this." + System.lineSeparator());
			}
			return;
		}

		String sourceUrl = getAttributeOrEmpty(s_nodes.item(sn), "url");
		if (sourceUrl.equals(""))
		{
			if (rtOptions.getopt_verbose())
			{
				System.out.println("No url attribute in sourceGitLab '" + sourcePath + "', skipping this.");
			}
			if (rtOptions.getopt_log())
			{
				s_log.write("No url attribute in sourceGitLab '" + sourcePath + "', skipping this." + System.lineSeparator());
			}
			return;
		}
		if (rtOptions.getopt_log() && additional_debug_log)
		{
			s_log.write("sourceGitLab:" + System.lineSeparator());
			s_log.write("  url  = '" + sourceUrl + "'" + System.lineSeparator());
			s_log.write("  name = '" + sourcePath + "'" + System.lineSeparator());
		}
		NodeList sel_s_nodes = (NodeList) xpath.evaluate("./select", s_nodes.item(sn), XPathConstants.NODESET);
		ArrayList<String> select = new ArrayList<String>();
		for (int ssn = 0; ssn < sel_s_nodes.getLength(); ssn++)
		{
			select.add(sel_s_nodes.item(ssn).getTextContent());
		}

		if (targetHasRoles) {
			// Set assigns back to contain only (global) targetArea assigns
			if (assign.size() > t_assigned)
			{
				assign = new ArrayList<String>(assign.subList(0, t_assigned));
			}
			NodeList ass_s_nodes = (NodeList) xpath.evaluate("./assign", s_nodes.item(sn), XPathConstants.NODESET);
			for (int asn = 0; asn < ass_s_nodes.getLength(); asn++)
			{
				assign.add(ass_s_nodes.item(asn).getTextContent());
			}

			// Set excludeAssigns back to contain only (global) targetArea excludeAssigns
			if (excludeAssign.size() > t_excluded)
			{
				excludeAssign = new ArrayList<String>(excludeAssign.subList(0, t_excluded));
			}
			NodeList excl_s_nodes = (NodeList) xpath.evaluate("./excludeAssign", s_nodes.item(sn), XPathConstants.NODESET);
			for (int esn = 0; esn < excl_s_nodes.getLength(); esn++)
			{
				excludeAssign.add(excl_s_nodes.item(esn).getTextContent());
			}
		}

		GitLabAccessObject glc;
		try {
			glc = new GitLabAccessObject(sourceUrl, sourcePath);

		} catch (AccessObjectNotFoundException e) {

			if (rtOptions.getopt_verbose())
			{
				System.out.println("GitLab group or project '" + sourcePath + "' not found.");
			}
			if (rtOptions.getopt_log())
			{
				s_log.write("GitLab group or project '" + sourcePath + "' not found." + System.lineSeparator());
			}
			return;
		}
		MemberRoleAssignments source_as_is = glc.getMemberRoleAssignementsForAccessObject();
		int srcCount = 0;
		for (MemberRoleAssignment mra: source_as_is.AllMembers())
		{
			srcCount++;
			boolean is_included = false;
			if (select.isEmpty())
			{
				is_included = true;
			}
			else
			{
				for (String role : select)
				{
					if (mra.hasRole(role))
					{
						is_included = true;
						break;
					}
				}
			}

			if (is_included)
			{
				if (targetHasRoles) {
					if (assign.isEmpty())
					{
						for (String role: mra.AllRoles())
						{
							if (!excludeAssign.contains(role))
							{
								target_as_should_be.addMemberRole(mra.getMember(), mra.isArchived(), role);
							}
						}
					}
					else
					{
						for (String targetRole : assign)
						{
							target_as_should_be.addMemberRole(mra.getMember(), mra.isArchived(), targetRole);
						}
					}
				}
				else
				{
					target_as_should_be.addMember(mra.getMember(), mra.isArchived());
				}
			}
		}
		if (rtOptions.getopt_log() && additional_debug_log)
		{
			s_log.write(" source gitlab group/project contains " + srcCount + " members" + System.lineSeparator());
		}
	}

	private static boolean performChangesTargetGitLab(String idstring,
			boolean write_id, PrintWriter s_log, String targetPath, String targetUrl,
			GitLabAccessObject glc, MemberRoleAssignments target_as_is,
			MemberRoleAssignments target_as_should_be) {

		// Perform required changes
		if (rtOptions.getopt_verbose())
		{
			System.out.printf("Processing target GitLab group/project '%1$s' in '%2$s'" + System.lineSeparator(), targetPath, targetUrl);
		}
		if (rtOptions.getopt_log())
		{
			s_log.println("Processing target GitLab group/project '" + targetPath + "' in '" + targetUrl + "'");
		}

		target_as_should_be.removeArchivedMembers();
		String[] gitLabAccessLevels = {"guest", "reporter", "developer", "master", "owner"};
		target_as_should_be.reduceRolesToOne(gitLabAccessLevels);

		for (MemberRoleAssignment mra : target_as_should_be.AllMembers())
		{
			MemberRoleAssignment t_mra = target_as_is.getMember(mra.getMember());
			if (t_mra == null)
			{
				// member must be added with all roles from mra.AllRoles()
				String userName = "";
				try
				{
					userName = glc.findUser(mra.getMember());
					if (userName.isEmpty())
						throw new NotFoundException("User with name '" + mra.getMember() + "' not found in GitLab");
					else
					{
						if (rtOptions.getopt_verbose())
						{
							System.out.printf("  Adding member '%1$s'" + System.lineSeparator(), userName);
						}
						if (rtOptions.getopt_log())
						{
							s_log.println("  Adding member '" + userName + "'");
						}

						glc.AddAccessObjectMember(userName);
						for (String role : mra.AllRoles()) {
							if (rtOptions.getopt_verbose())
							{
								System.out.printf("    Assigning role '%1$s'" + System.lineSeparator(), role);
							}
							if (rtOptions.getopt_log())
							{
								s_log.println("    Assigning role '" + role + "'");
							}
							glc.UpdateAccessObjectMember(userName, role);
						}
					}
				}
				catch (NotFoundException ex)
				{
					if (write_id)
					{
						System.out.println(idstring);
						System.out.println();
						write_id = false;
					}
					//Console.WriteLine("Exception occurred, message: {0}\n", ex.Message);
					System.out.printf(ex.getMessage() + System.lineSeparator());
					System.out.printf("Failed to add member '%1$s'" + System.lineSeparator(), mra.getMember());
					if (rtOptions.getopt_log())
					{
						s_log.println(ex.getMessage());
						s_log.println("Failed to add member '" + mra.getMember() + "'");
					}
				}
				catch (RuntimeException ex)
				{
					if (write_id)
					{
						System.out.println(idstring);
						System.out.println();
						write_id = false;
					}
					//Console.WriteLine("Exception occurred, message: {0}\n", ex.Message);
					System.out.printf("Failed to add member '%1$s'" + System.lineSeparator(), userName);
					if (rtOptions.getopt_log())
					{
						s_log.println("Failed to add member '" + userName + "'");
					}
				}
			}
		}

		for (MemberRoleAssignment mra : target_as_is.AllMembers())
		{
			MemberRoleAssignment t_mra = target_as_should_be.getMember(mra.getMember());
			String userName = glc.findUser(mra.getMember());
			if (t_mra == null)
			{
				// member must be deleted
				if (rtOptions.getopt_verbose())
				{
					System.out.printf("  Deleting member '%1$s'" + System.lineSeparator(), userName);
				}
				if (rtOptions.getopt_log())
				{
					s_log.println("  Deleting member '" + userName + "'");
				}
				try
				{
					glc.RemoveAccessObjectMember(userName);
				}
				catch (RuntimeException e5)
				{
					if (write_id)
					{
						System.out.println(idstring);
						System.out.println();
						write_id = false;
					}
					//Console.WriteLine("Exception occurred, message: {0}\n", ex.Message);
					System.out.printf("Failed to delete member '%1$s' from '%2$s'" + System.lineSeparator(), userName, targetPath);
					if (rtOptions.getopt_log())
					{
						s_log.println("Failed to delete member '" + userName + "' from '" + targetPath + "'");
					}
				}
			}
			else
			{
				for (String role : t_mra.AllRoles())
				{
					if (!mra.hasRole(role))
					{
						// role must be added
						if (rtOptions.getopt_verbose())
						{
							System.out.printf("  Adding role '%1$s' to member '%2$s'" + System.lineSeparator(), role, userName);
						}
						if (rtOptions.getopt_log())
						{
							s_log.println("  Adding role '" + role + "' to member '" + userName + "'");
						}
						try
						{
							glc.UpdateAccessObjectMember(userName, role);
						}
						catch (RuntimeException ex)
						{
							if (write_id)
							{
								System.out.println(idstring);
								System.out.println();
								write_id = false;
							}
							//Console.WriteLine("Exception occurred, message: {0}\n", ex.Message);
							System.out.printf("Failed to add role '%1$s' to member '%2$s'" + System.lineSeparator(), role, userName);
							if (rtOptions.getopt_log())
							{
								s_log.println("Failed to add role '" + role + "' to member '" + userName + "'");
							}
						}
					}
				}
				for (String role : mra.AllRoles())
				{
					if (!t_mra.hasRole(role))
					{
						// role must be deleted
						if (rtOptions.getopt_verbose())
						{
							System.out.printf("  Removing role '%1$s' from member '%2$s'" + System.lineSeparator(), role, userName);
						}
						if (rtOptions.getopt_log())
						{
							s_log.println("  Removing role '" + role + "' from member '" + userName + "'");
						}
						try
						{
							// Removing a role means set access level to guest access (i.e. lowest level)
							glc.UpdateAccessObjectMember(userName, "guest");
						}
						catch (RuntimeException ex)
						{
							if (write_id)
							{
								System.out.println(idstring);
								System.out.println();
								write_id = false;
							}
							//Console.WriteLine("Exception occurred, message: {0}\n", ex.Message);
							System.out.printf("Failed to remove role '%1$s' from member '%2$s'" + System.lineSeparator(), role, userName);
							if (rtOptions.getopt_log())
							{
								s_log.println("Failed to remove role '" + role + "' from member '" + userName + "'");
							}
						}
					}
				}
			}
		}
		return write_id;
	}

	private static boolean processTargetLDAPGroup(final String idstring,
			boolean write_id, PrintWriter s_log, boolean additional_debug_log,
			NodeList nodes, RepoManager theRM, String defaultJazzUrl,
			IJazz jazz, int tn) throws NamingException, XPathExpressionException {

		boolean byGuid = false;
		String targetGuid = "";
		String targetGroupName = getAttributeOrEmpty(nodes.item(tn), "name");
		if (targetGroupName.equals(""))
		{
			targetGuid = getAttributeOrEmpty(nodes.item(tn), "guid");
			if (!targetGuid.equals(""))
			{
				byGuid = true;
			}
		}
		String targetDomain = getAttributeOrEmpty(nodes.item(tn), "domain");
		if (targetDomain.equals(""))
		{
			if (rtOptions.getopt_verbose())
			{
				System.out.println("No domain attribute in targetLDAPGroup '" + targetGroupName + "', skipping this.");
			}
			if (rtOptions.getopt_log())
			{
				s_log.write("No domain attribute in targetLDAPGroup '" + targetGroupName + "', skipping this." + System.lineSeparator());
			}
			return write_id;
		}
		if (rtOptions.getopt_log() && additional_debug_log)
		{
			s_log.write("targetLDAPGroup:" + System.lineSeparator());
			s_log.write("  domain = '" + targetDomain + "'" + System.lineSeparator());
			s_log.write("  name   = '" + targetGroupName + "'" + System.lineSeparator());
			s_log.write("  guid   = '" + targetGuid + "'" + System.lineSeparator());
		}

		boolean mergeMode = (getAttributeOrEmpty(nodes.item(tn), "mode").equals("merge"));
		String searchScope = "dc=" + targetDomain.replaceAll("\\.", ",dc=");

		//ADControl ac = ADControl.GetADControl();
		LdapController lctrl = new LdapController();
		String targetGroup = "";
		try
		{
			if (byGuid)
			{
				targetGroup = lctrl.getGroupDNbyGUID(targetGuid, searchScope);
				targetGroupName = targetGroup.substring(3, targetGroup.indexOf(','));
				if (rtOptions.getopt_log() && additional_debug_log)
				{
					s_log.write("  access by guid, name = '" + targetGroupName + "'" + System.lineSeparator());
				}
			}
			else
			{
				targetGroup = lctrl.getGroupDN(targetGroupName, searchScope);
			}
		}
		catch (NamingException ex)
		{}
		if (targetGroup.isEmpty())
		{
			if (rtOptions.getopt_verbose())
			{
				System.out.println("LDAP Group '" + targetGroupName + "' not found.");
			}
			if (rtOptions.getopt_log())
			{
				s_log.write("LDAP Group '" + targetGroupName + "' not found." + System.lineSeparator());
			}
			lctrl.close();
			return write_id;
		}

		MemberRoleAssignments target_as_is = new MemberRoleAssignments();
		ArrayList<String> targetMemberList = lctrl.getGroupMemberList(targetGroupName, searchScope);
		if (rtOptions.getopt_log() && additional_debug_log)
		{
			s_log.write(" target ldap group contains " + targetMemberList.size() + " members" + System.lineSeparator());
		}

		for (String member : targetMemberList)
		{
			target_as_is.addMember(member, false);
		}

		MemberRoleAssignments target_as_should_be;
		if (mergeMode)
		{
			target_as_should_be = new MemberRoleAssignments(target_as_is);
		}
		else
		{
			target_as_should_be = new MemberRoleAssignments();
		}

		XPath xpath = XPathFactory.newInstance().newXPath();
		NodeList s_nodes = (NodeList) xpath.evaluate("./sourceArea", nodes.item(tn), XPathConstants.NODESET);
		for (int sn = 0; sn < s_nodes.getLength(); sn++)
		{
			processReadSourceArea(s_log, additional_debug_log, theRM,
					defaultJazzUrl, jazz, xpath, null, 0,
					null, 0, target_as_should_be, s_nodes, sn, false);
		}

		s_nodes = (NodeList) xpath.evaluate("./sourceLDAPGroup", nodes.item(tn), XPathConstants.NODESET);
		for (int sn = 0; sn < s_nodes.getLength(); sn++)
		{
			processReadSourceLDAPGroup(s_log, additional_debug_log,
					xpath, null, 0, target_as_should_be, s_nodes, sn);
		}
		s_nodes = (NodeList) xpath.evaluate("./sourceGitLab", nodes.item(tn), XPathConstants.NODESET);
		for (int sn = 0; sn < s_nodes.getLength(); sn++)
		{
			processReadSourceGitLab(s_log, additional_debug_log,
					xpath, null, 0, null, 0, target_as_should_be, s_nodes, sn, false);
		}
		s_nodes = (NodeList) xpath.evaluate("./sourceArtifactory", nodes.item(tn), XPathConstants.NODESET);
		for (int sn = 0; sn < s_nodes.getLength(); sn++)
		{
			processReadSourceArtifactory(s_log, additional_debug_log,
					xpath, null, 0, null, 0, target_as_should_be, s_nodes, sn, false);
		}

		write_id = performChangesTargetLDAPGroup(idstring,
				write_id, s_log, targetGroupName, searchScope,
				lctrl, targetGroup, target_as_is,
				target_as_should_be);

		lctrl.close();
		return write_id;
	}

	private static boolean performChangesTargetLDAPGroup(final String idstring,
			boolean write_id, PrintWriter s_log, String targetGroupName,
			String searchScope, LdapController lctrl, String targetGroup,
			MemberRoleAssignments target_as_is,
			MemberRoleAssignments target_as_should_be) throws NamingException {

		//Perform required changes
		if (rtOptions.getopt_verbose())
		{
			System.out.printf("Processing target LDAP group '%1$s'" + System.lineSeparator(), targetGroupName);
		}
		if (rtOptions.getopt_log())
		{
			s_log.println("Processing target LDAP group '" + targetGroupName + "'");
		}

		target_as_should_be.removeArchivedMembers();

		for (MemberRoleAssignment mra : target_as_should_be.AllMembers())
		{
			MemberRoleAssignment t_mra = target_as_is.getMember(mra.getMember());
			if (t_mra == null)
			{
				// member must be added
				try
				{
					String userAccountName = lctrl.getExactAccountName(mra.getMember());
					if (userAccountName.isEmpty())
						throw new NotFoundException("User with name '" + mra.getMember() + "' not found in LDAP");
					else
					{
						if (rtOptions.getopt_verbose())
						{
							System.out.printf("  Adding member '%1$s'" + System.lineSeparator(), userAccountName);
						}
						if (rtOptions.getopt_log())
						{
							s_log.println("  Adding member '" + userAccountName + "'");
						}

						//String userDn = ac.GetObjectDistinguishedName(ADControl.objectClass.user, ADControl.returnType.distinguishedName, mra.getMember(), targetDomain);
						String userDn = lctrl.getUserDN(mra.getMember(), searchScope);
						//ac.AddToGroup(userDn, targetGroup);
						lctrl.addGroupMember(targetGroup, userDn);
					}
				}
				catch (NotFoundException ex)
				{
					if (write_id)
					{
						System.out.println(idstring);
						System.out.println();
						write_id = false;
					}
					//Console.WriteLine("Exception occurred, message: {0}\n", ex.Message);
					System.out.printf(ex.getMessage() + System.lineSeparator());
					System.out.printf("Failed to add member '%1$s'" + System.lineSeparator(), mra.getMember());
					if (rtOptions.getopt_log())
					{
						s_log.println(ex.getMessage());
						s_log.println("Failed to add member '" + mra.getMember() + "'");
					}
				}
				catch (RuntimeException ex)
				{
					if (write_id)
					{
						System.out.println(idstring);
						System.out.println();
						write_id = false;
					}
					//Console.WriteLine("Exception occurred, message: {0}\n", ex.Message);
					System.out.printf("Failed to add member '%1$s'" + System.lineSeparator(), mra.getMember());
					if (rtOptions.getopt_log())
					{
						s_log.println("Failed to add member '" + mra.getMember() + "'");
					}
				}
			}
		}

		for (MemberRoleAssignment mra : target_as_is.AllMembers())
		{
			MemberRoleAssignment t_mra = target_as_should_be.getMember(mra.getMember());
			if (t_mra == null)
			{
				// member must be deleted
				if (rtOptions.getopt_verbose())
				{
					System.out.printf("  Deleting member '%1$s'" + System.lineSeparator(), mra.getMember());
				}
				if (rtOptions.getopt_log())
				{
					s_log.println("  Deleting member '" + mra.getMember() + "'");
				}
				try
				{
					//String userDn = ac.GetObjectDistinguishedName(ADControl.objectClass.user, ADControl.returnType.distinguishedName, mra.getMember(), targetDomain);
					String userDn = lctrl.getUserDN(mra.getMember(), searchScope);
					//ac.RemoveUserFromGroup(userDn, targetGroup);
					lctrl.removeGroupMember(targetGroup, userDn);
				}
				catch (RuntimeException ex)
				{
					if (write_id)
					{
						System.out.println(idstring);
						System.out.println();
						write_id = false;
					}
					//Console.WriteLine("Exception occurred, message: {0}\n", ex.Message);
					System.out.printf("Failed to delete member '%1$s' from LDAP group '%2$s'" + System.lineSeparator(), mra.getMember(), targetGroupName);
					if (rtOptions.getopt_log())
					{
						s_log.println("Failed to delete member '" + mra.getMember() + "' from LDAP group '" + targetGroupName + "'");
					}
				}
			}
		}
		return write_id;
	}

	private static boolean processTargetArea(final String idstring,
			boolean write_id, PrintWriter s_log, boolean additional_debug_log,
			NodeList nodes, RepoManager theRM, String defaultJazzUrl,
			IJazz jazz, int tn) throws XPathExpressionException, NamingException {

		// Process <targetArea> i.e. Jazz
		boolean byOid = false;
		boolean skip_due_to_target_not_ok = false;

		// New v 2.0
		String targetRepo = getAttributeOrEmpty(nodes.item(tn), "repo");
		IJazz t_jazz;
		if (targetRepo.equals(""))
		{
			t_jazz = jazz;
			targetRepo = defaultJazzUrl;
		}
		else
		{
			t_jazz = theRM.GetRepository(targetRepo);
		}

		String targetPath = getAttributeOrEmpty(nodes.item(tn), "name");
		if (targetPath.equals(""))
		{
			targetPath = getAttributeOrEmpty(nodes.item(tn), "oid");
			byOid = true;
		}
		if (rtOptions.getopt_log() && additional_debug_log)
		{
			s_log.write("targetArea:" + System.lineSeparator());
			s_log.write("  repo     = '" + targetRepo + "'" + System.lineSeparator());
			s_log.write("  name/oid = '" + targetPath + "'" + System.lineSeparator());
		}
		//string targetRole = nodes.Current.GetAttribute("role", string.Empty);
		boolean mergeMode = (getAttributeOrEmpty(nodes.item(tn), "mode").equals("merge"));

		String[] t_arr = targetPath.split("[/]", -1);
		String t_projectArea = t_arr[0];
		IJazzProjectArea t_pa;
		IJazzTeamArea t_ta = null;
		try
		{
			if (byOid)
			{
				t_pa = t_jazz.GetJazzProjectAreaByOid(t_projectArea);
				if (t_pa == null) {
					t_ta = t_jazz.GetJazzTeamAreaByOid(t_projectArea);
				}
			}
			else
			{
				t_pa = t_jazz.GetJazzProjectAreaByName(t_projectArea);
			}
		}
		catch (RuntimeException e)
		{
			t_pa = null;
		}

		if (t_ta == null)
		{
			if (t_pa == null)
			{
				if (rtOptions.getopt_verbose())
				{
					System.out.println("ProjectArea '" + t_projectArea + "' not found in '" + targetRepo + "'");
				}
				if (rtOptions.getopt_log())
				{
					s_log.write("ProjectArea '" + t_projectArea + "' not found in '" + targetRepo + "'" + System.lineSeparator());
				}
				return write_id;
			}
			targetPath = t_pa.getName();

			//MemberRoleAssignments target_as_inherited = new MemberRoleAssignments();
			//foreach (JazzMember m in t_pa.JazzProjectMembers())
			//    foreach (JazzRole r in m.JazzMemberRoles())
			//        target_as_inherited.addMemberRole(m.JazzMemberUser.Name, r.Label);

			for (int i = 1; i < t_arr.length; i++)
			{
				if (t_ta == null)
				{
					if (byOid)
					{
						try
						{
							t_ta = t_pa.GetJazzTeamAreaByOid(t_arr[i]);
						}
						catch (RuntimeException e2)
						{
							t_ta = null;
						}
					}
					else
					{
						t_ta = t_pa.GetJazzTeamAreaByName(t_arr[i]);
					}
				}
				else
				{
					if (byOid)
					{
						try
						{
							t_ta = t_ta.GetJazzSubTeamAreaByOid(t_arr[i]);
						}
						catch (RuntimeException e3)
						{
							t_ta = null;
						}
					}
					else
					{
						t_ta = t_ta.GetJazzSubTeamAreaByName(t_arr[i]);
					}
				}
				if (t_ta == null)
				{
					if (rtOptions.getopt_verbose())
					{
						System.out.println("TeamArea '" + t_arr[i] + "' not found within ProjectArea '" + t_projectArea + "'");
					}
					if (rtOptions.getopt_log())
					{
						s_log.write("TeamArea '" + t_arr[i] + "' not found within ProjectArea '" + t_projectArea + "'" + System.lineSeparator());
					}
					skip_due_to_target_not_ok = true;
					return write_id;
				}
				targetPath += "/" + t_ta.getName();

				//if (i < (t_arr.Length - 1))
				//    foreach (JazzMember m in t_ta.JazzTeamMembers())
				//        foreach (JazzRole r in m.JazzMemberRoles())
				//            target_as_inherited.addMemberRole(m.JazzMemberUser.Name, r.Label);
			}
			// He 2015-06-17: Don't use project area as target if any team area is not found
			if (skip_due_to_target_not_ok)
			{
				return write_id;
			}
		}
		else
			targetPath = t_ta.getFullPath();

		int trgCount = 0;
		MemberRoleAssignments target_as_is = new MemberRoleAssignments();
		if (t_ta == null)
		{
			for (IJazzMember m : t_pa.JazzProjectMembers())
			{
				trgCount++;
				for (IJazzRole r : m.JazzMemberRoles())
				{
					//if (!target_as_inherited.memberHasRole(m.JazzMemberUser.Name, r.Label))
					target_as_is.addMemberRole(m.getJazzMemberUser().getName(), m.getJazzMemberUser().isArchived(), r.getLabel());
				}
			}
		}
		else
		{
			for (IJazzMember m : t_ta.JazzTeamMembers())
			{
				trgCount++;
				for (IJazzRole r : m.JazzMemberRoles())
				{
					//if (!target_as_inherited.memberHasRole(m.JazzMemberUser.Name, r.Label))
					target_as_is.addMemberRole(m.getJazzMemberUser().getName(), m.getJazzMemberUser().isArchived(), r.getLabel());
				}
			}
		}
		if (rtOptions.getopt_log() && additional_debug_log)
		{
			s_log.write(" target area contains " + trgCount + " members" + System.lineSeparator());
		}

		// target i.e. "global" assign
		XPath xpath = XPathFactory.newInstance().newXPath();
		NodeList ass_nodes = (NodeList) xpath.evaluate("./assign", nodes.item(tn), XPathConstants.NODESET);
		ArrayList<String> assign = new ArrayList<String>();
		for (int an = 0; an < ass_nodes.getLength(); an++)
		{
			assign.add(ass_nodes.item(an).getTextContent());
		}
		int t_assigned = assign.size();
		// target i.e. "global" excludeAssign
		NodeList excl_nodes = (NodeList) xpath.evaluate("./excludeAssign", nodes.item(tn), XPathConstants.NODESET);
		ArrayList<String> excludeAssign = new ArrayList<String>();
		for (int en = 0; en < excl_nodes.getLength(); en++)
		{
			excludeAssign.add(excl_nodes.item(en).getTextContent());
		}
		int t_excluded = excludeAssign.size();

		MemberRoleAssignments target_as_should_be;
		if (mergeMode)
		{
			target_as_should_be = new MemberRoleAssignments(target_as_is);
		}
		else
		{
			target_as_should_be = new MemberRoleAssignments();
		}

		NodeList s_nodes = (NodeList) xpath.evaluate("./sourceArea", nodes.item(tn), XPathConstants.NODESET);
		for (int sn = 0; sn < s_nodes.getLength(); sn++)
		{
			processReadSourceArea(s_log, additional_debug_log, theRM,
					defaultJazzUrl, jazz, xpath, assign, t_assigned,
					excludeAssign, t_excluded, target_as_should_be, s_nodes, sn, true);
		}

		// v 3.0
		s_nodes = (NodeList) xpath.evaluate("./sourceLDAPGroup", nodes.item(tn), XPathConstants.NODESET);
		for (int sn = 0; sn < s_nodes.getLength(); sn++)
		{
			processReadSourceLDAPGroup(s_log, additional_debug_log,
					xpath, assign, t_assigned, target_as_should_be, s_nodes, sn);
		}
		// v 4.0
		s_nodes = (NodeList) xpath.evaluate("./sourceGitLab", nodes.item(tn), XPathConstants.NODESET);
		for (int sn = 0; sn < s_nodes.getLength(); sn++)
		{
			processReadSourceGitLab(s_log, additional_debug_log,
					xpath, assign, t_assigned, excludeAssign, t_excluded, target_as_should_be, s_nodes, sn, true);
		}
		// v 4.1
		s_nodes = (NodeList) xpath.evaluate("./sourceArtifactory", nodes.item(tn), XPathConstants.NODESET);
		for (int sn = 0; sn < s_nodes.getLength(); sn++)
		{
			processReadSourceArtifactory(s_log, additional_debug_log,
					xpath, assign, t_assigned, excludeAssign, t_excluded, target_as_should_be, s_nodes, sn, true);
		}

		write_id = performChangesTargetArea(idstring, write_id, s_log,
						targetRepo, t_jazz, targetPath, t_pa, t_ta, target_as_is, target_as_should_be);

		return write_id;
	}

	private static void processReadSourceLDAPGroup(
			PrintWriter s_log, boolean additional_debug_log, XPath xpath,
			ArrayList<String> assign, int t_assigned,
			MemberRoleAssignments target_as_should_be, NodeList s_nodes, int sn)
			throws XPathExpressionException, NamingException {

		boolean s_byGuid = false;
		String sourceGuid = "";
		String sourceGroupName = getAttributeOrEmpty(s_nodes.item(sn), "name");
		if (sourceGroupName.equals(""))
		{
			sourceGuid = getAttributeOrEmpty(s_nodes.item(sn), "guid");
			if (!sourceGuid.equals(""))
			{
				s_byGuid = true;
			}
		}
		String sourceDomain = getAttributeOrEmpty(s_nodes.item(sn), "domain");
		if (sourceDomain.equals(""))
		{
			if (rtOptions.getopt_verbose())
			{
				System.out.println("No domain attribute in sourceLDAPGroup '" + sourceGroupName + "', skipping this.");
			}
			if (rtOptions.getopt_log())
			{
				s_log.write("No domain attribute in sourceLDAPGroup '" + sourceGroupName + "', skipping this." + System.lineSeparator());
			}
			return;
		}
		if (rtOptions.getopt_log() && additional_debug_log)
		{
			s_log.write("sourceLDAPGroup:" + System.lineSeparator());
			s_log.write("  domain = '" + sourceDomain + "'" + System.lineSeparator());
			s_log.write("  name   = '" + sourceGroupName + "'" + System.lineSeparator());
			s_log.write("  guid   = '" + sourceGuid + "'" + System.lineSeparator());
		}
		String searchScope = "dc=" + sourceDomain.replaceAll("\\.", ",dc=");

		// Only assigns make sense in LDAP context as group members have no roles to select from or to exclude

		// assign can be null if containing target has no meaningful assign option (e.g. LDAP groups)
		if (assign != null) {
			// Set assigns back to contain only (global) targetArea assigns
			if (assign.size() > t_assigned)
			{
				assign = new ArrayList<String>(assign.subList(0, t_assigned));
			}
			NodeList ass_s_nodes = (NodeList) xpath.evaluate("./assign", s_nodes.item(sn), XPathConstants.NODESET);
			for (int asn = 0; asn < ass_s_nodes.getLength(); asn++)
			{
				assign.add(ass_s_nodes.item(asn).getTextContent());
			}
		}

		//ADControl ac = ADControl.GetADControl();
		LdapController lctrl = new LdapController();
		String sourceGroup = "";
		try
		{
			if (s_byGuid)
			{
				try {
					sourceGroup = lctrl.getGroupDNbyGUID(sourceGuid, searchScope);
				} catch (Exception e) {
					if (rtOptions.getopt_log() && additional_debug_log)
					{
						s_log.write("  INFO: caught exception on getGroupDNbyGUID" + System.lineSeparator());
						s_log.write("        exception message: " + e.getMessage() + System.lineSeparator());
					}
					lctrl = new LdapController();
					sourceGroup = lctrl.getGroupDNbyGUID(sourceGuid, searchScope);
				}
				sourceGroupName = sourceGroup.substring(3, sourceGroup.indexOf(','));
				if (rtOptions.getopt_log() && additional_debug_log)
				{
					s_log.write("  access by guid, name = '" + sourceGroupName + "'" + System.lineSeparator());
				}
			}
			else
			{
				sourceGroup = lctrl.getGroupDN(sourceGroupName, searchScope);
			}
		}
		catch (NamingException ex)
		{}
		catch (NullPointerException ex)
		{
			if (rtOptions.getopt_verbose())
			{
				System.out.println("LDAP Group '" + sourceGroupName + "' not found.");
			}
			if (rtOptions.getopt_log())
			{
				s_log.write("LDAP Group '" + sourceGroupName + "' not found." + System.lineSeparator());
			}
			lctrl.close();
			return;
		}

		ArrayList<String> memberList = lctrl.getGroupMemberList(sourceGroupName, searchScope);
		if (rtOptions.getopt_log() && additional_debug_log)
		{
			s_log.write(" source ldap group contains " + memberList.size() + " members" + System.lineSeparator());
		}

		for (String member : memberList)
		{
			if ((assign == null) || assign.isEmpty())
			{
				target_as_should_be.addMember(member, false);
			}
			else
			{
				for (String targetRole : assign)
				{
					target_as_should_be.addMemberRole(member, false, targetRole);
				}
			}
		}
		lctrl.close();
	}

	private static boolean performChangesTargetArea(final String idstring,
			boolean write_id, PrintWriter s_log, String targetRepo,
			IJazz t_jazz, String targetPath, IJazzProjectArea t_pa,
			IJazzTeamArea t_ta, MemberRoleAssignments target_as_is,
			MemberRoleAssignments target_as_should_be) {

		// Perform required changes
		if (rtOptions.getopt_verbose())
		{
			System.out.printf("Processing target area '%1$s' in '%2$s'" + System.lineSeparator(), targetPath, targetRepo);
		}
		if (rtOptions.getopt_log())
		{
			s_log.println("Processing target area '" + targetPath + "' in '" + targetRepo + "'");
		}

		target_as_should_be.removeArchivedMembers();

		// to work around caching problem
		ArrayList<IJazzMember> added_mems = new ArrayList<IJazzMember>();

		for (MemberRoleAssignment mra : target_as_should_be.AllMembers())
		{
			MemberRoleAssignment t_mra = target_as_is.getMember(mra.getMember());
			if (t_mra == null)
			{
				// member must be added with all roles from mra.AllRoles()
				String jazzUserName = "";
				try
				{
					IJazzMember jm;
					try {
						jm = t_jazz.GetNewJazzMember(mra.getMember());
						jazzUserName = jm.getJazzMemberUser().getName();
					} catch (Exception e) {
						throw new NotFoundException("User with name '" + mra.getMember() + "' not found in Jazz");
					}
					if (!jm.getJazzMemberUser().isArchived())
					{
						if (rtOptions.getopt_verbose())
						{
							System.out.printf("  Adding member '%1$s'" + System.lineSeparator(), jazzUserName);
						}
						if (rtOptions.getopt_log())
						{
							s_log.println("  Adding member '" + jazzUserName + "'");
						}
						added_mems.add(jm);
						if (t_ta == null)
						{
							t_pa.AddMember(jm);
							for (String role : mra.AllRoles())
							{
								IJazzRole jr = t_pa.GetJazzRoleByLabel(role);
								if (jr != null)
								{
									if (rtOptions.getopt_verbose())
									{
										System.out.printf("    Assigning role '%1$s'" + System.lineSeparator(), role);
									}
									if (rtOptions.getopt_log())
									{
										s_log.println("    Assigning role '" + role + "'");
									}
									jm.AddRole(jr);
								}
							}
						}
						else
						{
							t_ta.AddMember(jm);
							for (String role : mra.AllRoles())
							{
								IJazzRole jr = t_ta.GetJazzRoleByLabel(role);
								if (jr != null)
								{
									if (rtOptions.getopt_verbose())
									{
										System.out.printf("    Assigning role '%1$s'" + System.lineSeparator(), role);
									}
									if (rtOptions.getopt_log())
									{
										s_log.println("    Assigning role '" + role + "'");
									}
									jm.AddRole(jr);
								}
							}
						}
					}
				}
				catch (NotFoundException ex)
				{
					if (write_id)
					{
						System.out.println(idstring);
						System.out.println();
						write_id = false;
					}
					//Console.WriteLine("Exception occurred, message: {0}\n", ex.Message);
					System.out.printf(ex.getMessage() + System.lineSeparator());
					System.out.printf("Failed to add member '%1$s'" + System.lineSeparator(), mra.getMember());
					if (rtOptions.getopt_log())
					{
						s_log.println(ex.getMessage());
						s_log.println("Failed to add member '" + mra.getMember() + "'");
					}
				}
				catch (RuntimeException ex)
				{
					if (write_id)
					{
						System.out.println(idstring);
						System.out.println();
						write_id = false;
					}
					//Console.WriteLine("Exception occurred, message: {0}\n", ex.Message);
					System.out.printf("Failed to add member '%1$s'" + System.lineSeparator(), jazzUserName);
					if (rtOptions.getopt_log())
					{
						s_log.println("Failed to add member '" + jazzUserName + "'");
					}
				}
			}
		}
		//if (t_ta == null)
		//    t_pa.PostNewMembers();
		//else
		//    t_ta.PostNewMembers();

		// to work around caching problem
		for (IJazzMember jm : added_mems)
		{
			try
			{
				jm.SetRoles();
			}
			catch (RuntimeException ex)
			{
				if (write_id)
				{
					System.out.println(idstring);
					System.out.println();
					write_id = false;
				}
				//Console.WriteLine("Exception occurred, message: {0}\n", ex.Message);
				System.out.printf("Failed to set roles to member '%1$s'" + System.lineSeparator(), jm.getJazzMemberUser().getName());
				if (rtOptions.getopt_log())
				{
					s_log.println("Failed to set roles to member '" + jm.getJazzMemberUser().getName() + "'");
				}
			}
		}

		for (MemberRoleAssignment mra : target_as_is.AllMembers())
		{
			MemberRoleAssignment t_mra = target_as_should_be.getMember(mra.getMember());
			if (t_mra == null)
			{
				// member must be deleted
				if (rtOptions.getopt_verbose())
				{
					System.out.printf("  Deleting member '%1$s'" + System.lineSeparator(), mra.getMember());
				}
				if (rtOptions.getopt_log())
				{
					s_log.println("  Deleting member '" + mra.getMember() + "'");
				}
				try
				{
					if (t_ta == null)
					{
						t_pa.DeleteMemberWithName(mra.getMember());
					}
					else
					{
						t_ta.DeleteMemberWithName(mra.getMember());
					}
				}
				catch (RuntimeException e5)
				{
					if (write_id)
					{
						System.out.println(idstring);
						System.out.println();
						write_id = false;
					}
					//Console.WriteLine("Exception occurred, message: {0}\n", ex.Message);
					System.out.printf("Failed to delete member '%1$s' from '%2$s'" + System.lineSeparator(), mra.getMember(), targetPath);
					if (rtOptions.getopt_log())
					{
						s_log.println("Failed to delete member '" + mra.getMember() + "' from '" + targetPath + "'");
					}
				}
			}
			else
			{
				for (String role : t_mra.AllRoles())
				{
					if (!mra.hasRole(role))
					{
						// role must be added
						if (rtOptions.getopt_verbose())
						{
							System.out.printf("  Adding role '%1$s' to member '%2$s'" + System.lineSeparator(), role, mra.getMember());
						}
						if (rtOptions.getopt_log())
						{
							s_log.println("  Adding role '" + role + "' to member '" + mra.getMember() + "'");
						}
						try
						{
							if (t_ta == null)
							{
								IJazzMember jm = t_pa.GetMemberWithName(mra.getMember());
								IJazzRole jr = t_pa.GetJazzRoleByLabel(role);
								jm.AssignRole(jr);
							}
							else
							{
								IJazzMember jm = t_ta.GetMemberWithName(mra.getMember());
								IJazzRole jr = t_ta.GetJazzRoleByLabel(role);
								jm.AssignRole(jr);
							}
						}
						catch (RuntimeException ex)
						{
							if (write_id)
							{
								System.out.println(idstring);
								System.out.println();
								write_id = false;
							}
							//Console.WriteLine("Exception occurred, message: {0}\n", ex.Message);
							System.out.printf("Failed to add role '%1$s' to member '%2$s'" + System.lineSeparator(), role, mra.getMember());
							if (rtOptions.getopt_log())
							{
								s_log.println("Failed to add role '" + role + "' to member '" + mra.getMember() + "'");
							}
						}
					}
				}
				for (String role : mra.AllRoles())
				{
					if (!t_mra.hasRole(role))
					{
						// role must be deleted
						if (rtOptions.getopt_verbose())
						{
							System.out.printf("  Removing role '%1$s' from member '%2$s'" + System.lineSeparator(), role, mra.getMember());
						}
						if (rtOptions.getopt_log())
						{
							s_log.println("  Removing role '" + role + "' from member '" + mra.getMember() + "'");
						}
						try
						{
							if (t_ta == null)
							{
								IJazzMember jm = t_pa.GetMemberWithName(mra.getMember());
								jm.UnassignRole(role);
							}
							else
							{
								IJazzMember jm = t_ta.GetMemberWithName(mra.getMember());
								jm.UnassignRole(role);
							}
						}
						catch (RuntimeException ex)
						{
							if (write_id)
							{
								System.out.println(idstring);
								System.out.println();
								write_id = false;
							}
							//Console.WriteLine("Exception occurred, message: {0}\n", ex.Message);
							System.out.printf("Failed to remove role '%1$s' from member '%2$s'" + System.lineSeparator(), role, mra.getMember());
							if (rtOptions.getopt_log())
							{
								s_log.println("Failed to remove role '" + role + "' from member '" + mra.getMember() + "'");
							}
						}
					}
				}
			}
		}
		return write_id;
	}

	private static void processReadSourceArea(PrintWriter s_log,
			boolean additional_debug_log, RepoManager theRM,
			String defaultJazzUrl, IJazz jazz, XPath xpath,
			ArrayList<String> assign, int t_assigned,
			ArrayList<String> excludeAssign, int t_excluded, MemberRoleAssignments target_as_should_be,
			NodeList s_nodes, int sn, boolean targetHasRoles)
			throws XPathExpressionException {

		// Process <sourceArea>
		boolean s_byOid = false;
		boolean skip_due_to_source_not_ok = false;

		// New v 2.0
		String sourceRepo = getAttributeOrEmpty(s_nodes.item(sn), "repo");
		IJazz s_jazz;
		if (sourceRepo.equals(""))
		{
			s_jazz = jazz;
			sourceRepo = defaultJazzUrl;
		}
		else
		{
			s_jazz = theRM.GetRepository(sourceRepo);
		}
		boolean recursiveMode = (getAttributeOrEmpty(s_nodes.item(sn), "mode").equals("recursive"));

		String sourcePath = getAttributeOrEmpty(s_nodes.item(sn), "name");
		if (sourcePath.equals(""))
		{
			sourcePath = getAttributeOrEmpty(s_nodes.item(sn), "oid");
			s_byOid = true;
		}
		if (rtOptions.getopt_log() && additional_debug_log)
		{
			s_log.write("sourceArea:" + System.lineSeparator());
			s_log.write("  repo     = '" + sourceRepo + "'" + System.lineSeparator());
			s_log.write("  name/oid = '" + sourcePath + "'" + System.lineSeparator());
		}
		//string sourceRole = s_nodes.Current.GetAttribute("role", string.Empty);

		NodeList sel_s_nodes = (NodeList) xpath.evaluate("./select", s_nodes.item(sn), XPathConstants.NODESET);
		ArrayList<String> select = new ArrayList<String>();
		for (int ssn = 0; ssn < sel_s_nodes.getLength(); ssn++)
		{
			select.add(sel_s_nodes.item(ssn).getTextContent());
		}

		if (targetHasRoles) {
			// Set assigns back to contain only (global) targetArea assigns
			if (assign.size() > t_assigned)
			{
				assign = new ArrayList<String>(assign.subList(0, t_assigned));
			}
			NodeList ass_s_nodes = (NodeList) xpath.evaluate("./assign", s_nodes.item(sn), XPathConstants.NODESET);
			for (int asn = 0; asn < ass_s_nodes.getLength(); asn++)
			{
				assign.add(ass_s_nodes.item(asn).getTextContent());
			}

			// Set excludeAssigns back to contain only (global) targetArea excludeAssigns
			if (excludeAssign.size() > t_excluded)
			{
				excludeAssign = new ArrayList<String>(excludeAssign.subList(0, t_excluded));
			}
			NodeList excl_s_nodes = (NodeList) xpath.evaluate("./excludeAssign", s_nodes.item(sn), XPathConstants.NODESET);
			for (int esn = 0; esn < excl_s_nodes.getLength(); esn++)
			{
				excludeAssign.add(excl_s_nodes.item(esn).getTextContent());
			}
		}

		String[] s_arr = sourcePath.split("[/]", -1);
		String s_projectArea = s_arr[0];
		IJazzProjectArea s_pa;
		IJazzTeamArea s_ta = null;
		try
		{
			if (s_byOid)
			{
				s_pa = s_jazz.GetJazzProjectAreaByOid(s_projectArea);
				if (s_pa == null)
					s_ta = s_jazz.GetJazzTeamAreaByOid(s_projectArea);
			}
			else
			{
				s_pa = s_jazz.GetJazzProjectAreaByName(s_projectArea);
			}
		}
		catch (RuntimeException e4)
		{
			s_pa = null;
		}

		if (s_ta == null)
		{
			if (s_pa == null)
			{
				if (rtOptions.getopt_verbose())
				{
					System.out.println("ProjectArea '" + s_projectArea + "' not found in '" + sourceRepo + "'");
				}
				if (rtOptions.getopt_log())
				{
					s_log.write("ProjectArea '" + s_projectArea + "' not found in '" + sourceRepo + "'" + System.lineSeparator());
				}
				return;
			}
			for (int i = 1; i < s_arr.length; i++)
			{
				if (s_ta == null)
				{
					if (s_byOid)
					{
						s_ta = s_pa.GetJazzTeamAreaByOid(s_arr[i]);
					}
					else
					{
						s_ta = s_pa.GetJazzTeamAreaByName(s_arr[i]);
					}
				}
				else
				{
					if (s_byOid)
					{
						s_ta = s_ta.GetJazzSubTeamAreaByOid(s_arr[i]);
					}
					else
					{
						s_ta = s_ta.GetJazzSubTeamAreaByName(s_arr[i]);
					}
				}

				if (s_ta == null)
				{
					if (rtOptions.getopt_verbose())
					{
						System.out.println("TeamArea '" + s_arr[i] + "' not found within ProjectArea '" + s_projectArea + "'");
					}
					if (rtOptions.getopt_log())
					{
						s_log.write("TeamArea '" + s_arr[i] + "' not found within ProjectArea '" + s_projectArea + "'" + System.lineSeparator());
					}
					skip_due_to_source_not_ok = true;
					return;
				}
			}
			// He 2015-06-17: Don't use project area as source if any team area is not found
			if (skip_due_to_source_not_ok)
			{
				return;
			}
		}

		Iterable<IJazzMember> members;
		if (s_ta == null)
			members = s_pa.JazzProjectMembers();
		else
			members = s_ta.JazzTeamMembers();

		int srcCount = 0;
		for (IJazzMember m : members)
		{
			srcCount++;
			boolean is_included = false;
			if (select.isEmpty())
			{
				is_included = true;
			}
			else
			{
				for (String role : select)
				{
					if (m.JazzMemberHasRoleWithLabel(role))
					{
						is_included = true;
						break;
					}
				}
			}

			if (is_included)
			{
				if (targetHasRoles) {
					if (assign.isEmpty())
					{
						for (IJazzRole r : m.JazzMemberRoles())
						{
							if (!excludeAssign.contains(r.getLabel()))
							{
								//if (!target_as_inherited.memberHasRole(m.JazzMemberUser.Name, r.Label))
								target_as_should_be.addMemberRole(m.getJazzMemberUser().getName(), m.getJazzMemberUser().isArchived(), r.getLabel());
							}
						}
					}
					else
					{
						for (String targetRole : assign)
						{
							//if (!target_as_inherited.memberHasRole(m.JazzMemberUser.Name, targetRole))
							target_as_should_be.addMemberRole(m.getJazzMemberUser().getName(), m.getJazzMemberUser().isArchived(), targetRole);
						}
					}
				}
				else
				{
					target_as_should_be.addMember(m.getJazzMemberUser().getName(), m.getJazzMemberUser().isArchived());
				}
			}
		}
		if (rtOptions.getopt_log() && additional_debug_log)
		{
			s_log.write(" source area contains " + srcCount + " members" + System.lineSeparator());
		}

		if (recursiveMode) {
			Iterable<IJazzTeamArea> taList;
			if (s_ta == null)
				taList = s_pa.JazzTeamAreas();
			else
				taList = s_ta.JazzTeamAreas();

			RecursivelyCollectMembersOfTAs(taList, target_as_should_be, select, assign, excludeAssign,
											targetHasRoles, additional_debug_log, s_log);
		}
	}

	private static void RecursivelyCollectMembersOfTAs(
			Iterable<IJazzTeamArea> taList, MemberRoleAssignments target_as_should_be, ArrayList<String> select,
			ArrayList<String> assign, ArrayList<String> excludeAssign, boolean targetHasRoles,
			boolean additional_debug_log, PrintWriter s_log) {

		for (IJazzTeamArea ta: taList) {

			Iterable<IJazzMember> members = ta.JazzTeamMembers();

			int srcCount = 0;
			for (IJazzMember m : members)
			{
				srcCount++;
				boolean is_included = false;
				if (select.isEmpty())
				{
					is_included = true;
				}
				else
				{
					for (String role : select)
					{
						if (m.JazzMemberHasRoleWithLabel(role))
						{
							is_included = true;
							break;
						}
					}
				}

				if (is_included)
				{
					if (targetHasRoles) {
						if (assign.isEmpty())
						{
							for (IJazzRole r : m.JazzMemberRoles())
							{
								if (!excludeAssign.contains(r.getLabel()))
								{
									//if (!target_as_inherited.memberHasRole(m.JazzMemberUser.Name, r.Label))
									target_as_should_be.addMemberRole(m.getJazzMemberUser().getName(), m.getJazzMemberUser().isArchived(), r.getLabel());
								}
							}
						}
						else
						{
							for (String targetRole : assign)
							{
								//if (!target_as_inherited.memberHasRole(m.JazzMemberUser.Name, targetRole))
								target_as_should_be.addMemberRole(m.getJazzMemberUser().getName(), m.getJazzMemberUser().isArchived(), targetRole);
							}
						}
					}
					else
					{
						target_as_should_be.addMember(m.getJazzMemberUser().getName(), m.getJazzMemberUser().isArchived());
					}
				}
			}
			if (rtOptions.getopt_log() && additional_debug_log)
			{
				s_log.write(" recursive: source team '" + ta.getName() + "' contains " + srcCount + " members" + System.lineSeparator());
			}

			Iterable<IJazzTeamArea> taSubList = ta.JazzTeamAreas();

			RecursivelyCollectMembersOfTAs(taSubList, target_as_should_be, select, assign, excludeAssign,
											targetHasRoles, additional_debug_log, s_log);
		}
	}
}
