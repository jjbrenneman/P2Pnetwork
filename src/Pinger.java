
public class Pinger extends Thread {

	Node n;
	
	public Pinger(Node n)
	{
		this.n = n;
	}
	
	public void run()
	{
		n.runPing();
	}
}
