package org.sakaiproject.blog.impl;

import org.sakaiproject.search.api.EntityContentProducer;
import org.sakaiproject.search.api.SearchUtils;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.blog.api.BlogManager;
import org.sakaiproject.blog.api.SakaiProxy;
import org.sakaiproject.blog.api.datamodel.Post;
import org.sakaiproject.blog.api.datamodel.Paragraph;
import org.sakaiproject.blog.api.datamodel.Comment;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.EntityProducer;
import org.apache.log4j.Logger;

import java.io.Reader;
import java.util.List;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: fisha
 * Date: Jul 15, 2008
 * Time: 2:37:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class BlogEntityContentProducer implements EntityContentProducer
{
    private Logger logger = Logger.getLogger(BlogEntityContentProducer.class);

   private BlogManager blogManager;
   private SakaiProxy sakaiProxy; 

    public boolean isContentFromReader(String s)
    {
        return false;
    }

    public Reader getContentReader(String s)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getContent(String reference)
    {
        Reference ref = getReference(reference);
        Entity cr = ref.getEntity();
        Post post = (Post) cr;

        StringBuilder builder = new StringBuilder();

        builder.append(post.getTitle());
        builder.append(post.getShortText());
        builder.append(post.getKeywords());

        for(Paragraph paragraph : post.getParagraphs())
            builder.append(paragraph.getText());

        for(Comment comment : post.getComments())
            builder.append(comment.getText());

        return builder.toString();
    }

    public String getTitle(String reference)
    {
        Reference ref = getReference(reference);
        Entity cr = ref.getEntity();
        Post post = (Post) cr;
        String r = SearchUtils.appendCleanString(post.getTitle(), null).toString();
        if (logger.isDebugEnabled())
            logger.debug("getTitle:" + reference + ":" + r);
        return r;
    }

    public String getUrl(String reference)
    {
        Reference ref = getReference(reference);
        return ref.getUrl() + "html";
    }

    public boolean matches(String reference)
    {
        try
        {
            Reference ref = getReference(reference);
            EntityProducer ep = ref.getEntityProducer();
            return (ep instanceof BlogManager);
        }
        catch (Exception ex)
        {
            return false;
        }
    }

    public List getAllContent()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Integer getAction(Event event)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean matches(Event event)
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getTool()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getSiteId(String s)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List getSiteContent(String s)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Iterator getSiteContentIterator(String s)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isForIndex(String s)
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean canRead(String s)
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Map getCustomProperties(String s)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getCustomRDF(String s)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getId(String s)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getType(String s)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getSubType(String s)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getContainer(String s)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    private Reference getReference(String reference)
    {
        try
        {
            Reference r = sakaiProxy.newReference(reference);
            if (logger.isDebugEnabled())
            {
                logger.debug("getReference:" + reference + ":" + r);
            }
            return r;
        }
        catch (Exception ex)
        {
        }
        return null;
    }

    public void setBlogManager(BlogManager blogManager)
    {
        this.blogManager = blogManager;

    }

    public void setSakaiProxy(SakaiProxy sakaiProxy)
    {
        this.sakaiProxy = sakaiProxy;
    }
}
