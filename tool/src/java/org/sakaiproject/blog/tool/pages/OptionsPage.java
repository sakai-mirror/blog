package org.sakaiproject.blog.tool.pages;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.wicket.Component;
import org.apache.wicket.Response;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.body.BodyTagAttributeModifier;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.internal.HeaderResponse;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.parser.XmlTag;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.blog.api.datamodel.BlogOptions;
import org.sakaiproject.blog.api.datamodel.BlogPermissions;
import org.sakaiproject.blog.api.datamodel.Modes;

public class OptionsPage extends BasePage
{
	private transient Logger logger = Logger.getLogger(OptionsPage.class);
	
	public OptionsPage()
	{
		super();
		
		final BlogOptions options = persistenceManager.getOptions();
		
		add(new Label("optionsLabel",new ResourceModel("options")));
		
		Form form = new Form("form");
		
		form.add(new Label("modeLabel",new ResourceModel("mode")));
		
		List<String> hoursModel = Arrays.asList(new String[] {"0","1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","23","23"});
		form.add(new DropDownChoice("hours", new PropertyModel(options,"timeoutHours"), hoursModel));
		
		List<String> daysModel = Arrays.asList(new String[] {"0","1","2","3","4","5","6","7"});
		form.add(new DropDownChoice("days", new PropertyModel(options,"timeoutDays"), daysModel));
		
		form.add(new Label("timeoutLabel",new ResourceModel("timeoutTitle")));
		form.add(new Label("hoursLabel",new ResourceModel("hours")));
		form.add(new Label("daysLabel",new ResourceModel("days")));
		
		RadioGroup radioGroup = new RadioGroup("modes",new PropertyModel(options,"mode"));
		
		Radio learningLogRadio = new Radio("learningLog",new Model(Modes.LEARNING_LOG));
		learningLogRadio.add(new AttributeAppender("onClick",new Model("showTimeoutPanel(event);"),";"));
		/*
		if(options.isLearningLogMode())
		{
			XmlTag bodyTag = new XmlTag();
			bodyTag.setName("body");
			bodyTag.setType(XmlTag.OPEN);
			ComponentTag tag = new ComponentTag(bodyTag);
			c.add(new AttributeAppender("onload",new Model("showTimeoutPanel(event);"),";"));
			//getParent().add(new BodyTagAttributeModifier("onload", true, new Model("showTimeoutPanel(event);"), this));
		}
		*/
		radioGroup.add(learningLogRadio);
		radioGroup.add(new Label("learningLogModeLabel",new ResourceModel("learningLogMode")));
		
		Radio blogRadio = new Radio("blog",new Model(Modes.BLOG));
		blogRadio.add(new AttributeAppender("onClick",new Model("hideTimeoutPanel(event);"),";"));
		radioGroup.add(blogRadio);
		radioGroup.add(new Label("blogModeLabel",new ResourceModel("blogMode")));
		
		form.add(radioGroup);
		
		//form.add(ddc);
		
		form.add(new Button("saveButton",new ResourceModel("save"))
		{
			public void onSubmit()
			{
				if(options.isLearningLogMode())
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
