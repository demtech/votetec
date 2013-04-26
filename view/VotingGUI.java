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

package view;

import java.awt.ScrollPane;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.BoxLayout;

/* Dumb GUI implementation. All logic lies in the Controller. */
public class VotingGUI extends JFrame {
	private JPanel pane, globalStatusPanel, localStatusPanel, contentPanel;
	private ScrollPane contentScrollPane;
	
	public VotingGUI(String frametitle){

		/* Create panels, set sizes, layouts etc.*/
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		setPreferredSize(new Dimension(1920,1200));
		setBackground(new Color(0,200,200));
		setUndecorated(true);
				
		pane = new JPanel();

		contentScrollPane = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
		contentScrollPane.setPreferredSize(new Dimension(1920,1000));
		
		globalStatusPanel = new JPanel();
		globalStatusPanel.setBackground(new Color(0,200,200));
		globalStatusPanel.setPreferredSize(new Dimension(1920,90));

		localStatusPanel = new JPanel();
		localStatusPanel.setBackground(new Color(0,200,200));
		localStatusPanel.setPreferredSize(new Dimension(1920,190));

		contentPanel = new JPanel();
		contentPanel.setBackground(new Color(0,150,150));

		BoxLayout l1 = new BoxLayout(this, BoxLayout.Y_AXIS);
		BoxLayout l2 = new BoxLayout(pane, BoxLayout.Y_AXIS);
		FlowLayout f1 = new FlowLayout(FlowLayout.LEFT);
		f1.setHgap(20);

		GridBagLayout gridbag = new GridBagLayout();
		
		setLayout(l1);
		pane.setLayout(l2);
		globalStatusPanel.setLayout(gridbag);
		localStatusPanel.setLayout(f1);
		contentPanel.setLayout(gridbag);
		contentPanel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
		
		contentScrollPane.add(contentPanel);

		pane.add(globalStatusPanel);
		pane.add(contentScrollPane);
		pane.add(localStatusPanel);
		setContentPane(pane);
		pack();
		setVisible(true);
	}
	
	public void update() {
		paintComponents(getGraphics());
	}
	
	public void wipe() {
		/* Remove alle GUI content. */
		globalStatusPanel.removeAll();
		contentPanel.removeAll();
		localStatusPanel.removeAll();
		
		globalStatusPanel.repaint();
		contentPanel.repaint();
		localStatusPanel.repaint();
	}
	
	public JPanel getGlobalStatusPanel() { return globalStatusPanel; }	
	public JPanel getContentPanel() { return contentPanel; }
	public JPanel getLocalStatusPanel() { return localStatusPanel; }
}
