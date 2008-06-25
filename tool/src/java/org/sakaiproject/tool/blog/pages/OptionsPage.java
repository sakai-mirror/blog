package org.sakaiproject.tool.blog.pages;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.tool.blog.BlogApplication;
import org.sakaiproject.tool.blog.api.BlogFunctions;
import org.sakaiproject.tool.blog.api.datamodel.BlogOptions;
import org.sakaiproject.tool.blog.api.datamodel.BlogPermissions;
import org.sakaiproject.tool.blog.api.datamodel.Modes;
import org.sakaiproject.tool.blog.api.datamodel.Post;
import org.sakaiproject.tool.blog.api.datamodel.State;
import org.sakaiproject.tool.blog.impl.managers.PersistenceManager;
import org.sakaiproject.tool.blog.impl.managers.PostManager;
import org.sakaiproject.tool.blog.pages.models.PostModel;

public class OptionsPage extends BasePage
{
	private transient Logger logger = Logger.getLogger(OptionsPage.class);
	
	private BlogOptions options;
	
	public OptionsPage()
	{
		super();
		
		final BlogOptions options = persistenceManager.getOptions();
		
		add(new Label("optionsLabel",new ResourceModel("options")));
		
		Form form = new Form("form");
		
		form.add(new Label("modeLabel",new ResourceModel("mode")));
		
		List<String> model = Arrays.asList(new String[] {Modes.LEARNING_LOG,Modes.BLOG});
		DropDownChoice ddc = new DropDownChoice("modes", new PropertyModel(options,"mode"), model);
		
		form.add(ddc);
		
		form.add(new Button("saveButton",new ResourceModel("save"))
		{
			public void onSubmit()
			{
				if(options.getMode().equals(Modes.LEARNING_LOG))
				{
					try
					{
						BlogPermissions permissions = new BlogPermissions();
						permissions.setRole("Student");
				
						permissions.setPostCreate(true);
						permissions.setPostReadOwn(true);
						permissions.setPostDeleteOwn(true); // Need to set timeout
						permissions.setCommentCreate(true);
						permissions.setCommentReadAny(true);
						permissions.setCommentReadOwn(true);
						permissions.setCommentUpdateOwn(true);
					
						sakaiProxy.savePermissions(permissions);
					}
					catch (Exception e)
					{
						logger.error("Caught exception whilst saving 'Student' permissions",e);
					}
					
					try
					{
						BlogPermissions permissions = new BlogPermissions();
						permissions.setRole("tutor");
				
						permissions.setPostReadAny(true);
						permissions.setCommentCreate(true);
						permissions.setCommentReadAny(true);
						permissions.setCommentReadOwn(true);
						permissions.setCommentUpdateOwn(true);
						permissions.setCommentDeleteOwn(true);
					
						sakaiProxy.savePermissions(permissions);
					}
					catch (Exception e)
					{
						logger.error("Caught exception whilst saving 'tutor' permissions",e);
					}
				}
					
				try
				{
					persistenceManager.saveOptions(options);
				}
				catch (Exception e)
				{
					logger.error("Caught exception whilst saving options",e);
				}
				
				setResponsePage(new ViewAll());
			}
		});
		
		Button cancelButton = new Button("cancelButton",new ResourceModel("cancel"))
		{
			public void onSubmit()
			{
				setResponsePage(new ViewAll());
			}
		};
		
		cancelButton.setDefaultFormProcessing(false);
		
		form.add(cancelButton);
		
		add(form);
	}
}
