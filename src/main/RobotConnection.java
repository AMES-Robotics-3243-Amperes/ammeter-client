package main;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import main.Display.TestResults;
import main.Display.TestSuccess;

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
	
	private void running(PrintWriter writer, BufferedReader reader) throws IOException {
		String firstResultsMessage;
		while (true) {
			String message = reader.readLine();
			if (message == QUESTION_HEADER) {
				// Ignore header and process rest of question.
				processQuestion(writer, reader);
			} else {
				// Store first message and move to results phase.
				firstResultsMessage = message;
				break;
			}
		}
		
		var results = processResultsSection(writer, reader, firstResultsMessage);
		display.displayTestResults(results);
	}
	
    private void processQuestion(PrintWriter writer, BufferedReader reader) throws IOException {
		String question = reader.readLine();
		String yesOption = reader.readLine();
		String noOptions = reader.readLine();
		
		boolean result = display.askUserQuestion(question, yesOption, noOptions);
		
		if (result) {
			writer.println("T");
		} else {
			writer.println("F");
		}
	}

	
	private HashMap<String, Map<String, TestResults>> processResultsSection(PrintWriter writer, BufferedReader reader, String firstLine) throws IOException {
		
		HashMap<String, Map<String, TestResults>> out = new HashMap<String, Map<String, TestResults>>();
		boolean done = false;
		
		HashMap<String, TestResults> currentTestGroup = null;
		String currentTestGroupName = null;

		// Handle special case with first line
		if (firstLine == RESULTS_TERMINATOR) {
			done = true;
		} else {
			if (firstLine.substring(0,2) != "G:") {
				throw new RuntimeException("First line was neither the terminator, nor a test group header.");
			}
			currentTestGroup = new HashMap<String, TestResults>();
			currentTestGroupName = firstLine.substring(2);
		}
		
		// Iterate through the rest.
		while (!done) {
			String line = reader.readLine();
			if (line == RESULTS_TERMINATOR) {
				done = true;
			} else {
				switch (line.substring(0,2)) {
				    // Test group header
				    case "G:" -> {
				    	out.put(currentTestGroupName, currentTestGroup);
				    	currentTestGroup = new HashMap<String, TestResults>();
				    	currentTestGroupName = line.substring(2);
				    	break;
				    }
				    // Tests
				    //   Success
                    case "S:" -> {
                    	String testMessage = reader.readLine();
                    	currentTestGroup.put(line.substring(2), new TestResults(TestSuccess.SUCCESS, testMessage));
				    	break;
				    }
                    //   Failure
                    case "F:" -> {
                    	String testMessage = reader.readLine();
                    	currentTestGroup.put(line.substring(2), new TestResults(TestSuccess.FAIL, testMessage));
				    	break;
				    }
                    //   Not run
                    case "N:" -> {
                    	String testMessage = reader.readLine();
                    	currentTestGroup.put(line.substring(2), new TestResults(TestSuccess.NOTRUN, testMessage));
				    	break;
				    }
                    // Something else. Note that test messages are immediately captured after the header is received.
				    default -> {
					    throw new RuntimeException("Invalid item in results stream.");
				    }
				}
			}
		}
		
		if (currentTestGroupName != null) {
			out.put(currentTestGroupName, currentTestGroup);
		}
		
		return out;
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
