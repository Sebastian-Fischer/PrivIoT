package priviot.utils.communication.tcp;

public interface TCPServerHandler {
	/**
 	 * Handles an incomming message from a client and generates a response.
 	 * @param message Incomming
 	 * @return        Response
 	 */
 	byte[] handleMessage(byte[] message);
}
