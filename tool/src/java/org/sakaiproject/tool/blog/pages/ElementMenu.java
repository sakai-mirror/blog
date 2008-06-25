package org.sakaiproject.tool.blog.pages;

import org.apache.wicket.markup.html.link.Link;
import org.sakaiproject.tool.blog.api.datamodel.Post;

public class ElementMenu extends BasePage
{
	public ElementMenu(final Post post, final int elementIndex)
	{
		super();
		
		Link newTextLink = new Link("newTextLink")
		{
			@Override
			public void onClick()
			{
				setResponsePage(new EditText(post,elementIndex,false));
			}
		};
		
		add(newTextLink);
		
		Link newFileLink = new Link("newFileLink")
		{
			@Override
			public void onClick()
			{
				setResponsePage(new EditFile(post,elementIndex,false));
			}
		};
		
		add(newFileLink);
		
		Link newLinkLink = new Link("newLinkLink")
		{
			@Override
			public void onClick()
			{
				setResponsePage(new EditLink(post,elementIndex,false));
			}
		};
		
		add(newLinkLink);
		
		Link newImageLink = new Link("newImageLink")
		{
			@Override
			public void onClick()
			{
				setResponsePage(new EditImage(post,elementIndex,false));
			}
		};
		
		add(newImageLink);
	}
}
