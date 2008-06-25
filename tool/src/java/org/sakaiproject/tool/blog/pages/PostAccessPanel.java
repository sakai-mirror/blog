package org.sakaiproject.tool.blog.pages;

import java.util.Arrays;
import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.tool.blog.BlogApplication;
import org.sakaiproject.tool.blog.api.datamodel.Post;
import org.sakaiproject.tool.blog.api.datamodel.State;
import org.sakaiproject.tool.blog.impl.managers.PersistenceManager;
import org.sakaiproject.tool.blog.impl.managers.PostManager;
import org.sakaiproject.tool.blog.pages.models.PostModel;

public class PostAccessPanel extends Panel
{
	private PostModel postModel;

	private Post post;

	private transient PostManager postManager;

	public PostAccessPanel(String id,final PostModel postModel)
	{
		super(id);
		
		this.postModel = postModel;
		
		this.postManager = BlogApplication.get().getPostManager();
		
		post = (Post) this.postModel.getObject();
		
		Form form = new Form("form");
		
		form.add(new Label("postVisibilityLabel",new ResourceModel("postVisibility")));
		
		List<String> model = Arrays.asList(new String[] {State.PRIVATE,State.READY});
		if(!BlogApplication.get().getPersistenceManager().getOptions().isLearningLogMode())
			model.add(State.PUBLIC);
		
		DropDownChoice ddc = new DropDownChoice("visibilities", new PropertyModel(post,"visibility"), model)
		{
			protected boolean wantOnSelectionChangedNotifications()
			{
				return true;
			}
			
			protected void onSelectionChanged(Object newSelection)
			{
				postManager.saveVisibility(post);
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
				postManager.saveReadOnly(post);
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
				postManager.saveAllowComments(post);
			}
		};
		form.add(allowCommentsCheckbox);
		Label allowCommentsLabel = new Label("allowCommentsLabel",new ResourceModel("allowComments"));
		form.add(allowCommentsLabel);
		
		add(form);
		
		PersistenceManager pm = BlogApplication.get().getPersistenceManager();
		if(pm.getOptions().isLearningLogMode())
		{
			readOnlyLabel.setVisible(false);
			readOnlyCheckbox.setVisible(false);
			allowCommentsLabel.setVisible(false);
			allowCommentsCheckbox.setVisible(false);
		}
	}
}
