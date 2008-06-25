package org.sakaiproject.tool.blog.dataproviders;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.sakaiproject.tool.blog.BlogApplication;
import org.sakaiproject.tool.blog.api.BlogMember;
import org.sakaiproject.tool.blog.api.QueryBean;
import org.sakaiproject.tool.blog.api.SakaiProxy;
import org.sakaiproject.tool.blog.api.datamodel.Post;
import org.sakaiproject.tool.blog.api.datamodel.State;
import org.sakaiproject.tool.blog.impl.managers.PostManager;
import org.sakaiproject.tool.blog.pages.models.MemberModel;

public class SiteMembersDataProvider implements IDataProvider
{
	private List<BlogMember> members;
	private String sort;
	private String direction;

	public SiteMembersDataProvider(String sort,String direction)
	{
		this.sort = sort;
		this.direction = direction;
		load();
	}
	
	private void load()
	{
		members = BlogApplication.get().getSakaiProxy().getSiteMembers();
		
		SakaiProxy sakaiProxy = BlogApplication.get().getSakaiProxy();
		QueryBean qb = new QueryBean();
		qb.setSiteId(sakaiProxy.getCurrentSiteId());
		qb.setVisibilities(new String[] { State.PUBLIC, State.READY });
		qb.setCaller(sakaiProxy.getCurrentUserId());
		PostManager postManager = BlogApplication.get().getPostManager();

		for (BlogMember member : members)
		{
			qb.setCreator(member.getUserId());
			List<Post> posts = null;
			
			try
			{
				posts = postManager.getPosts(qb);
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
                
            	/*
            	Object value1 = null;
            	Object value2 = null;
            	
            	String property = sortColumn.substring(0,1).toUpperCase() + sortColumn.substring(1, sortColumn.length());
            	
            	try
            	{
            		Method m = obj1.getClass().getMethod("get" + property, null);
            		
            		try
            		{
            			value1 = m.invoke(obj1,null);
            			value2 = m.invoke(obj2,null);
            		}
            		catch(Exception e)
            		{
            			e.printStackTrace();
            		}
            	}
            	catch(NoSuchMethodException e)
            	{
            		e.printStackTrace();
            	}
            	
            	int compare = ((Comparable) value1).compareTo(value2);
            	
            	if (!ascending)
            		compare *= -1;
            	
            	return compare;
            	*/
            }
        });
	}

	public Iterator iterator(int first, int count)
	{
		load();
		
		if (members == null)
			return Collections.EMPTY_LIST.iterator();

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
