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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Stack;

/* Implements the physical trail as a file stored on e.g. a smart card. */
public class PhysicalTrailFile implements PhysicalTrail {

	private final File file;
	private String folder;
	private int voterCounter; // Counts the number of voters using this machine
	private int ballotCounter; // Counts the number of ballots cast (choices selected) on this machine
	private String s;
		
	public PhysicalTrailFile(String foldername, String filename) {
		folder = foldername;
		file = new File(foldername + filename);
		voterCounter = 0;
		ballotCounter = 0;
	}
	
	public boolean isPresent() {
		return file.exists();
	}	

	/* Must be called after setVote() */
	public void setCompleteVoteList(Group e) {
		try {
			//FileWriter get the argument true which append what's printed to the file
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(folder+"ballots.quy", true)));
			pw.print(s);
			pw.close();
		} catch (IOException ioe) { /* Do nothing */ }
	}

	public void setVote(Group e) {
		try {
			voterCounter ++;
			s = "";
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(folder+"voter"+voterCounter+".quy")));
			castBallots(e);
			pw.print(s);
			pw.close();
		} catch (IOException ioe) { /* Do nothing */ }
	}

	private Stack<Group> groupStack = new Stack<Group>(); // Helper variable for castBallots()

	/* Traverse the tree, creating ballots, dropping them in the ballotbox 
	 * and writing them to the physical trail and audit file. */
	private void castBallots(Group e) {
		for(Choice c: e.getChoices()) {
			if(c.getVotes() > 0) {
				for(int i=0; i<c.getVotes(); i++) {
					ballotCounter++;
					s = s.concat("x"+ballotCounter+" = ");
					for(Group g: groupStack) {
						s = s.concat("group"+g.getId()+" (");
					}
					s = s.concat(c.getValue()+" doinc");
					for(Group g: groupStack) {
						s = s.concat(")");
					}
					s = s.concat(".\n");
					s = s.concat("e"+ballotCounter+" = cast x"+ballotCounter+" e"+(ballotCounter-1)+".\n");
				}
			}
		}
		for(Group g: e.getGroups()) {
			groupStack.push(g);
			castBallots(g);
			groupStack.pop();
		}
	}
}
