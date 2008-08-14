package uk.ac.lancs.e_science.sakaiproject.api.blogger;

public class Member {
	String id;
	String displayId;
	
	public void setUserId(String id){
		this.id = id;
	}
	
	public String getUserId(){
		return id;
	}
	public void setUserDisplayId(String displayId){
		this.displayId = displayId;
	}
	public String getUserDisplayId(){
		return displayId;
	}
	
}