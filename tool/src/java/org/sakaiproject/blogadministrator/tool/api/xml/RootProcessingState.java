/*************************************************************************************
 * Copyright 2006, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.

 *************************************************************************************/

package org.sakaiproject.blogadministrator.tool.api.xml;

import java.util.Date;

import org.sakaiproject.blog.api.datamodel.Post;
import org.sakaiproject.blog.api.datamodel.Paragraph;
import org.sakaiproject.blog.api.sql.ISQLGenerator;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class RootProcessingState extends XMLPostContentHandleState
{
	private Post _post;
	private String _currentText;

	public RootProcessingState(String mode,Post post)
	{
		super(mode);
		
		_currentText = new String();
		_post = post;
	}

	public XMLPostContentHandleState startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
	{
		if (qName.equals("keywordsList"))
		{
			return new KeywordsProcessingState(_post, this);
		}
		
		if (qName.equals("creator"))
		{
			return new CreatorProcessingState(_post, this);
		}
		
		if (qName.equals("state"))
		{
			return new StateProcessingState(_post, this);
		}
		
		if (qName.equals("comments"))
		{
			return new CommentsProcessingState(_post, this);
		}
		
		if (qName.equals("image"))
		{
			return new ImageProcessingState(mode,_post, this);
		}
		
		if (qName.equals("file"))
		{
			return new FileProcessingState(mode,_post, this);
		}
		
		if (qName.equals("linkRule"))
		{
			return new LinkRuleProcessingState(_post, this);
		}

		if (!isKnowTag(qName))
		{
			// Non expected tags, probabily they are html tags. We will add them to the text
			_currentText += "<" + qName + " ";
			int numberAttributes = atts.getLength();
			for (int i = 0; i < numberAttributes; i++)
			{
				String attName = atts.getLocalName(i);
				String attValue = atts.getValue(i);
				_currentText += attName + "=\"" + attValue + "\" ";
			}
			_currentText += ">";
		}
		
		return this;
	}

	public XMLPostContentHandleState endElement(String uri, String localName, String qName) throws SAXException
	{
		if (qName.equals("post"))
			return this;
		
		if (qName.equals("oid"))
		{
			_post.setId(_currentText);
			
			_currentText = new String();
			return this;
		}
		
		if (qName.equals("title"))
		{
			_post.setTitle(_currentText);
			_currentText = new String();
			return this;
		}
		
		if (qName.equals("shortText"))
		{
			_post.setShortText(_currentText);
			_currentText = new String();
			
			return this;
		}
		
		if (qName.equals("date"))
		{
			_post.setCreatedDate(new Date(Long.parseLong(_currentText)));
			_currentText = new String();
			return this;
		}
		
		if (qName.equals("keywordsList"))
		{
			_post.setKeywords(_currentText);
			_currentText = new String();
			return this;
		}
		
		if (qName.equals("paragraph"))
		{
			Paragraph paragraph = new Paragraph();
			paragraph.setText(_currentText.trim());
			_post.addElement(paragraph);
			//postManager.addElement(_post, paragraph);
			_currentText = new String();
			return this;
		}

		if (!isKnowTag(qName))
		{
			// Non expected tags, probabily they are html tags. We will add them to the text
			_currentText += "</" + qName + ">";
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
		
		_currentText = _currentText.replaceAll(ISQLGenerator.APOSTROFE, "'");
		return this;
	}

	private boolean isKnowTag(String tag)
	{
		if (tag.equals("post") || tag.equals("oid") || tag.equals("title") || tag.equals("shortText") || tag.equals("date") || tag.equals("keywordsList") || tag.equals("paragraph") || tag.equals("creator") || tag.equals("comment") || tag.equals("linkRule") || tag.equals("keywordsList") || tag.equals("file") || tag.equals("image"))
			return true;

		return false;

	}
}
