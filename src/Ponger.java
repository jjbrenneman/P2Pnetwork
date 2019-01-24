
public class Ponger extends Thread {

	Node n;
	
	public Ponger(Node n)
	{
		this.n = n;
	}
	
	public void run()
	{
		n.runPong();
	}
	
}
