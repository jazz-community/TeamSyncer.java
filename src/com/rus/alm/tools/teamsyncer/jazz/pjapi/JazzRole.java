package com.rus.alm.tools.teamsyncer.jazz.pjapi;

import com.ibm.team.process.common.IRole2;
import com.rus.alm.tools.teamsyncer.jazz.IJazzRole;

public class JazzRole implements IJazzRole
{
	private String _id;
	private String _label;
	private String _description;

	public final String getId()
	{
		return _id;
	}

	@Override
	public final String getLabel()
	{
		return _label;
	}

	public final String getDescription()
	{
		return _description;
	}

	public JazzRole(IRole2 role)
	{
		this._id = role.getId();
		this._label = role.getRoleLabel();
		this._description = role.getDescription();
	}
}