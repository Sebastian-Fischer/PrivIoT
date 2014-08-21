package priviot.client;

/**
 * Controls the work of the Client.
 */
public class Controller {
	
	private String sensorUri;
	private byte[] sensorSecret;
	private int sensorUpdateInterval;
	private String sspHttpUri;
	private int sspHttpPort;
	
	private Thread worker;

	/**
	 * Constructor.
	 * 
	 * @param sensorUri
	 * @param sensorSecret
	 * @param sensorUpdateInterval
	 * @param sspHttpUri
	 * @param sspHttpPort
	 */
	public Controller(String sensorUri, byte[] sensorSecret, int sensorUpdateInterval, String sspHttpUri, int sspHttpPort) {
		this.sensorUri = sensorUri;
		this.sensorSecret = sensorSecret;
		this.sensorUpdateInterval = sensorUpdateInterval;
		this.sspHttpUri = sspHttpUri;
		this.sspHttpPort = sspHttpPort;
		
		//TODO: need HTTP client
	}
	
	/**
	 * Let the Controller start.
	 */
	public void start() {
		worker = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					requestStatus();
				}
			}
		});
	}
	
	/**
	 * Requests the status of the specified sensor from the Smart Service Proxy.
	 */
	private void requestStatus() {
		//TODO: create actual pseudonym
		
		//TODO: send http request to ssp
	}
}
