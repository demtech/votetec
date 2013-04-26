package model;

/* Describes the requirements for a physical trail, where the 
 * voter's ballot is stored. A physical trail can be a 
 * CD-ROM, USB key, smartcard, paper receipt etc. */
public interface PhysicalTrail {
    public String folder = "|CARSTEN";
	public boolean isPresent();
	public void setVote(Group e);
	public void setCompleteVoteList(Group e);
}
