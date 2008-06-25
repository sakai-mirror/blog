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

public class RecycledPostDataProvider implements IDataProvider
{
	private List<Post> posts;
	
	private transient SakaiProxy sakaiProxy;
	private transient PostManager postManager;
	
	public RecycledPostDataProvider()
	{
		this(null);
	}

	public RecycledPostDataProvider(String requestedUserId)
	{
		super();
		
		sakaiProxy = BlogApplication.get().getSakaiProxy();
		
		// If null, then we could be in the gateway
		
		postManager = BlogApplication.get().getPostManager();
		
		try
		{
			QueryBean query = new QueryBean();
			query.setCreator(requestedUserId);
			query.setVisibilities(new String[] {State.RECYCLED});
			query.setSiteId(sakaiProxy.getCurrentSiteId());
				
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
