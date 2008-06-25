package org.sakaiproject.tool.blog.dataproviders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.sakaiproject.tool.blog.BlogApplication;
import org.sakaiproject.tool.blog.api.QueryBean;
import org.sakaiproject.tool.blog.api.SakaiProxy;
import org.sakaiproject.tool.blog.api.datamodel.Post;
import org.sakaiproject.tool.blog.api.datamodel.State;
import org.sakaiproject.tool.blog.impl.managers.PostManager;
import org.sakaiproject.tool.blog.pages.models.PostModel;

public class PostDataProvider implements IDataProvider
{
	private List<Post> posts;
	
	private transient SakaiProxy sakaiProxy;
	private transient PostManager postManager;
	
	public PostDataProvider()
	{
		QueryBean query = new QueryBean();
		query.setSiteId(sakaiProxy.getCurrentSiteId());
		
		init(query);
	}

	public PostDataProvider(QueryBean queryBean)
	{
		super();
		
		init(queryBean);
	}
	
	private void init(QueryBean query)
	{
		sakaiProxy = BlogApplication.get().getSakaiProxy();
		
		// If null, then we could be in the gateway
		
		postManager = BlogApplication.get().getPostManager();
		
		try
		{
			/*
			QueryBean query = new QueryBean();
			
			if(requestedUserId != null && requestedUserId.length() > 0)
				query.setCreator(requestedUserId);
			
			if(sakaiProxy.isOnGateway())
				query.setVisibilities(new String[] {State.PUBLIC});
			else
			{
				query.setSiteId(sakaiProxy.getCurrentSiteId());
				
				if(visibility != null)
					query.setVisibilities(new String[] {visibility});
			}
			*/
				
			posts = postManager.getPosts(query);
		}
		catch(Exception e)
		{
			posts = new ArrayList<Post>();
		}
	}

	public Iterator iterator(int first, int count)
	{
		if (posts == null)
			return Collections.EMPTY_LIST.iterator();

		try
		{
			List<Post> slice = posts.subList(first, first + count);
			return slice.iterator();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return Collections.EMPTY_LIST.iterator();
		}
	}

	public IModel model(Object object)
	{
		return new PostModel((Post) object);
	}

	public int size()
	{
		if (posts == null)
			return 0;

		return posts.size();
	}

	public void detach() {}
}
