package com.rus.alm.tools.teamsyncer;

import java.util.*;

import com.rus.alm.tools.teamsyncer.jazz.IJazz;

public class RepoManager
{
	private HashMap<String, IJazz> repoDict;
	private String _account;
	private String _password;

	public RepoManager(String account, String password)
	{
		repoDict = new HashMap<String, IJazz>();
		_account = account;
		_password = password;
	}

	public final IJazz GetRepository(String repositoryUrl)
	{
		IJazz repo;
		repo = repoDict.get(repositoryUrl);
		if (repo == null) {
			// user-specific as login to .../rm at R&S not working with PlainJavaAPI due to Kerberos
			if (repositoryUrl.endsWith("/rm"))
				repo = new com.rus.alm.tools.teamsyncer.jazz.oslc.Jazz(repositoryUrl, _account, _password);
			else
				repo = new com.rus.alm.tools.teamsyncer.jazz.pjapi.Jazz(repositoryUrl, _account, _password);
			repoDict.put(repositoryUrl, repo);
		}
		return repo;
	}
}
