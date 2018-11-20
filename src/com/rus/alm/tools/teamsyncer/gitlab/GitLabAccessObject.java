package com.rus.alm.tools.teamsyncer.gitlab;

import java.util.List;

import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.AccessLevel;
import org.gitlab4j.api.models.Group;
import org.gitlab4j.api.models.Member;
import org.gitlab4j.api.models.Project;
import org.gitlab4j.api.models.User;

import com.rus.alm.tools.teamsyncer.MemberRoleAssignments;

public class GitLabAccessObject {

	private GitLabApi gitLabApi;
	private Project currentProject = null;
	private Group currentGroup = null;
	private boolean currentAccessObjectIsGroup;
	// The following array T contains access codes for the GitLab servers of array V, both arrays are user-specific 
	private String[] T = {"xxx", "xxx",
							"xxx", "xxx"};
	private String[] V = {"code", "code2", "codequal", "codedev"};

	private String getT(String Url) {
		String s = Url.toLowerCase().replace("https://", "").replace(".rsint.net", "");
		for (int i = 0; i < V.length; i++)
			if (s.equalsIgnoreCase(V[i]))
				return T[i];
		return "";
	}

	@SuppressWarnings("serial")
	public static class AccessObjectNotFoundException extends RuntimeException
	{
		public AccessObjectNotFoundException()
		{
		}

		public AccessObjectNotFoundException(String message)
		{
			super(message);
		}

		public AccessObjectNotFoundException(String message, RuntimeException inner)
		{
			super(message, inner);
		}
	}

	public GitLabAccessObject(String gitLabUrl, String accessObjectPath) {

		gitLabApi = new GitLabApi(gitLabUrl, getT(gitLabUrl));
		//n3DEXxxhfxC6paMuzMef

		try {
			currentGroup = gitLabApi.getGroupApi().getGroup(accessObjectPath);
			if (currentGroup != null)
				currentAccessObjectIsGroup = true;

		} catch (GitLabApiException e1) {
			try {
				String ns, pn;
				if (accessObjectPath.indexOf("/") >=0) {
					ns = accessObjectPath.substring(0, accessObjectPath.lastIndexOf("/"));
					pn = accessObjectPath.substring(accessObjectPath.lastIndexOf("/") + 1);
				} else {
					ns = "";
					pn = accessObjectPath;
				}
				currentProject = gitLabApi.getProjectApi().getProject(ns, pn);
				if (currentProject != null)
					currentAccessObjectIsGroup = false;

			} catch (GitLabApiException e2) {
				throw new AccessObjectNotFoundException("GitLab group or project not found or not accessible");
			}
		}
	}

	public MemberRoleAssignments getMemberRoleAssignementsForAccessObject() {

		MemberRoleAssignments mras = new MemberRoleAssignments();
		try {
			List<Member> members;
			if (currentAccessObjectIsGroup) {
				members = gitLabApi.getGroupApi().getMembers(currentGroup.getId());
			} else {
				members = gitLabApi.getProjectApi().getMembers(currentProject.getId());
			}
			for (Member m: members) {
				mras.addMemberRole(m.getUsername(), false, m.getAccessLevel().name().toLowerCase());
			}
		} catch (GitLabApiException e) {
			return null;
		}
		return mras;
	}

	int roleToAccessLevel(String role) {
		AccessLevel[] al = AccessLevel.values();
		for (int i = 0; i < al.length; i++)
			if (al[i].name().equalsIgnoreCase(role))
				return al[i].toValue();
		return 0;
	}

	public String findUser(String user) {

		try {
			List<User> uList = gitLabApi.getUserApi().findUsers(user);
			for (User u: uList) {
				String n = u.getUsername();
				if (n.equalsIgnoreCase(user))
					return n;
			}
		} catch (GitLabApiException e) {}

		return "";
	}

	public void AddAccessObjectMember(String member) {

		try {
			User u = gitLabApi.getUserApi().getUser(member);
			if (currentAccessObjectIsGroup) {
				gitLabApi.getGroupApi().addMember(currentGroup.getId(), u.getId(), roleToAccessLevel("guest"));
			} else {
				gitLabApi.getProjectApi().addMember(currentProject.getId(), u.getId(), roleToAccessLevel("guest"));
			}

		} catch (GitLabApiException e) {
			throw new RuntimeException("Could not add user to group or project");
		}
	}

	public void AddAccessObjectMember(String member, String role) {

		try {
			User u = gitLabApi.getUserApi().getUser(member);
			if (currentAccessObjectIsGroup) {
				gitLabApi.getGroupApi().addMember(currentGroup.getId(), u.getId(), roleToAccessLevel(role));
			} else {
				gitLabApi.getProjectApi().addMember(currentProject.getId(), u.getId(), roleToAccessLevel(role));
			}

		} catch (GitLabApiException e) {
			throw new RuntimeException("Could not add user & accesslevel to group or project");
		}
	}

	public void UpdateAccessObjectMember(String member, String role) {

		try {
			User u = gitLabApi.getUserApi().getUser(member);
			if (currentAccessObjectIsGroup) {
				gitLabApi.getGroupApi().updateMember(currentGroup.getId(), u.getId(), roleToAccessLevel(role));
			} else {
				gitLabApi.getProjectApi().updateMember(currentProject.getId(), u.getId(), roleToAccessLevel(role));
			}

		} catch (GitLabApiException e) {
			throw new RuntimeException("Could not update user accesslevel in group or project");
		}
	}

	public void RemoveAccessObjectMember(String member) {

		try {
			User u = gitLabApi.getUserApi().getUser(member);
			if (currentAccessObjectIsGroup) {
				gitLabApi.getGroupApi().removeMember(currentGroup.getId(), u.getId());
			} else {
				gitLabApi.getProjectApi().removeMember(currentProject.getId(), u.getId());
			}

		} catch (GitLabApiException e) {
			throw new RuntimeException("Could not remove user from group or project");
		}
	}
}
