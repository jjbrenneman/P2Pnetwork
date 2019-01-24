import java.io.File;
import java.util.Scanner;

public class Client {

	
	public static void main(String[] args)
	{
		Node node;
		
		//running on localhost, taking in port number
		//cannot be 2345 as that is the port number of the base node
		System.out.println("enter port number (not 2345): ");
		Scanner input = new Scanner(System.in);
		int PORT = input.nextInt();

		//construct new node
		node = new Node("127.0.0.1", PORT);
		
		//get names of files that the node has
		System.out.println("Enter file names(file1.txt,file2.txt,...): ");
		String files = input.next();
		String[] filenames = files.split(",");
		node.setFiles(filenames);
		
		System.out.println("To ping base to enter system type 'base'\nelse enter 'address,port' of known node(use 127.0.0.1 for testing)");
		String cmd = input.next();
		
		try {
			
			if(cmd.equals("base"))
			{
				//ping the base node to enter system
				node.pingBase();
			}
			else {
				//ping known node in system to enter
				String[] data = cmd.split(",");
				node.ping(data[0], Integer.parseInt(data[1]));
			}

			//run server for ponging
			node.runPong();

			//run thread to ping periodically
			node.runPing();
			
			//set up quereying
			System.out.println("enter Quereys in the format of: nameOfFile,idnumber,timeToLive");
			while(true)
			{
				System.out.println("querey? ");
				
				//take in querey message
				String q = input.next();
				if(q == null)
					continue;
				
				//send querey 
				File file =node.querey("127.0.0.1,"+PORT+",querey,"+q);
				
				//check if the file was found
				if(file != null)
					System.out.println("file found");
				else System.out.println("file not found");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
