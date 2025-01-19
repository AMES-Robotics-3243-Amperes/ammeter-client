import java.io.IOException;
import java.net.Socket;

/**
 * Performs TCP connection to the robot. Controller part of MVC, Model is on robot,
 * View is the display object.
 * @author Hale Barber
 */
public class RobotConnection implements AutoCloseable {
	private Display display;
	private Socket server;

	public RobotConnection(Display display, String host, int port) {
		this.display = display;
		try {
			this.server = new Socket(host, port);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void start() {
		
	}

	@Override
	public void close() {
		try {
			server.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
