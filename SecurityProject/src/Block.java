import java.util.ArrayList;

public class Block {
	public static int counter = 0;
	public int blockID;
	public String hashpointerBlock;
	public String hashpointerToPreviousBlock;
	public ArrayList<Transaction> transactions = new ArrayList<Transaction>();
	public String ScroogeSignIfLastBlock = "";
	public Block() {
		blockID = counter;
		counter++;
	}

}
