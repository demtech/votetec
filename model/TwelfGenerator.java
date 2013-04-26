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

import java.io.*;

/* Generates twelf files based on the provided XML. */
public class TwelfGenerator {

	private final Group election;
	private PrintWriter srcwriter;
	private PrintWriter sumwriter;
	
	public TwelfGenerator (Group election) {
		this.election = election;
	}
	
	public void generateTwelfFile() {
		try {
			/* Write the summary file collecting all the twelf source files. */
			srcwriter = new PrintWriter(new BufferedWriter(new FileWriter("sources.cfg")));
			srcwriter.print("common.elf\n");
			generateTwelfFile(election);
			srcwriter.print("summary.elf\n");
			srcwriter.print("common.quy\n");
			srcwriter.print("ballots.quy\n");
			srcwriter.print("record.quy\n");
			srcwriter.close();
		} catch (IOException ioe) { /* Do nothing */ }

		try {
			/* Write the twelf file zeroing all values and summarizing the election. */
			sumwriter = new PrintWriter(new BufferedWriter(new FileWriter("summary.elf")));
			sumwriter.print("election: ");
			sumwriter.print("tally"+election.getId()+" -> ");
			sumwriter.print("type.\n");
			sumwriter.print("init : election");
			sumHelper(election);
			sumwriter.print(".\n");
			sumwriter.print("cast : election A'\n"+
							"\t\t<- election A\n"+
							"\t\t<- select"+election.getId()+" A A'.\n");
			sumwriter.close();
		} catch (IOException ioe) { /* Do nothing */ }
	}

	/* Recursive method constructing the initializing string. */
	private void sumHelper(Group e) {
		sumwriter.print(" (#"+e.getId());
		for (Choice c: e.getChoices())
			sumwriter.print(" 0");
		for (Group g: e.getGroups())
			sumHelper(g);
		sumwriter.print(")");
	}
	
	/* Recursive method constructing the twelf files describing 
	 * groups and choices. Leaves in the tree are written first, 
	 * then the parent node. One file per group. */
	public void generateTwelfFile(Group e) {
		for(Group g: e.getGroups())
			generateTwelfFile(g);

		String id = e.getId();
		try {
			String filename = "election"+id+".elf";
			srcwriter.print(filename+"\n");
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
			
			writer.print("tally"+id+" :  type.\n#"+id+" : ");
			for (Choice c: e.getChoices())
				writer.print("counter -> "); 
			for (Group g: e.getGroups())
				writer.print("tally"+g.getId()+" -> ");
			writer.print("tally"+id+".\n\n");
			writer.print("select"+id+" : tally"+id+" -> tally"+id+" -> type.\n\n");			
			
			for(Choice c: e.getChoices()) {
				writer.print(c.getValue() + ": select"+id+" (#"+id);
				for(Choice c2: e.getChoices())
					writer.print(" "+c2.getId());
				for(Group g: e.getGroups())
					writer.print(" "+g.getId());
				writer.print(") (#"+id);
				for(Choice c2: e.getChoices()) {
					writer.print(" "+c2.getId());
					if(c2.getId()==c.getId())
						writer.print("'");
				}
				for(Group g: e.getGroups())
					writer.print(" "+g.getId());
				writer.print(")\n");
				writer.print("\t\t<- inc "+c.getId()+" "+c.getId()+"'.\n");
			}
			for(Group g: e.getGroups()) {
				writer.print("group"+g.getId() + ": select"+id+" (#"+id);
				for(Choice c: e.getChoices())
					writer.print(" "+c.getId());
				for(Group g2: e.getGroups())
					writer.print(" "+g2.getId());
				writer.print(") (#"+id);
				for(Choice c: e.getChoices())
					writer.print(" "+c.getId());
				for(Group g2: e.getGroups()) {
					writer.print(" "+g2.getId());
					if(g2.getId()==g.getId())
						writer.print("'");
				}
				writer.print(")\n");
				writer.print("\t\t<- select"+g.getId()+" "+g.getId()+" "+g.getId()+"'.\n");
			}
			writer.close();
		} catch (IOException ioe) { /* Do nothing */ }
	}
}
