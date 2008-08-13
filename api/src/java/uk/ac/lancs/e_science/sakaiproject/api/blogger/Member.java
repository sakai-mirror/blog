package uk.ac.lancs.e_science.sakaiproject.api.blogger;

public class Member {
	String eid;
	String displayId;
	
	public void setUserEid(String eid){
		this.eid = eid;
	}
	public String getUserEid(){
		return eid;
		
	}
	public void setUserDisplayId(String displayId){
		this.displayId = displayId;
	}
	public String getUserDisplayId(){
		return displayId;
	}
	
}