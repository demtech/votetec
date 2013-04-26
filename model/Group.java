/*  VoteTec
 *  Copyright (C) 2009 Eric Cederstrand, Carsten Schuermann
 *  and Kenneth Sjøholm
 *
 *  This file is part of VoteTec.
 *
 *  VoteTec is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  VoteTec is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with VoteTec.  If not, see <http://www.gnu.org/licenses/>.
 */

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
