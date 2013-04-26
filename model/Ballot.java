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
