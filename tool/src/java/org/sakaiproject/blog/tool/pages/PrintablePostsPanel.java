package org.sakaiproject.blog.tool.pages;

import org.apache.log4j.Logger;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.blog.tool.BlogApplication;
import org.sakaiproject.blog.api.QueryBean;
import org.sakaiproject.blog.api.SakaiProxy;
import org.sakaiproject.blog.api.datamodel.Post;
import org.sakaiproject.blog.api.datamodel.State;
import org.sakaiproject.blog.tool.dataproviders.PostDataProvider;
import org.sakaiproject.blog.tool.pages.models.PostModel;

public class PrintablePostsPanel extends Panel
{
	private transient Logger logger = Logger.getLogger(PrintablePostsPanel.class);
	
	public PrintablePostsPanel(String id)
	{
		this(id,new QueryBean());
	}
	
	public PrintablePostsPanel(String id,QueryBean query)
	{
		super(id);
		
		SakaiProxy sakaiProxy = BlogApplication.get().getSakaiProxy();
		
		if(logger.isDebugEnabled()) logger.debug("PrintablePostsPanel()");
		
		Form form = new Form("form");
		
		Label noPostsLabel = new Label("noPostsLabel",new ResourceModel("noPosts"));
		add(noPostsLabel);
		
		if(query != null)
		{
			if(sakaiProxy.isOnGateway())
				query.setVisibilities(new String[] {State.PUBLIC});
			else
				query.setSiteId(sakaiProxy.getCurrentSiteId());
		}
	
		PostDataProvider provider = new PostDataProvider(query);
		
		// For each post create a post panel
		DataView posts = new DataView("posts", provider)
		{
			@Override
			protected void populateItem(Item postItem)
			{
				PostModel postModel = (PostModel) postItem.getModel();
				
				PostPanel postPanel = new PostPanel("postPanel",postModel,postItem.getIndex());
				
				postPanel.setShowFullContent(true);
				postPanel.setShowComments(true);
				
				postItem.add(postPanel);
				
				Post post = (Post) postModel.getObject();
				
				// TODO: Add some kind of transparency effect here to show that this is a recycled post
				if(post.isRecycled())
					postPanel.add(new AttributeAppender("style", true,new Model("filter:alpha(opacity=30);opacity: 0.3;"),";"));
			}
		};
		
		form.add(posts);
		add(form);
		
		if(provider.size() > 0)
			noPostsLabel.setVisible(false);
	}
}
