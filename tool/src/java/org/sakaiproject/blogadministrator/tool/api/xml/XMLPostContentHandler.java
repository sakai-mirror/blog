package org.sakaiproject.blogadministrator.tool.api.xml;

import org.sakaiproject.blog.api.datamodel.Post;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class XMLPostContentHandler extends DefaultHandler
{
	private Post _post;

	private XMLPostContentHandleState _state;

	private String mode;
	
	public XMLPostContentHandler(String mode,String siteId)
	{
		this.mode = mode;
		_post = new Post();
		_post.setSiteId(siteId);
	}

	public Post getPost()
	{
		return _post;
	}

	public void startDocument() throws SAXException
	{
		_state = new RootProcessingState(mode,_post);
	}

	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
	{
		_state = _state.startElement(uri, localName, qName, atts);
	}

	public void endElement(String uri, String localName, String qName) throws SAXException
	{
		_state = _state.endElement(uri, localName, qName);
	}

	public void characters(char ch[], int start, int length) throws SAXException
	{
		_state = _state.characters(ch, start, length);
	}
}
