package com.rus.alm.tools.teamsyncer;

import java.util.*;

public class MemberRoleAssignments
{
	private ArrayList<MemberRoleAssignment> _ma_list;

	public MemberRoleAssignments()
	{
		_ma_list = new ArrayList<MemberRoleAssignment>();
	}

	public MemberRoleAssignments(MemberRoleAssignments copyFrom)
	{
		_ma_list = new ArrayList<MemberRoleAssignment>();
		for (MemberRoleAssignment ma : copyFrom._ma_list)
		{
			_ma_list.add(new MemberRoleAssignment(ma));
		}
	}

	public final MemberRoleAssignment getMember(String name)
	{
		for (int i = 0; i < _ma_list.size(); i++)
		{
			if (_ma_list.get(i).getMember().equalsIgnoreCase(name))
			{
				return _ma_list.get(i);
			}
		}
		return null;
	}

	public final int getMemberCount()
	{
		return _ma_list.size();
	}
	
	public final void addMemberRole(String member, boolean isArchived, String role)
	{
		MemberRoleAssignment ass = getMember(member);
		if (ass == null)
		{
			ass = new MemberRoleAssignment(member, isArchived, role);
			_ma_list.add(ass);
		}
		else
		{
			ass.addRole(role);
		}
	}

	public final void addMember(String member, boolean isArchived)
	{
		MemberRoleAssignment ass = getMember(member);
		if (ass == null)
		{
			ass = new MemberRoleAssignment(member, isArchived);
			_ma_list.add(ass);
		}
	}

	public final boolean memberHasRole(String member, String role)
	{
		MemberRoleAssignment ass = getMember(member);
		if (ass != null)
		{
			return ass.hasRole(role);
		}
		return false;
	}

	public final void removeRoleFromMember(String member, String role)
	{
		MemberRoleAssignment ass = getMember(member);
		if (ass != null)
		{
			ass.removeRole(role);
		}
	}

	public final void removeMembersLackingRoles()
	{
		for (int i = 0; i < _ma_list.size(); i++)
		{
			if (_ma_list.get(i).hasNoRole())
			{
				_ma_list.remove(i--);
			}
		}
	}

	public final void removeArchivedMembers()
	{
		for (int i = 0; i < _ma_list.size(); i++)
		{
			if (_ma_list.get(i).isArchived())
			{
				_ma_list.remove(i--);
			}
		}
	}
	
	public final void reduceRolesToOne(String[] roleHierarchy)
	{
		for (int i = 0; i < _ma_list.size(); i++)
		{
			MemberRoleAssignment mra = _ma_list.get(i);
			int j = 0;
			while ((mra.roleCount() > 1) && (j < roleHierarchy.length)) {
				if (mra.hasRole(roleHierarchy[j]))
					mra.removeRole(roleHierarchy[j]);
				j++;
			}
			if (mra.hasNoRole())
				mra.addRole(roleHierarchy[0]);
		}
	}

	public final Iterable<MemberRoleAssignment> AllMembers()
	{
		if (_ma_list == null)
			return Collections.emptyList();
		else
			return _ma_list;
	}

	//public bool isMember(string member)
	//{
	//    for (int i = 0; i < _ma_list.Count; i++)
	//        if (_ma_list[i].Member.equalsIgnoreCase(member))
	//            return true;
	//    return false;
	//}

	public final void dumpAssignments()
	{
		for (int i = 0; i < _ma_list.size(); i++)
		{
			_ma_list.get(i).dump("  ");
		}
	}
}