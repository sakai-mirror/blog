package org.sakaiproject.blog.tool.dataproviders;

import java.util.Iterator;

import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.sakaiproject.blog.api.datamodel.Post;

public class PostElementsDataProvider implements IDataProvider
{
	private Post post;

	public PostElementsDataProvider(Post post)
	{
		this.post = post;
		// TODO Auto-generated constructor stub
	}

	public Iterator iterator(int first, int count)
	{
		return post.getElements();
	}

	public IModel model(Object object)
	{
		IModel model = new Model();
		model.setObject(object);
		return model;
	}

	public int size()
	{
		return post.size();
	}

	public void detach()
	{
		// TODO Auto-generated method stub
	}
}
