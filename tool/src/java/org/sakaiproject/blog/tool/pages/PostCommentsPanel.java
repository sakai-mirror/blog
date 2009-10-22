package org.sakaiproject.blog.tool.pages;

import java.text.DateFormat;
import java.util.Date;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.blog.tool.BlogApplication;
import org.sakaiproject.blog.api.SakaiProxy;
import org.sakaiproject.blog.api.BlogManager;
import org.sakaiproject.blog.api.BlogSecurityManager;
import org.sakaiproject.blog.api.datamodel.Comment;
import org.sakaiproject.blog.api.datamodel.Post;
import org.sakaiproject.blog.tool.dataproviders.PostCommentsDataProvider;
import org.sakaiproject.blog.tool.pages.models.PostModel;

public class PostCommentsPanel extends Panel
{
	private transient SakaiProxy sakaiProxy;
	private transient BlogSecurityManager securityManager;
	private transient BlogManager blogManager;

	public PostCommentsPanel(String id, final Post post)
	{
		super(id);
		
		sakaiProxy = BlogApplication.get().getSakaiProxy();
		
		securityManager = BlogApplication.get().getSecurityManager();
		
		blogManager = BlogApplication.get().getBlogManager();
		
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
						blogManager.deleteComment(post, comment);
						setResponsePage(new PostPage(new PostModel(post)));
					}
				};
				
				deleteLink.add(new AttributeModifier("title",true,new ResourceModel("deleteCommentTooltip")));
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
				
				editLink.add(new AttributeModifier("title",true,new ResourceModel("editCommentTooltip")));
				commentItem.add(editLink);
				
				if(!securityManager.canCurrentUserEditComment(post,comment) )
					editLink.setVisible(false);
			}
		};

		add(postComments);
	}
}
