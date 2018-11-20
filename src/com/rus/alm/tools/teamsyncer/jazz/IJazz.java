package com.rus.alm.tools.teamsyncer.jazz;

public interface IJazz {

	public enum repositoryType {
		ccm,
		rm,
		qm;
	}

	public abstract IJazzProjectArea GetJazzProjectAreaByName(String name);

	public abstract IJazzProjectArea GetJazzProjectAreaByOid(String oid);

	public abstract Iterable<IJazzProjectArea> JazzProjectAreas();
	
	public abstract IJazzMember GetNewJazzMember(String userName);
	
	public abstract IJazzTeamArea GetJazzTeamAreaByOid(String oid);

	public abstract void close();

}