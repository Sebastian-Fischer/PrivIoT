package priviot.utils.communication.tcp;

import java.io.*;

public class Server implements Runnable {
	
 	private java.net.ServerSocket serverSocket;
 	
 	private boolean outputDebugMessages = false;
 	private boolean outputDebugConnections = false;
	
 	public Server(int port) { 		
 		try {
			serverSocket = new java.net.ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
 		
 		System.out.println("Server: opened Server at port " + port);
 	}
 	
 	public void setDebug(boolean outputDebugMessages, boolean outputDebugConnections) {
		this.outputDebugMessages = outputDebugMessages;
		this.outputDebugConnections =  outputDebugConnections;
	}
 	
 	
 	private byte[] getMessage(java.net.Socket socket) throws IOException {
 		BufferedReader bufferedReader = 
 		 	    new BufferedReader(
 		 	 	new InputStreamReader(
 		 		    socket.getInputStream()));
 		 	char[] buffer = new char[1000];
 		 	int charCount = bufferedReader.read(buffer, 0, 1000); // blockiert bis Nachricht empfangen
 		 	
 		 	if (charCount > 0) {
 		 		byte[] response = new byte[charCount];
 	 	 	 	for (int i = 0; i < charCount; i++) {
 	 	 	 		response[i] = (byte)(buffer[i]);
 	 	 	 	}
 		 		return response;
 		 	}
 		 	else {
 		 		return null;
 		 	}
 	}
 	
 	private void sendMessage(byte[] message, java.net.Socket socket) throws IOException {
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
 	}
 	
 	/**
 	 * Handles the incomming message and generates a response.
 	 * @param message Incomming
 	 * @return        Response
 	 */
 	protected byte[] handleMessage(byte[] message) {
 		return null;
 	}
 	
 	public void setOutputDebugMessages(boolean value) {
		outputDebugMessages = value;
	}
 	
 	public void setOutputDebugConnections(boolean value) {
		outputDebugConnections = value;
	}
 	
	@Override
	public void run() {
		while (true) {
			java.net.Socket clientSocket = null;
			byte[] message;
			
			try {
				clientSocket = serverSocket.accept();
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
			
			if (outputDebugConnections) System.out.println("Server: got connection to " + clientSocket.getRemoteSocketAddress());
			
			while (!clientSocket.isClosed()) {
				try {
					message = getMessage(clientSocket);
				} catch(IOException e) {
					e.printStackTrace();
					return;
				}
				
				if (message == null) {
					if (outputDebugConnections) System.out.println("Server: Connection closed by client: " + clientSocket.getRemoteSocketAddress());
					break;
				}
				
				String messageStr = new String(message);
				if (outputDebugMessages) System.out.println("Server: received Message: '" + messageStr + "'");
				
				byte[] response = handleMessage(message);
				
				
				if (response != null) {
					if (outputDebugMessages) System.out.println("Server: send Response: '" + new String(response) + "'");
					
					try {
						sendMessage(response, clientSocket);
					} catch (IOException e) {
						e.printStackTrace();
						return;
					}
				}
			}
			
			if (outputDebugConnections) System.out.println("Server: connection closed to '" + clientSocket.getRemoteSocketAddress() + "'");
		}
		
	}

}
