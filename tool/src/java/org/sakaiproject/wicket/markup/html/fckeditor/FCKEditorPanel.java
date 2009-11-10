package org.sakaiproject.wicket.markup.html.fckeditor;

import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.collections.MiniMap;
import org.apache.wicket.util.template.PackagedTextTemplate;
import org.apache.wicket.util.template.TextTemplate;

/**
 * @author Adrian Fish
 */
public class FCKEditorPanel extends Panel
{
	private transient Logger logger = Logger.getLogger(FCKEditorPanel.class);
	
	public static final String BASIC = "Basic";
	public static final String DEFAULT = "Default";
	
	private static final String OPENING_P = "<p>";
    private static final String CLOSING_P = "</p>";
    private static final String ESCAPED_OPENING_P = "&lt;p&gt;";
    private static final String ESCAPED_CLOSING_P = "&lt;/p&gt;";
	
	private boolean stripContainingParagraphTags = false;
	private boolean unescapeHtml;
	
	/**
     * 
     * @param id The wicket:id
     * @param model The data model
     * @param width The width of the rendered textarea
     * @param height The height of the rendered text area
     * @param toolbarSet The set of toolbars you want rendering, either basic of
     *                   default
     * @param collectionId The Sakai collection id for the FCKeditor to use
     * @param strip Set to true if you want the component to strip off the
     *              surrounding paragraph tags that FCKeditor produces
     */
    public FCKEditorPanel(String id,IModel model,String width,String height,String toolbarSet, String collectionId,boolean stripContainingParagraphTags)
    {
        this(id,model,width,height,toolbarSet,collectionId);
        this.stripContainingParagraphTags = stripContainingParagraphTags;
    }
    
    /**
     * 
     * @param id The wicket:id
     * @param model The data model
     * @param width The width of the rendered textarea
     * @param height The height of the rendered text area
     * @param toolbarSet The set of toolbars you want rendering, either basic of
     *                   default
     * @param collectionId The Sakai collection id for the FCKeditor to use
     * @param strip Set to true if you want the component to strip off the
     *              surrounding paragraph tags that FCKeditor produces
     */
    public FCKEditorPanel(String id,IModel model,String width,String height,String toolbarSet, String collectionId,boolean strip,boolean unescape)
    {
        this(id,model,width,height,toolbarSet,collectionId,strip);

        unescapeHtml = unescape;
    }
	
    /**
     * @param id The wicket:id
     * @param model The data model
     * @param width The width of the rendered textarea
     * @param height The height of the rendered text area
     * @param toolbarSet The set of toolbars you want rendering, either basic of
     *                   default
     * @param collectionId The Sakai collection id for the FCKeditor to use
     */
	public FCKEditorPanel(String id,IModel model,String width,String height,String toolbarSet, String collectionId)
	{
		super(id);
		
		TextArea textArea = new TextArea("editor",model)
        {
            protected void onModelChanged()
            {
                if(logger.isDebugEnabled()) logger.debug("onModelChanged()");

                String value = getModelValue();

                if(logger.isDebugEnabled()) logger.debug("Value Before:" + value);

                if(unescapeHtml)
                {
                    if(logger.isDebugEnabled()) logger.debug("Unescaping HTML ...");
                    value = StringEscapeUtils.unescapeHtml(value.trim());
                    if(logger.isDebugEnabled()) logger.debug("Value after unescaping:" + value);
                }

                if(stripContainingParagraphTags)
                {
                    if(logger.isDebugEnabled()) logger.debug("Stripping containing paragraphs ...");

                    String openingP = ESCAPED_OPENING_P;
                    String closingP = ESCAPED_CLOSING_P;

                    if(unescapeHtml)
                    {
                        openingP = OPENING_P;
                        closingP = CLOSING_P;
                    }

                    if(value.length() >= (openingP.length() + closingP.length()))
                    {
                        if(value.startsWith(openingP))
                            value = value.substring(openingP.length());

                        if(value.endsWith(closingP))
                        	 value = value.substring(0,value.length() - closingP.length());
                    }
                }
                
                if(logger.isDebugEnabled()) logger.debug("Value After:" + value);

                getModel().setObject(value);

                super.onModelChanged();
            }
        };
		
		
		//TextArea textArea = new TextArea("editor",model);
		String textareaId = id + ((int)(Math.random() * 100));
		textArea.setMarkupId(textareaId);
		textArea.setOutputMarkupId(true);
		add(textArea);
		TextTemplate scriptTemplate = new PackagedTextTemplate(FCKEditorPanel.class,"FCKEditorScript.js");
		Map map = new MiniMap(6);
		map.put("width", width);
		map.put("height", height);
		map.put("toolbarSet", toolbarSet);
		map.put("textareaId", textareaId);
		map.put("collectionId", collectionId);
		map.put("enterMode","br");
		
		/*
		WebApplication webApp = null;
		
		webApp = (WebApplication) Application.get();
		IResourceFinder finder = webApp.getResourceSettings().getResourceFinder();
		IResourceStream resource = finder.find(FCKEditorPanel.class, "config.js");
		
		if(resource == null)
		{
			
		}
		*/
		
		String script = scriptTemplate.asString(map);
		Label scriptLabel = new Label("script",script);
		scriptLabel.setEscapeModelStrings(false);
		add(scriptLabel);
	}
}
