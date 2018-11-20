package com.rus.alm.tools.teamsyncer.jazz.pjapi;

import java.util.*;

import com.ibm.team.process.common.IRole2;

public class JazzRoleCache
{
	private HashMap<String, JazzRole> _role_dict;

	public JazzRoleCache(Jazz owner)
	{
		_role_dict = new HashMap<String, JazzRole>();
	}

	public final JazzRole GetRole(String id)
	{
		return _role_dict.get(id);
	}

	public final void AddRole(JazzRole role)
	{
		_role_dict.put(role.getId(), role);
	}

	public final JazzRole GetOrAddRole(IRole2 role)
	{
		JazzRole r = GetRole(role.getId());
		if (r == null)
		{
			r = new JazzRole(role);
			AddRole(r);
		}
		return r;
	}
}