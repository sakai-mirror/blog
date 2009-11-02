package org.sakaiproject.blog.tool.pages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.blog.tool.BlogApplication;
import org.sakaiproject.blog.api.datamodel.Post;
import org.sakaiproject.blog.api.datamodel.State;
import org.sakaiproject.blog.api.BlogManager;
import org.sakaiproject.blog.api.PersistenceManager;
import org.sakaiproject.blog.tool.pages.models.PostModel;

public class PostAccessPanel extends Panel
{
	private PostModel postModel;

	private Post post;

	private transient BlogManager blogManager;

	public PostAccessPanel(String id,final PostModel postModel)
	{
		super(id);
		
		this.postModel = postModel;
		
		this.blogManager = BlogApplication.get().getBlogManager();
		
		post = (Post) this.postModel.getObject();
		
		Form form = new Form("form");
		
		form.add(new Label("postVisibilityLabel",new ResourceModel("postVisibility")));
		
		List<String> temp = Arrays.asList(new String[] {State.PRIVATE,State.READY});
		List<String> model = new ArrayList<String>(temp);
		
		DropDownChoice ddc = new DropDownChoice("visibilities", new PropertyModel(post,"visibility"), model)
		{
			protected boolean wantOnSelectionChangedNotifications()
			{
				return true;
			}
			
			protected void onSelectionChanged(Object newSelection)
			{
				if(blogManager != null && post != null)
					blogManager.saveVisibility(post);
				
				// TODO: Add log message
			}
		};
		form.add(ddc);
		
		CheckBox readOnlyCheckbox = new CheckBox("readOnlyCheckbox", new PropertyModel(post,"readOnly"))
		{
			public boolean wantOnSelectionChangedNotifications()
			{
				return true;
			}
			protected void onSelectionChanged(Object newSelection)
			{
				blogManager.saveReadOnly(post);
			}
		};
		form.add(readOnlyCheckbox);
		
		Label readOnlyLabel = new Label("readOnlyLabel",new ResourceModel("readOnly"));
		form.add(readOnlyLabel);
		
		CheckBox allowCommentsCheckbox = new CheckBox("allowCommentsCheckbox", new PropertyModel(post,"commentable"))
		{
			public boolean wantOnSelectionChangedNotifications()
			{
				return true;
			}
			
			protected void onSelectionChanged(Object newSelection)
			{
				blogManager.saveAllowComments(post);
			}
		};
		form.add(allowCommentsCheckbox);
		Label allowCommentsLabel = new Label("allowCommentsLabel",new ResourceModel("allowComments"));
		form.add(allowCommentsLabel);
		
		add(form);
	}
}
