package com.rus.alm.tools.teamsyncer.artifactory;

import java.util.HashMap;

public class ArtifactoryGroupHandlerController {

	private static HashMap<String, ArtifactoryGroupHandler> ghMap = new HashMap<String, ArtifactoryGroupHandler>();
	
	private ArtifactoryGroupHandlerController() {}
	
	public static ArtifactoryGroupHandler getInstance(
			String artifactoryServerUrl) {
		
		String key = artifactoryServerUrl.toLowerCase();
		if (!ghMap.containsKey(key))
			ghMap.put(key, new ArtifactoryGroupHandler(artifactoryServerUrl));

		return ghMap.get(key);		
	}
}
