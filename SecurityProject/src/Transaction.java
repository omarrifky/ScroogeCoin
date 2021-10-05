import java.util.ArrayList;

public class Transaction {
	public static int counter = 0;
	public boolean createcoin = false;
	public  int transactioinID;
	public String hashpointer;
	public String PreviousHashpointer;
	String UserSign = "";
	public User from;
	public User to;
	public ArrayList<Coin> coinsInvolved = new ArrayList<Coin>();
	
	String ScroogeSign = "";
	public Coin coin; // if transaction is creating coin
	public Transaction(User f, User t, ArrayList<Coin> c) throws Exception {
		from = f;
		to = t;
		coinsInvolved = c;
		transactioinID = counter;
		counter++;
		UserSign = Crypto.sign(this.toString(), f.keys.getPrivate());
	}
	public Transaction(Coin c,User t) {
		to = t;
		coin = c;
		createcoin = true;
		transactioinID = counter;
		counter++;
	}
}
