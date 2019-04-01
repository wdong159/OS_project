
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
	/*
	 * create two sets that store the usernames and all the messages that user send
	 * to the server
	 */
	private static Set<String> allClient = new HashSet<>();
	private static Set<DataOutputStream> writers = new HashSet<>();
	// public static boolean moreThenTwo = false;

	public static void main(String[] args) {
		ServerSocket servSock;
		Socket sock;
		try {
			/*
			 * each client has its own thread
			 */
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

	}// End-of-main

	// Thread class
	private static class ChatRoom implements Runnable {

		Socket socket;
		String name;
		DataOutputStream output2client;
		DataInputStream input4mclient;

		// pass down the client socket and assign it to local socket
		public ChatRoom(Socket socket) {
			this.socket = socket;
		}

		public void run() {
			try {
				// create data input and data output streams
				input4mclient = new DataInputStream(socket.getInputStream());
				output2client = new DataOutputStream(socket.getOutputStream());

				/*
				 * Ask for the username and if username already exists ask for another username
				 */
				while (true) {
					output2client.writeUTF("YOURNAME");
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

				// broadcasts to everyone in the group chat that a new user has entered the chat
				for (DataOutputStream w : writers) {
					w.writeUTF("JOINGROUP" + name);
				}

				// stores the new client out put stream so we can boardcast to everyone
				writers.add(output2client);

				output2client.writeUTF("GOCHAT!");

				// conversation starts here, once someone types LOGOUT he/she is going to sign
				// out
				while (true) {
					String message = input4mclient.readUTF();
					if (message.equals("LOGOUT")) {
						output2client.writeUTF("YOUHAVELOGOUT");
						break;
					}
					for (DataOutputStream w : writers) {

						w.writeUTF("Message" + name + ": " + message);
					}
				} // end of while sending message
					// System.out.println("123");

			} catch (Exception e) {
				System.out.println(e);
			} finally {

				/*
				 * base case when someone leaves the chat removes the client from the list
				 */
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

					// close the socket
					socket.close();

				} catch (Exception e) {

				}
			}
		}
	}

}// End-of-class