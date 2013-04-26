package model;

import java.util.List;
import java.util.LinkedList;

/* Models the equivalent XML entity, describing a group of 
 * candidates or a question. */
public class Group extends Election {
	private List<Choice> choices;
	private List<Group> groups;
	private int votesLeft, usedVotes = 0; //usedvotes only used for electionGroups

	public Group(String id, String value, int min, int max, Group parent) {
		super (id, value, min, max, parent);
		this.choices = new LinkedList<Choice>();
		this.groups = new LinkedList<Group>();
	}
		
	public void addChoice (Choice choice) { choices.add(choice); }
	public List<Choice> getChoices() { return choices; }
	public void addGroup(Group group) { groups.add(group); }
	public List<Group> getGroups() { return groups; }
	public int getUsedVotes() { return usedVotes; }
	public void setUsedVotes(int nr) { usedVotes = nr; }
	public int getVotesLeft() { return votesLeft; }
	public void setVotesLeft(int i) { votesLeft = i; }
	
	public String toString() {
		String s = "\n\nparent: ";
		s = s.concat(getParent()!=null?getParent().getValue():"null");
		s = s.concat(super.toString() +"\nchoices: ");
		for (Choice c: choices)
			s = s.concat(c.getValue()+", ");
		s = s.concat("\ngroups: ");
				for (Group g: groups)
			s = s.concat(g.getValue()+", ");
		for (Choice c: choices)
			s = s.concat(c.toString());
		s = s.concat("\ngroups: ");
		for (Group g: groups)
			s = s.concat(g.toString());
		return s;
	}
}
