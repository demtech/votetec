package model;

import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Stack;
import java.io.IOException;

public class PhysicalTrailPdf417 implements PhysicalTrail {

	public String ballotfolder;
	public final File file;
	public final File terminator;
	public String folder;
	private int voterCounter; // Counts the number of voters using this machine
	private int ballotCounter; // Counts the number of ballots cast (choices selected) on this machine
	private String s;
	private String evidence;

	public PhysicalTrailPdf417(String foldername, String filename) {
		folder = foldername;
	        ballotfolder = "/Users/carsten/Krogerup-Ballotbox/";
		file = new File(foldername + filename);
		terminator = new File (foldername + "Stop.txt");
		voterCounter = 0;
		ballotCounter = 0;
	}
	
	public boolean isPresent() {
		if (terminator.exists()) { 
	          terminator.delete();
		  System.exit(0);
		  return false;
		}
		else {
		  if (file.exists()) {
		    return true;
	          }
	          else { return false; }
 		}
	}

	/* Must be called after setVote() */
	public void setCompleteVoteList(Group e) {
		// TODO Auto-generated method stub

	}

	public void setVote(Group e) {
        try {
			voterCounter ++;
			s = "";
		 	evidence = "";
			castBallots(e);

			Pdf417lib pd = new Pdf417lib();
            pd.setText(s);
            pd.setOptions(Pdf417lib.PDF417_INVERT_BITMAP);
            pd.paintCode();
            PrintWriter pr = new PrintWriter(new BufferedWriter(new FileWriter(ballotfolder+"ballot"+voterCounter+".ps")));
            int cols = (pd.getBitColumns() - 1) / 8 + 1;
	    pr.println ("%!PS-Adobe-2.0");
            pr.println("/Times findfont\n12 scalefont setfont\n100 80 moveto\n(" + evidence + ")show");
            pr.println("stroke\n100 100 translate\n" + pd.getBitColumns()/2.0 + " " + pd.getCodeRows() * 3/2.0 + " scale");
            pr.print(pd.getBitColumns() + " " + pd.getCodeRows() + " 1 [" + pd.getBitColumns() + " 0 0 " + (-pd.getCodeRows())
                + " 0 " + pd.getCodeRows() + "]{<");
            byte out[] = pd.getOutBits();
            for (int k = 0; k < out.length; ++k) {
                if ((k % cols) == 0)
                    pr.println();
                pr.print(Integer.toHexString((out[k] & 0xff) | 0x100).substring(1).toUpperCase());
            }
            pr.println("\n>}image\nshowpage");
            pr.close();
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }

Runtime run = Runtime.getRuntime();
      try {
         Process pp=run.exec("lpr "+ ballotfolder+"ballot"+voterCounter+".ps");
         BufferedReader in =new BufferedReader(new InputStreamReader(pp.getErrorStream()));
         String line;
         while ((line = in.readLine()) != null) {
            System.out.println(line);
         }
         int exitVal = pp.waitFor();
         System.out.println("Process exitValue: " + exitVal);
	 file.delete ();
      } catch (Exception error) {
         error.printStackTrace();
         System.out.println(error.getMessage());
      }



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
					evidence = evidence + c.getValue () + " ";
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
