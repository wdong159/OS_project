import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.Executors;

import jdk.nashorn.internal.parser.Scanner;

public class Server{
	private static Set<String> allClient = new HashSet<>();
	private static Set<DataOutputStream> writers = new HashSet<>();

	public static void main(String[] args){
		ServerSocket servSock;
		Socket sock;
		try{
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

		public ChatRoom (Socket socket){
			this.socket = socket;
		}

		public void run(){
			try{
			//create data input and data output streams
			input4mclient = new DataInputStream(socket.getInputStream());
			output2client = new DataOutputStream(socket.getOutputStream());

			while(true){
				output2client.writeUTF("YOURNAME");
				name = input4mclient.readUTF();
				if(name==null)
					return;
				synchronized (allClient){
					if(name != null && !allClient.contains(name)){
						allClient.add(name);
						break;
					}
				}
			}

			//write.println("JOINGROUP" + name);
			//output2client.writeUTF("JOINGROUP" + name);
			output2client.writeUTF("NAME" + name);

			for(DataOutputStream w:  writers){
				w.writeUTF("JOINGROUP" + name);
			}
			
			writers.add(output2client);

			while(true){
				//output2client.writeUTF("Message");
				String message = input4mclient.readUTF();
				if(message == "LOGOUT"){
					return;
				}
				for(DataOutputStream w : writers){
					
					w.writeUTF("Message" + name +":"+ message);
				}
				//System.out.println(message);
				//output2client.writeUTF("Message" + name +":"+ message);
			}

		}catch(Exception e){
			System.out.println(e);
		}finally{
			
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
				socket.close();
				
			}catch(Exception e){
				
			}
		}
		}
	}

}//End-of-class