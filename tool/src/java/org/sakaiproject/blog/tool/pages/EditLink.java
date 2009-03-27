package org.sakaiproject.blog.tool.pages;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.blog.api.datamodel.LinkRule;
import org.sakaiproject.blog.api.datamodel.Post;
import org.sakaiproject.blog.tool.pages.models.PostModel;

public class EditLink extends BasePage
{
	private LinkRule linkRule = new LinkRule();

	private Button saveButton;

	private Post post;

	private int elementIndex;

	public EditLink(Post post, int elementIndex,final boolean modify)
	{
		super();

		this.post = post;
		this.elementIndex = elementIndex;
		if(modify)
			this.linkRule = (LinkRule) post.getElement(elementIndex);
		
		add(new Label("editLink",new ResourceModel("editLink")));

		Form form = new Form("form");

		form.add(new Label("nameLabel", "Name:"));
		form.add(new TextField("nameField", new PropertyModel(linkRule, "displayName")));
		form.add(new Label("urlLabel", "URL:"));
		form.add(new TextField("urlField", new PropertyModel(linkRule, "url")));

		saveButton = new Button("saveButton", new ResourceModel("save"))
		{
			public void onSubmit()
			{
				try
				{
				if(modify)
					EditLink.this.blogManager.replaceElement(EditLink.this.post,linkRule, EditLink.this.elementIndex);
				else
					EditLink.this.blogManager.addElement(EditLink.this.post,linkRule,EditLink.this.elementIndex);

				setResponsePage(new PostPage(new PostModel(EditLink.this.post),true));
				}
				catch(Exception e)
				{
				}
			}
		};

		form.add(saveButton);

		Button cancelButton = new Button("cancelButton", new ResourceModel("cancel"))
		{
			public void onSubmit()
			{
				setResponsePage(new PostPage(new PostModel(EditLink.this.post),true));
			}
		};
		
		cancelButton.setDefaultFormProcessing(false);

		form.add(cancelButton);

		add(form);
	}
}