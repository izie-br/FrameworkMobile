package com.quantium.mobile.geradores.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLUtil {

	public static List<String> xpath(File f,String xpexp){
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = builder.parse(f);
			XPathExpression exp = XPathFactory.newInstance().newXPath().compile(xpexp);
			NodeList list = (NodeList)exp.evaluate(doc, XPathConstants.NODESET);
			ArrayList<String> out = new ArrayList<String>();
			for (int i=0; i < list.getLength(); i++) {
				Node item = list.item(i);
				out.add(item.getNodeValue());
			}
			return out;
		} catch (Exception e) {
			//e.printStackTrace();
			return null;
		}
	}

}
