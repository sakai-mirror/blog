package uk.ac.lancs.e_science.sakai.tools.blogger;

import java.util.Comparator;

import uk.ac.lancs.e_science.sakaiproject.api.blogger.Member;

public class MemberComparator implements Comparator{
    public int compare(Object o, Object o1) {
        Member member1 = (Member) o;
        Member member2 = (Member) o1;
        return member1.getUserDisplayId().compareToIgnoreCase(member2.getUserDisplayId());
     }
}