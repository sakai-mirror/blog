package org.sakaiproject.blogadministrator.tool.pages;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;
import org.sakaiproject.wicket.markup.html.SakaiPortletWebPage;
import org.sakaiproject.blogadministrator.tool.Migrator;

public class Main extends SakaiPortletWebPage
{
	private transient Logger logger = Logger.getLogger(Main.class);
	
	public Main()
	{
		super();
		
		if(logger.isDebugEnabled()) logger.debug("Main()");
		
		Form form = new Form("form");
		
		Button startButton = new Button("start",new Model("Start"))
		{
			public void onSubmit()
			{
				Migrator migrator = new Migrator();
				String report = migrator.migrate();
				
				setResponsePage(new ReportPage(report));
			}
		};
		
		form.add(startButton);
		
		add(form);
	}
}
