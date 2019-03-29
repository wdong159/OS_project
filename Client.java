/*
course number (CS4345)
Semester/Year (Spring 2019)
assignment identifier (Assignment 3)
all group members’ names (Wei Dong, Guanghui Li, Marco Colasito) 
*/



import java.net.*;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//import com.sun.org.apache.xerces.internal.impl.xs.identity.Selector.Matcher;

public class Client{
	static String name;
	static boolean go = false;
	static boolean talk = true;
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
			

			/*
			thread for  sending out message to the server
			*/
			Thread sendMsg = new Thread(new Runnable(){
			
				@Override
				public void run() {
					while(true){
							String msg = write.nextLine();
							try{
								if(msg.equals("LOGOUT")){
									data2server.writeUTF(msg);
									break;
								}
								data2server.writeUTF(msg);
							}catch(Exception e){
								System.out.println(e);
							}
					}
				}
			});


			/*
			thread for reading message from the client
			*/
			Thread readMsg = new Thread (new Runnable(){
			
				@Override
				public void run() {
					try{
						while(true){
							try{
								//bunch of if statement for reading the message
								String result = result4mserver.readUTF();
								if(result.startsWith("YOURNAME")){
										System.out.println(result);
										name = scn.next();
										name.toUpperCase();
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
								else if(result.equals("GOCHAT!")){
									if(talk)
										System.out.println("You can start talking now!");
									talk = false;
									sendMsg.start();						
								}else if(result.equals("YOUHAVELOGOUT")){
									System.out.println("YOU had log out from the chat");
									break;
								}
							}catch (Exception e){
								
								System.out.println(e);
							}
						}
					}catch (Exception e){
								
						System.out.println("Server coonection has failed");
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
