package com.rus.alm.tools.teamsyncer;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public final class CheckSyncerMappings
{
	public static int CheckSyncerMapping(String mappingfile, PrintWriter log_stream) throws SAXException, IOException, ParserConfigurationException, XPathExpressionException
	{
		int ret = 0;

		MapVtxList vtxList = new MapVtxList();
		ArrayList<Dependency> depList = new ArrayList<Dependency>();
		HashMap<Integer, String> ta_dict = new HashMap<Integer, String>();

	    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
	    Document doc = docBuilder.parse (new File(mappingfile));
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPathBuilder = xPathFactory.newXPath();
        XPathExpression xPath = xPathBuilder.compile("/mapping/targetArea");
        NodeList nodes = (NodeList) xPath.evaluate(doc, XPathConstants.NODESET);
		int mapCount = 0;
		for (int i = 0; i < nodes.getLength(); i++)
		{
			mapCount++;
			String targetPath = nodes.item(i).getAttributes().getNamedItem("name").getNodeValue();
			if (targetPath.equals(""))
			{
				targetPath = nodes.item(i).getAttributes().getNamedItem("oid").getNodeValue();
			}
			ta_dict.put(mapCount, targetPath);
	        XPathExpression s_xPath = xPathBuilder.compile("./sourceArea");
			NodeList s_nodes = (NodeList) s_xPath.evaluate(nodes.item(i), XPathConstants.NODESET);
			for (int j = 0; j < s_nodes.getLength(); j++)
			{
				String sourcePath = s_nodes.item(j).getAttributes().getNamedItem("name").getNodeValue();
				if (sourcePath.equals(""))
				{
					sourcePath = s_nodes.item(j).getAttributes().getNamedItem("oid").getNodeValue();
				}
				vtxList.Add(mapCount, sourcePath, targetPath);
			}
		}

		vtxList.BuildDepList(depList);

		for (Dependency d : depList)
		{
			if (d.getMapNum() < d.getDepNum())
			{
				ret = 1;
				System.out.printf("targetArea #%1$s name='%2$s' should be placed prior to targetArea #%3$s name='%4$s'" + "\r\n", d.getDepNum(), ta_dict.get(d.getDepNum()), d.getMapNum(), ta_dict.get(d.getMapNum()));
				if (log_stream != null)
				{
					log_stream.printf("targetArea #{0} name='{1}' should be placed prior to targetArea #{2} name='{3}'", d.getDepNum(), ta_dict.get(d.getDepNum()), d.getMapNum(), ta_dict.get(d.getMapNum()));
				}
			}
		}

		if (ret == 0)
		{
			System.out.println("No problems found.");
			if (log_stream != null)
			{
				log_stream.write("No problems found." + System.lineSeparator());
			}
		}
		return ret;
	}
}