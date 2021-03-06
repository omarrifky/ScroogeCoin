
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.FileWriter;
import java.security.KeyPair;
import java.util.ArrayList;
import javax.swing.JFrame;


public class Scrooge{
	 ArrayList<Block>  blocks = new ArrayList<Block>();
	ArrayList<Transaction> allTransactions = new ArrayList<Transaction>();
	ArrayList<Transaction> pendingTransactions = new ArrayList<Transaction>();
	ArrayList<User> users = new ArrayList<User>();
	public KeyPair keys;
	public boolean stopcode = false;
	public String line = "--------------------------------------------------------"+"\n";
	public String lastHashPointerSigniture = "";
	public int coinscreated=0;
	public Scrooge() throws Exception { // Code Logic 
		CreateSpaceStop();
		keys = Crypto.generateKeyPair();
		createUsers(); // 10 users only based on request from Dr Amr
		while(true){
			randomTransaction();
			
//			if(this.blocks.size()==20) { //Uncomment to test manipulation
//				
//				//blocks.get(0).blockID = 1; // Uncomment to test block manipulation then comment again to test the transaction manipulation
//				blocks.get(0).transactions.get(0).coinsInvolved.add(new Coin()); // Uncomment to test transaction manipulation
//				
//			}
			
			if(stopcode==true|| VerifyBlockChain()==false|| VerifyTransactions()==false) {
				ExitCode();
			}
		}
	}
	public void CreateSpaceStop() {
		JFrame frame = new JFrame("Key Listener");
		 
		  KeyListener listener = new KeyListener() {
		 
		public void keyPressed(KeyEvent event) {
		 
//		    printEventInfo("Key Pressed", event);
			
			if(event.getKeyCode()==KeyEvent.VK_SPACE) {
				stopcode = true;
			}
		 
		}

		@Override
		public void keyTyped(KeyEvent e) {
			if(e.getKeyCode()==KeyEvent.VK_SPACE) {
				stopcode = true;
			} 
			
		}

		@Override
		public void keyReleased(KeyEvent e) {
			// TODO Auto-generated method stub
			
		}
		 };
		 
		  //JTextField textField = new JTextField();
		    frame.setVisible(true);
		    frame.setResizable(false);
		    frame.setSize(600, 600);
		  frame.addKeyListener(listener);
		  
		    frame.setFocusable(true);
		    frame. setFocusTraversalKeysEnabled(false);
		 
		  //contentPane.add(textField, BorderLayout.NORTH);
	
		    
		  frame.pack();
		 
		  frame.setVisible(true);
		    }


	public ArrayList<Coin> removeCoin(ArrayList<Coin> x ,Coin c){
		int index = -1;
	
		for(int i = 0;i<x.size();i++) {
			if(c.coinId==x.get(i).coinId)
				index = i;
		}
		x.remove(index);
		return x;
	}
	public boolean checkcoins() {
	int coinnumb = 0;
	
		for(int i =0;i<users.size();i++) {

			coinnumb = coinnumb + users.get(i).Coins.size();
		}

	if(coinnumb==coinscreated)
		return true;
	else
		return false;
	}
	public int validCoins(User u) {
		int validcoins = 0;
		for (int j = 0; j < u.Coins.size(); j++) {
			if (isDoubleSpent(u.Coins.get(j)) == false) {
				validcoins++;
			}
		}
		return validcoins;
	}
	
	public String computeStringHashBlock(Block b) {
		String blockid = b.blockID+"";
		String prevhash = b.hashpointerToPreviousBlock;
		String Transactions = "";
		for(int i = 0;i<b.transactions.size();i++) {
			Transaction t = b.transactions.get(i);
			
			if(t.createcoin==false)
			Transactions = Transactions+t.from.toString()+t.to.toString()+t.hashpointer+t.PreviousHashpointer;
			else
				Transactions = Transactions+t.to.toString()+t.hashpointer+t.PreviousHashpointer;
		
			for(int j = 0;j<t.coinsInvolved.size();j++) {
				Transactions = Transactions+t.coinsInvolved.get(j).toString();
			}
		}
		return blockid+prevhash+Transactions;
	}
	public String computeStringHashTransaction(Transaction t) {
		String transactionid = t.transactioinID+"";
		String prevhash = t.PreviousHashpointer;
		String from = "";
		if(t.createcoin==false) {
			from = t.from.toString();
		}
		String to = t.toString();
		String coins = "";
		for(int j = 0;j<t.coinsInvolved.size();j++) {
			coins = coins+t.coinsInvolved.get(j).toString();
		}
		return transactionid+prevhash+from+to+coins;
		
	}
	
	public void createCoin(User u, int count) throws Exception {
		for (int i = 0; i < count; i++) {
			Coin c = new Coin();
			c.ScroogeSign = Crypto.sign(c.toString(), this.keys.getPrivate());
			createCoinTrans(u,c);
		}
	}

	public void createCoinTrans(User u , Coin c) throws Exception {
		Transaction tran = new Transaction(c,u);
		tran.ScroogeSign = Crypto.sign(tran.toString(), this.keys.getPrivate());
		   if(allTransactions.size()!=0)
	    tran.PreviousHashpointer = allTransactions.get(allTransactions.size()-1).hashpointer;
		  String hashString = computeStringHashTransaction(tran);
		  String hashvalue = Crypto.computeHash(hashString);
		  tran.hashpointer = hashvalue;
	    allTransactions.add(tran);
		pendingTransactions.add(tran);
		
		printPending();
		Engine.output+=line;
		if(pendingTransactions.size()==10) {
			CreateBlockandConfirmTran();
			pendingTransactions.clear();
		}
		
	}
	

	public boolean isCoinOwner(User u, Coin c) {
		if (u.userId == c.owner.userId)
			return true;
		return false;
	}
	
	public void UserTransfer(User f, User t, int amount) throws Exception {
		int validcoins = 0;
		ArrayList<Coin> coinInTrans = new ArrayList<Coin>();
		for (int i = 0; i < f.Coins.size(); i++) {
			boolean signedByScrooge = Crypto.verify(f.Coins.get(i).toString(), f.Coins.get(i).ScroogeSign, this.keys.getPublic());
			if (isDoubleSpent(f.Coins.get(i)) == false && isCoinOwner(f,f.Coins.get(i))&& signedByScrooge){
				validcoins++;
			}
				
		}
		if (validcoins < amount) {
			System.out.println("No Sufficient Funds");
			return;
		} else {
			int coinsadded = 0;
			
			while(coinsadded!=amount) {
				for (int j = 0; j < f.Coins.size(); j++) {
					if (isDoubleSpent(f.Coins.get(j)) == false) {
						coinInTrans.add(f.Coins.get(j));
						coinsadded++;
						if(coinsadded==amount)
							break;
						
						
					}
				}
				//coinInTrans.add(f.Coins.get(index));
			}
			Transfer(f, t, coinInTrans);
		}
		
	}

	public boolean isDoubleSpent(Coin c) {
		boolean coinSpent = false;
		for (int i = 0; i < pendingTransactions.size(); i++) {
			for (int j = 0; j < pendingTransactions.get(i).coinsInvolved.size(); j++) {
				if (c.coinId == pendingTransactions.get(i).coinsInvolved.get(j).coinId) {
					coinSpent = true;
				}
			}
		}
		return coinSpent;
	}

	public void Transfer(User f, User t, ArrayList<Coin> c) throws Exception {
		boolean verifyTransactions = VerifyTransactions();
		if(verifyTransactions==true) {
		Transaction tran = new Transaction(f, t, c);
		tran.UserSign =  Crypto.sign(tran.toString(), f.keys.getPrivate());
	    if(allTransactions.size()>=1) {
	    tran.PreviousHashpointer = allTransactions.get(allTransactions.size()-1).hashpointer;
	    }
	    String hashString = computeStringHashTransaction(tran);
		String hashvalue = Crypto.computeHash(hashString);
	    tran.hashpointer = hashvalue;
	    allTransactions.add(tran);
		pendingTransactions.add(tran);
		printPending();
		if (pendingTransactions.size() == 10) {
			CreateBlockandConfirmTran();
		}
		}
		
	}

	public void CreateBlockandConfirmTran() throws Exception {
	 boolean blocksverfied = VerifyBlockChain();
	 if(blocksverfied==true) {
		Block b = new Block();
		boolean valid =false;
		for (int i = 0; i < pendingTransactions.size(); i++) { // Add to block and perform transactions
			if(pendingTransactions.get(i).createcoin==true) {
				
				 valid = Crypto.verify(pendingTransactions.get(i).toString(), pendingTransactions.get(i).ScroogeSign, this.keys.getPublic());
			}
			else {
			 valid = Crypto.verify(pendingTransactions.get(i).toString(), pendingTransactions.get(i).UserSign, pendingTransactions.get(i).from.keys.getPublic());
			
			}
			if(valid) {
			b.transactions.add(pendingTransactions.get(i));
			if(pendingTransactions.get(i).createcoin==false) {
			FinalTransfer(pendingTransactions.get(i).from, pendingTransactions.get(i).to,pendingTransactions.get(i).coinsInvolved);
			}
			else {
				pendingTransactions.get(i).coin.owner = pendingTransactions.get(i).to;
				pendingTransactions.get(i).to.Coins.add(pendingTransactions.get(i).coin);
				coinscreated++;
			}
			}
			
		}
	
		if(blocks.size()>=1){
			
			b.hashpointerToPreviousBlock = blocks.get(blocks.size() - 1).hashpointerBlock;
		}
		String hashString = computeStringHashBlock(b);
		String hashValue = Crypto.computeHash(hashString);
		b.hashpointerBlock = hashValue;
		lastHashPointerSigniture = Crypto.sign(b.hashpointerBlock, this.keys.getPrivate()) ;
		blocks.add(b);
		printBlocks();
		Engine.output+=line;
		pendingTransactions.clear();
	 }
	
	}
	public void finalBlockSign() throws Exception{
		blocks.get(blocks.size()-1).ScroogeSignIfLastBlock = blocks.get(blocks.size()-1).hashpointerBlock = Crypto.sign(blocks.get(blocks.size()-1).hashpointerBlock, this.keys.getPrivate());
	
	Engine.output+=line;
	}
	public void FinalTransfer(User f, User t, ArrayList<Coin> c) {
	
		for (int i = 0; i < c.size(); i++) {
			Coin currentCoin = c.get(i);
//			for (int j = 0; j < f.Coins.size(); j++) {
//				if (c.get(i).coinId == f.Coins.get(j).coinId) {
//					t.Coins.add(f.Coins.get(j));
//					f.Coins.remove(f.Coins.get(j));
//					t.Coins.get(t.Coins.size() - 1).owner = t;
//				}
//			}
			
			t.Coins.add(currentCoin);
			f.Coins = removeCoin(f.Coins, currentCoin);
			//f.Coins.remove(currentCoin);
			currentCoin.owner = t;
			
		}
		

	}

	public boolean VerifyBlockChain() {
		
		for(int i = 1;i<blocks.size();i++){
			String hashString = computeStringHashBlock(blocks.get(i-1));
			String hashValue = Crypto.computeHash(hashString);
			if(!blocks.get(i).hashpointerToPreviousBlock.equals(hashValue)){
				
				return false;
			}
		}
		return true;
	}
	
	public boolean VerifyTransactions() {
	
		for(int i = 1;i<allTransactions.size();i++){
			String hashString = computeStringHashTransaction(allTransactions.get(i-1));
			String hashValue = Crypto.computeHash(hashString);
			if(!allTransactions.get(i).PreviousHashpointer.equals(hashValue)){
				return false;
			}
		}
for(int i = 0;i<blocks.size();i++) {
	for(int j = 1;j<blocks.get(i).transactions.size();j++) {
		String hashString = computeStringHashTransaction(blocks.get(i).transactions.get(j-1));
		String hashValue = Crypto.computeHash(hashString);

		if(!blocks.get(i).transactions.get(j).PreviousHashpointer.equals(hashValue)&& i!=0 && j!=0){
			
			return false;
		}
	}
}
		return true;
	}
	
	
	public void printBlocks(){
		String outputBlocks = "";
		outputBlocks+="block id "+blocks.get(0).blockID+" Block Hash Value "+blocks.get(0).hashpointerBlock+ " Hash Value of previous block  No previous this is first \n";
		for(int i = 0;i<blocks.get(0).transactions.size();i++) {
			if(blocks.get(0).transactions.get(i).createcoin==true) {
				if(i==0)
					outputBlocks+="Transaction ID: "+blocks.get(0).transactions.get(i).transactioinID+" Transaction Hash Value "+blocks.get(0).transactions.get(i).hashpointer+" Hash Value of previous transaction No pevious transaction this is the first "+" from Scrooge "+" to "+blocks.get(0).transactions.get(i).to.userId+" Id of created Coin "+blocks.get(0).transactions.get(i).coin.coinId+"\n";
					else
					
				outputBlocks+="Transaction ID: "+blocks.get(0).transactions.get(i).transactioinID+" Transaction Hash Value "+blocks.get(0).transactions.get(i).hashpointer+" Hash Value of previous transaction "+ blocks.get(0).transactions.get(i).PreviousHashpointer+" from Scrooge "+" to "+blocks.get(0).transactions.get(i).to.userId+" Id of created Coin "+blocks.get(0).transactions.get(i).coin.coinId+"\n";
					
			}
			else {
				if(i==0)
				outputBlocks+= "Transaction ID: "+blocks.get(0).transactions.get(i).transactioinID+" Transaction Hash Value "+blocks.get(0).transactions.get(i).hashpointer+" Hash Value of previous transaction No previous transaction this is the first " +"from "+blocks.get(0).transactions.get(i).from.userId+" to "+blocks.get(0).transactions.get(i).to.userId+" Amount of Coins "+blocks.get(0).transactions.get(i).coinsInvolved.size()+"\n";

				else
				outputBlocks+= "Transaction ID: "+blocks.get(0).transactions.get(i).transactioinID+" Transaction Hash Value "+blocks.get(0).transactions.get(i).hashpointer+" Hash Value of previous transaction "+blocks.get(0).transactions.get(i).PreviousHashpointer+" from "+blocks.get(0).transactions.get(i).from.userId+" to "+blocks.get(0).transactions.get(i).to.userId+" Amount of Coins "+blocks.get(0).transactions.get(i).coinsInvolved.size()+"\n";

			}
		}
		outputBlocks+=line;
		for(int i = 1;i<blocks.size();i++){
		outputBlocks+=line;
			
			outputBlocks+="block id "+blocks.get(i).blockID+" Block Hash Value "+blocks.get(0).hashpointerBlock+" Hash Value of previous block "+blocks.get(i).hashpointerToPreviousBlock+"\n";
			
			for(int j = 0;j<blocks.get(i).transactions.size();j++) {
				if(blocks.get(i).transactions.get(j).createcoin==true)
			outputBlocks+="Transaction ID: "+blocks.get(i).transactions.get(j).transactioinID+" Transaction Hash Value "+blocks.get(i).transactions.get(j).hashpointer+" Hash Value of previous transaction "+blocks.get(i).transactions.get(j).PreviousHashpointer+" from Scrooge "+" to User "+blocks.get(i).transactions.get(j).to.userId+" Id of Coin created "+blocks.get(i).transactions.get(j).coin.coinId+"\n";
				else
			outputBlocks+= "Transaction ID: "+blocks.get(i).transactions.get(j).transactioinID+" Transaction Hash Value "+blocks.get(i).transactions.get(j).hashpointer+" Hash Value of previous transaction "+blocks.get(i).transactions.get(j).PreviousHashpointer+" from User "+blocks.get(i).transactions.get(j).from.userId+" to User "+blocks.get(i).transactions.get(j).to.userId+" Amount of Coins "+blocks.get(i).transactions.get(j).coinsInvolved.size()+"\n";
	
			}
		
			
		}
		Engine.output+= outputBlocks;
		System.out.print(outputBlocks);
		Engine.output+=line;
	}
	
	
	public void printPending(){
		String pendingOutput = "";
		pendingOutput+=line;
		pendingOutput+="Pending Transactions - Block Under Construction"+"\n";
		for(int i = 0;i<pendingTransactions.size();i++){
			if(pendingTransactions.get(i).createcoin==false)
			pendingOutput+= "Transaction ID: "+pendingTransactions.get(i).transactioinID+" from User "+pendingTransactions.get(i).from.userId+" to User "+pendingTransactions.get(i).to.userId+" Amount of Coins "+pendingTransactions.get(i).coinsInvolved.size()+"\n";
			else
				pendingOutput+= "Transaction ID: "+pendingTransactions.get(i).transactioinID+" from Scrooge "+" to User "+pendingTransactions.get(i).to.userId+" Id of Coin created "+pendingTransactions.get(i).coin.coinId+"\n";
	
		}
		Engine.output+= pendingOutput;
		System.out.print(pendingOutput);
		Engine.output+=line;
	}
	public void randomTransaction() throws Exception{
		int user = (int)(Math.random()* users.size());
		int user2 = (int)(Math.random()* users.size());
		if(user == user2){
			return;
		}
		if(validCoins(users.get(user))>0) {
		int amount = (int)(Math.random()*validCoins(users.get(user)));//.Coins.size());
		if(amount==0)
			return;
		UserTransfer(users.get(user), users.get(user2),amount);
		}
	}
	
	public void createUsers() throws Exception  {
		for(int i  = 0 ;i<10;i++) {
			User u = new User();
			try {
				u.keys = Crypto.generateKeyPair();
				System.out.println("User ID :"+ u.userId+" User Public Key "+u.keys.getPublic()+" User number of Coins Intially "+u.Coins.size());
				Engine.output+="User ID :"+ u.userId+" User Public Key "+u.keys.getPublic()+" Number of Coins Intially "+u.Coins.size()+"\n";
			} catch (Exception e) {
				System.out.println("Error");
			}
			this.createCoin(u, 10);
			this.users.add(u);
			if(stopcode==true)
				break;	
		}
	}

	 public void ExitCode() throws Exception {
	    Engine.output+= line;
	    System.out.print(line);
		boolean validblocks =  VerifyBlockChain();
		boolean validtrans = VerifyTransactions();
		Engine.output+= "Scrooge Signature "+ lastHashPointerSigniture + " On the hash of the last block "+ blocks.get(blocks.size()-1).blockID+"\n";
		System.out.println( "Scrooge Signature "+ lastHashPointerSigniture + " On the hash of the last block "+ blocks.get(blocks.size()-1).blockID);
		if(validblocks==false) {
			Engine.output = Engine.output+"The blocks have been manipulated"+"\n";
			System.out.println("The blocks have been manipulated");
		}
		 if(validtrans==false) {
			 Engine.output = Engine.output+"The transactions have been manipulated"+"\n";
			 System.out.println("The transactions have been manipulated");
		 }
//		 if(checkcoins()==false) {
//			 Engine.output = Engine.output+"The number of coins in the system has been manipulated";
//			 System.out.println("The number of coins in the system has been manipulated");
//		 }
		
		 	
		FileWriter myWriter = new FileWriter("output.txt");
		myWriter.write(Engine.output);
		myWriter.close();
		
		 System.exit(0);
	 

}
}