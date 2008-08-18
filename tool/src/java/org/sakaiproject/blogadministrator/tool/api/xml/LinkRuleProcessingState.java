package org.sakaiproject.blogadministrator.tool.api.xml;

import org.sakaiproject.blog.api.datamodel.Post;
import org.sakaiproject.blog.api.datamodel.LinkRule;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class LinkRuleProcessingState extends XMLPostContentHandleState
{
	private Post _post;

	private String _currentText;

	private LinkRule _linkRuleUnderConstruction;

	private XMLPostContentHandleState _previousState;

	public LinkRuleProcessingState(Post post, XMLPostContentHandleState previousState)
	{
		super();

		_currentText = new String();
		_previousState = previousState;
		_post = post;
		_linkRuleUnderConstruction = new LinkRule();
	}

	public XMLPostContentHandleState startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
	{
		return this;
	}

	public XMLPostContentHandleState endElement(String uri, String localName, String qName) throws SAXException
	{
		if (qName.equals("linkRule"))
		{
			_post.addElement(_linkRuleUnderConstruction);
			//postManager.addElement(_post,_linkRuleUnderConstruction);
			return _previousState;
		}
		if (qName.equals("linkRuleDescription"))
		{
			_linkRuleUnderConstruction.setDisplayName(_currentText);
			_currentText = new String();
			return this;
		}
		if (qName.equals("linkExpression"))
		{
			_linkRuleUnderConstruction.setUrl(_currentText);
			_currentText = new String();
			return this;

		}
		return this;
	}

	public XMLPostContentHandleState characters(char ch[], int start, int length) throws SAXException
	{
		if (_currentText == null)
		{
			_currentText = new String(ch, start, length);
		}
		else
		{
			_currentText += new String(ch, start, length);
		}
		return this;
	}
}
