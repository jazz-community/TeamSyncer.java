package com.rus.alm.tools.teamsyncer.jazz;

public interface IJazzTeamArea {

	public abstract String getName();
	
	public abstract String getFullPath();

	public abstract IJazzTeamArea GetJazzSubTeamAreaByName(String name);

	public abstract IJazzTeamArea GetJazzSubTeamAreaByOid(String oid);

	public abstract Iterable<IJazzMember> JazzTeamMembers();

	public abstract IJazzMember GetMemberWithName(String MemberName);

	public abstract void DeleteMemberWithName(String MemberName);

	public abstract void AddMember(IJazzMember member);

	public abstract IJazzRole GetJazzRoleByLabel(String role);

	public abstract boolean HasMemberWithName(String name);

	public abstract Iterable<IJazzRole> JazzTeamRoles();

	public abstract Iterable<IJazzTeamArea> JazzTeamAreas();

}