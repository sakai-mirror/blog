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
import org.sakaiproject.blog.api.datamodel.State;
import org.sakaiproject.blog.tool.pages.models.PostModel;

public class RecycledPostDataProvider implements IDataProvider
{
	private List<Post> posts;
	
	private transient SakaiProxy sakaiProxy;
	private transient BlogManager blogManager;
	
	public RecycledPostDataProvider()
	{
		this(null);
	}

	public RecycledPostDataProvider(String requestedUserId)
	{
		super();
		
		sakaiProxy = BlogApplication.get().getSakaiProxy();
		
		// If null, then we could be in the gateway
		
		blogManager = BlogApplication.get().getBlogManager();
		
		try
		{
			QueryBean query = new QueryBean();
			query.setCreator(requestedUserId);
			query.setVisibilities(new String[] {State.RECYCLED});
			query.setSiteId(sakaiProxy.getCurrentSiteId());
				
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
