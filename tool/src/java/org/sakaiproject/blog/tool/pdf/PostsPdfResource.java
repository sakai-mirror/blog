package org.sakaiproject.blog.tool.pdf;

import java.awt.Toolkit;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.DynamicWebResource;
import org.sakaiproject.blog.api.BlogManager;
import org.sakaiproject.blog.api.SakaiProxy;
import org.sakaiproject.blog.api.datamodel.LinkRule;
import org.sakaiproject.blog.api.datamodel.Post;
import org.sakaiproject.blog.api.datamodel.PostElement;
import org.sakaiproject.blog.api.datamodel.State;
import org.sakaiproject.blog.tool.BlogApplication;
import org.sakaiproject.blog.tool.dataproviders.PostDataProvider;

import com.lowagie.text.Anchor;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.html.simpleparser.HTMLWorker;
import com.lowagie.text.html.simpleparser.StyleSheet;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;

public class PostsPdfResource extends DynamicWebResource
{
	private transient Logger logger = Logger.getLogger(PostsPdfResource.class);
	
	private transient PostDataProvider postDataProvider = null;
	
	private transient BlogManager blogManager = null;
	
	private transient SakaiProxy sakaiProxy = null;
	
	public PostsPdfResource(String filename,SakaiProxy sakaiProxy, PostDataProvider postDataProvider)
	{
		super(filename);
		this.sakaiProxy = sakaiProxy;
		this.postDataProvider = postDataProvider;
		blogManager = BlogApplication.get().getBlogManager();
	}
	
	@Override
	protected ResourceState getResourceState()
	{
		ResourceState state = new ResourceState()
		{

			@Override
			public String getContentType()
			{
				return "application/pdf";
			}

			@Override
			public byte[] getData()
			{
				Iterator i = postDataProvider.iterator(0, postDataProvider.size());
				
				Document pdf = null;
				PdfWriter writer = null;
				ByteArrayOutputStream baos = null;
			
				try
				{
					pdf = new Document();
					//HTMLWorker htmlWorker = new HTMLWorker(pdf);
					baos = new ByteArrayOutputStream(1000);
					
					Rectangle pageDimensions = new Rectangle(841.89f, 595.276f);

					pdf.setPageSize(pageDimensions);
					writer = PdfWriter.getInstance(pdf,baos);
					pdf.open();

					//add metadata  
					pdf.addCreationDate();
					pdf.addTitle("All blog posts for site: " + sakaiProxy.getCurrentSiteId());
					pdf.addCreator("Lancaster University Management School");
					pdf.addSubject("All Blog Posts");
					
					//setup text
	                BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
	                
	                Font titleFont = new Font(bf,20f,Font.BOLD);
	                Font authorFont = new Font(bf,12f);
	                Font dateFont = new Font(bf,12f,Font.ITALIC);
	                Font abstractFont = new Font(bf,14f,Font.ITALIC);
	                Font paragraphFont = new Font(bf,14f);
			
					while(i.hasNext())
					{
						Post post = (Post) i.next();
						
						if(post.isRecycled())
							continue;
						
						if(post.isPrivate()
							&& !post.getCreatorId().equals(sakaiProxy.getCurrentUserId()))
						{
							continue;
						}
						
						Paragraph titlePara = new Paragraph();
						titlePara.add(new Chunk(post.getTitle(),titleFont));
						pdf.add(titlePara);
						
						pdf.add(Chunk.NEWLINE);
						
						String author = sakaiProxy.getDisplayNameForTheUser(post.getCreatorId());
						Paragraph authorPara = new Paragraph();
						authorPara.add(new Chunk("Created By: " + author,authorFont));
						pdf.add(authorPara);
						
						Paragraph createdPara = new Paragraph();
						String createdDate = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.MEDIUM,Locale.UK).format(post.getCreatedDate());
						createdDate = "CreatedOn: " + createdDate;
						createdPara.add(new Chunk(createdDate,dateFont));
						pdf.add(createdPara);
						
						Paragraph modifiedPara = new Paragraph();
						String modifiedDate = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.MEDIUM,Locale.UK).format(post.getModifiedDate());
						modifiedDate = "Modifed On: " + modifiedDate;
						modifiedPara.add(new Chunk(modifiedDate,dateFont));
						pdf.add(modifiedPara);
						
						pdf.add(Chunk.NEWLINE);
						
						//Paragraph abstractParagraph = new Paragraph();
						//String shortText
						//	= post.getShortText().replaceAll("</?[ \\w\"=]*/?>","");
						String shortText
							= post.getShortText();
						ArrayList list = HTMLWorker.parseToList(new StringReader(shortText),new StyleSheet());
						for(Iterator j = list.iterator();j.hasNext();)
						{
							//abstractParagraph.add(new Chunk(shortText,abstractFont));
							Object o = j.next();
							pdf.add((Element) o);
						}
						
						pdf.add(Chunk.NEWLINE);
						
						Iterator<PostElement> elements = post.getElements();
						
						while(elements.hasNext())
						{
							PostElement element = elements.next();
							
							if(element instanceof org.sakaiproject.blog.api.datamodel.Paragraph)
							{
								org.sakaiproject.blog.api.datamodel.Paragraph postPara = (org.sakaiproject.blog.api.datamodel.Paragraph) element;
								String text = postPara.getText();
								logger.debug("Text: " + text);
								
								Paragraph textPara = new Paragraph();
								textPara.setIndentationLeft(postPara.getIndentation() * 4);
								
								Pattern imagePattern = Pattern.compile("<img[ \\w=\"]*src=\"([^\"]*)\"[^>]*>");
								Matcher matcher = imagePattern.matcher(text);
								
								int lastEnd = 0;
								if(!matcher.find())
								{
									text = text.replaceAll("</?[ \\w\"=]*/?>","");
									textPara.add(new Chunk(text,paragraphFont));
								}
								else
								{
									do
									{
										String urlMatch = matcher.group(1);
										logger.debug("URL: " + urlMatch);
										textPara.add(new Chunk(text.substring(lastEnd,matcher.start() - 1),paragraphFont));
										lastEnd = matcher.end();
										try
										{
											Image image = Image.getInstance(new URL(urlMatch));
											textPara.add(image);
										}
										catch(Exception e)
										{
											textPara.add(new Chunk(urlMatch,paragraphFont));
											logger.error("Caught exception.",e);
										}
									}
									while(matcher.find());
									
									textPara.add(new Chunk(text.substring(lastEnd),paragraphFont));
								}
								
								pdf.add(textPara);
							}
							else if(element instanceof org.sakaiproject.blog.api.datamodel.Image)
							{
								org.sakaiproject.blog.api.datamodel.Image postImage = (org.sakaiproject.blog.api.datamodel.Image) element;
								postImage = blogManager.getImage(postImage.getId(), postImage.WEB);
								Image image = Image.getInstance(postImage.getImageContentWithWebSize());
								image.setIndentationLeft(postImage.getIndentation() * 4);
								pdf.add(image);
							}
							else if(element instanceof LinkRule)
							{
								LinkRule link = (LinkRule) element;
								Paragraph linkPara = new Paragraph();
								Anchor anchor = new Anchor(0f,link.getDisplayName(),paragraphFont);
								anchor.setReference(link.getUrl());
								linkPara.add(anchor);
								pdf.add(linkPara);
							}
							else if(element instanceof org.sakaiproject.blog.api.datamodel.File)
							{
								
							}
							
							pdf.add(Chunk.NEWLINE);
						}
						
						pdf.newPage();
					}
				}
				catch(Exception e)
				{
					logger.error("Caught exception whilst creating PDF document.",e);
				}
				finally
				{
					try
					{
					//if(baos != null) baos.close();
					//if(writer != null) writer.close();
					if(pdf != null) pdf.close();
					}
					catch(Exception e) {}
				}
				
				return baos.toByteArray();
			}
		};
		
		return state;
	}
}