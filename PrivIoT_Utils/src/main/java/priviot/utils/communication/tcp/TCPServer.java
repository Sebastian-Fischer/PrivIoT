package priviot.utils.communication.tcp;

import java.io.*;
import java.net.InetSocketAddress;

/**
 * The TCPServer encapsulates the TCP communication.
 * 
 * A class that uses the TCPServer has to implement the interface TCPServerHandler.
 * To register a TCPServerHandler, call setTCPServerHandler.
 * To start the Server create a new thread with the TCPServer object as Runnable and start the thread.
 */
public class TCPServer implements Runnable {
	
 	private java.net.ServerSocket serverSocket;
 	
 	private boolean outputDebugMessages = false;
 	private boolean outputDebugConnections = false;
 	
 	private TCPServerHandler tcpServerHandler;
	
 	/**
 	 * Constructor
 	 * @param port Local TCP port to open the server
 	 */
 	public TCPServer(int port) { 		
 		try {
			serverSocket = new java.net.ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
 		
 		System.out.println("TCPServer: opened Server at port " + port);
 	}
 	
 	/** Sets the debug flags */
 	public void setDebug(boolean outputDebugMessages, boolean outputDebugConnections) {
		this.outputDebugMessages = outputDebugMessages;
		this.outputDebugConnections =  outputDebugConnections;
	}
 	
 	/** Sets the handler for tcp messages */
 	public void setTCPServerHandler(TCPServerHandler tcpServerHandler) {
 		this.tcpServerHandler = tcpServerHandler;
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
			
			if (outputDebugConnections) System.out.println("TCPServer: got connection to " + clientSocket.getRemoteSocketAddress());
			
			while (!clientSocket.isClosed()) {
				try {
					message = getMessage(clientSocket);
				} catch(IOException e) {
					e.printStackTrace();
					return;
				}
				
				if (message == null) {
					if (outputDebugConnections) System.out.println("TCPServer: Connection closed by client: " + clientSocket.getRemoteSocketAddress());
					break;
				}
				
				String messageStr = new String(message);
				if (outputDebugMessages) System.out.println("TCPServer: received Message: '" + messageStr + "'");
				
				InetSocketAddress remoteAddress = (InetSocketAddress)(clientSocket.getRemoteSocketAddress());
				byte[] response = handleMessage(message, remoteAddress.getAddress().getHostAddress(), remoteAddress.getPort());
				
				
				if (response != null) {
					if (outputDebugMessages) System.out.println("TCPServer: send Response: '" + new String(response) + "'");
					
					try {
						sendMessage(response, clientSocket);
					} catch (IOException e) {
						e.printStackTrace();
						return;
					}
				}
			}
			
			if (outputDebugConnections) System.out.println("TCPServer: connection closed to '" + clientSocket.getRemoteSocketAddress() + "'");
		}
		
	}
	
	/**
 	 * Handles the incomming message and generates a response.
 	 * @param message Incomming
 	 * @return        Response
 	 */
 	protected byte[] handleMessage(byte[] message, String clientAddress, int clientPort) {
 		if (tcpServerHandler != null) {
 			return tcpServerHandler.handleMessage(message, clientAddress, clientPort);
 		}
 		else {
 			return null;
 		}
 	}

}
