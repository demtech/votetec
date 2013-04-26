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
