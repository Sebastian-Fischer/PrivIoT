package priviot.data_origin.service;

import priviot.utils.communication.tcp.TCPServer;
import priviot.utils.communication.tcp.TCPServerHandler;

public class TCPService extends Service implements TCPServerHandler {
	
	/** TCP Message: Register as observer */
	private final byte[] MSG_OBSERVE = {1, 2, 3};
	/** TCP Message: Get actual data set */
	private final byte[] MSG_GET = {1, 2, 3, 4};
	
	/** encapsulates the TCP communication */
	TCPServer tcpServer;
	
	/** Thread in which the tcpServer runs as Runnable */
	Thread tcpServerThread;
	
	/**
	 * Constructor.
	 * @param tcpPort The TCP port where the server should listen.
	 */
	public TCPService(int tcpPort) {
		tcpServer = new TCPServer(tcpPort);
		
		tcpServer.setTCPServerHandler(this);
	}

	@Override
	public void startService() {
		tcpServerThread = new Thread(tcpServer);
		
		tcpServerThread.start();
	}
	
	@Override
	protected void receivedSensorData() {
		System.out.println("TCPService: received new data set: " + actualData.toString());
		//TODO: publish actualData
	}
	
	@Override
	public byte[] handleMessage(byte[] message) {
		System.out.println("Received message");
		
		if (MSG_OBSERVE.equals(message)) {
			System.out.println("It's an Observe Message");
		}
		else if (MSG_GET.equals(message)) {
			System.out.println("It's a Get Message");
		}
		else {
			System.out.println("It's an unknown command");
		}
		
		return null;
	}
	
}
