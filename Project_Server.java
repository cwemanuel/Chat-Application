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

public class Project_Server
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
		catch(IOException ex){}
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
				BufferedReader inputFromClient = new BufferedReader(new InputStreamReader(client.getInputStream()));
				outputToClient = new DataOutputStream(client.getOutputStream());

				InetAddress inetAddress = client.getInetAddress();
				System.out.println("Connected to Client " + clientID + " at " +
						inetAddress.getHostName() + " " + inetAddress.getHostAddress());

				boolean check = true;
				while (check)
				{
					String username = (String)inputFromClient.readLine();
					username.trim();
					this.userName = username;

					String password = (String)inputFromClient.readLine();
					password.trim();

					Class.forName("com.mysql.jdbc.Driver");
					System.out.println("Driver loaded");

					Connection connection = DriverManager.getConnection(
							"jdbc:mysql://localhost:3307/carlProject?useSSL=false", "scott", "tiger");
					System.out.println("Database connected");

					PreparedStatement select = connection.prepareStatement(
							"select * from accounts where username = ?");
					select.setString(1, username);
					ResultSet resultSet = select.executeQuery();
					System.out.println("TEST1");
					PreparedStatement insert = connection.prepareStatement(
							"insert into accounts(username, password) values  (?, ?)");

					if (resultSet.next() == false)
					{
						//System.out.println("TEST2");
						insert.setString(1, username);
						insert.setString(2, password);
						insert.executeUpdate();
						String success = "Account " + username + " created.";
						System.out.println(success);
						outputToClient.writeChars(success + "\n");
						outputToClient.flush();
						check = false;
						connection.close();
					}
					else
					{
						System.out.println("TEST3");
						select = connection.prepareStatement("select * from accounts where username = ? and password = ?");
						select.setString(1, username);
						select.setString(2, password);
						resultSet = select.executeQuery();

						if(resultSet.next() == false)
						{
							System.out.println("TEST4");
							String failed = "Username  already exists or Username/Password is Incorrect";
							outputToClient.writeChars(failed + "\n");
							outputToClient.flush();
						}
						else
						{
							System.out.println("TEST5");
							String success = "Successful";
							outputToClient.writeChars(success + "\n");
							outputToClient.flush();
							System.out.println("TEST6");
							check = false;
							connection.close();
						}
					}
					System.out.println("TEST7");
				}

				while(true)
				{
					System.out.println("TEST8");
					String message = (String)inputFromClient.readLine();
					//System.out.println(message);

					if(message.equals("Close Window"))
					{
						System.out.println("TEST9");
						message = this.userName + " has left";
						for(HandleClientTask client : clients)
						{
							client.outputToClient.writeChars(message + "\n");
							outputToClient.flush();
						}
						clients.remove(this.clientID);
						break;
					}

					for(HandleClientTask client : clients)
					{
						client.outputToClient.writeChars(message + "\n");
						outputToClient.flush();
					}
				}
			}
			catch (Exception ex){}
		}
	}
}