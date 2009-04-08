package org.sakaiproject.blog.tool.pages;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.blog.api.datamodel.Comment;
import org.sakaiproject.blog.api.datamodel.Post;
import org.sakaiproject.blog.tool.BlogApplication;
import org.sakaiproject.blog.tool.markup.html.fckeditor.FCKEditorPanel;
import org.sakaiproject.blog.tool.pages.models.PostModel;

public class EditComment extends BasePage
{
	private Comment comment;
	private Post post;

	public EditComment(final Post post,final Comment comment)
	{
		super();
		this.comment = comment;
		
		this.post= post;

		Form form = new Form("form");

		String cId = BlogApplication.get().getSakaiProxy().getCurrentSiteCollectionId();

		form.add(new FCKEditorPanel("commentEditor", new PropertyModel(comment,"text"), "650", "300", "Default", cId,true,true));

		Button modifyParagraphButton = new Button("modifyButton", new Model("Modify"))
		{
			public void onSubmit()
			{
				blogManager.updateComment(post,comment);

				setResponsePage(new PostPage(new PostModel(EditComment.this.post)));
			}
		};

		form.add(modifyParagraphButton);
		
		Button cancelButton = new Button("cancelButton", new ResourceModel("cancel"))
		{
			public void onSubmit()
			{
				//setResponsePage(new PostPage(new PostModel(EditComment.this.post),true));
			}
		};
		
		cancelButton.setDefaultFormProcessing(false);

		form.add(cancelButton);

		add(form);
	}
}