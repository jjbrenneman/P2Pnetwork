import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class Base {
	
	//create base node
	private static Node home = new Node("127.0.0.1", 2345);
	
	//handle pings to let nodes into the system
	public static void handel(Socket s) throws Exception
	{
		if(s == null)
			System.out.println("null");
		
		ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
		ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
		
		//retrive ping message
		String ping = (String) ois.readObject();
		
		//send the new node neighbors if there are any other nodes in the system known to the base
		if(home.getNeighbors().size() > 0)
			oos.writeObject("recived, "+ nList());
		else
			oos.writeObject("recieved");
		oos.flush();
		
		ois.close();
		oos.close();
		s.close();
		
		String[] data = ping.split(",");
		//add new node to base neighbor list
		home.addNeighbor(new Node(data[0], Integer.parseInt(data[1])));
	}
	
	//returns string of all neighbor information
	public static String nList()
	{
		ArrayList<Node> neighbors = home.getNeighbors();
		String list= neighbors.size()+ ",";
		
		for(int i = 0; i < neighbors.size(); i++)
		{
			list += neighbors.get(i).getAddress() +","+ neighbors.get(i).getPort() + ",";
		}
		return list;
	}
	
	//run the server
	public static void main(String[] args) throws Exception
	{
		ServerSocket socket = new ServerSocket(2345);
		
		System.out.println("running");
		
		while(true)
		{
			Socket s = socket.accept();
			handel(s);
		}
	}

}
