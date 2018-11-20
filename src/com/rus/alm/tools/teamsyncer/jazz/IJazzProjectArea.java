package com.rus.alm.tools.teamsyncer.jazz;

public interface IJazzProjectArea {

	public abstract String getName();

	public abstract IJazzTeamArea GetJazzTeamAreaByName(String name);

	public abstract IJazzTeamArea GetJazzTeamAreaByOid(String oid);

	public abstract Iterable<IJazzMember> JazzProjectMembers();

	public abstract IJazzMember GetMemberWithName(String MemberName);

	public abstract void DeleteMemberWithName(String MemberName);

	public abstract void AddMember(IJazzMember jm);

	public abstract IJazzRole GetJazzRoleByLabel(String role);

	public abstract Iterable<IJazzTeamArea> JazzTeamAreas();

	public abstract Iterable<IJazzRole> JazzProjectRoles();

}