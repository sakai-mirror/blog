package org.sakaiproject.blog.tool.pages;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.sakaiproject.blog.tool.BlogApplication;
import org.sakaiproject.blog.api.SakaiProxy;
import org.sakaiproject.blog.api.BlogSecurityManager;
import org.sakaiproject.blog.api.BlogManager;
import org.sakaiproject.blog.api.datamodel.Post;
import org.sakaiproject.blog.tool.pages.models.PostModel;

public class PostPanel extends Panel
{
	private boolean showFullContent = false;
	private boolean showComments = false;
	private PostCommentsPanel postCommentsPanel;
	private PostElementsPanel postElementsPanel;
	private Post post;
	private transient BlogSecurityManager securityManager;
	private transient BlogManager blogManager;
	private transient SakaiProxy sakaiProxy;
	private Link showCommentsLink;
	
	public PostPanel(String id, final PostModel postModel,final int currentIndex)
	{
		this(id,postModel,currentIndex,false);
	}

	public PostPanel(String id, final PostModel postModel,final int currentIndex,boolean editingMode)
	{
		super(id);
		
		securityManager = BlogApplication.get().getSecurityManager();
		sakaiProxy = BlogApplication.get().getSakaiProxy();
		blogManager = BlogApplication.get().getBlogManager();
		
		this.post = (Post) postModel.getObject();

		Link editTitleLink = new Link("editTitleLink",postModel)
		{
			@Override
			public void onClick()
			{
				setResponsePage(new EditTitle(post));
			}
		};
		
		editTitleLink.setVisible(editingMode);
		
		add(editTitleLink);
        
		Link commentLink = new Link("commentLink",postModel)
		{
			@Override
			public void onClick()
			{
				setResponsePage(new AddCommentPage(post));
			}
		};
		
		add(commentLink);
		
		if(!securityManager.canCurrentUserCommentOnPost(post))
			commentLink.setVisible(false);
        
        Link editLink = new Link("editLink",postModel)
		{
			@Override
			public void onClick()
			{
				setResponsePage(new PostPage(new PostModel(post),true));
			}
		};
		
		add(editLink);
		
		editLink.setVisible(!editingMode);
		
		if(!securityManager.canCurrentUserEditPost(post))
        	editLink.setVisible(false);
		
		
        Link stopEditingLink = new Link("stopEditingLink",postModel)
		{
			@Override
			public void onClick()
			{
				setResponsePage(new PostPage(new PostModel(post),false));
			}
		};
		
		add(stopEditingLink);
		
		stopEditingLink.setVisible(editingMode);
		
		Link deleteLink = new Link("deleteLink",postModel)
		{
			@Override
			public void onClick()
			{
				Post post = (Post) getModelObject();
				blogManager.recyclePost(post.getId());
				//blogManager.deletePost(post.getId());
				
				postModel.setObject(null);
				
				setResponsePage(new ViewAll());
			}
		};
		
		add(deleteLink);
		
		if(!securityManager.canCurrentUserDeletePost(post) )
			deleteLink.setVisible(false);
		
		PostAccessPanel postAccessPanel = new PostAccessPanel("postAccessPanel",postModel);
		postAccessPanel.setVisible(editingMode);
		add(postAccessPanel);
		
		PageParameters pp = new PageParameters();
		pp.put("postId", post.getId());
		BookmarkablePageLink postLink = new BookmarkablePageLink("postLink",PostPage.class,pp);
        
        Label titleLabel = new Label("title",post.getTitle());
        titleLabel.setEscapeModelStrings(false);
        postLink.add(titleLabel);
        
        add(postLink);
		
		//if(!editMode)
        	//deleteLink.setVisible(false);
        
        String creatorDisplayName = "";
        String creatorId = post.getCreatorId();
        if(creatorId != null)
        	creatorDisplayName = sakaiProxy.getDisplayNameForTheUser(creatorId);
        	
        add(new Label("author",creatorDisplayName));
        
        Date date = post.getCreatedDate();
        
        add(new Label("postDate",DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.MEDIUM,Locale.UK).format(date)));
        
        Date modifiedDate = post.getModifiedDate();
        
        add(new Label("modifiedDate",DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.MEDIUM,Locale.UK).format(modifiedDate)));
        
		Label shortText = new Label("shortText",post.getShortText());
        
        shortText.setEscapeModelStrings(false);
        
        add(shortText);
        
		Link editShortTextLink = new Link("editShortTextLink",postModel)
		{
			@Override
			public void onClick()
			{
				setResponsePage(new EditShortText(post));
			}
		};
		
		editShortTextLink.setVisible(editingMode);
		
		add(editShortTextLink);

		postElementsPanel = new PostElementsPanel("postElementsPanel", post);
		add(postElementsPanel);
		postElementsPanel.setEditMode(editingMode);
		
		//postElementsPanel.setVisible(showFullContent);
		
		Link newLink = new Link("newLink")
		{
			@Override
			public void onClick()
			{
				//int index = post.size() - 1;
				int index = post.size();
				index = (index < 0) ? 0 : index;
				setResponsePage(new ElementMenu(post,index));
			}
		};
				
		newLink.setVisible(editingMode);
		
		add(newLink);
		
		showCommentsLink = new Link("showCommentsLink")
		{
			@Override
			public void onClick()
			{
				setResponsePage(new PostPage(new PostModel(post)));
			}
		};
		
		showCommentsLink.add(new Label("commentTotal",post.getComments().size() + " Comments (Click to show)"));
		add(showCommentsLink);
		
		showCommentsLink.setVisible(!showComments && post.hasComments());
		
		postCommentsPanel = new PostCommentsPanel("postCommentsPanel", post);
		add(postCommentsPanel);
		
		postCommentsPanel.setVisible(showComments);
	}

	public void setShowFullContent(boolean showFullContent)
	{
		this.showFullContent = showFullContent;
		postElementsPanel.setVisible(showFullContent);
		// TODO Auto-generated method stub
	}
	
	public void setShowComments(boolean showComments)
	{
		this.showComments = showComments;
		
		if(post.hasComments())
		{
			postCommentsPanel.setVisible(showComments);
			showCommentsLink.setVisible(!showComments);
		}
	}
}
