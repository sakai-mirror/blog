package org.sakaiproject.blog.tool.pages;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.blog.tool.BlogApplication;
import org.sakaiproject.blog.api.datamodel.Post;
import org.sakaiproject.blog.tool.pages.models.PostModel;
import org.sakaiproject.wicket.markup.html.fckeditor.FCKEditorPanel;

public class EditShortText extends BasePage
{
	private Post post;

	private Button saveButton;

	public EditShortText(Post post)
	{
		super();
		this.post = post;
		
		add(new Label("editShortText",new ResourceModel("editShortText")));

		Form form = new Form("form");

		String cId = BlogApplication.get().getSakaiProxy().getCurrentSiteCollectionId();

		form.add(new FCKEditorPanel("shortTextEditor", new PropertyModel(this.post,"shortText"), "650", "200", "Basic", cId));
		saveButton = new Button("saveButton", new ResourceModel("save"))
		{
			public void onSubmit()
			{
				EditShortText.this.blogManager.saveShortText(EditShortText.this.post);

				setResponsePage(new PostPage(new PostModel(EditShortText.this.post),true));
			}
		};

		form.add(saveButton);
		
		Button cancelButton = new Button("cancelButton", new ResourceModel("cancel"))
		{
			public void onSubmit()
			{
				setResponsePage(new PostPage(new PostModel(EditShortText.this.post),true));
			}
		};
		
		cancelButton.setDefaultFormProcessing(false);

		form.add(cancelButton);
		
		add(form);
	}
}