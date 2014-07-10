package priviot.utils.communication.tcp;

import java.io.*;
import java.net.UnknownHostException;

/**
 * The TCPClient encapsulates the TCP communication.
 */
public class TCPClient implements Runnable {
	
	private java.net.Socket socket;
	private boolean isConnected = false;
	
	private boolean outputDebugMessages = false;
	private boolean outputDebugConnections = false;
	
	public TCPClient(String ip, int port) {
		try {
			socket = new java.net.Socket(ip, port);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		if (outputDebugConnections) System.out.println("TCPClient: connected to " + socket.getRemoteSocketAddress());
		
		isConnected = true;
	}
	
	public void setDebug(boolean outputDebugMessages, boolean outputDebugConnections) {
		this.outputDebugMessages = outputDebugMessages;
		this.outputDebugConnections =  outputDebugConnections;
	}
	
	/**
	 * Sends a message and returns the response.
	 * @param message Message to send
	 * @return response
	 * @throws IOException 
	 */
	public byte[] sendMessage(byte[] message) throws IOException {
		if (!isConnected) {
			System.out.println("TCPClient: Error in sendMessage(): not connected");
			return null;
		}
		
		if (outputDebugMessages) System.out.println("TCPClient: send message: '" + new String(message) + "'");
		
		PrintWriter printWriter =
		 	    new PrintWriter(
		 	 	new OutputStreamWriter(
		 		    socket.getOutputStream()));
		char[] msg = new char[message.length];
 	 	for (int i = 0; i < message.length; i++) {
 	 		msg[i] = (char)(message[i]);
 	 	}
	 	printWriter.print(msg);
	 	printWriter.flush();
	 	
	 	BufferedReader bufferedReader =
	 	 	    new BufferedReader(
	 	 		new InputStreamReader(
	 	 	  	    socket.getInputStream()));
	 	
 	 	char[] buffer = new char[1000];
 	 	int charCount = bufferedReader.read(buffer, 0, 1000);
 	 	byte[] response = new byte[charCount];
 	 	for (int i = 0; i < charCount; i++) {
 	 		response[i] = (byte)(buffer[i]);
 	 	}
 	 	
 	 	if (outputDebugMessages) System.out.println("TCPClient: received response: '" + new String(response) + "'");
 	 	
 	 	return response;
	}
	
	public void disconnect() {
		try {
			Thread.sleep(10);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
			return;
		}
		
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		if (outputDebugConnections) System.out.println("TCPClient: connection closed to " + socket.getRemoteSocketAddress());
		
		isConnected = false;
	}

	public void setOutputDebugMessages(boolean value) {
		outputDebugMessages = value;
	}
 	
 	public void setOutputDebugConnections(boolean value) {
		outputDebugConnections = value;
	}
	
	@Override
	public void run() {
		
	}
	
	
}
