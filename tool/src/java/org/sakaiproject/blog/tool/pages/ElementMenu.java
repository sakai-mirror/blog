package org.sakaiproject.blog.tool.pages;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.blog.api.datamodel.Post;
import org.sakaiproject.blog.tool.pages.models.PostModel;

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
		
		Form form = new Form("cancelForm");
		
		Button cancelButton = new Button("cancelButton", new ResourceModel("cancel"))
		{
			public void onSubmit()
			{
				setResponsePage(new PostPage(new PostModel(post),true));
			}
		};
		
		cancelButton.setDefaultFormProcessing(false);

		form.add(cancelButton);
		
		add(form);
	}
}
