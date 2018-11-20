package com.rus.alm.tools.teamsyncer.jazz.oslc;

import java.util.*;

import com.rus.alm.tools.teamsyncer.jazz.IJazz;

public class JazzUserRepository
{
	private HashMap<String, JazzUser> _user_dict;

	public JazzUserRepository(IJazz owner)
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
		_user_dict.put(user.getURL(), user);
	}

	public final JazzUser GetOrAddUser(String url, Jazz owner)
	{
		JazzUser u = GetUser(url);
		if (u == null)
		{
			u = new JazzUser(url, owner);
			AddUser(u);
		}
		return u;
	}
}