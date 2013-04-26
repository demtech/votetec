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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;
import java.util.HashSet;

/* Monitors the presence of a physical trail, and generates 
 * actions based on change of presence. Implemented as a 
 * thread running in the background, sleep()ing most of the
 * time. */
public class PhysicalTrailMonitor extends Thread {

	private final PhysicalTrail trail;
	private boolean currentState, startState;
	private Set<ActionListener> actionListeners = new HashSet<ActionListener>();

	public PhysicalTrailMonitor(PhysicalTrail trail) {
		this.trail = trail;
		this.currentState = trail.isPresent();
		this.startState = trail.isPresent();
	}

	/* Run forever, checking for presence once every second. */
	public void run() {
		while(true) {
			Boolean newState = trail.isPresent();
			if(stateChanged(newState) || startState) {
				startState = false;
				currentState = newState;
				fireAction(new ActionEvent(this, 0, newState.toString()));
			}
			try { Thread.sleep(1000); }
			catch(InterruptedException e) { /* Do nothing*/ }
		}
	}
	
	/* Returns true if newState is different from currentState. */
	private boolean stateChanged(boolean newState) {
		return newState ^ currentState;
	}
	
	public void addActionListener(ActionListener listener) {
		actionListeners.add(listener);
	}

	public void removeActionListener(ActionListener listener) {
		actionListeners.remove(listener);
	}

	private void fireAction(ActionEvent actionEvent) {
		for (ActionListener l: actionListeners)
			l.actionPerformed(actionEvent);
	}
}
