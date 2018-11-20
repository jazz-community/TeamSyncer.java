package com.rus.alm.tools.teamsyncer.jazz.pjapi;

import java.util.ArrayList;

import com.ibm.team.process.client.IClientProcess;
import com.ibm.team.process.common.IProcessArea;
import com.ibm.team.process.common.IRole;
import com.ibm.team.process.common.IRole2;
import com.ibm.team.repository.common.IContributor;
import com.ibm.team.repository.common.IContributorHandle;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.rus.alm.tools.teamsyncer.jazz.IJazzMember;
import com.rus.alm.tools.teamsyncer.jazz.IJazzRole;
import com.rus.alm.tools.teamsyncer.jazz.IJazzUser;

public class JazzMember implements IJazzMember {

	private IJazzUser _user;
	private Jazz _owner;
	private ArrayList<IJazzRole> _roles;
	private IProcessArea _area;
	private IContributor _contrib;

	private boolean PostIsPending;
	
	@Override
	public final boolean getPostIsPending()
	{
		return PostIsPending;
	}
	
	@Override
	public final void setPostIsPending(boolean value)
	{
		PostIsPending = value;
	}
	
	public JazzMember(IContributor contributor, IProcessArea processArea, Jazz owner) {
		
		_owner = owner;
		_area = processArea;
		_contrib = contributor;
		_user = _owner.getUserRepository().GetOrAddUser(contributor);
		try {
			IClientProcess process = _owner.getItemService().getClientProcess(processArea, null);
			IRole[] contributorRoles = process.getContributorRoles(contributor, processArea, null);
			_roles = new ArrayList<IJazzRole>();
			for (int j = 0; j < contributorRoles.length; j++) {
				IRole2 role = (IRole2) contributorRoles[j];
				_roles.add(new JazzRole(role));
			}
		} catch (TeamRepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public JazzMember(IContributor contributor, Jazz owner) {
		
		_owner = owner;
		_area = null;
		_contrib = contributor;
		_user = _owner.getUserRepository().GetOrAddUser(contributor);
	}
	
	public void setMemberArea(IProcessArea area) {
		_area = area;
		try {
			IClientProcess process = _owner.getItemService().getClientProcess(_area, null);
			IRole[] contributorRoles = process.getContributorRoles(_contrib, _area, null);
			_roles = new ArrayList<IJazzRole>();
			for (int j = 0; j < contributorRoles.length; j++) {
				IRole2 role = (IRole2) contributorRoles[j];
				_roles.add(new JazzRole(role));
			}
		} catch (TeamRepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public IContributor getContributor() {
		return _contrib;
	}

	@Override
	public IJazzUser getJazzMemberUser() {
		return _user;
	}

	@Override
	public Iterable<IJazzRole> JazzMemberRoles() {
		return _roles;
	}

	@Override
	public boolean JazzMemberHasRoleWithLabel(String role) {

		if (_roles != null)
		{
			for (int i = 0; i < _roles.size(); i++)
			{
				if (role.equalsIgnoreCase(_roles.get(i).getLabel()))
				{
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void AssignRole(IJazzRole jr) {

		IContributorHandle ccHandle = (IContributorHandle) _contrib.getItemHandle();
		try {
			IClientProcess clientProcess = _owner.getItemService().getClientProcess(_area, null);
			IRole2[] availableRoles = (IRole2[]) clientProcess.getRoles(_area, null);
			IRole[] roleToSet = new IRole[1];
			for (int i = 0; i < availableRoles.length; i++) {
				String roleLabel = availableRoles[i].getRoleLabel();
				if (roleLabel.equalsIgnoreCase(jr.getLabel())) {
			        IProcessArea aCopy = (IProcessArea) _area.getWorkingCopy();
					roleToSet[0] = (IRole) availableRoles[i];
					aCopy.addRoleAssignments(ccHandle, roleToSet);
					_owner.getItemService().save(aCopy, null);
					break;
				}
			}
		} catch (TeamRepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void UnassignRole(String role) {

		IContributorHandle ccHandle = (IContributorHandle) _contrib.getItemHandle();
		try {
			IClientProcess clientProcess = _owner.getItemService().getClientProcess(_area, null);
			IRole2[] availableRoles = (IRole2[]) clientProcess.getRoles(_area, null);
			IRole[] roleToSet = new IRole[1];
			for (int i = 0; i < availableRoles.length; i++) {
				String roleLabel = availableRoles[i].getRoleLabel();
				if (roleLabel.equalsIgnoreCase(role)) {
			        IProcessArea aCopy = (IProcessArea) _area.getWorkingCopy();
					roleToSet[0] = (IRole) availableRoles[i];
					aCopy.removeRoleAssignments(ccHandle, roleToSet);
					_owner.getItemService().save(aCopy, null);
					break;
				}
			}
		} catch (TeamRepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void AddRole(IJazzRole jazzRole) {

		if (_roles == null)
		{
			_roles = new ArrayList<IJazzRole>();
		}
		if (!JazzMemberHasRoleWithLabel(jazzRole.getLabel()))
		{
			_roles.add(jazzRole);
		}
	}

	@Override
	public void SetRoles() {

		IContributorHandle ccHandle = (IContributorHandle) _contrib.getItemHandle();
		try {
			IClientProcess clientProcess = _owner.getItemService().getClientProcess(_area, null);
			IRole2[] availableRoles = (IRole2[]) clientProcess.getRoles(_area, null);
			IRole[] rolesToSet = new IRole[_roles.size()];
			for (int i = 0; i < _roles.size(); i++) {
				for (int j = 0; j < availableRoles.length; j++) {
					String roleLabel = availableRoles[j].getRoleLabel();
					if (roleLabel.equalsIgnoreCase(_roles.get(i).getLabel())) {
						rolesToSet[i] = (IRole) availableRoles[j];
						break;
					}
				}
			}
			IProcessArea aCopy = (IProcessArea) _area.getWorkingCopy();
			aCopy.addRoleAssignments(ccHandle, rolesToSet);
			_owner.getItemService().save(aCopy, null);
			
		} catch (TeamRepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
