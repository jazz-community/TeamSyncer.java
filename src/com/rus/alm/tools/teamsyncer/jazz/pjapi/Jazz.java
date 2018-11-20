package com.rus.alm.tools.teamsyncer.jazz.pjapi;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.ibm.team.process.client.IProcessItemService;
import com.ibm.team.process.common.IProjectArea;
import com.ibm.team.process.common.IProjectAreaHandle;
import com.ibm.team.process.common.ITeamArea;
import com.ibm.team.process.common.ITeamAreaHandle;
import com.ibm.team.process.common.ITeamAreaHierarchy;
import com.ibm.team.repository.client.IItemManager;
import com.ibm.team.repository.client.ITeamRepository;
import com.ibm.team.repository.client.TeamPlatform;
import com.ibm.team.repository.client.ITeamRepository.ILoginHandler;
import com.ibm.team.repository.client.ITeamRepository.ILoginHandler.ILoginInfo;
import com.ibm.team.repository.common.IContributor;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.repository.common.UUID;
import com.ibm.team.repository.transport.client.InvalidUserCredentialsException;
import com.rus.alm.tools.teamsyncer.jazz.IJazz;
import com.rus.alm.tools.teamsyncer.jazz.IJazzMember;
import com.rus.alm.tools.teamsyncer.jazz.IJazzProjectArea;
import com.rus.alm.tools.teamsyncer.jazz.IJazzTeamArea;

public class Jazz implements IJazz {
	
	private ITeamRepository teamRepository;
	private IProcessItemService itemService;
	private JazzUserRepository _user_repo;
	private ArrayList<IJazzProjectArea> _pa_list = null;
	private boolean platformIsUp = false;
	
	public ITeamRepository getTeamRepository() {
		return teamRepository;
	}

	public IProcessItemService getItemService() {
		return itemService;
	}

	public JazzUserRepository getUserRepository() {
		return _user_repo;
	}

    private static class LoginHandler implements ILoginHandler, ILoginInfo {

		private String fUserId;
		private String fPassword;

		private LoginHandler(String userId, String password) {
			fUserId = userId;
			fPassword = password;
		}

		public String getUserId() {
			return fUserId;
		}

		public String getPassword() {
			return fPassword;
		}

		public ILoginInfo challenge(ITeamRepository repository) {
			return this;
		}
	}

	public Jazz(String repositoryURL, repositoryType rType, String username, String password)
	{
		String jazzAppl;
		if (rType == repositoryType.ccm) 		jazzAppl = "ccm";
		else if (rType == repositoryType.rm)    jazzAppl = "rm";
		else									jazzAppl = "qm";
		
		TeamPlatform.startup();
		platformIsUp = true;
		teamRepository = TeamPlatform
				.getTeamRepositoryService().getTeamRepository(repositoryURL + "/" + jazzAppl);
		teamRepository.registerLoginHandler(new LoginHandler(username,password));
		try {
			teamRepository.login(null);
			//System.out.println("Logged in as: "	+ teamRepository.loggedInContributor().getName());
			itemService = (IProcessItemService) teamRepository.getClientLibrary(IProcessItemService.class);
			_user_repo = new JazzUserRepository();

		} catch (InvalidUserCredentialsException e) {
			TeamPlatform.shutdown();
			platformIsUp = false;
			throw new AuthenticationException("Authentication failure: " + e.getMessage());
		} catch (TeamRepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			TeamPlatform.shutdown();
			platformIsUp = false;
		}
	}

	public Jazz(String repositoryExtendedURL, String username, String password) {

		TeamPlatform.startup();
		platformIsUp = true;
		teamRepository = TeamPlatform
				.getTeamRepositoryService().getTeamRepository(repositoryExtendedURL);
		teamRepository.registerLoginHandler(new LoginHandler(username,password));
		try {
			teamRepository.login(null);
			//System.out.println("Logged in as: "	+ teamRepository.loggedInContributor().getName());
			itemService = (IProcessItemService) teamRepository.getClientLibrary(IProcessItemService.class);
			_user_repo = new JazzUserRepository();

		} catch (InvalidUserCredentialsException e) {
			TeamPlatform.shutdown();
			platformIsUp = false;
			throw new AuthenticationException("Authentication failure: " + e.getMessage());
		} catch (TeamRepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			TeamPlatform.shutdown();
			platformIsUp = false;
		}
	}

	@Override
	public IJazzProjectArea GetJazzProjectAreaByName(String name) {

		if (_pa_list == null) {

			try {
				IProjectArea pa = (IProjectArea) itemService.findProcessArea(URI.create(name.replaceAll(" ", "%20")), null, null);
				if (pa != null)
					return new JazzProjectArea(pa, this);

			} catch (TeamRepositoryException e)	{}

		} else {
			for (int i = 0; i < _pa_list.size(); i++)
				if (name.equalsIgnoreCase(_pa_list.get(i).getName()))
					return _pa_list.get(i);
		}
		return null;
	}

	@Override
	public IJazzProjectArea GetJazzProjectAreaByOid(String oid) {
		
		if (_pa_list == null) {

			UUID paUUID = UUID.valueOf(oid);
			IProjectAreaHandle paHandle = (IProjectAreaHandle) IProjectArea.ITEM_TYPE.createItemHandle(paUUID, null);
			try {
				IProjectArea pa = (IProjectArea) teamRepository.itemManager().fetchCompleteItem(paHandle, IItemManager.DEFAULT, null);
				if (pa != null)
					return new JazzProjectArea(pa, this);

			} catch (TeamRepositoryException e1) {
			} catch (ClassCastException e2) {}

		} else {
			for (int i = 0; i < _pa_list.size(); i++)
				if (oid.equalsIgnoreCase(((JazzProjectArea)_pa_list.get(i)).getOid()))
					return _pa_list.get(i);
		}
		return null;
	}

	@Override
	public Iterable<IJazzProjectArea> JazzProjectAreas() {

		if (_pa_list == null) {
			
			_pa_list = new ArrayList<IJazzProjectArea>();
			try {
				@SuppressWarnings("unchecked")
				List<IProjectArea> paList = itemService.findAllProjectAreas(null, null);
				for (IProjectArea pa: paList)
					_pa_list.add(new JazzProjectArea(pa, this));
				
			} catch (TeamRepositoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (_pa_list == null)
			return Collections.emptyList();
		else
			return _pa_list;
	}

	@Override
	public IJazzMember GetNewJazzMember(String userName) {

		try {
			IContributor c = teamRepository.contributorManager().fetchContributorByUserId(userName, null);
			return new JazzMember(c, this);

		} catch (TeamRepositoryException e) {}
		return null;
	}

	@Override
	public IJazzTeamArea GetJazzTeamAreaByOid(String oid) {

		UUID taUUID = UUID.valueOf(oid);
		ITeamAreaHandle taHandle = (ITeamAreaHandle) ITeamArea.ITEM_TYPE.createItemHandle(taUUID, null);
		try {
			ITeamArea ta = (ITeamArea) teamRepository.itemManager().fetchCompleteItem(taHandle, IItemManager.DEFAULT, null);
			if (ta != null) {
				ITeamAreaHierarchy taH = null;
				IProjectAreaHandle paHandle = ta.getProjectArea();
				String taPath = "";
				if (paHandle != null) {
					IProjectArea pa = (IProjectArea) teamRepository.itemManager().fetchCompleteItem(paHandle, IItemManager.DEFAULT, null);
					taH = pa.getTeamAreaHierarchy();
					ITeamAreaHandle cH = taH.getParent(taHandle);
					while (cH != null) {
						ITeamArea c = (ITeamArea) teamRepository.itemManager().fetchCompleteItem(cH, IItemManager.DEFAULT, null);
						if (taPath.isEmpty())
							taPath = c.getName();
						else
							taPath = c.getName() + "/" + taPath;
						cH = taH.getParent(cH);
					}	
					taPath = pa.getName() + "/" + taPath;
				}
				return new JazzTeamArea(ta, this, taH, taPath);
			}
			

		} catch (TeamRepositoryException e) {}
		
		return null;
	}
	
	public void close() {
		if (platformIsUp) {	
			TeamPlatform.shutdown();
			platformIsUp = false;
		}
	}
}
