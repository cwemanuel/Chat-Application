/*
	Carl Emanuel
	CS4120 Advanced Java
	Project
	Server
*/

import java.util.*;
import java.io.*;
import java.net.*;
import java.sql.*;

public class Project_Server_Experimental
{
	public static List<HandleClientTask> clients = new ArrayList<>();

	public static void main(String[] args) throws SQLException, ClassNotFoundException
	{
		try
		{
			ServerSocket serverSocket = new ServerSocket(8000);
			int clientCount = 0;

			while(true)
			{
				Socket socket = serverSocket.accept();
				HandleClientTask task = new HandleClientTask(socket, clientCount);
				clients.add(task);
				Thread thread = new Thread(task);
				thread.start();
				clientCount++;
			}
		}
		catch(IOException ex){System.out.println(ex);}
	}

	public static class HandleClientTask implements Runnable
	{
		private Socket client;
		private int clientID;
		private String userName;
		DataOutputStream outputToClient;

		public HandleClientTask(Socket client, int clientID)
		{
			this.client = client;
			this.clientID = clientID;
			this.userName = "";
		}

		public void run()
		{
			try
			{
				Class.forName("com.mysql.jdbc.Driver");
				System.out.println("Driver loaded");

				Connection connection = DriverManager.getConnection(
						"jdbc:mysql://localhost:3307/carlProject?useSSL=false", "scott", "tiger");
				System.out.println("Database connected");

				BufferedReader inputFromClient = new BufferedReader(new InputStreamReader(client.getInputStream()));
				outputToClient = new DataOutputStream(client.getOutputStream());

				InetAddress inetAddress = client.getInetAddress();
				System.out.println("Connected to Client " + clientID + " at " +
						inetAddress.getHostName() + " " + inetAddress.getHostAddress());

				String username;
				String password;
				PreparedStatement select;
				ResultSet resultSet;
				boolean check = true;

				while(true)
				{
					String message = (String)inputFromClient.readLine();

					if(message.equals("**REGISTER**"))
					{
						while (check)
						{
							System.out.println(clientID +": Inside Register while loop");
							username = (String)inputFromClient.readLine();
							username.trim();
							this.userName = username;

							password = (String)inputFromClient.readLine();
							password.trim();

							select = connection.prepareStatement("select * from accounts where username = ?");
							select.setString(1, username);
							resultSet = select.executeQuery();
							System.out.println("Register query success");
							PreparedStatement insert = connection.prepareStatement(
									"insert into accounts(username, password) values  (?, ?)");

							if (resultSet.next() == false)
							{
								insert.setString(1, username);
								insert.setString(2, password);
								insert.executeUpdate();
								System.out.println(clientID + ": Register success");
								String success = "Account " + username + " created.";
								System.out.println(success);
								outputToClient.writeChars(success + "\n");
								outputToClient.flush();
								check = false;
								connection.close();
							}
							else
							{
								System.out.println(clientID + ": Register fail");
								outputToClient.writeChars("Username is taken. Try again." + "\n");
								outputToClient.flush();
								break;
							}
						}
						System.out.println(clientID + ": Left Register while loop");
					}
					else if(message.equals("**LOGIN**"))
					{
						while(check)
						{
							System.out.println(clientID + ": Inside Login while loop");
							username = (String)inputFromClient.readLine();
							username.trim();
							this.userName = username;

							password = (String)inputFromClient.readLine();
							password.trim();

							select = connection.prepareStatement("select * from accounts where username = ? and password = ?");
							select.setString(1, username);
							select.setString(2, password);
							resultSet = select.executeQuery();
							System.out.println(clientID + ": Login query success");

							if(resultSet.next())
							{
								System.out.println(clientID + ": Login success");
								outputToClient.writeChars("Login successful" + "\n");
								outputToClient.flush();
								connection.close();
								check = false;
							}
							else
							{
								System.out.println(clientID + ": Login fail");
								outputToClient.writeChars("Username or Password incorrect" + "\n");
								outputToClient.flush();
								break;
							}
						}
						System.out.println(clientID + ": Left Login while loop");
					}
					else if(message.equals("**MESSAGE**"))
					{
						System.out.println(clientID + ": Getting message from client");
						message = (String)inputFromClient.readLine();
						//System.out.println(message);

						if(message.equals("Close Window"))
						{
							System.out.println(clientID + ": Client disconnect");
							message = this.userName + " has left";
							for(HandleClientTask client : clients)
							{
								client.outputToClient.writeChars(message + "\n");
								outputToClient.flush();
							}
							clients.remove(this.clientID);
						}
						else
						{
							System.out.println(clientID + ": Sending messages to clients");
							message = this.userName + ": " + message;
							for(HandleClientTask client : clients)
							{
								client.outputToClient.writeChars(message + "\n");
								outputToClient.flush();
							}
						}
					}
				}
			}
			catch (Exception ex){System.out.println(ex);}
		}
	}
}