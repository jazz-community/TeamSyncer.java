package com.rus.alm.tools.teamsyncer.jazz.pjapi;

import java.util.*;

import com.ibm.team.repository.common.IContributor;

public class JazzUserRepository
{
	private HashMap<String, JazzUser> _user_dict;

	public JazzUserRepository()
	{
		_user_dict = new HashMap<String, JazzUser>();
	}

	public final JazzUser GetUser(String url)
	{
		return _user_dict.get(url);
	}

	public final JazzUser GetUserByName(String userName)
	{
		for (JazzUser ju : _user_dict.values())
		{
			if (ju.getName().equals(userName))
			{
				return ju;
			}
		}
		return null;
	}

	public final void AddUser(JazzUser user)
	{
		_user_dict.put(user.getName(), user);
	}

	public final JazzUser GetOrAddUser(IContributor contributor)
	{
		JazzUser u = GetUser(contributor.getUserId());
		if (u == null)
		{
			u = new JazzUser(contributor);
			AddUser(u);
		}
		return u;
	}
}