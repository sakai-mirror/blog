package org.sakaiproject.blogadministrator.tool.api.xml;

import org.sakaiproject.blog.api.BlogManager;
import org.sakaiproject.blog.api.SakaiProxy;
import org.sakaiproject.blogadministrator.tool.BlogDataMigratorApplication;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public abstract class XMLPostContentHandleState
{
	//protected BlogManager postManager;
	protected SakaiProxy sakaiProxy;
	protected String mode;
	
	protected XMLPostContentHandleState()
	{
		this(null);
	}
	protected XMLPostContentHandleState(String mode)
	{
		this.mode = mode;
		//postManager = BlogDataMigratorApplication.get().getBlogManager();
		sakaiProxy = BlogDataMigratorApplication.get().getSakaiProxy();
	}
	
    public abstract XMLPostContentHandleState startElement(String uri, String localName, String qName, Attributes atts) throws SAXException;
    public abstract XMLPostContentHandleState endElement(String uri, String localName, String qName) throws SAXException;
    public abstract XMLPostContentHandleState characters(char ch[], int start, int length) throws SAXException;
}
