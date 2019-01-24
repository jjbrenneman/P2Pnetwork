import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.ArrayList;

public class Node implements Serializable {

	//list of all of the neighbors of this node
	private ArrayList<Node> neighbors = new ArrayList<Node>();

	//list of all the files the node has
	private String[] files;
	
	//stores the IP address of this node
	private String address;
	//stores the port of this node
	private int port;
	
	//construct a new node
	public Node(String addr, int port)
	{
		address = addr;
		this.port = port;
	}
	
	//pings the base node to add node to system
	public int pingBase() throws Exception
	{
		//open socket to base node
		Socket s = new Socket("127.0.0.1", 2345);
		
		//write ping message
		String ping = address + "," + port;
		
		//System.out.println("Sending: "+ping);
		
		ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
		ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
		
		//send ping
		oos.writeObject(ping);
		oos.flush();
		
		//retreive pong
		String test = (String) ois.readObject();
		
		ois.close();
		oos.close();
		s.close();
		
		//parse pong
		String[] data = test.split(",");
		if(data.length > 1)
		{
			int i = Integer.parseInt(data[1].trim());
			//add neighbors sent from base
			while( i > 0)
			{	
				int j = 0;
				Node n = new Node(data[2+j], Integer.parseInt(data[2+j+1]));
			
				if(!containsNeighbor(n))
					addNeighbor(n);
				j+=2;
				i--;
			}
		}
		
		//System.out.println("recieved: "+test);
		return 0;
		
	}
	
	//creates thread to ping periodically
	public void runPing()
	{
		Runnable pinger = new Runnable() {
			public void run()
			{
				while(true)
				{
					try {
						int i = 0;
						//System.out.println("num neighbors = "+ neighbors.size());
						while(i < neighbors.size())
						{
							try{
								ping(neighbors.get(i).address, neighbors.get(i).port);
								i++;
							}
							catch (Exception e)
							{
								//e.printStackTrace();
								System.out.println("neighbor at port: "+neighbors.get(i).port + " has died");
								removeNeighbor(i);
								continue;
							}
						}
						//run every 15 seconds
						Thread.sleep(15000);
					}
					catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};
		new Thread(pinger).start();
	}
	
	//send normal ping
	public int ping(String addr, int port) throws Exception
	{
		//create socket to node to ping
		Socket s = new Socket(addr, port);
		
		//write ping
		String ping = address + "," + this.port;
		
		//if you have less than three neighbors, ask for more neighbors
		if(neighbors.size()<3)
		{
			ping += ", SendNeighbors";
		}
		
		//System.out.println("Sending: " +ping);
		
		ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
		ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
		
		//send ping
		oos.writeObject(ping);
		oos.flush();
		
		//retrieve pong
		String recieved = (String) ois.readObject();
		
		ois.close();
		oos.close();
		s.close();
		
		//parse pong
		String[] data = recieved.split(",");
		
		if(data.length > 2)
		{
			int i = Integer.parseInt(data[1]);
			
			//add neighbors found in pong
			while( i > 0)
			{
				int j = 0;
				Node n = new Node(data[2+j], Integer.parseInt(data[2+j+1]));
				
				if(!containsNeighbor(n))
					addNeighbor(n);
				j+=2;
				i--;
			}
		}
		
		//System.out.println("recieved: "+recieved);
		return 0;
	}
	
	//creates thread to run the pong server
	public void runPong()
	{
		Runnable ponger = new Runnable() {
			public void run() {
				
				try {
					ServerSocket ss = new ServerSocket(port);
					while(true)
					{
						Socket s = ss.accept();
						Thread.sleep(1);
						pong(s);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		new Thread(ponger).start();
	}

	//sends pong
	public int pong(Socket s) throws Exception
	{
		ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
		ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
		
		String recieved = "recieved";
		
		//retrieve ping
		String ping = (String) ois.readObject();
		
		//System.out.println(ping);
		
		//parse ping
		String[] data = ping.split(",");
		
		if(data.length >2)
		{
			//if it is a querey
			if(data[2].equals("querey"))
			{
				//ois.close();
				//oos.close();
				quereyHandler(oos, ping);
				ois.close();
				
				return 0;
			}
			//if they are asking for more neighbors
			else if(neighbors.size()>0)
				recieved += "," + nList();
		}
		
		//send pong
		oos.writeObject(recieved);
		oos.flush();
		
		ois.close();
		oos.close();
		s.close();
		
		//if the ping was from a none neighbor, add as neighbor
		Node n = new Node(data[0], Integer.parseInt(data[1]));
		if(!containsNeighbor(n))
			addNeighbor(n);
		
		return 0;
	}
	
	//add node to neighbors list
	public void addNeighbor(Node newNeighbor)
	{
		System.out.println("new neighbor added: "+newNeighbor.address + ","+ newNeighbor.port);
		neighbors.add(newNeighbor);
	}
	
	//removes neighbor at given index
	public void removeNeighbor(int index)
	{
		neighbors.remove(index);
	}
	
	//return the list of neighbors
	public ArrayList<Node> getNeighbors()
	{
		return neighbors;
	}
	
	//check if you are already neighbors with a node
	public boolean containsNeighbor(Node neighbor)
	{
		String addr = neighbor.address;
		int p = neighbor.port;
		
		//check if the node is you
		if(addr.equals(address))
			if(p == port)
				return true;
		
		for (int i = 0; i< neighbors.size(); i++)
		{
			if(neighbors.get(i).address.equals(addr))
				if(neighbors.get(i).port == p)
					return true;
		}
		return false;
	}
	
	//return IP address of the node
	public String getAddress()
	{
		return address;
	}
	
	//return the port of the node
	public int getPort()
	{
		return port;
	}
	
	public void setFiles(String[] fs)
	{
		files = fs;
	}
	public boolean hasFile(String filename)
	{
		for(int i = 0; i < files.length; i++)
		{
			if(filename.equals(files[i]))
				return true;
		}
		return false;
	}
	
	//create a list of all neighbor information
	public String nList()
	{
		String list= neighbors.size()+ ",";
		
		for(int i = 0; i < neighbors.size(); i++)
		{
			list += neighbors.get(i).getAddress() +","+ neighbors.get(i).getPort() + ",";
		}
		return list;
	}
	
	//send a querey to your neighbors
	public File querey(String q) throws IOException, ClassNotFoundException
	{
		File file;
		byte[] content;
		
		//parse the querey String
		String[] data = q.split(",");
		int timeToLive = Integer.parseInt(data[5]);
		
		int i = 0;
		//System.out.println("num neighbors = "+ neighbors.size());
		
		//querey your neighbors until you've asked them all, or you  have reached the time to live
		while(i < neighbors.size()& i <= timeToLive)
		{	
			//create socket to neighbor	
			Socket s = new Socket(neighbors.get(i).address,neighbors.get(i).port);
				
			ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
			ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
				
			//send querey message
			oos.writeObject(q);
			oos.flush();
				
			//retrieve file
			content = (byte[])ois.readObject();
				
			//check if there was a file
			if(content != null)
			{
				//create file
				file = new File("found_"+data[3]);
				Files.write(file.toPath(), content);
				
				ois.close();
				oos.close();
				s.close();
				
				return file;
			}
			
			ois.close();
			oos.close();
			s.close();
			
			i++;
		}
		//no file was found
		return null;
	}
	
	//handle querey messages
	public void quereyHandler(ObjectOutputStream oos, String q) throws IOException
	{
		//parse querey message
		String[] data = q.split(",");
				
		//check if the node has the file
		if(hasFile(data[3]))
		{
			File file = new File(data[3]);
			//send the file contents
			byte[] content = Files.readAllBytes(file.toPath());
			oos.writeObject(content);
		}
		else
		{
			//send null if file does not exist
			oos.writeObject(null);
		}
		
		oos.close();
	}
}
