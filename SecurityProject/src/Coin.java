
public class Coin {
	public static int counter = 0;
	public int coinId = 0;
	public User owner;
	public String ScroogeSign = "";
	public Coin() {
		coinId = counter;
		counter++;
	}
}
