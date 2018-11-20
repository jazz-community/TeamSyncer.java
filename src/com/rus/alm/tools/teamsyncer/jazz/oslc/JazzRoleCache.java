package com.rus.alm.tools.teamsyncer.jazz.oslc;

import java.util.*;

public class JazzRoleCache
{
	private HashMap<String, JazzRole> _role_dict;
	private Jazz _owner;

	public JazzRoleCache(Jazz owner)
	{
		_role_dict = new HashMap<String, JazzRole>();
		_owner = owner;
	}

	public final JazzRole GetRole(String url)
	{
		return _role_dict.get(url);
	}

	public final void AddRole(JazzRole role)
	{
		_role_dict.put(role.getURL(), role);
	}

	public final JazzRole GetOrAddRole(String url)
	{
		JazzRole r = GetRole(url);
		if (r == null)
		{
			r = new JazzRole(url, _owner);
			AddRole(r);
		}
		return r;
	}
}