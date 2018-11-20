package com.rus.alm.tools.teamsyncer;

import java.util.*;

public class MemberRoleAssignment
{
	private String _member;
	private boolean _isArchived;
	private HashMap<String, String> _roles;

	public final String getMember()
	{
		return _member;
	}

	public MemberRoleAssignment(String Member, boolean isArchived)
	{
		_member = Member;
		_isArchived = isArchived;
		_roles = new HashMap<String, String>();
	}

	public MemberRoleAssignment(String Member, boolean isArchived, String role)
	{
		_member = Member;
		_isArchived = isArchived;
		_roles = new HashMap<String, String>();
		if (!role.equals("default"))
			_roles.put(role.toLowerCase(), role);
	}

	public MemberRoleAssignment(MemberRoleAssignment copyFrom)
	{
		_member = copyFrom._member;
		_roles = new HashMap<String, String>();
		for (String r: copyFrom._roles.keySet())
			_roles.put(r, copyFrom._roles.get(r));
	}

	public final boolean hasRole(String role)
	{
		return _roles.containsKey(role.toLowerCase());
	}

	public final boolean isArchived()
	{
		return _isArchived;
	}

	public final boolean hasNoRole()
	{
		return (_roles.isEmpty());
	}

	public final void addRole(String role)
	{
		if (!hasRole(role) && (!role.equals("default")))
			_roles.put(role.toLowerCase(), role);
	}

	public final void addDefaultRole()
	{
		if (!hasRole("default"))
			_roles.put("default", "default");
	}

	public final void removeRole(String role)
	{
		if (_roles.containsKey(role.toLowerCase()))
			_roles.remove(role.toLowerCase());
	}

	public final Iterable<String> AllRoles()
	{
		if (_roles == null)
			return Collections.emptyList();
		else
			return _roles.values();
	}
	
	public final int roleCount()
	{
		return _roles.size();
	}

	public final void dump(String ident)
	{
		System.out.println(ident + _member);
		for (String role: _roles.values())
			System.out.println(ident + "  " + role);
	}
}