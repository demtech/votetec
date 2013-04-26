package model;

/* Represents the voter's choice, as recorded when the program
 * leaves confirm state and enters end state. There is one ballot 
 * object for every candidate. */
public class Ballot {

	private final int votes; // Number of votes for this candidate
	private final String id;
	private final String value;
	
	/* Converts a choice object to a ballot object. */
	public Ballot(Choice c) {
		this.id = c.getId();
		this.value = c.getValue();
		this.votes = c.getVotes();
	}

	public String getId() {	return id; }
	public String getValue() { return value; }
	public int getVotes() { return votes; }

	public String toString() {
		return "Ballot ID: "+id+" Value: "+value+" Vote count: "+votes;
	}
}
