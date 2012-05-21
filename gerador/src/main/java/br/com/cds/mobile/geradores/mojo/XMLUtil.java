package br.com.cds.mobile.geradores.mojo;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLUtil {

	public static Map<String,String> getChildren(File f,String xpexp){
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = builder.parse(f);
			Element root = doc.getDocumentElement();
			XPathExpression exp = XPathFactory.newInstance().newXPath().compile(xpexp);
			NodeList list = (NodeList)exp.evaluate(doc, XPathConstants.NODESET);
			HashMap<String, String> out = new HashMap<String, String>();
			for(int i=0;i<list.getLength();i++){
				Node item = list.item(i);
				out.put(
						item.getAttributes().getNamedItem("name").toString(),
						item.getTextContent()
				);
			}
			return out;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
