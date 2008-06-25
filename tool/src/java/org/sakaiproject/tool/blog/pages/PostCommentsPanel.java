package org.sakaiproject.tool.blog.pages;

import java.text.DateFormat;
import java.util.Date;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.tool.blog.BlogApplication;
import org.sakaiproject.tool.blog.api.SakaiProxy;
import org.sakaiproject.tool.blog.api.datamodel.Comment;
import org.sakaiproject.tool.blog.api.datamodel.Post;
import org.sakaiproject.tool.blog.dataproviders.PostCommentsDataProvider;
import org.sakaiproject.tool.blog.impl.managers.BlogSecurityManager;
import org.sakaiproject.tool.blog.impl.managers.PostManager;
import org.sakaiproject.tool.blog.pages.models.PostModel;

public class PostCommentsPanel extends Panel
{
	private transient SakaiProxy sakaiProxy;
	private transient BlogSecurityManager securityManager;
	private transient PostManager postManager;

	public PostCommentsPanel(String id, final Post post)
	{
		super(id);
		
		sakaiProxy = BlogApplication.get().getSakaiProxy();
		
		securityManager = BlogApplication.get().getSecurityManager();
		
		postManager = BlogApplication.get().getPostManager();
		
		add(new Label("commentsLabel",new ResourceModel("postComments")));

		PostCommentsDataProvider commentsProvider = new PostCommentsDataProvider(post);

		DataView postComments = new DataView("comments", commentsProvider)
		{
			@Override
			protected void populateItem(Item commentItem)
			{
				Comment comment = (Comment) commentItem.getModelObject();
				
				Date createdDate = comment.getCreatedDate();
				commentItem.add(new Label("createdDate"," (" + DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.MEDIUM).format(createdDate)+")"));
				
				Date modifiedDate = comment.getModifiedDate();
				
				if(modifiedDate.equals(createdDate))
					commentItem.add(new Label("modifiedDate",""));
				else
					commentItem.add(new Label("modifiedDate"," (" + DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.MEDIUM).format(modifiedDate)+")"));
				
				commentItem.add(new Label("creator",sakaiProxy.getDisplayNameForTheUser(comment.getCreatorId())));
				Label commentText = new Label("commentText",comment.getText());
				commentText.setEscapeModelStrings(false);
				commentItem.add(commentText);
				
				Link deleteLink = new Link("deleteLink",new Model(comment))
				{
					@Override
					public void onClick()
					{
						Comment comment = (Comment) getModelObject();
						postManager.deleteComment(post, comment);
						setResponsePage(new PostPage(new PostModel(post)));
					}
				};
				
				commentItem.add(deleteLink);
				
				if(!securityManager.canCurrentUserDeleteComment(post,comment) )
					deleteLink.setVisible(false);
				
				Link editLink = new Link("editLink",new Model(comment))
				{
					@Override
					public void onClick()
					{
						Comment comment = (Comment) getModelObject();
						setResponsePage(new EditComment(post,comment));
					}
				};
				
				commentItem.add(editLink);
				
				if(!securityManager.canCurrentUserEditComment(post,comment) )
					editLink.setVisible(false);
			}
		};

		add(postComments);
	}
}
