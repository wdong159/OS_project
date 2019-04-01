
/*
Course Number: CS4345
Spring 2019
Assignment 3
Members: Wei Dong, Guanghui Li, Marco Colasito
*/

import java.net.*;
import java.io.*;
import java.util.*;

public class Server {

	// Create two sets that store the usernames and all the messages that user send
	// to the server
	private static Set<String> allClient = new HashSet<>();
	private static Set<DataOutputStream> writers = new HashSet<>();
	private static HashMap<String, DataOutputStream> users = new HashMap<>();

	public static void main(String[] args) {
		ServerSocket servSock;
		Socket sock;
		try {
			servSock = new ServerSocket(7000);
			System.out.println("Server started at " + new Date() + '\n');
			while (true) {
				// Listen for a connection request
				sock = servSock.accept();
				ChatRoom room = new ChatRoom(sock);
				Thread clinet = new Thread(room);
				clinet.start();
			}

		} catch (IOException ioe) {
			System.err.println(ioe);
		}

	}

	// Thread class
	private static class ChatRoom implements Runnable {

		Socket socket;
		String name;
		DataOutputStream output2client;
		DataInputStream input4mclient;

		// Pass down the client socket and assign it to local socket
		public ChatRoom(Socket socket) {
			this.socket = socket;
		}

		public void run() {
			try {
				// Create data input and data output streams
				input4mclient = new DataInputStream(socket.getInputStream());
				output2client = new DataOutputStream(socket.getOutputStream());

				// Ask for the username and if username already exists ask for another username
				while (true) {
					output2client.writeUTF("Enter your name: ");
					name = input4mclient.readUTF();
					if (name == null)
						return;
					// synchronized - adds a new client to the group
					synchronized (allClient) {
						if (name != null && !allClient.contains(name)) {
							allClient.add(name);
							break;
						}
					}
				}

				// Broadcasts to everyone in the group chat that a new user has entered the chat
				for (DataOutputStream w : writers) {
					w.writeUTF("JOINGROUP" + name);
				}

				// Stores the new client output stream so we can boardcast to everyone
				writers.add(output2client);

				output2client.writeUTF("GOCHAT!");

				// Conversation starts here, once someone types LOGOUT he/she is going to sign
				// out
				while (true) {
					String message = input4mclient.readUTF();
					if (message.equals("LOGOUT")) {
						output2client.writeUTF("YOUHAVELOGOUT");
						break;
					}

					// If the user tags someone, the message will only be sent to that user
					else if(message.startsWith("@")){
						String[] temp = message.split(" ", 2);
						
					}
					for (DataOutputStream w : writers) {

						w.writeUTF("Message" + name + ": " + message);
					}
				}

			} catch (Exception e) {
				System.out.println(e);
			} finally {

				// When someone leaves the chat, it removes the client from the list
				try {
					System.out.println(name + " has left\n");

					if (output2client != null) {
						writers.remove(output2client);
					}
					if (name != null) {
						allClient.remove(name);
						for (DataOutputStream w : writers) {
							w.writeUTF("Message" + name + ": " + "has left the chat\n");
						}
					}

					// Close the socket
					socket.close();

				} catch (Exception e) {

				}
			}
		}
	}
}