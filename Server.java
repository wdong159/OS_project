/*
course number (CS4345)
Semester/Year (Spring 2019)
assignment identifier (Assignment 3)
all group members’ names (Wei Dong, Guanghui Li, Marco Colasito) 
*/


import java.net.*;
import java.io.*;
import java.util.*;


public class Server{
	/*
	create two set that store the user name and all the message user send to the server
	*/
	private static Set<String> allClient = new HashSet<>();
	private static Set<DataOutputStream> writers = new HashSet<>();
	public static boolean moreThenTwo = false;
	public static void main(String[] args){
		ServerSocket servSock;
		Socket sock;
		try{
			/*
			each client has it own thread
			*/
			servSock = new ServerSocket(7000);
			System.out.println("Server started at "+ new Date() + '\n');
			while(true){
				//Listen for a connection request
				sock = servSock.accept();
				ChatRoom room = new ChatRoom(sock);
				Thread clinet = new Thread(room);
				clinet.start();
			}
		 } catch(IOException ioe){
				System.err.println(ioe);
			}

	}//End-of-main

	/*
	thread class
	*/
	private static class ChatRoom implements Runnable{
		
		Socket socket;
		String name;
		DataOutputStream output2client;
		DataInputStream input4mclient;

		//pass down the client socket and assign it to local socket
		public ChatRoom (Socket socket){
			this.socket = socket;
		}

		public void run(){
			try{
				//create data input and data output streams
				input4mclient = new DataInputStream(socket.getInputStream());
				output2client = new DataOutputStream(socket.getOutputStream());

			/*
			Ask the user name and if user name are ready exist ask for another user name
			*/
			while(true){
				output2client.writeUTF("YOURNAME");
				name = input4mclient.readUTF();
				if(name==null)
					return;
				//synchronized- let add new client to the group
				synchronized (allClient){
					if(name != null && !allClient.contains(name)){
						allClient.add(name);
						break;
					}
				}
			}

			
			//boardcast to everyone in the group chat that a new user has enter the chat
			for(DataOutputStream w:  writers){
				w.writeUTF("JOINGROUP" + name);
			}
			
			//store the new client out put stream so we can boardcast to everyone
			writers.add(output2client);

			for(DataOutputStream w:  writers){
				w.writeUTF("GOCHAT!");
			}

			//conversition start here, once someon type LOGOUT he/she is going to sign out
			while(true){
				String message = input4mclient.readUTF();
				if(message.equals("LOGOUT")){
					output2client.writeUTF("YOUHAVELOGOUT");
					System.out.println("you");
					break;
				}
				for(DataOutputStream w : writers){
					
					w.writeUTF("Message" + name +":"+ message);
				}
			}//end of while sending message

		}catch(Exception e){
			System.out.println(e);
		}finally{
			
			/*
			boardcase when someone had leave the chat
			remove client from the list
			*/
			try {
				System.out.println(name+" someone has left");

				if(output2client != null){
					writers.remove(output2client);
				}
				if(name != null){
					allClient.remove(name);
					for(DataOutputStream w : writers){
						w.writeUTF("Message" + name +":"+ "has left the chat");
					}
				}

				//close the socket
				socket.close();
				
			}catch(Exception e){
				
			}
		}
		}
	}

}//End-of-class