package model;

/* Models the equivalent XML entity, describing a candidate 
 * or answers to a question. Uses it's super class and only 
 * adds a counter for the number of votes assigned to this 
 * canddate by the current voter. */
public class Choice extends Election {

	private int votes = 0;
	
	public Choice(String id, String value, int min, int max, Group parent) {
		super(id, value, min, max, parent);
	}
	
	public void setVotes(int votes) { this.votes = votes; }
	public int getVotes() { return votes; }
}
