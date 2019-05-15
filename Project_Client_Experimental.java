/*
	Carl Emanuel
	CS4120 Advanced Java
	Project
	Client
*/

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.geometry.*;
import java.io.*;
import java.net.*;

public class Project_Client_Experimental extends Application
{
	private Stage stage = new Stage();
	private Stage primaryStage = new Stage();
	private TextField tfUserName = new TextField();
	private PasswordField pfPassword = new PasswordField();
	private Label welcome = new Label("Welcome!");
	private Button btnRegister = new Button("Register");
	private Button btnLogin = new Button("Login");
	private TextArea taMessages = new TextArea();
	private TextArea taResults = new TextArea();
	private TextField tfMessage = new TextField();
	private PrintWriter toServer;
	private BufferedReader fromServer;
	private String userName;

	public void start(Stage myPrimaryStage)
	{
		showLoginRegisterWindow();

		primaryStage = myPrimaryStage;

		GridPane pane = new GridPane();
		pane.setAlignment(Pos.CENTER);
		pane.setHgap(5);
		pane.setVgap(5);

		pane.add(welcome, 1, 0);
		welcome.setFont(new Font(20.0));
		pane.add(taMessages, 1, 1);
		taMessages.setPrefHeight(300);
		taMessages.setPrefWidth(400);
		taMessages.setEditable(false);
		pane.add(new Label("Message:"), 0, 2);
		pane.add(tfMessage, 1, 2);

		tfMessage.setOnAction(e -> sendMessage());

		primaryStage.setOnCloseRequest(e -> windowClose());

		Scene scene = new Scene(pane, 490, 460);
		primaryStage.setTitle("Chat Window");
		primaryStage.setScene(scene);
		primaryStage.hide();

		try
		{
			Socket socket = new Socket("localhost", 8000);
			toServer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
			fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			new ReceiveMessage(socket);
		}
		catch(IOException ex){System.out.println(ex);}
	}

	private void showLoginRegisterWindow()
	{
		GridPane pane = new GridPane();
		pane.setAlignment(Pos.CENTER);
		pane.setHgap(5);
		pane.setVgap(5);

		pane.add(new Label("Username:"), 0, 0);
		pane.add(tfUserName, 1, 0);
		pane.add(new Label("Password:"), 0, 1);
		pane.add(pfPassword, 1, 1);
		pane.add(btnRegister, 0, 2);
		pane.add(btnLogin, 1, 2);
		pane.add(taResults, 1, 3);
		taResults.setPrefHeight(40);
		taResults.setPrefWidth(200);
		taResults.setEditable(false);

		btnRegister.setOnAction(e -> register());

		btnLogin.setOnAction(e -> login());

		Scene scene = new Scene(pane, 300, 250);
		stage.setTitle("Login Window");
		stage.setScene(scene);
		stage.show();
	}

	private void register()
	{
		try
		{
			String user = tfUserName.getText();
			String password = pfPassword.getText();

			toServer.print("**REGISTER**" + "\n");
			toServer.flush();
			toServer.print(user + "\n");
			toServer.flush();
			toServer.print(password + "\n");
			toServer.flush();

			String results = fromServer.readLine();
			taResults.setText(results);

			if(results.contains("created"))
			{
				stage.close();
				primaryStage.show();
			}
		}
		catch(IOException ex){System.out.println(ex);}
	}

	private void login()
	{
		try
		{
			this.userName = tfUserName.getText();
			String user = tfUserName.getText();
			String password = pfPassword.getText();

			toServer.print("**LOGIN**" + "\n");
			toServer.flush();
			toServer.print(user + "\n");
			toServer.flush();
			toServer.print(password + "\n");
			toServer.flush();

			String results = fromServer.readLine();
			taResults.setText(results);

			if(results.equals("Login successful"))
			{
				stage.close();
				primaryStage.show();
			}
		}
		catch(IOException ex){System.out.println(ex);}
	}

	private void sendMessage()
	{
		String message = tfMessage.getText();

		toServer.print("**MESSAGE**" + "\n");
		toServer.flush();
		toServer.print(message + "\n");
		toServer.flush();
		tfMessage.setText("");
	}

	private void windowClose()
	{
		toServer.print("**MESSAGE**" + "\n");
		toServer.flush();
		toServer.print("Close Window" + "\n");
		toServer.flush();
	}

	class ReceiveMessage implements Runnable
	{
		private Socket socket;
		public ReceiveMessage(Socket socket)
		{
			this.socket = socket;
			Thread thread = new Thread(this);
			thread.start();
		}

		public void run()
		{
			String message = "";
			try
			{
				BufferedReader fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				while((message = fromServer.readLine()) != null)
				{
					taMessages.appendText(message + "\n");
					message = "";
				}
			}
			catch(IOException ex){System.out.println(ex);}
		}
	}
}