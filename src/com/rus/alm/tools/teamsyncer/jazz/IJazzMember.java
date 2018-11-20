package com.rus.alm.tools.teamsyncer.jazz;

public interface IJazzMember {

	public abstract IJazzUser getJazzMemberUser();

	public abstract Iterable<IJazzRole> JazzMemberRoles();

	public abstract boolean JazzMemberHasRoleWithLabel(String role);

	public abstract void AssignRole(IJazzRole jr);

	public abstract void UnassignRole(String role);

	public abstract void AddRole(IJazzRole jazzRole);

	public abstract void SetRoles();

	public abstract boolean getPostIsPending();

	public abstract void setPostIsPending(boolean b);

}