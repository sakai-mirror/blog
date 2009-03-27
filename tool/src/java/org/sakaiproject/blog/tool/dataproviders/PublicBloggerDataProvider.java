package org.sakaiproject.blog.tool.dataproviders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.sakaiproject.blog.tool.BlogApplication;
import org.sakaiproject.blog.api.*;
import org.sakaiproject.blog.api.datamodel.Post;
import org.sakaiproject.blog.api.datamodel.State;
import org.sakaiproject.blog.tool.pages.models.MemberModel;

/**
 * This class provides the user details of Sakai user's who have marked blog
 * entries as PUBLIC.
 * 
 * @author Adrian Fish (a.fish@lancaster.ac.uk)
 *
 */
public class PublicBloggerDataProvider implements IDataProvider
{
	private List<BlogMember> members = new ArrayList<BlogMember>();
	
	public PublicBloggerDataProvider(final String sort,final String direction)
	{
		PersistenceManager persistenceManager = BlogApplication.get().getPersistenceManager();
		members = persistenceManager.getPublicBloggers();
		
		SakaiProxy sakaiProxy = BlogApplication.get().getSakaiProxy();
		QueryBean qb = new QueryBean();
		qb.setVisibilities(new String[] { State.PUBLIC });
		BlogManager blogManager = BlogApplication.get().getBlogManager();

		for (BlogMember member : members)
		{
			qb.setCreator(member.getUserId());
			List<Post> posts = null;
			
			try
			{
				posts = blogManager.getPosts(qb);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
			if (posts != null)
			{
				member.setNumberOfPosts(posts.size());
				long last = 0L;

				for (Post post : posts)
				{
					long date = post.getCreatedDate().getTime();
					if (date > last)
						last = date;
				}

				member.setDateOfLastPost(last);
			}
			else
				member.setNumberOfPosts(0);
		}
		
		Collections.sort(members, new Comparator()
		{
            public int compare(Object obj1, Object obj2)
            {
            	PropertyModel model1 = new PropertyModel(obj1, sort);
                PropertyModel model2 = new PropertyModel(obj2, sort);

                Object modelObject1 = model1.getObject();
                Object modelObject2 = model2.getObject();

                int compare = ((Comparable) modelObject1).compareTo(modelObject2);

                //if (!ascending)
                if (!direction.equals("ascending"))
                    compare *= -1;

                return compare;
            }
        });
	}

	public Iterator iterator(int first, int count)
	{
		try
		{
			List<BlogMember> slice = members.subList(first, first + count);
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
		return new MemberModel((BlogMember) object);
	}

	public int size()
	{
		if (members == null)
			return 0;

		return members.size();
	}

	public void detach()
	{
	}
}
