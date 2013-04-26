package model;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Stack;

/* Collects all ballots cast using this program. */
public class BallotBox {
	
	private Set<Ballot> ballots;
	HashMap<String, Integer> stats; // Convenience collection for toString()
	PrintWriter recordWriter;
	int voteCounter;
	
	public BallotBox() {
		this.ballots = new HashSet<Ballot>();
		this.stats  = new HashMap<String, Integer>();
	}
	
	private void addBallot(Ballot ballot) {
		// Add map entry if needed
		if(!stats.containsKey(ballot.getId()))
			stats.put(ballot.getId(), 0);
		stats.put(ballot.getId(), stats.get(ballot.getId()) + ballot.getVotes());
		ballots.add(ballot);
	}
	
	private Stack<Group> groupStack = new Stack<Group>(); // Helper variable for castBallots()

	/* Recursively add ballots from the tree. */
	public void castBallots(Group e) {
		for(Choice c: e.getChoices()) {
			if(c.getVotes() > 0) {
				Ballot ballot = new Ballot(c);
				addBallot(ballot);
			}
		}
		for(Group g: e.getGroups()) {
			groupStack.push(g);
			castBallots(g);
			groupStack.pop();
		}
	}

	/* Write current ballotbox content proof to twelf file */
	public void writeTwelfRecord(Group e) {
		voteCounter = 0;
		try {
			recordWriter = new PrintWriter(new BufferedWriter(new FileWriter("record.quy")));
			recordWriter.print("result : election\n\t");
			recordHelper(e);
			recordWriter.print("\n\t= e"+voteCounter+".\n");
			recordWriter.close();
		} catch (IOException ioe) { /* Do nothing */ }
	}

	/* Recursive method to build twelf string */
	private void recordHelper(Group e) {
		recordWriter.print(" (#"+e.getId());
		for (Choice c: e.getChoices()) {
			int votes = 0;
			if(stats.containsKey(c.getId()))
				votes += stats.get(c.getId());

			voteCounter += votes;

			if(votes > 0)
				for(int i=0; i < votes; i++)
					recordWriter.print(" (!");
			recordWriter.print(" 0");
			if(votes > 0)
				for(int i=0; i < votes; i++)
					recordWriter.print(")");
		}
		for (Group g: e.getGroups())
			recordHelper(g);
		recordWriter.print(")");
	}

	public String toString() {
		String str = "BallotBox contents:\n";
		for(Ballot ballot: ballots)
			 str = str.concat(ballot+"\n");
		str = str.concat("Ballot count: "+ballots.size()+"\n");
		str = str.concat("Vote count: ");
		int voteCounter = 0;
		for(Ballot b: ballots)
			voteCounter += b.getVotes();
		str = str.concat(voteCounter+"\n");
		str = str.concat("Vote distribution:\n");
		for(String s: stats.keySet())
			str = str.concat("Choice: "+s+"\tVotes: "+stats.get(s)+"\n");
		return str;
	}
}
