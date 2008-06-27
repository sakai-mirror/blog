package org.sakaiproject.tool.blog.pages;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.sakaiproject.tool.blog.BlogApplication;
import org.sakaiproject.tool.blog.api.datamodel.Comment;
import org.sakaiproject.tool.blog.api.datamodel.Post;
import org.sakaiproject.tool.blog.pages.models.PostModel;
import org.sakaiproject.wicket.markup.html.fckeditor.FCKEditorPanel;

public class AddCommentPage extends BasePage
{
	private String comment;
	
	public AddCommentPage(final Post post)
	{
		super();
		
		add(new PostPanel("postPanel",new PostModel(post),0));
		
		Form commentForm = new Form("commentForm");
		
		String cId = BlogApplication.get().getSakaiProxy().getCurrentSiteCollectionId();
		commentForm.add(new FCKEditorPanel("commentEditor",new PropertyModel(this,"comment"),"650","300",FCKEditorPanel.BASIC,cId));
		
		Button saveButton = new Button("save",new Model("Save"))
		{
			public void onSubmit()
			{
				Comment comment = new Comment(getComment());
				comment.setCreatorId(sakaiProxy.getCurrentUserId());
				postManager.addComment(post,comment);
				
				setResponsePage(new MemberBlog(post.getCreatorId()));
			}
		};
		
		commentForm.add(saveButton);
		
		Button cancelButton = new Button("cancel",new Model("Cancel"))
		{
			public void onSubmit()
			{
				setResponsePage(new MemberBlog(post.getCreatorId()));
				//setResponsePage(new PostPage(new PostModel(post)));
			}
		};
		
		cancelButton.setDefaultFormProcessing(false);
		
		commentForm.add(cancelButton);
		
		add(commentForm);
	}

	public void setComment(String comment)
	{
		this.comment = comment;
	}

	public String getComment()
	{
		return comment;
	}
}