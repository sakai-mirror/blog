package org.sakaiproject.tool.blog.pages;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.tool.blog.BlogApplication;
import org.sakaiproject.tool.blog.api.datamodel.Post;
import org.sakaiproject.tool.blog.pages.models.PostModel;
import org.sakaiproject.tool.blog.pages.validators.TitleValidator;
import org.sakaiproject.wicket.markup.html.fckeditor.FCKEditorPanel;

public class EditTitle extends BasePage
{
	private Post post;

	private Button saveButton;

	public EditTitle(Post post)
	{
		super();
		this.post = post;
		
		add(new Label("editTitle",new ResourceModel("editTitle")));

		Form form = new Form("form");

		TextField titleField = new TextField("titleField",new PropertyModel(post,"title"));
		titleField.add(new TitleValidator());
		
		form.add(titleField);
		saveButton = new Button("saveButton", new ResourceModel("save"))
		{
			public void onSubmit()
			{
				EditTitle.this.postManager.saveTitle(EditTitle.this.post);

				setResponsePage(new PostPage(new PostModel(EditTitle.this.post),true));
			}
		};

		form.add(saveButton);
		
		Button cancelButton = new Button("cancelButton", new ResourceModel("cancel"))
		{
			public void onSubmit()
			{
				setResponsePage(new PostPage(new PostModel(EditTitle.this.post),true));
			}
		};
		
		cancelButton.setDefaultFormProcessing(false);

		form.add(cancelButton);
		
		add(form);
	}
}