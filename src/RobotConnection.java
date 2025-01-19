import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Performs TCP connection to the robot. Controller part of MVC, Model is on robot,
 * View is the display object.
 * @author Hale Barber
 */
public class RobotConnection implements AutoCloseable {
	
	private final String GROUP_SELECTION_TERMINATOR = "END_SELECTION";
    private final String RESULTS_TERMINATOR = "END_RESULTS";
    private final String QUESTION_HEADER = "BEGIN_QUESTION";
	
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
		final PrintWriter writer;
		final BufferedReader reader;
	    try {
	        writer = new PrintWriter(server.getOutputStream(), true);
	    } catch (IOException e) {
	        throw new IllegalStateException("Server disconnected");
	    }
	    try {
	        reader = new BufferedReader(new InputStreamReader(server.getInputStream()));
	    } catch (IOException e) {
	        throw new IllegalStateException("Server disconnected");
	    }
	    
	    // Enter main loop
	    while (true) {
	    	try {
				holding(writer, reader);
				running(writer, reader);
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
	    
	}
	
	private void holding(PrintWriter writer, BufferedReader reader) throws IOException {
		ArrayList<String> testGroups = new ArrayList<String>();
		while (true) {
			String message = reader.readLine();
			if (message == GROUP_SELECTION_TERMINATOR) {
				break;
			} else {
				testGroups.add(message);
			}
		}
		
		boolean[] selections = display.showTestGroupSelection(testGroups);
		
		StringBuilder toReply = new StringBuilder();
		for (boolean b : selections) {
			if (b) {
				toReply.append("T");
			} else {
				toReply.append("F");
			}
		}
	}
	
	private void running(PrintWriter writer, BufferedReader reader) {
		
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
