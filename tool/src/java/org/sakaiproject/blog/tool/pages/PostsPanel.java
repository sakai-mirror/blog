package org.sakaiproject.blog.tool.pages;

import org.apache.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.blog.tool.BlogApplication;
import org.sakaiproject.blog.api.QueryBean;
import org.sakaiproject.blog.api.SakaiProxy;
import org.sakaiproject.blog.api.datamodel.Post;
import org.sakaiproject.blog.api.datamodel.State;
import org.sakaiproject.blog.tool.dataproviders.PostDataProvider;
import org.sakaiproject.blog.tool.pages.models.PostModel;

public class PostsPanel extends Panel
{
	private boolean showComments = false;
	private boolean showFullContent = false;
	
	private transient Logger logger = Logger.getLogger(PostsPanel.class);
	
	public PostsPanel(String id)
	{
		this(id,new QueryBean());
	}
	
	public PostsPanel(String id,QueryBean query)
	{
		this(id,query,5,0);
	}
	
	public PostsPanel(String id,final QueryBean query,int pageSize,int currentPage)
	{
		super(id);
		
		SakaiProxy sakaiProxy = BlogApplication.get().getSakaiProxy();
		
		if(logger.isDebugEnabled()) logger.debug("PostsPanel(" + id + "," + pageSize + "," + currentPage + ")");
		
		Form form = new Form("form");
		
		Label noPostsLabel = new Label("noPostsLabel",new ResourceModel("noPosts"));
		add(noPostsLabel);
		
		CheckBox showCommentsCheckbox = new CheckBox("showCommentsCheckbox", new PropertyModel(this,"showComments"))
		{
			protected boolean wantOnSelectionChangedNotifications()
			{
				return true;
			}
		};
		form.add(showCommentsCheckbox);
		Label showCommentsLabel = new Label("showCommentsLabel",new ResourceModel("showComments"));
		form.add(showCommentsLabel);
		
		CheckBox showFullContentCheckbox = new CheckBox("showFullContentCheckbox", new PropertyModel(this,"showFullContent"))
		{
			protected boolean wantOnSelectionChangedNotifications()
			{
				return true;
			}
		};
		form.add(showFullContentCheckbox);
		Label showFullContentLabel = new Label("showFullContentLabel",new ResourceModel("showFullContent"));
		form.add(showFullContentLabel);
		
		if(query != null)
		{
			if(sakaiProxy.isOnGateway())
				query.setVisibilities(new String[] {State.PUBLIC});
			else
				query.setSiteId(sakaiProxy.getCurrentSiteId());
		}
	
		PostDataProvider provider = new PostDataProvider(query);
		
		Link printLink = new Link("printLink")
		{
			@Override
			public void onClick()
			{
				setResponsePage(new PrintablePostsPage(query.getCreator()));
			}
		};
		
		printLink.add(new AttributeModifier("title",true,new ResourceModel("printTooltip")));
		form.add(printLink);
		
		if("".equals(query.getCreator()))
			printLink.setVisible(false);
		
		// For each post create a post panel
		DataView posts = new DataView("posts", provider)
		{
			@Override
			protected void populateItem(Item postItem)
			{
				PostModel postModel = (PostModel) postItem.getModel();
				
				PostPanel postPanel = new PostPanel("postPanel",postModel,postItem.getIndex());
				
				postPanel.setShowFullContent(showFullContent);
				postPanel.setShowComments(showComments);
				
				postItem.add(postPanel);
				
				Post post = (Post) postModel.getObject();
				
				// TODO: Add some kind of transparency effect here to show that this is a recycled post
				if(post.isRecycled())
					postPanel.add(new AttributeAppender("style", true,new Model("filter:alpha(opacity=30);opacity: 0.3;"),";"));
			}
		};
		
		posts.setItemsPerPage(pageSize);
		posts.setCurrentPage(currentPage);

		PagingNavigator nav = new PagingNavigator("postNavigator", posts);

		form.add(nav);
		
		form.add(posts);
		
		add(form);
		
		if(provider.size() > 0)
			noPostsLabel.setVisible(false);
		else
		{
			showCommentsLabel.setVisible(false);
			showCommentsCheckbox.setVisible(false);
			showFullContentLabel.setVisible(false);
			showFullContentCheckbox.setVisible(false);
			nav.setVisible(false);
			printLink.setVisible(false);
		}
	}

	public void setShowComments(boolean showComments)
	{
		this.showComments = showComments;
	}

	public boolean isShowComments()
	{
		return showComments;
	}

	public void setShowFullContent(boolean showFullContent)
	{
		this.showFullContent = showFullContent;
	}

	public boolean isShowFullContent()
	{
		return showFullContent;
	}
}
