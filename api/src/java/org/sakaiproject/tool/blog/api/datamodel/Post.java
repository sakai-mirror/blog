/*************************************************************************************
 Copyright (c) 2006. Centre for e-Science. Lancaster University. United Kingdom.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 *************************************************************************************/

package org.sakaiproject.tool.blog.api.datamodel;

import java.io.Serializable;
import java.util.Date;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.UUID;

import org.apache.commons.lang.StringEscapeUtils;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.ResourceProperties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Post implements Entity, Serializable
{
	private String id = "";

	private String title = "";

	private String shortText = "";

	private Date createdDate;
	
	private Date modifiedDate;

	private State state = new State();

	private String creatorId = null;

	private String keywords = "";

	private List<PostElement> elements = new ArrayList<PostElement>(); // PostElement

	//private Collection authors = new ArrayList();

	private List<Comment> comments = new ArrayList<Comment>();

	private String siteId;

	private boolean dirty = true;

	public Post()
	{
		id = UUID.randomUUID().toString();
		createdDate = new Date();
		modifiedDate = new Date();
	}

	public void setId(String oid)
	{
		this.id = oid;
	}
	
	public void addKeyword(String keyword)
	{
		keywords += keyword;
	}

	/**
	 * From Entity
	 */
	public String getId()
	{
		return id;
	}

	public void setTitle(String title)
	{
		/*
		if(title.length() >= 7)
		{
			title = title.substring(3);
			title = title.substring(0,title.length() - 4);
		}
		*/
		this.title = title;
		dirty = true;
	}

	public String getTitle()
	{
		return title;
	}

	public void setShortText(String shortText)
	{
		if (shortText != null)
		{
			/*
			if(shortText.length() >= 7)
			{
				shortText = shortText.substring(3);
				shortText = shortText.substring(0,shortText.length() - 4);
			}
			*/
			this.shortText = StringEscapeUtils.unescapeHtml(shortText.trim());
			dirty = true;
		}
	}

	public String getShortText()
	{
		return shortText;
	}

	/*
	 * public void setState(State state) { this.state = state; dirty = true; }
	 * 
	 * public State getState() { return state; }
	 */

	public void setCreatorId(String creatorId)
	{
		this.creatorId = creatorId;
		dirty = true;
	}

	public String getCreatorId()
	{
		return creatorId;
	}

	public String getKeywords()
	{
		return keywords;
	}

	public void setKeywords(String keywords)
	{
		this.keywords = keywords;
		
		if(this.keywords == null)
			this.keywords = "";
		
		dirty = true;
	}

	public void addElement(PostElement element)
	{
		elements.add(element);
		dirty = true;
	}
	
	public void addElement(PostElement element, int index)
	{
		elements.add(index,element);
		dirty = true;
	}

	public void replaceElement(PostElement newElement, int index)
	{
		elements.set(index,newElement);
		dirty = true;
	}

	/*
	public void addAuthor(IUser author)
	{
		authors.add(author);
		dirty = true;
	}

	public IUser[] getAuthors()
	{
		return (IUser[]) authors.toArray(new IUser[0]);
	}
	*/

	public void setPlainBody(String body)
	{
		elements = new ArrayList<PostElement>();
		elements.add(new Paragraph(body));
		dirty = true;
	}

	public void addComment(Comment comment)
	{
		comment.setPostId(id);
		comments.add(comment);
		dirty = true;
	}

	public List<Comment> getComments()
	{
		return comments;
	}

	public List<Image> getImages()
	{
		List<Image> result = new ArrayList<Image>();
		
		for(PostElement element : elements)
		{
			if (element instanceof Image)
				result.add((Image) element);
		}
		
		return result;

	}
	
	public List<Paragraph> getParagraphs()
	{
		List<Paragraph> result = new ArrayList<Paragraph>();
		
		for(PostElement element : elements)
		{
			if (element instanceof Paragraph)
				result.add((Paragraph) element);
		}
		
		return result;
	}
	
	public List<LinkRule> getLinks()
	{
		List<LinkRule> result = new ArrayList<LinkRule>();
		
		for(PostElement element : elements)
		{
			if (element instanceof LinkRule)
				result.add((LinkRule)element);
		}
		
		return result;
	}

	public List<File> getFiles()
	{
		List<File> result = new ArrayList<File>();
		for (PostElement element : elements)
		{
			if (element instanceof File)
				result.add((File) element);
		}
		
		return result;
	}

	public Iterator<PostElement> getElements()
	{
		return elements.iterator();
	}
	
	public int size()
	{
		return elements.size();
	}
	
	public void removeElement(PostElement element)
	{
		removeElement(getElementPosition(element));
	}

	public void removeElement(int index)
	{
		elements.remove(index);
		dirty = true;
	}

	public boolean hasImage(Image test)
	{
		for (Image image : getImages())
		{
			if (image.equals(test))
				return true;
		}
		
		return false;
	}

	public boolean hasFile(String fileId)
	{
		for(File file : getFiles())
		{
			if(file.getId().equals(fileId.trim()))
				return true;
		}
		
		return false;
	}

	public void setSiteId(String siteId)
	{
		this.siteId = siteId;
		dirty = true;
	}

	public String getSiteId()
	{
		return siteId;
	}

	public void setDirty(boolean dirty)
	{
		this.dirty = dirty;
	}

	public boolean isDirty()
	{
		return dirty;
	}

	public boolean isReadOnly()
	{
		return state.isReadOnly();
	}

	public boolean isCommentable()
	{
		return state.isAllowComments();
	}

	public void setVisibility(String visibility)
	{
		state.setVisibility(visibility);
		dirty = true;
	}

	public String getVisibility()
	{
		return state.getVisibility();
	}

	public void setReadOnly(boolean b)
	{
		state.setReadOnly(b);
		dirty = true;
	}

	public void setCommentable(boolean b)
	{
		state.setAllowComments(b);
		dirty = true;
	}

	public boolean isPrivate()
	{
		return state.isPrivate();
	}

	public boolean isPublic()
	{
		// TODO Auto-generated method stub
		return state.isPublic();
	}

	/**
	 * @see org.sakaiproject.entity.api.Entity#getProperties()
	 */
	public ResourceProperties getProperties()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * From Entity
	 * 
	 * @return
	 */
	public String getReference()
	{
		return "/blog" + Entity.SEPARATOR + "post" + Entity.SEPARATOR + id;
	}

	/**
	 * From Entity
	 * 
	 * @return
	 */
	public String getReference(String rootProperty)
	{
		// TODO Auto-generated method stub
		return getReference();
	}

	/**
	 * @see org.sakaiproject.entity.api.Entity#getUrl()
	 */
	public String getUrl()
	{
		return null;//sakaiProxy.getAccessUrl() + getReference();
	}

	/**
	 * @see org.sakaiproject.entity.api.Entity#getUrl()
	 */
	public String getUrl(String rootProperty)
	{
		return getUrl();
	}

	/**
	 * From Entity
	 * 
	 * @return
	 */
	public Element toXml(Document doc, Stack stack)
	{
		Element post = doc.createElement("post");

		if (stack.isEmpty())
		{
			doc.appendChild(post);
		}
		else
		{
			((Element) stack.peek()).appendChild(post);
		}

		stack.push(post);

		post.setAttribute("readOnly", ((isReadOnly()) ? "true" : "false"));
		post.setAttribute("commentable", ((isCommentable()) ? "true" : "false"));
		post.setAttribute("visibility", getVisibility());

		Element idElement = doc.createElement("id");
		idElement.setTextContent(id);
		post.appendChild(idElement);

		Element createdDateElement = doc.createElement("createdDate");
		createdDateElement.setTextContent(Long.toString(createdDate.getTime()));
		post.appendChild(createdDateElement);
		
		Element modifiedDateElement = doc.createElement("modifiedDate");
		modifiedDateElement.setTextContent(Long.toString(modifiedDate.getTime()));
		post.appendChild(modifiedDateElement);

		Element creatorIdElement = doc.createElement("creatorId");
		creatorIdElement.setTextContent(creatorId);
		post.appendChild(creatorIdElement);

		Element keywordsElement = doc.createElement("keywords");
		keywordsElement.setTextContent(wrapWithCDATA(keywords));
		post.appendChild(keywordsElement);

		Element titleElement = doc.createElement("title");
		titleElement.setTextContent(wrapWithCDATA(title));
		post.appendChild(titleElement);

		Element shortTextElement = doc.createElement("shortText");
		shortTextElement.setTextContent(wrapWithCDATA(shortText));
		post.appendChild(shortTextElement);
		
		if(comments.size() > 0)
		{
			Element commentsElement = doc.createElement("comments");
			
			for(Comment comment: comments)
			{
				Element commentElement = doc.createElement("comment");
				commentElement.setAttribute("id", comment.getId());
				commentElement.setAttribute("creatorId", comment.getCreatorId());
				commentElement.setAttribute("createdDate", Long.toString(comment.getCreatedDate().getTime()));
				commentElement.setAttribute("modifiedDate", Long.toString(comment.getModifiedDate().getTime()));
				commentElement.setTextContent(wrapWithCDATA(comment.getText()));
				
				commentsElement.appendChild(commentElement);
			}
		
			post.appendChild(commentsElement);
		}
		
		List<Image> images = getImages();
		
		if(images.size() > 0)
		{
			Element imagesElement = doc.createElement("images");
			
			for(Image image : images)
			{
				Element imageElement = doc.createElement("image");
				imageElement.setAttribute("id", image.getId());
				imageElement.setAttribute("fileName", image.getFileName());
				imageElement.setAttribute("fullResourceId", image.getFullResourceId());
				imageElement.setAttribute("webResourceId", image.getWebResourceId());
				imageElement.setAttribute("displayName", image.getDisplayName());
				imageElement.setAttribute("indentation", Integer.toString(image.getIndentation()));
				
				imagesElement.appendChild(imageElement);
			}
			
			post.appendChild(imagesElement);
		}
		
		List<File> files = getFiles();
		
		if(files.size() > 0)
		{
			Element filesElement = doc.createElement("files");
			
			for(File file : files)
			{
				Element fileElement = doc.createElement("file");
				fileElement.setAttribute("id", file.getId());
				fileElement.setAttribute("fileName", file.getFileName());
				fileElement.setAttribute("resourceId", file.getResourceId());
				fileElement.setAttribute("displayName", file.getDisplayName());
				fileElement.setAttribute("indentation", Integer.toString(file.getIndentation()));
				
				filesElement.appendChild(fileElement);
			}
			
			post.appendChild(filesElement);
		}
		
		List<Paragraph> paragraphs = getParagraphs();
		
		if(paragraphs.size() > 0)
		{
			Element paragraphsElement = doc.createElement("paragraphs");
			
			for(Paragraph paragraph : paragraphs)
			{
				Element paragraphElement = doc.createElement("paragraph");
				paragraphElement.setAttribute("id", paragraph.getId());
				paragraphElement.setAttribute("indentation", Integer.toString(paragraph.getIndentation()));
				
				Element paragraphElementText = doc.createElement("text");
				paragraphElementText.setTextContent(paragraph.getText());
				paragraphElement.appendChild(paragraphElementText);
				
				paragraphsElement.appendChild(paragraphElement);
			}
			
			post.appendChild(paragraphsElement);
		}
		
		List<LinkRule> links = getLinks();
		
		if(links.size() > 0)
		{
			Element linksElement = doc.createElement("links");
			
			for(LinkRule link : links)
			{
				Element linkElement = doc.createElement("link");
				linkElement.setAttribute("id", link.getId());
				linkElement.setAttribute("indentation", Integer.toString(link.getIndentation()));
				linkElement.setAttribute("displayName", link.getDisplayName());
				linkElement.setAttribute("url", link.getUrl());
				
				linksElement.appendChild(linkElement);
			}
			
			post.appendChild(linksElement);
		}

		stack.pop();

		return post;
	}
	
	private String wrapWithCDATA(String s)
	{
		return "<![CDATA[" + s + "]]>";
	}

	public void fromXml(Element postElement)
	{
		if(!postElement.getTagName().equals("post"))
		{
			return;
		}
		
		NodeList children = postElement.getChildNodes();
		
		final int numChildren = children.getLength();
		
		for(int i = 0; i < numChildren;i++)
		{
			Node childNode = children.item(i);
			
			if(childNode.getNodeType() != Node.ELEMENT_NODE) continue;
			
			Element childElement = (Element) childNode;
			
			String name = childElement.getTagName();
			
			Node textNode = childElement.getFirstChild();
			
			if(textNode.getNodeType() != Node.ELEMENT_NODE) continue;
			
			String value = textNode.getTextContent();
			
			if(name.equals("id"))
				id = value;
			
			else if(name.equals("title"))
				title = value;
			
			else if(name.equals("creatorId"))
				creatorId = value;
			
			else if(name.equals("createdDate"))
				createdDate = new Date(Long.parseLong(value));
			
			else if(name.equals("modifiedDate"))
				modifiedDate = new Date(Long.parseLong(value));
			
			else if(name.equals("comments"))
			{
				NodeList commentNodes = childElement.getChildNodes();
				
				int numComments = commentNodes.getLength();
				
				for(int j = 0;j < numComments;j++)
				{
					Node commentNode = commentNodes.item(j);
					
					if(commentNode.getNodeType() != Node.ELEMENT_NODE) continue;
					
					Element commentElement = (Element) commentNode;
					String commentId = commentElement.getAttribute("id");
					String commentCreatorId = commentElement.getAttribute("creatorId");
					String commentCreatedDate = commentElement.getAttribute("createdDate");
					String commentModifiedDate = commentElement.getAttribute("modifiedDate");
					Node commentTextNode = commentNode.getFirstChild();
					
					if(commentTextNode.getNodeType() != Node.TEXT_NODE) continue;
					
					String text = ((Element) commentTextNode).getTextContent();
					
					Comment comment = new Comment(commentId);
					comment.setCreatorId(commentCreatorId);
					comment.setCreatedDate(new Date(Long.parseLong(commentCreatedDate)));
					comment.setModifiedDate(new Date(Long.parseLong(commentModifiedDate)));
					comment.setText(text);
					
					addComment(comment);
				}
			}
		}
	}

	public void setCreatedDate(Date createdDate)
	{
		this.createdDate = createdDate;
	}

	public Date getCreatedDate()
	{
		return createdDate;
	}

	public void setModifiedDate(Date modifiedDate)
	{
		this.modifiedDate = modifiedDate;
	}

	public Date getModifiedDate()
	{
		return modifiedDate;
	}

	public PostElement getElement(int index)
	{
		return elements.get(index);
	}

	public boolean hasComments()
	{
		return comments.size() > 0;
	}

	public void removeComment(Comment comment)
	{
		comments.remove(comment);
		dirty = true;
	}

	public int getElementPosition(PostElement e)
	{
		return elements.indexOf(e);
	}

	public boolean isRecycled()
	{
		return state.isRecycled();
	}
}
