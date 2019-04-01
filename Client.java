
/*
Course Number: CS4345
Spring 2019
Assignment 3
Members: Wei Dong, Guanghui Li, Marco Colasito
*/

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Client {
	static String name;

	public static void main(String[] args) throws Exception {
		try {
			// create a socket to make a connection to the server socket
			Socket sock = new Socket("127.0.0.1", 7000);
			Scanner scn = new Scanner(System.in);
			Scanner write = new Scanner(System.in);

			// create an output stream to send data to the server
			DataOutputStream data2server = new DataOutputStream(sock.getOutputStream());
			// create an input stream to receive data from the server
			DataInputStream result4mserver = new DataInputStream(sock.getInputStream());

			// Thread for sending messages to the server
			Thread sendMsg = new Thread(new Runnable() {

				@Override
				public void run() {
					while (true) {
						String msg = write.nextLine();
						try {
							if (msg.equals("LOGOUT")) {
								data2server.writeUTF(msg);
								break;
							}
							data2server.writeUTF(msg);
						} catch (Exception e) {
							System.out.println(e);
							break;
						}
					}
				}
			});

			// Used for reading messages from the client
			Thread readMsg = new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						while (true) {
							try {
								/*
								 * read the output from the server and take action based on what server had
								 * asked for Ex: when you join, the server will ask for username
								 */
								String result = result4mserver.readUTF();

								// If user joins the chat, asks for a username
								if (result.startsWith("Enter your name: ")) {
									System.out.println(result);
									name = scn.next();
									data2server.writeUTF(name);
									sendMsg.start();
								} else if (result.startsWith("Message")) {

									// Regex pattern matcher used to distinguish between actions to be performed
									Pattern pattern = Pattern.compile(name);
									Matcher there = pattern.matcher(result);
									if (!there.find())
										System.out.println(result.substring(7));

									// User enters the chat (displayed on all other users)
								} else if (result.startsWith("JOINGROUP")) {
									System.out.println(result.substring(9) + " has joined the chat\n");

									// User enters the chat (only displayed for the user)
								} else if (result.equals("GOCHAT!")) {
									System.out
											.println("You can start talking now!\nType 'LOGOUT' at anytime to leave\n");

									// User leaves the chat
								} else if (result.equals("YOUHAVELOGOUT")) {
									System.out.println("YOU have left the chat\n");
									break;
								}
							} catch (Exception e) {
								System.out.println(e);
								break;
							}
						}
					} catch (Exception e) {

						System.out.println("Server connection has failed");
					}
				}
			});

			readMsg.start();

		} catch (IOException ioe) {
			System.err.println(ioe);
		}
	}
}
