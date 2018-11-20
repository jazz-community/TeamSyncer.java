package com.rus.alm.tools.teamsyncer.jazz.pjapi;

import com.ibm.team.repository.common.IContributor;
import com.rus.alm.tools.teamsyncer.jazz.IJazzUser;

public class JazzUser implements IJazzUser
{
	private String _name;

	@Override
	public final String getName()
	{
		return _name;
	}
	private String _nick;
	public final String getNick()
	{
		return _nick;
	}
	private String _mail;
	public final String getMailAddress()
	{
		return _mail;
	}
//	private String _url;
//	public final String getURL()
//	{
//		return _url;
//	}
	private boolean _archived = false;

	@Override
	public final boolean isArchived()
	{
		return _archived;
	}
	
	public JazzUser(IContributor contributor) {
		_name = contributor.getUserId();
		_nick = contributor.getName();
		_mail = contributor.getEmailAddress();
		_archived = contributor.isArchived();
	}
}