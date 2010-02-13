package solace.net;

import java.net.*;
import java.util.*;
import java.io.*;
import solace.util.*;
import solace.game.*;
import solace.cmd.*;

/**
 * Player connection object, handles basic user input and output, login and main menu.
 * @author Ryan Sandor Richards (Gaius)
 */
public class Connection
	implements Runnable
{
	Socket socket;
	PrintWriter out;
	BufferedReader in;
	String prompt = "";
	Account account;
	boolean color = false;
	StateController controller;
	Date connectionTime;
	
	/**
	 * Creates a new connection through the given socket.
	 * @param s Socket for the connection
	 * @throws IOException If the input and output streams could not be used for the socket.
	 */
	public Connection(Socket s)
		throws IOException
	{
		socket = s;
		connectionTime = new Date();
		out = new PrintWriter(socket.getOutputStream());
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		controller = new LoginController(this);
	}

	/**
	 * Sets the state controller for the connection.
	 * @param c State controller.
	 */
	public void setStateController(StateController c)
	{
		controller = c;
	}
	
	/**
	 * Closes this connection.
	 */
	public void close()
	{
		try {
			socket.close();
		}
		catch (IOException ioe) 
		{
			Log.error(ioe.getMessage());
		}
	}
	
	/**
	 * [Spike] Basic output function for connections.
	 * @param s String to send.
	 */
	public void send(String s)
	{
		// Handle color formatting
		if (color)
			s = Color.format(s);
		else
			s = Color.strip(s);
		
		// Output and flush information through the socket
		out.print(s);
		out.flush();
	}
	
	/**
	 * Sends a string followed by a newline to the connection
	 * @param s String to send.
	 */
	public void sendln(String s)
	{
		send(s+"\n");
	}
		
	/**
	 * Continuously collects commands from the user and handles them.
	 */
	public void run()
	{
		setPrompt("Use ANSI Color [Y/N]? ");
		try 
		{
			while (socket.isConnected())
			{
				send(prompt);
				String input = in.readLine();
				if (input != null)
					controller.parse(input);
			}
		}
		catch (IOException ioe)
		{
			close();
		}
	}

	/**
	 * @return the prompt
	 */
	public String getPrompt() {
		return prompt;
	}

	/**
	 * @param prompt the prompt to set
	 */
	public void setPrompt(String prompt) {
		this.prompt = prompt;
	}

	/**
	 * Determines if the connection has a logged in account. 
	 * @return True if the connection has an account, false otherwise.
	 */
	public boolean hasAccount()
	{
		return (account != null);
	}
	
	/**
	 * @return the account
	 */
	public Account getAccount() {
		return account;
	}

	/**
	 * @param account the account to set
	 */
	public void setAccount(Account account) {
		this.account = account;
	}
	
	/**
	 * Returns the internet address from which this connection is connected.
	 * @return The internet address of the connection.
	 */
	public InetAddress getInetAddress()
	{
		return socket.getInetAddress();
	}
	
	/**
	 * Sets whether or not this connection uses ANSI color.
	 * @param c True if the connection is to use color, false otherwise.
	 */
	public void setUseColor(boolean c)
	{
		color = c;
	}
	
	/**
	 * @return the connectionTime
	 */
	public Date getConnectionTime() {
		return connectionTime;
	}
}