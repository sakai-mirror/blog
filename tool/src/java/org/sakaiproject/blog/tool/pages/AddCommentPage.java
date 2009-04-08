package org.sakaiproject.blog.tool.pages;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.sakaiproject.blog.api.datamodel.Comment;
import org.sakaiproject.blog.api.datamodel.Post;
import org.sakaiproject.blog.tool.BlogApplication;
import org.sakaiproject.blog.tool.markup.html.fckeditor.FCKEditorPanel;
import org.sakaiproject.blog.tool.pages.models.PostModel;

public class AddCommentPage extends BasePage
{
	private String comment;
	
	public AddCommentPage(final Post post)
	{
		super();
		
		add(new PostPanel("postPanel",new PostModel(post),0));
		
		Form commentForm = new Form("commentForm");
		
		String cId = BlogApplication.get().getSakaiProxy().getCurrentSiteCollectionId();
		commentForm.add(new FCKEditorPanel("commentEditor",new PropertyModel(this,"comment"),"650","300",FCKEditorPanel.BASIC,cId,true));
		
		Button saveButton = new Button("save",new Model("Save"))
		{
			public void onSubmit()
			{
				Comment comment = new Comment(getComment());
				comment.setCreatorId(sakaiProxy.getCurrentUserId());
				comment.setPostId(post.getId());
				blogManager.addComment(post,comment);
				
				setResponsePage(new PostPage(new PostModel(post)));
			}
		};
		
		commentForm.add(saveButton);
		
		Button cancelButton = new Button("cancel",new Model("Cancel"))
		{
			public void onSubmit()
			{
				//setResponsePage(new MemberBlog(post.getCreatorId()));
				setResponsePage(new PostPage(new PostModel(post)));
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
