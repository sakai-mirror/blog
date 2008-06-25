package org.sakaiproject.tool.blog.pages;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Check;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.tool.blog.BlogApplication;
import org.sakaiproject.tool.blog.api.QueryBean;
import org.sakaiproject.tool.blog.api.SakaiProxy;
import org.sakaiproject.tool.blog.api.datamodel.Post;
import org.sakaiproject.tool.blog.api.datamodel.State;
import org.sakaiproject.tool.blog.dataproviders.PostDataProvider;
import org.sakaiproject.tool.blog.impl.managers.PostManager;
import org.sakaiproject.tool.blog.pages.models.PostModel;

public class RecycledPostsPanel extends Panel
{
	private transient Logger logger = Logger.getLogger(RecycledPostsPanel.class);
	
	private List<Post> selectedPosts = new ArrayList<Post>();

	private transient PostManager postManager;
	
	public RecycledPostsPanel(String id)
	{
		this(id,null);
	}
	
	public RecycledPostsPanel(String id,String userId)
	{
		this(id,userId,5,0);
	}
	
	public RecycledPostsPanel(String id,String userId,int pageSize,int currentPage)
	{
		super(id);
		
		SakaiProxy sakaiProxy = BlogApplication.get().getSakaiProxy();
		
		postManager = BlogApplication.get().getPostManager();
		
		if(logger.isDebugEnabled()) logger.debug("RecycledPostsPanel(" + id + "," + pageSize + "," + currentPage + ")");
		
		Form form = new Form("form");
		
		Label noPostsLabel = new Label("emptyLabel",new ResourceModel("recycleBinEmpty"));
		add(noPostsLabel);
		
		Button deleteButton = new Button("deleteButton",new ResourceModel("delete"))
		{
			public void onSubmit()
			{
				for(Post post : selectedPosts)
				{
					System.out.println("Deleting post with id '" + post.getId() + "'");
					postManager.deletePost(post.getId());
				}
				
				setResponsePage(new RecycledPage());
			}
		};
		
		form.add(deleteButton);
		
		Button restoreButton = new Button("restoreButton",new ResourceModel("restore"))
		{
			public void onSubmit()
			{
				for(Post post : selectedPosts)
				{
					System.out.println("Restoring post with id '" + post.getId() + "'");
					post.setVisibility(State.READY);
					postManager.saveVisibility(post);
				}
				
				setResponsePage(new RecycledPage());
			}
		};
		
		form.add(restoreButton);
		
		CheckGroup group = new CheckGroup("group",new PropertyModel(this,"selectedPosts"));
		
		form.add(group);
		
		QueryBean query = new QueryBean();
		query.setVisibilities(new String[] { State.RECYCLED });
		query.setSiteId(sakaiProxy.getCurrentSiteId());
		
		PostDataProvider provider = new PostDataProvider(query);
		
		// For each post create a post panel
		DataView posts = new DataView("posts", provider)
		{
			@Override
			protected void populateItem(Item postItem)
			{
				PostModel postModel = (PostModel) postItem.getModel();
				
				postItem.add(new Check("selectionBox",postModel));
				
				PostPanel postPanel = new PostPanel("postPanel",postModel,postItem.getIndex());
				
				postItem.add(postPanel);
				
				postPanel.add(new AttributeAppender("style", true,new Model("opacity: 0.3;filter:alpha(opacity=30);"),";"));
			}
		};
		
		group.add(posts);
		
		posts.setItemsPerPage(pageSize);
		posts.setCurrentPage(currentPage);

		PagingNavigator nav = new PagingNavigator("postNavigator", posts);

		form.add(nav);
		
		add(form);
		
		if(provider.size() > 0)
			noPostsLabel.setVisible(false);
		else
		{
			nav.setVisible(false);
			deleteButton.setVisible(false);
			restoreButton.setVisible(false);
		}
	}

	public void setSelectedPosts(List<Post> selectedPosts)
	{
		this.selectedPosts = selectedPosts;
	}

	public List<Post> getSelectedPosts()
	{
		return selectedPosts;
	}
}
