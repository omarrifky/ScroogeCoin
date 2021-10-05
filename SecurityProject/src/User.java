import java.security.KeyPair;
import java.util.ArrayList;

public class User {
	public static int counter = 0;
	public int userId;
	public KeyPair keys;
	public ArrayList<Coin> Coins = new ArrayList<Coin>();

	public User() {
		userId = counter;
		counter++;
	}
}
