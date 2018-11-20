package com.rus.alm.tools.teamsyncer.artifactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jfrog.artifactory.client.impl.ArtifactoryRequestImpl;
import org.jfrog.artifactory.client.model.Group;
import org.jfrog.artifactory.client.model.User;
import org.jfrog.artifactory.client.Artifactory;
import org.jfrog.artifactory.client.ArtifactoryRequest;
import org.jfrog.artifactory.client.ArtifactoryClientBuilder;
import org.jfrog.artifactory.client.ArtifactoryResponse;

import com.google.gson.Gson;

public class ArtifactoryGroupHandler {

	private Artifactory _artifactory = null;
	private HashMap<String, ArrayList<String>> groupMemberMap = new HashMap<String, ArrayList<String>>();
	private Gson gson = new Gson();

	// username and password are user-specific
	public ArtifactoryGroupHandler(String artifactoryURL) {

		_artifactory =  ArtifactoryClientBuilder.create()
				.setUrl(artifactoryURL)
				.setUsername("<admin account>")
				.setPassword("<password>")
				.build();
	}

	public boolean isGroupExisting(String groupName) {

		try {
			Group g = _artifactory.security().group(groupName);
			if (g.getName().equalsIgnoreCase(groupName))
				return true;
		} catch (Exception e) {}
		return false;
	}

	public ArrayList<String> getGroupMemberList_old(String targetGroup) {

		String key = targetGroup.toLowerCase();
		if (groupMemberMap.containsKey(key))
			return groupMemberMap.get(targetGroup.toLowerCase());
		else
			return new ArrayList<String>();
	}

	@SuppressWarnings("unchecked")
	public ArrayList<String> getGroupMemberList(String groupName) {

		ArtifactoryRequest repositoryRequest = new ArtifactoryRequestImpl().apiUrl("api/groups/" + groupName)
		    	.method(ArtifactoryRequest.Method.GET)
		    	.responseType(ArtifactoryRequest.ContentType.JSON);
		//_artifactory.restCall(repositoryRequest);
		try {
			ArtifactoryResponse response = _artifactory.restCall(repositoryRequest);
			if (response.getStatusLine().getStatusCode() == 200) {
				Map<String, ArrayList<String>> parsedBody = response.parseBody(Map.class);
				return parsedBody.get("usersInGroup");
			}
		} catch (Exception e) {}
		return new ArrayList<String>();
	}

	public String getExactAccountName(String member) {

		try {
			User u = _artifactory.security().user(member);
			return u.getName();
		} catch (Exception e) {
			return "";
		}
	}

	@SuppressWarnings("unchecked")
	public boolean addGroupMember(String targetGroup, String userAccountName) {

		ArtifactoryRequest getRequest = new ArtifactoryRequestImpl().apiUrl("api/groups/" + targetGroup)
			    	.method(ArtifactoryRequest.Method.GET)
			    	.responseType(ArtifactoryRequest.ContentType.JSON);

		ArtifactoryResponse response, putResponse;
		try {
			response = _artifactory.restCall(getRequest);
			if (response.getStatusLine().getStatusCode() == 200) {
				Map<String, ArrayList<String>> parsedBody = response.parseBody(Map.class);
				if (!parsedBody.get("usersInGroup").contains(userAccountName)) {
					parsedBody.get("usersInGroup").add(userAccountName);
					String json = gson.toJson(parsedBody);
					ArtifactoryRequest putRequest = new ArtifactoryRequestImpl().apiUrl("api/groups/" + targetGroup)
							.method(ArtifactoryRequest.Method.PUT)
							.requestType(ArtifactoryRequest.ContentType.JSON)
							.requestBody(json);
					putResponse = _artifactory.restCall(putRequest);
					if (putResponse.getStatusLine().getStatusCode() == 200) {
						return true;
					}
				}
				else
					return true;
			}
		} catch (Exception e) {}
		return false;
	}

	@SuppressWarnings("unchecked")
	public boolean removeGroupMember(String targetGroup, String member) {

		ArtifactoryRequest getRequest = new ArtifactoryRequestImpl().apiUrl("api/groups/" + targetGroup)
		    	.method(ArtifactoryRequest.Method.GET)
		    	.responseType(ArtifactoryRequest.ContentType.JSON);

		ArtifactoryResponse response, putResponse;
		try {
			response = _artifactory.restCall(getRequest);
			if (response.getStatusLine().getStatusCode() == 200) {
				Map<String, ArrayList<String>> parsedBody = response.parseBody(Map.class);
				if (parsedBody.get("usersInGroup").contains(member)) {
					parsedBody.get("usersInGroup").remove(member);
					String json = gson.toJson(parsedBody);
					ArtifactoryRequest putRequest = new ArtifactoryRequestImpl().apiUrl("api/groups/" + targetGroup)
							.method(ArtifactoryRequest.Method.PUT)
							.requestType(ArtifactoryRequest.ContentType.JSON)
							.requestBody(json);
					putResponse = _artifactory.restCall(putRequest);
					if (putResponse.getStatusLine().getStatusCode() == 200) {
						return true;
					}
				}
				else
					return true;
			}
		} catch (Exception e) {}
		return false;
	}

	@SuppressWarnings("unchecked")
	public boolean setGroupMembers(String targetGroup, List<String> memberList) {

		ArtifactoryRequest getRequest = new ArtifactoryRequestImpl().apiUrl("api/groups/" + targetGroup)
			    	.method(ArtifactoryRequest.Method.GET)
			    	.responseType(ArtifactoryRequest.ContentType.JSON);

		ArtifactoryResponse response, putResponse;
		try {
			response = _artifactory.restCall(getRequest);
			if (response.getStatusLine().getStatusCode() == 200) {
				Map<String, ArrayList<String>> parsedBody = response.parseBody(Map.class);
				parsedBody.get("usersInGroup").clear();
				parsedBody.get("usersInGroup").addAll(memberList);
				String json = gson.toJson(parsedBody);
				ArtifactoryRequest putRequest = new ArtifactoryRequestImpl().apiUrl("api/groups/" + targetGroup)
						.method(ArtifactoryRequest.Method.PUT)
						.requestType(ArtifactoryRequest.ContentType.JSON)
						.requestBody(json);
				putResponse = _artifactory.restCall(putRequest);
				if (putResponse.getStatusLine().getStatusCode() == 200) {
					return true;
				}
			}
		} catch (Exception e) {}
		return false;
	}
}
