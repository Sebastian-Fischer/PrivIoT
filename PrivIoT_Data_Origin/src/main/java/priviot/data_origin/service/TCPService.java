package priviot.data_origin.service;

public class TCPService extends Service {
	
	public TCPService() {
		
	}

	@Override
	protected void receivedSensorData() {
		System.out.println("TCPService: received new data set: " + actualData.toString());
		//TODO: publish actualData
	}
	
	
}
