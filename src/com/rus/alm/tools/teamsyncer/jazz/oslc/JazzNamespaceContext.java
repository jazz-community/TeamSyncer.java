package com.rus.alm.tools.teamsyncer.jazz.oslc;

import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;

public final class JazzNamespaceContext implements NamespaceContext {
	
    @Override
    public String getNamespaceURI(String prefix) {
        if ("rdf".equals(prefix))
        	return "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
        else if (JazzConstants.jazzProc.equals(prefix))
        	return JazzConstants.jazzProcNsUrl;
        else if (JazzConstants.jazzProcNum.equals(prefix))
        	return JazzConstants.jazzProcNsUrl06;
        else if ("jfs".equals(prefix))
        	return "http://jazz.net/xmlns/prod/jazz/jfs/1.0/";
        else if ("foaf".equals(prefix))
        	return "http://xmlns.com/foaf/0.1/";
        else
        	return null;
    }

	@Override
	public String getPrefix(String namespaceURI) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<?> getPrefixes(String namespaceURI) {
		// TODO Auto-generated method stub
		return null;
	}
}
