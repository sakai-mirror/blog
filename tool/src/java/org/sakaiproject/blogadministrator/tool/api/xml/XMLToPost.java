package org.sakaiproject.blogadministrator.tool.api.xml;

import org.sakaiproject.blog.api.datamodel.Post;
import org.xml.sax.InputSource;

import java.io.StringReader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class XMLToPost
{
	public Post convertXMLInPost(String mode,String xml,String siteId)
	{
		try
		{
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			XMLPostContentHandler ch = new XMLPostContentHandler(mode,siteId);
			InputSource is = new InputSource(new StringReader(xml));
			parser.parse(is,ch);
			Post post = ch.getPost();
			return post;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
}
