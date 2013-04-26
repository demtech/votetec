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

/* Super class for Choice and Group. Collects the information 
 * provided by the XML file. */
public abstract class Election {
	private final String id, value;
	private final int min, max;
	private final Group parent; // Pointer for both Choice and Group to their parent
	
	public Election (String id, String value, int min, int max, Group parent) {
		this.id = id;
		this.value = value;
		this.min = min;
		this.max = max;
		this.parent = parent;
	}

	public String getId() { return id; }
	public String getValue() { return value; }
	public int getMin() { return min; }
	public int getMax() { return max; }
	public Group getParent() { return parent; }

	public String toString() {
		return("\nid: " + id + "\nvalue: "+ value +
				"\nmin: " + min + "\nmax: " + max);
	}
}
