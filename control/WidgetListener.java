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

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.*;

import control.Controller.StateException;
import control.Controller.state;

/* Collects alle actions from the GUI. */
public class WidgetListener implements ActionListener{
	
	Controller controller;
	Choice choice;
	Group group;
	
	public WidgetListener(Controller controller, Choice choice) {
		this.controller = controller;
		this.choice = choice;
	}
	
	public WidgetListener(Controller controller, Group group) {
		this.controller = controller;
		this.group = group;
	}
	
	public WidgetListener(Controller controller) {
		this.controller = controller;
	}
	
	public void actionPerformed(ActionEvent ae) {
		//Button pushed
		if(ae.getSource().getClass().getName().equalsIgnoreCase("javax.swing.JButton")) {
			//System.out.println("You just pushed the button: "+ae.getActionCommand());
			String id = ae.getActionCommand();
			if(controller.getMap().containsKey(id)) {
				//System.out.println("Known action: "+id);
				controller.getGui().wipe();
				controller.setGlobalStatusPanel();
				
				if(controller.getMap().get(ae.getActionCommand()).getClass().getName().equalsIgnoreCase("model.Group"))
					controller.presentChoices((Group)controller.getMap().get(id));
				controller.getGui().update();
			}
		}
		
		//Item changed in ComboBox
		if (ae.getSource().getClass().getName().equalsIgnoreCase("Javax.swing.JComboBox")) {
			JComboBox cb = (JComboBox)ae.getSource();
			int voteNr = (Integer) cb.getSelectedItem();
			if(controller.validate(choice, voteNr)) {
		        System.out.println("You just put " + voteNr + " votes on " + choice.getValue());
		        int dif = voteNr - choice.getVotes();
		        choice.setVotes(voteNr);
		        //controller.usedVotes += dif;
		        controller.getCurrentGroup().setUsedVotes(controller.getCurrentGroup().getUsedVotes() + dif);
		        controller.getGui().wipe();
		        controller.presentChoices(choice.getParent());
		        controller.getGui().update();
			} else {
				cb.setSelectedItem(choice.getVotes());
				//System.out.println("group: "+choice.getParent().getValue()+"  votesLeft: "+choice.getParent().getVotesLeft());
				if (choice.getParent().getVotesLeft()>0)
					//System.out.println(">0");
					JOptionPane.showMessageDialog(null,"You only have "+votesLeft(choice, voteNr)+" votes left", "Information", JOptionPane.INFORMATION_MESSAGE);
				else
					JOptionPane.showMessageDialog(null,"You have 0 votes left", "Information", JOptionPane.INFORMATION_MESSAGE);
			}
		}
		
		//Checkbox selected or unselected
		if (ae.getSource().getClass().getName().equalsIgnoreCase("Javax.swing.JCheckBox")) {
			
			JCheckBox jcb = (JCheckBox) ae.getSource();
			if (jcb.isSelected() && controller.validate(choice, 1)) {
				System.out.println("Du har netop givet "+ae.getActionCommand() + " 1 stemme!");
				choice.setVotes(1);
				controller.getCurrentGroup().setUsedVotes(controller.getCurrentGroup().getUsedVotes() + 1);
				controller.setGlobalStatusPanel();
			} else if(!jcb.isSelected() && controller.validate(choice,0)) {
				System.out.println("Du har netop givet "+ae.getActionCommand() + "0 stemmer!");
				choice.setVotes(0);
				controller.getCurrentGroup().setUsedVotes(controller.getCurrentGroup().getUsedVotes() - 1);
			} else {
				jcb.setSelected(false);
				//				JOptionPane.showMessageDialog(null,"You have 0 votes left in this gruop", "Information", JOptionPane.INFORMATION_MESSAGE);
			}
			controller.getGui().wipe();
			controller.presentChoices(choice.getParent());
			controller.getGui().update();
		} 
		
		// One of the buttons 'vote', 'confirm' or 'regret' is pushed
		if(ae.getActionCommand().equalsIgnoreCase("vote")) {
			try { controller.switchState(state.CONFIRM); }
			catch (StateException se) { System.out.println(se); }
		} else if(ae.getActionCommand().equalsIgnoreCase("confirm")) {
			try {controller.switchState(state.END); }
			catch (StateException se) { System.out.println(se); } 
		} else if(ae.getActionCommand().equalsIgnoreCase("regret")) {
			try { controller.switchState(state.CHOICE); }
			catch (StateException se) { System.out.println(se); }
		}
	}

	private int votesLeft(Choice c, int vote) {
		Group electionGroup = c.getParent();
		int subtractVote = vote - c.getVotes();
		int votesLeft = 0;
		//Find out how many votes is left in the parent groups
		while(electionGroup.getParent()!=null) {
			if (electionGroup.getVotesLeft() < subtractVote)
				votesLeft = electionGroup.getVotesLeft();
			electionGroup = electionGroup.getParent();
		}
		if(votesLeft<0)
			return 0;
		else
			return votesLeft;
	}
}
