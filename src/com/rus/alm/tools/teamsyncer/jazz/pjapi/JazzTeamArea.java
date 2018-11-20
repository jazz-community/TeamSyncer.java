package com.rus.alm.tools.teamsyncer.jazz.pjapi;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import com.ibm.team.process.client.IClientProcess;
import com.ibm.team.process.client.IProcessItemService;
import com.ibm.team.process.common.IProcessArea;
import com.ibm.team.process.common.IRole2;
import com.ibm.team.process.common.ITeamArea;
import com.ibm.team.process.common.ITeamAreaHandle;
import com.ibm.team.process.common.ITeamAreaHierarchy;
import com.ibm.team.repository.client.IItemManager;
import com.ibm.team.repository.client.ITeamRepository;
import com.ibm.team.repository.common.IContributor;
import com.ibm.team.repository.common.IContributorHandle;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.repository.common.UUID;
import com.rus.alm.tools.teamsyncer.jazz.IJazzMember;
import com.rus.alm.tools.teamsyncer.jazz.IJazzRole;
import com.rus.alm.tools.teamsyncer.jazz.IJazzTeamArea;

public class JazzTeamArea implements IJazzTeamArea {

	private ITeamArea teamArea;
	private ITeamRepository teamRepository;
	private IProcessItemService itemService;
	private ITeamAreaHierarchy _taH;
	private String hierarchyPath;
	private Jazz _owner;
	private ArrayList<IJazzTeamArea> _ta_list = null;
	private ArrayList<IJazzMember> _mem_list = null;
	private ArrayList<IJazzRole> _role_list = null;

	public JazzTeamArea(ITeamArea teamArea, Jazz owner, ITeamAreaHierarchy taH, String currentHierarchyPath) {
		this.teamArea = teamArea;
		this._owner = owner;
		this.teamRepository = _owner.getTeamRepository();
		this.itemService = _owner.getItemService();
		this._taH = taH;
		this.hierarchyPath = currentHierarchyPath + "/" + teamArea.getName();
	}

	@Override
	public String getName() {
		return teamArea.getName();
	}

	@Override
	public String getFullPath() {
		return hierarchyPath;
	}

	public String getOid() {
		return teamArea.getItemId().getUuidValue();
	}

	@Override
	public IJazzTeamArea GetJazzSubTeamAreaByName(String name) {

		if (_ta_list == null) {
			try {
				String hName = hierarchyPath + "/" + name;
				//teamArea.
				ITeamArea ta = (ITeamArea) itemService.findProcessArea(URI.create(hName.replaceAll(" ", "%20")), null, null);
				if (ta != null)
					return new JazzTeamArea(ta, _owner, _taH, hierarchyPath);

			} catch (TeamRepositoryException e)	{}

		} else {
			for (int i = 0; i < _ta_list.size(); i++)
				if (name.equalsIgnoreCase(_ta_list.get(i).getName()))
					return _ta_list.get(i);
		}
		return null;
	}

	@Override
	public IJazzTeamArea GetJazzSubTeamAreaByOid(String oid) {

		if (_ta_list == null) {

			UUID taUUID = UUID.valueOf(oid);
			ITeamAreaHandle taHandle = (ITeamAreaHandle) ITeamArea.ITEM_TYPE.createItemHandle(taUUID, null);
			try {
				ITeamArea ta = (ITeamArea) teamRepository.itemManager().fetchCompleteItem(taHandle, IItemManager.DEFAULT, null);
				if (ta != null)
					return new JazzTeamArea(ta, _owner, _taH, this.getName());

			} catch (TeamRepositoryException e) {}

		} else {
			for (int i = 0; i < _ta_list.size(); i++)
				if (oid.equalsIgnoreCase(((JazzTeamArea)_ta_list.get(i)).getOid()))
					return _ta_list.get(i);
		}
		return null;
	}

	private void GetMembers()
	{
		_mem_list = new ArrayList<IJazzMember>();
		IContributorHandle[] contributors = teamArea.getMembers();
		for (int i = 0; i < contributors.length; i++) {
			IContributorHandle handle = (IContributorHandle) contributors[i];
			try {
				IContributor contributor = (IContributor) teamRepository.itemManager().fetchCompleteItem(handle, IItemManager.DEFAULT, null);
				_mem_list.add(new JazzMember(contributor, teamArea, _owner));

			} catch (TeamRepositoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public final Iterable<IJazzMember> JazzTeamMembers()
	{
		if (_mem_list == null)
		{
			GetMembers();
		}

		if (_mem_list == null)
			return Collections.emptyList();
		else
			return _mem_list;
	}

	@Override
	public final boolean HasMemberWithName(String MemberName)
	{
		if (_mem_list == null)
		{
			GetMembers();
		}

		if (_mem_list != null)
		{
			for (int i = 0; i < _mem_list.size(); i++)
			{
				if (MemberName.equalsIgnoreCase(_mem_list.get(i).getJazzMemberUser().getName()))
				{
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public final IJazzMember GetMemberWithName(String MemberName)
	{
		if (_mem_list == null)
		{
			GetMembers();
		}

		if (_mem_list != null)
		{
			for (int i = 0; i < _mem_list.size(); i++)
			{
				if (MemberName.equalsIgnoreCase(_mem_list.get(i).getJazzMemberUser().getName()))
				{
					return _mem_list.get(i);
				}
			}
		}
		return null;
	}

	@Override
	public final void DeleteMemberWithName(String MemberName)
	{
		if (_mem_list == null)
		{
			GetMembers();
		}

		if (_mem_list != null)
		{
			for (int i = 0; i < _mem_list.size(); i++)
			{
				if (MemberName.equalsIgnoreCase(_mem_list.get(i).getJazzMemberUser().getName()))
				{
					IContributorHandle ccHandle = (IContributorHandle) ((JazzMember) _mem_list.get(i)).getContributor().getItemHandle();
					IProcessArea aCopy = (IProcessArea) teamArea.getWorkingCopy();
					try {
						IClientProcess clientProcess = _owner.getItemService().getClientProcess(teamArea, null);
						aCopy.removeRoleAssignments(ccHandle, clientProcess.getRoles(teamArea, null));
						aCopy.removeMember(ccHandle);
						itemService.save(aCopy, null);
					} catch (TeamRepositoryException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return;
				}
			}
		}
	}

	public final void AddMemberPending(IJazzMember member)
	{
		//member.setPostIsPending(true);
		//_mem_list.add(member);
		System.out.println("AddMemberPending (TeamArea) not implemented.");
	}

	@Override
	public void AddMember(IJazzMember member) {

		IContributorHandle ccHandle = (IContributorHandle) ((JazzMember) member).getContributor().getItemHandle();
		IProcessArea aCopy = (IProcessArea) teamArea.getWorkingCopy();
		aCopy.addMember(ccHandle);
		try {
			itemService.save(aCopy, null);
		} catch (TeamRepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		((JazzMember) member).setMemberArea(teamArea);
	}

	@Override
	public IJazzRole GetJazzRoleByLabel(String role) {

		for (IJazzRole jr : this.JazzTeamRoles())
		{
			if (jr.getLabel().equals(role))
			{
				return jr;
			}
		}
		return null;
	}

	public void PostNewMembers() {

		System.out.println("PostNewMembers (TeamArea) not implemented.");
	}

	@Override
	public Iterable<IJazzRole> JazzTeamRoles() {

		if (_role_list == null)
		{
			_role_list = new ArrayList<IJazzRole>();
			try {
				IClientProcess clientProcess = _owner.getItemService().getClientProcess(teamArea, null);
				IRole2[] availableRoles = (IRole2[]) clientProcess.getRoles(teamArea, null);
				_role_list = new ArrayList<IJazzRole>();
				for (int i = 0; i < availableRoles.length; i++)
					_role_list.add(new JazzRole(availableRoles[i]));

			} catch (TeamRepositoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		if (_role_list == null)
			return Collections.emptyList();
		else
			return _role_list;
	}

	@Override
	public Iterable<IJazzTeamArea> JazzTeamAreas() {

		if (_ta_list == null) {

			_ta_list = new ArrayList<IJazzTeamArea>();
			@SuppressWarnings("unchecked")
			Set<ITeamAreaHandle> childrenSet = _taH.getChildren((ITeamAreaHandle) teamArea.getItemHandle());
			for (ITeamAreaHandle taHandle: childrenSet) {
				ITeamArea ta;
				try {
					ta = (ITeamArea) teamRepository.itemManager().fetchCompleteItem(taHandle, IItemManager.DEFAULT, null);
					if (ta != null)
						_ta_list.add(new JazzTeamArea(ta, _owner, _taH, this.hierarchyPath));			

				} catch (TeamRepositoryException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		if (_ta_list == null)
			return Collections.emptyList();
		else
			return _ta_list;
	}
}
