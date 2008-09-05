package org.sakaiproject.blog.tool.dataproviders;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.sakaiproject.blog.tool.BlogApplication;
import org.sakaiproject.blog.api.SakaiProxy;
import org.sakaiproject.blog.api.datamodel.Comment;
import org.sakaiproject.blog.api.datamodel.Post;
import org.sakaiproject.blog.api.datamodel.State;
import org.sakaiproject.blog.api.BlogMember;
import org.sakaiproject.blog.api.QueryBean;
import org.sakaiproject.blog.api.BlogManager;
import org.sakaiproject.blog.tool.pages.models.MemberModel;

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
		//qb.setVisibilities(new String[] { State.PUBLIC, State.READY });
		qb.setCaller(sakaiProxy.getCurrentUserId());
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
				long lastPost = 0L;
				long lastComment = 0L;
				String lastCommentCreator = "";

				for (Post post : posts)
				{
					for(Comment comment : post.getComments())
					{
						long date = comment.getCreatedDate().getTime();
						if (date > lastComment)
						{
							lastComment = date;
							lastCommentCreator = comment.getCreatorId();
						}
					}
					
					long date = post.getCreatedDate().getTime();
					if (date > lastPost)
						lastPost = date;
				}

				member.setDateOfLastPost(lastPost);
				member.setDateOfLastComment(lastComment);
				member.setLastCommentCreator(lastCommentCreator);
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

                if (!direction.equals("ascending"))
                    compare *= -1;

                return compare;
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
