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

package control;

import model.*;
import view.*;

//import java.io.BufferedWriter;
import java.io.File;
//import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
//import java.io.PrintWriter;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.GraphicsEnvironment;
import java.awt.GraphicsDevice;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Map;
//import java.util.Stack;

import javax.swing.*;

/* Description of program goes here... */
public class Controller  {

	private Group election, currentGroup; //The currentGroup object contains the current group
	public enum state {START, CHOICE, CONFIRM, END};
	private state currentState;
	private final VotingGUI gui;
	private JPanel globalStatusPanel, contentPanel, localStatusPanel;
	private Font font;
	private JLabel label1, label2, label3;
	private int min = 0, max = 0; 
	private Map<String, Election> map; // Maps a group/choice ID to the corresponding object
	private PhysicalTrail trail;
	
	private final BallotBox ballotBox;

	public Controller(String configFileName) {
		
		//Instantiate the GUI object 
		gui = new VotingGUI("Valg");
		
		GraphicsDevice device;
                device = 
                    GraphicsEnvironment.
                    getLocalGraphicsEnvironment().
                    getDefaultScreenDevice();
                if ( device.isFullScreenSupported() ) {
                    device.setFullScreenWindow(gui);
                }
                else {
                    System.err.println("Full screen not supported");
                }


		font = new Font("Arial", 1, 40);
		label1 = new JLabel("");
		label1.setFont(font);
		label2 = new JLabel("");
		label2.setFont(font);
		label3 = new JLabel("");
		label3.setFont(font);
		
		globalStatusPanel = gui.getGlobalStatusPanel();
		contentPanel = gui.getContentPanel();
		localStatusPanel = gui.getLocalStatusPanel();

		// Move the program to the initial state
		try { switchState(state.START); }
		catch (StateException e) { System.out.println(e); }

		// Generate twelf files of the election setup
		TwelfGenerator twelf = new TwelfGenerator(election);
		twelf.generateTwelfFile();

		ballotBox = new BallotBox();
		
		// By design: remove previous audit files
		String curDir = System.getProperty("user.dir") + "/";
		File file = new File(curDir+"ballots.quy");
		if(file.exists())
			file.delete();
		
		// Read config file and extract filename of physical file
		String physicalTrailType = new String();
		String physicalTrailFile = new String();
		String physicalTrailFolder = new String();
		try {
			BufferedReader in = new BufferedReader(new FileReader(configFileName));
			System.out.println("Config file found: "+configFileName);
			String str;

			while ((str = in.readLine()) != null) {
				str = str.trim();
				if(str.startsWith("PhysicalTrailFile")) {
					// Parse config file syntax and extract filename
					physicalTrailFile = str.split("=")[1].replace("\"", "").trim();
					System.out.println("Physical trail file used: "+physicalTrailFile);
				}
				if(str.startsWith("PhysicalTrailFolder")) {
					// Parse config file syntax and extract filename
					physicalTrailFolder = str.split("=")[1].replace("\"", "").trim();
					System.out.println("Physical trail folder used: "+physicalTrailFolder);
				}
				if(str.startsWith("PhysicalTrailType")) {
					// Parse config file syntax and extract filename
					physicalTrailType = str.split("=")[1].replace("\"", "").trim();
					System.out.println("Physical trail type used: "+physicalTrailType);
				}
			}
			in.close();
		} catch (IOException e) {
			System.out.println("WARNING: config file not found, using default values!");
		} finally {
			if(physicalTrailFile.length() == 0) {
				physicalTrailFile = "PhysTrail.txt";
				System.out.println("No physical trail filename found, using default: "+physicalTrailFile);
			}
			if(physicalTrailFolder.length() == 0) {
				physicalTrailFolder = "/Users/erik/Documents/workspace/evoting/";
				System.out.println("No physical trail folder found, using default: "+physicalTrailFolder);
			}
			if(physicalTrailType.length() == 0) {
				physicalTrailType = "file";
				System.out.println("No physical trail type found, using default: "+physicalTrailType);
			}
		}

		PhysicalTrailMonitor ptm;
		// Create and start physical trail monitor thread
		if(physicalTrailType.equals("pdf417")) {
			trail = new PhysicalTrailPdf417(physicalTrailFolder, physicalTrailFile);
			ptm = new PhysicalTrailMonitor(trail);
		} else {
			trail = new PhysicalTrailFile(physicalTrailFolder, physicalTrailFile);
			ptm = new PhysicalTrailMonitor(trail);
		}
		
		ptm.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(e.getActionCommand().equalsIgnoreCase("TRUE")) {
					try { switchState(state.CHOICE); }
					catch(StateException se) { System.out.println(se); }
				}
				else {
					try { switchState(state.START); }
					catch(StateException se) { System.out.println(se); }
				}
			}
		});
		ptm.start(); 

		
		//fakeAnElection();
	}
	
	protected Map<String, Election> getMap() { return map; }
	protected VotingGUI getGui() { return gui; }
	protected Group getCurrentGroup() { return currentGroup; }
		
/* Controls the switch of states:
	 * START:   No physical trail present, display welcome screen. Only user
	 * 			action possible is insterion of physical trail, resulting in a
	 * 			switch to CHOICE state.
	 * CHOICE:  Physical trail inserted, display choices and let the user 
	 * 			browse the options. Actions possible: removal of physical trail 
	 * 			(returning to START state) or pressing button to continue
	 * 			to CONFIRM state.
	 * COMFIRM: Show confirmation screen, either returning to CONFIRM state 
	 * 			or continuing to END state. Removal of physical trail will switch
	 * 			the program to START state.
	 * END:		Record user choices, prepare for a new user and ask user 
	 * 			to remove physical trail, returning to START state. */
	protected void switchState(state newState) throws StateException {
		switch (newState) {
		case START: 	currentState = state.START;
						startState();
						break;
						
		case CHOICE: 	if (currentState.equals(state.END))
							throw new StateException(currentState, newState);
						currentState = state.CHOICE;
						choiceState();
						break;
						
		case CONFIRM:	if(!currentState.equals(state.CHOICE))
							throw new StateException(currentState, newState);
						currentState = state.CONFIRM;
						confirmState();
						break;
						
		case END:		if(!currentState.equals(state.CONFIRM))
							throw new StateException(currentState, newState);
						currentState = state.END;
						endState();
						/*
						//If we're not looking for a file just take a break when 
						// displaying the goodbye message
						try { Thread.sleep(5000); }
						catch (Exception e) {
						 	//do nothing  
						}
						switchState(state.START);
						*/
						break;
		}
	}

	protected class StateException extends Exception {
		static final long serialVersionUID = 0;
		public StateException(state currentState, state newState) {
			super("You cant switch from "+currentState+" to "+newState);
		}
		public StateException(String error) { super(error); }
	}
	
	private void startState() {
		System.out.println("Start state");

		// Wipe data structures for previous choices
		XMLRecursiveReader reader = new XMLRecursiveReader(new File("Election.xml"));
		election = reader.getRoot();
		currentGroup = election;
		map = reader.getMap();
		
		gui.wipe();

		GridBagConstraints gbcon = new GridBagConstraints();
		//make the borders around each component
		gbcon.insets = new Insets(10,10,10,10);
		gbcon.fill = GridBagConstraints.BOTH;
		gbcon.anchor = GridBagConstraints.CENTER;
		gbcon.gridwidth = GridBagConstraints.REMAINDER;

		label1.setText("Welcome");
		label2.setText("Version 0.3");
		label1.setFont(new Font("Arial", 1, 100));
		contentPanel.add(label1, gbcon);
		contentPanel.add(label2, gbcon);
		
		gui.update();
	}
	
	private void choiceState() {
		System.out.println("Choice state");

		gui.wipe();
		// If one regrets we need to set the layout back to GridBag
		contentPanel.setLayout(new GridBagLayout());
		presentChoices(election);
		gui.update();
	}

	private void confirmState() {
		System.out.println("Confirm state");
		
		gui.wipe();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		contentPanel = confirm(election, gui.getContentPanel());
		
		JButton btn1 = new JButton("Regret");
		btn1.addActionListener(new WidgetListener(this));
		localStatusPanel.add(btn1);
		JButton btn2 = new JButton("Confirm");
		btn2.addActionListener(new WidgetListener(this));
		localStatusPanel.add(btn2);
		gui.update();
	}
	
	/* Present user choices, recursively traversing the tree. */
	private JPanel confirm(Group group, JPanel panel) {
		JLabel newLine = new JLabel("----------------");
		JLabel value = new JLabel("Group: "+group.getValue());
		panel.add(newLine);
		panel.add(value);
		
		for (Choice c : group.getChoices()) {
			JLabel vote = new JLabel(c.getValue()+",  " + c.getVotes() + " votes");
			panel.add(vote);
		}
		
		for (Group g : group.getGroups())
			confirm (g, panel);
		
		return panel;
	}
	
	private void endState() {
		System.out.println("End state");
		
		/* Drop ballots into ballotbox. */
		ballotBox.castBallots(election);

		/* Print ballot to physical media (twelf-style), both on physical 
		 * trail and in audit file. */
		trail.setVote(election);
		trail.setCompleteVoteList(election);

		/* Drop ballotbox content proof to physical trail (twelf-style) */
		ballotBox.writeTwelfRecord(election);
		System.out.println(ballotBox);
		
		gui.wipe();
		
		contentPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbcon = new GridBagConstraints();
		// Make the borders around each component
		gbcon.insets = new Insets(10,10,10,10);
		gbcon.fill = GridBagConstraints.BOTH;
		gbcon.anchor = GridBagConstraints.CENTER;
		gbcon.gridwidth = GridBagConstraints.REMAINDER;
		
		
		label1.setText("Thank you!");
		label2.setText("Please remember");
		label3.setText("your ballot!");
		
		contentPanel.add(label1, gbcon);
		contentPanel.add(label2, gbcon);
		contentPanel.add(label3, gbcon);
		
		gui.update();
	}
		
	/* Keeps track of voting constraints and displays in formation for the user. */
	protected void setGlobalStatusPanel() {
		// Remove all from the Panel
		globalStatusPanel.removeAll();
		
		GridBagConstraints gbcon = new GridBagConstraints();
		// Make the borders around each component
		gbcon.insets = new Insets(0,0,0,10);
		gbcon.gridy = 0;
		gbcon.anchor = GridBagConstraints.WEST;
		gbcon.weightx = 1.0;
		
		int votesLeft = max-currentGroup.getUsedVotes();
		JLabel votesMax = new JLabel("Votes left: "+votesLeft);
		JLabel votesUsed = new JLabel("Votes used: "+currentGroup.getUsedVotes());
		JLabel votesAll = new JLabel();

		JLabel votesToCast = new JLabel();
		if(min==max) {
			votesAll.setText("Total votes in this election:   " + min + " votes");
		} else {
			votesAll.setText("Total votes in this election:   min=" + min + ",   max="+max);
			votesLeft = min-currentGroup.getUsedVotes();
		}
		if (votesLeft>0) 
			votesToCast.setText("Votes left you have to cast: "+votesLeft);
		else
			votesToCast.setText("Votes left you have to cast: 0");
		
		globalStatusPanel.add(votesAll, gbcon);
		gbcon.gridy = 1;
		globalStatusPanel.add(votesMax, gbcon);
		globalStatusPanel.add(votesUsed, gbcon);
		globalStatusPanel.add(votesToCast, gbcon);
	}
	
	/* Create the GUI elements reflecting the XML. */
	protected void presentChoices(Group group) {
		GridBagConstraints gbcon = new GridBagConstraints();
		gbcon.insets = new Insets(5,5,5,5);
				
		int row = 0, col = 0;
		gbcon.anchor = GridBagConstraints.WEST;
		
		if(group.getParent() != null) {
			gbcon.gridwidth = GridBagConstraints.REMAINDER;
			JLabel parent = new JLabel(group.getValue());	
			parent.setFont(font);
			contentPanel.add(parent, gbcon);
			row++;
			gbcon.gridwidth = GridBagConstraints.RELATIVE;
		}

		for(Choice c: group.getChoices()) {
			if(c.getMax() > 1) {
				// Dropdown
				gbcon.gridx = col;
				JLabel l = new JLabel(c.getValue());
				JComboBox jcb = new JComboBox();
				jcb.setLightWeightPopupEnabled(false);
				for (int i=c.getMin(); i<=c.getMax(); i++){ 
					jcb.addItem(i);
				}
				jcb.setSelectedItem(c.getVotes());
				jcb.addActionListener(new WidgetListener(this, c));
				
				l.setOpaque(false);
				l.setFont(font);
				gbcon.gridwidth = GridBagConstraints.RELATIVE;
				contentPanel.add(jcb, gbcon);
				gbcon.gridx = ++col;
				gbcon.gridwidth = GridBagConstraints.REMAINDER;
				contentPanel.add(l, gbcon);
				col = 0;
				row++;
				gbcon.gridy = row;
				
			} else if(c.getMax() == 1) {
				// Checkbox
				JCheckBox jch = new JCheckBox(c.getValue());
				if (c.getVotes()==0) 
					jch.setSelected(false);
				else
					jch.setSelected(true);
				jch.setOpaque(false);
				jch.setFont(font);
				jch.addActionListener(new WidgetListener(this, c));
				gbcon.gridx = col;
				contentPanel.add(jch, gbcon);
				row++;
				gbcon.gridy = row;
			} else {
				// Max votes is 0 or less. Should never happen.
			}
		}
		
		for(Group g: group.getGroups()) {
			// Show a button for each child group.
			// Disable, if all votes are used.

			gbcon.gridx = 0;
			
			JButton btn = new JButton();
			btn.setEnabled(true);
			btn.setOpaque(false);
			btn.setFont(font);
			btn.setText(g.getValue());
			btn.setActionCommand(g.getId());
			btn.addActionListener(new WidgetListener(this, g));
			
			gbcon.gridy = row;
			contentPanel.add(btn, gbcon);
			row++;
			gbcon.gridy = row;
		}

		// Add the 'back' button
		if(group.getParent() != null) {
			JButton btnBack = new JButton();
			btnBack.setFont(font);
			btnBack.setText("Back");
			btnBack.setActionCommand(group.getParent().getId());
			btnBack.addActionListener(new WidgetListener(this, group));
			gbcon.gridy = row;
			gbcon.gridwidth = GridBagConstraints.RELATIVE;
			localStatusPanel.add(btnBack, gbcon);
		}
		
		//Set global values min and max to the values of this election and set currentGroup
		if(group.getParent() != null && group.getParent().getParent() == null) {
			//group.getParent().getValue().equalsIgnoreCase("election")) {
			min = group.getMin();
			max = group.getMax();
			currentGroup = group;
		}
		
		setGlobalStatusPanel();
		
		if(group.getParent() == null) {
			//remove all from the Panel
			globalStatusPanel.removeAll();
			
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridy = 0;
			gbc.anchor = GridBagConstraints.CENTER;
			gbc.weightx = 1.0;
			
			JLabel title = new JLabel("Content of election");
			title.setFont(new Font("Arial", 1, 20));
			globalStatusPanel.add(title, gbcon);
		}

		// Add the 'vote' button
		boolean isComplete = true;
		for(Group g: election.getGroups()) {
			if(g.getUsedVotes() < g.getMin() || g.getUsedVotes() > g.getMax() ) {
				isComplete = false;
			}
		}
		if(isComplete) {
			System.out.println("usedVotes= "+currentGroup.getUsedVotes()+"	max="+max);
			JButton btnConfirm = new JButton();
			btnConfirm.setFont(font);
			btnConfirm.setText("Vote");
			btnConfirm.setActionCommand("vote");
			btnConfirm.addActionListener(new WidgetListener(this));
			gbcon.gridy = row;
			gbcon.gridx++;
			gbcon.gridwidth = GridBagConstraints.RELATIVE;
			localStatusPanel.add(btnConfirm, gbcon);
			gbcon.gridx--;
		}
	}
	//Returns votes left in the current group
    private int validateVotesLeft(Group group) {
        int votes = group.getVotesLeft();
        //while(!group.getValue().equalsIgnoreCase("election")) {
        while(group.getParent()!=null) {
                if (group.getVotesLeft() < votes)
                        votes = group.getVotesLeft();
                group = group.getParent();
        }
        return votes;
    }

		
	/* Check if vote constraints are met for a choice and an intended number of votes. */
	protected boolean validate(Choice c, int vote) {
		Group electionGroup = c.getParent();
		int subtractVote = vote - c.getVotes();
		// Ensure user is allowed by the parent's constraints to cast the vote
		while(electionGroup.getParent()!=null){
			System.out.println("Group : "+electionGroup.getValue()+"   votesLeft:"+electionGroup.getVotesLeft());
			if (electionGroup.getVotesLeft() < subtractVote) {
				System.out.println("false from validate()");
				return false;
			} 
			electionGroup = electionGroup.getParent();
		}
		System.out.println("you have permission");
		// User has permission to make this vote, so we'll update the parent's 'votesLeft'
		electionGroup = c.getParent();	
		while (electionGroup.getParent().getParent()!=null) {
			electionGroup.setVotesLeft(electionGroup.getVotesLeft()-subtractVote);
			electionGroup = electionGroup.getParent();
		}
		electionGroup.setVotesLeft(electionGroup.getVotesLeft()-subtractVote);
		
		return true;
	}
	
	/* For testing purposes: generate 1000 ballots. */
	void fakeAnElection() {
		for(int i = 0; i < 100; i++) {
			election.getGroups().get(0).getChoices().get(0).setVotes(5);
			election.getGroups().get(0).getChoices().get(1).setVotes(5);
			ballotBox.castBallots(election);
			trail.setVote(election);
			trail.setCompleteVoteList(election);
			ballotBox.writeTwelfRecord(election);			
		}			
	}
}
