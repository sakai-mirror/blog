package org.sakaiproject.blog.tool.dataproviders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.sakaiproject.blog.tool.BlogApplication;
import org.sakaiproject.blog.api.QueryBean;
import org.sakaiproject.blog.api.SakaiProxy;
import org.sakaiproject.blog.api.BlogManager;
import org.sakaiproject.blog.api.datamodel.Post;
import org.sakaiproject.blog.tool.pages.models.PostModel;

public class PostDataProvider implements IDataProvider
{
	private List<Post> posts;
	
	private transient SakaiProxy sakaiProxy;
	private transient BlogManager blogManager;
	
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
		
		blogManager = BlogApplication.get().getBlogManager();
		
		if(query == null)
		{
			posts = new ArrayList<Post>();
			return;
		}
		
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
				
			posts = blogManager.getPosts(query);
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
