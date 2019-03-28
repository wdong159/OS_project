import java.net.*;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//import com.sun.org.apache.xerces.internal.impl.xs.identity.Selector.Matcher;

public class Client{
	static String name;
	static boolean go = false;
	public static void main(String[] args) throws Exception{
		try{
			//create a socket to make connection to server socket
			Socket sock = new Socket("127.0.0.1", 7000);
			Scanner scn = new Scanner (System.in);
			Scanner write = new Scanner (System.in);
		
			//create an output stream to send data to the server
			DataOutputStream data2server = new DataOutputStream(sock.getOutputStream());
			//create an input stream to receive data from server
			DataInputStream result4mserver = new DataInputStream(sock.getInputStream());
			

			Thread sendMsg = new Thread(new Runnable(){
			
				@Override
				public void run() {
					while(true){
							String msg = write.nextLine();
							try{
								data2server.writeUTF(msg);
							}catch(Exception e){
								System.out.println(e);
							}
					}
				}
			});



			Thread readMsg = new Thread (new Runnable(){
			
				@Override
				public void run() {
					while(true){
						try{
							String result = result4mserver.readUTF();
							if(result.startsWith("YOURNAME")){
									System.out.println(result);
									name = scn.next();
									data2server.writeUTF(name);
							}else if(result.startsWith("Message")){
								Pattern pattern = Pattern.compile(name);
								Matcher there = pattern.matcher(result);
								if(!there.find())
									System.out.println(result.substring(7));
							}else if(result.startsWith("JOINGROUP")){
								System.out.println(result.substring(9) + " has join the chat");
							}else if(result.startsWith("NAME")){
								System.out.println(result.substring(4));
							}
							if(!go){
								sendMsg.start();						
								go = true;
							}
						}catch (Exception e){
							
							System.out.println(e);
						}
				}
				}
			});
			

			readMsg.start();
			//sendMsg.start();
			
		 } catch(IOException ioe){
				System.err.println(ioe);
			}finally{
			}
	}//End-of-main
}//End-ofclass
