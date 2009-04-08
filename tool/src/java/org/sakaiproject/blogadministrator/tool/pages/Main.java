package org.sakaiproject.blogadministrator.tool.pages;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;
import org.sakaiproject.blogadministrator.tool.Migrator;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.cover.SessionManager;

public class Main extends WebPage implements IHeaderContributor
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
	
	//Style it like a Sakai tool
	protected static final String HEADSCRIPTS = "/library/js/headscripts.js";
	protected static final String BODY_ONLOAD_ADDTL="setMainFrameHeight( window.name )";
	
	public void renderHead(IHeaderResponse response) {
		//get Sakai skin
		String skinRepo = ServerConfigurationService.getString("skin.repo");
		String toolCSS = getToolSkinCSS(skinRepo);
		String toolBaseCSS = skinRepo + "/tool_base.css";
		
		//Sakai additions
		response.renderJavascriptReference(HEADSCRIPTS);
		response.renderCSSReference(toolBaseCSS);
		response.renderCSSReference(toolCSS);
		response.renderOnLoadJavascript(BODY_ONLOAD_ADDTL);
		
		//for jQuery
		response.renderJavascriptReference("js/jquery-1.2.5.min.js");

		//for cluetip
		response.renderCSSReference("css/jquery.cluetip.css");
		response.renderJavascriptReference("javascript/jquery.dimensions.js");
		response.renderJavascriptReference("javascript/jquery.hoverIntent.js");
		response.renderJavascriptReference("javascript/jquery.cluetip.js");
		
		//for tablesorter
		response.renderJavascriptReference("javascript/jquery.tablesorter.min.js");
		response.renderJavascriptReference("javascript/jquery.tablesorter.pager.js");
		
		//Tool additions (at end so we can override if required)
		response.renderString("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />");
		response.renderCSSReference("css/blog.css");
		response.renderJavascriptReference("js/blog.js");
		
	}
	
	protected String getToolSkinCSS(String skinRepo) {
		String skin = null;
		try {
			skin = SiteService.findTool(SessionManager.getCurrentToolSession().getPlacementId()).getSkin();			
		}
		catch(Exception e) {
			skin = ServerConfigurationService.getString("skin.default");
		}
		
		if(skin == null) {
			skin = ServerConfigurationService.getString("skin.default");
		}
		
		return skinRepo + "/" + skin + "/tool.css";
	}
}
