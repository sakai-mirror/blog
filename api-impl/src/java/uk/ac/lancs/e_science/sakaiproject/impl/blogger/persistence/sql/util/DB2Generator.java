package uk.ac.lancs.e_science.sakaiproject.impl.blogger.persistence.sql.util;

import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Post;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.reader.XMLConverter;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.reader.PostReader;

import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.SQLException;

public class DB2Generator extends SQLGenerator {

	public DB2Generator(){
        BLOB="BLOB";
		BIGINT = "BIGINT";
        CLOB="CLOB";
	}

    protected String doTableForPost(String prefix){
        StringBuffer statement = new StringBuffer();
        statement.append("CREATE TABLE ").append(prefix).append(TABLE_POST);
        statement.append("(");
        statement.append(POST_ID+" CHAR(32) NOT NULL,");
        statement.append(TITLE+" VARCHAR(255), ");
        statement.append(DATE+" "+BIGINT+", ");
        statement.append(IDCREATOR+" VARCHAR(255), ");
        statement.append(VISIBILITY+" INT, ");
        statement.append(SITE_ID+" VARCHAR(255), ");
        statement.append(XMLCOLUMN+" "+CLOB+", ");
        statement.append("CONSTRAINT post_pk PRIMARY KEY ("+POST_ID+")");
        statement.append(")");
        return statement.toString();
    }
    protected String doTableForImages(String prefix){
    	StringBuffer statement = new StringBuffer();
    	statement.append("CREATE TABLE ").append(prefix).append(TABLE_IMAGE);
    	statement.append("(");
    	statement.append(IMAGE_ID+" CHAR(32) NOT NULL,");
        statement.append(POST_ID+" CHAR(32),");
    	statement.append(IMAGE_CONTENT+" "+BLOB+", ");
    	statement.append(THUMBNAIL_IMAGE+" "+BLOB+", ");
    	statement.append(WEBSIZE_IMAGE+" "+BLOB+", ");
        statement.append("CONSTRAINT image_pk PRIMARY KEY ("+IMAGE_ID+")");
        statement.append(")");
        return statement.toString();
    }
    protected String doTableForFiles(String prefix){
    	StringBuffer statement = new StringBuffer();
    	statement.append("CREATE TABLE ").append(prefix).append(TABLE_FILE);
    	statement.append("(");
    	statement.append(FILE_ID+" CHAR(32) NOT NULL,");
        statement.append(POST_ID+" CHAR(32),");
    	statement.append(FILE_CONTENT+" "+BLOB+", ");
        statement.append("CONSTRAINT file_pk PRIMARY KEY ("+FILE_ID+")");
        statement.append(")");
        return statement.toString();
    }

   protected PreparedStatement doInsertStatementForPost(Post post, String prefix, String siteId, Connection connection)
    {
       /*
           Changed this from a normal statement to a PreparedStatement in
           response to SAK-13376. Oracle will not allow a string literal of
           length exceeding 4000 characters unless it is bound - AF.
        */

       XMLConverter xmlConverter = new XMLConverter();
        PostReader reader = new PostReader(xmlConverter);
        reader.parsePost(post);
        String postAsXML = xmlConverter.getXML();

        StringBuilder statement = new StringBuilder();
        statement.append("INSERT INTO ").append(prefix).append(TABLE_POST).append(" (");
        statement.append(POST_ID+",");
        statement.append(TITLE+",");
        statement.append(DATE+",");
        statement.append(IDCREATOR+",");
        statement.append(VISIBILITY+",");
        statement.append(SITE_ID+",");
        statement.append(XMLCOLUMN);
        statement.append(") VALUES (");
        statement.append("'").append(post.getOID()).append("',");
        String title = post.getTitle().replaceAll("'",APOSTROFE); //we can't have any ' because hypersonic complains. so we can't reeplace ' for ////', what it is valid in mysql
        statement.append("'").append(title).append("',");
        statement.append(post.getDate()).append(",");
        String creator = post.getCreator().getId().replaceAll("'",APOSTROFE);
        statement.append("'").append(creator).append("',");
        statement.append(post.getState().getVisibility()) .append(",");
        statement.append("'").append(siteId).append("',?)");

        String sql = statement.toString();

        String xml = postAsXML.replaceAll("'",APOSTROFE);

        try
        {
           PreparedStatement ps = connection.prepareStatement(sql);

           ps.setString(1,xml);
           return ps;
        }
        catch(SQLException sqle)
        {
           sqle.printStackTrace();
           return null;
        }
    }


    protected String doInsertStatementForPost(Post post, String prefix, String siteId){

    	XMLConverter xmlConverter = new XMLConverter();
        PostReader reader = new PostReader(xmlConverter);
        reader.parsePost(post);
        String postAsXML = xmlConverter.getXML();

        StringBuffer statement = new StringBuffer();
        statement.append("INSERT INTO ").append(prefix).append(TABLE_POST).append(" (");
        statement.append(POST_ID+",");
        statement.append(TITLE+",");
        statement.append(DATE+",");
        statement.append(IDCREATOR+",");
        statement.append(VISIBILITY+",");
        statement.append(SITE_ID+",");
        statement.append(XMLCOLUMN);
        statement.append(") VALUES (");
        statement.append("'").append(post.getOID()).append("',");
        String title = post.getTitle().replaceAll("'",APOSTROFE); //we can't have any ' because hypersonic complains. so we can't reeplace ' for ////', what it is valid in mysql
        statement.append("'").append(title).append(",");
        statement.append(post.getDate()).append(",");
        String creator = post.getCreator().getId().replaceAll("'",APOSTROFE);
        statement.append(creator).append("',");
        statement.append(post.getState().getVisibility()) .append(",");
        statement.append("'").append(siteId).append("',");
        String xml = postAsXML.replaceAll("'",APOSTROFE);
        statement.append("'").append(xml).append("\'");
        statement.append(")");
        return statement.toString();
    }
}
