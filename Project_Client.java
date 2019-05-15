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
import javafx.scene.control.PasswordField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.geometry.*;
import java.io.*;
import java.net.*;

public class Project_Client extends Application
{
	private TextField tfUserName = new TextField();
	private PasswordField pfPassword = new PasswordField();
	private TextArea taResults = new TextArea();
	private Button btnRegisterOrLogin = new Button("Register/" + "\n" + "Login");
	private TextArea taMessages = new TextArea();
	private TextField tfMessage = new TextField();
	private PrintWriter toServer;
	private BufferedReader fromServer;

	public void start(Stage primaryStage)
	{
		GridPane pane = new GridPane();
		pane.setAlignment(Pos.CENTER);
		pane.setHgap(5);
		pane.setVgap(5);

		pane.add(new Label("Username:"), 0, 0);
		pane.add(tfUserName, 1, 0);
		pane.add(new Label("Password:"), 0, 1);
		pane.add(pfPassword, 1, 1);
		pane.add(btnRegisterOrLogin, 0, 2);
		pane.add(taResults, 1, 2);
		taResults.setPrefHeight(20);
		taResults.setPrefWidth(80);
		taResults.setEditable(false);
		pane.add(taMessages, 1, 3);
		taMessages.setPrefHeight(300);
		taMessages.setPrefWidth(400);
		taMessages.setEditable(false);
		pane.add(new Label("Message:"), 0, 4);
		pane.add(tfMessage, 1, 4);

		btnRegisterOrLogin.setOnAction(e -> register());

		tfMessage.setOnAction(e -> sendMessage());

		primaryStage.setOnCloseRequest(e -> {
			toServer.print("Close Window" + "\n");
			toServer.flush();
		});

		Scene scene = new Scene(pane, 490, 460);
		primaryStage.setTitle("Chat Window");
		primaryStage.setScene(scene);
		primaryStage.show();

		try
		{
			Socket socket = new Socket("localhost", 8000);
			toServer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
			fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			new ReceiveMessage(socket);
		}
		catch(IOException ex){}
	}

	private void register()
	{
		try
		{
			String user = tfUserName.getText();
			String password = pfPassword.getText();

			toServer.print(user + "\n");
			toServer.flush();
			toServer.print(password + "\n");
			toServer.flush();

			String results = fromServer.readLine();
			taResults.setText(results);
		}
		catch(IOException ex){}
	}

	private void sendMessage()
	{
		String message = tfUserName.getText() + ": " + tfMessage.getText();

		toServer.print(message + "\n");
		toServer.flush();
		tfMessage.setText("");
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
			catch(IOException ex){}
		}
	}
}