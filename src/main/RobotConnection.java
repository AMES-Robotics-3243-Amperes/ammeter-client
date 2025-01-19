package main;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

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
	private Logger log;

	public RobotConnection(Logger log, Display display) {
		this.log = log;
		this.display = display;
	}
	
	public void start(String host, int port) throws IOException {
		this.server = new Socket(host, port);
		log.log(Level.FINEST, "Socket connected.");
		
		final PrintWriter writer;
		final BufferedReader reader;
	    writer = new PrintWriter(server.getOutputStream(), true);
	    log.log(Level.FINEST, "Writer initialized.");
	    reader = new BufferedReader(new InputStreamReader(server.getInputStream()));
	    log.log(Level.FINEST, "Reader initialized.");
	    
	    log.log(Level.FINE, "Beginning connection logic.");
	    log.log(Level.INFO, "Begin holding.");
		holding(writer, reader);
		log.log(Level.INFO, "Begin running.");
		running(writer, reader);
	}
	
	private void holding(PrintWriter writer, BufferedReader reader) throws IOException {
		
		ArrayList<String> testGroups = new ArrayList<String>();
		while (true) {
			String message = reader.readLine();
			log.log(Level.FINEST, "Test group message recived: " + message);
			if (message.equals(GROUP_SELECTION_TERMINATOR)) {
				log.log(Level.FINER, "Test group selection termination recieved.");
				break;
			} else {
				testGroups.add(message);
			}
		}
		
		log.log(Level.FINE, "Test groups recieved.");
		
		boolean[] selections = display.showTestGroupSelection(testGroups);
		
		log.log(Level.FINE, "Test group selections selected.");
		
		StringBuilder toReply = new StringBuilder();
		for (boolean b : selections) {
			if (b) {
				toReply.append("T");
			} else {
				toReply.append("F");
			}
		}
		
		writer.println(toReply);
		log.log(Level.FINEST, "Sent: " + toReply);
	}
	
	private void running(PrintWriter writer, BufferedReader reader) throws IOException {
		String firstResultsMessage;
		while (true) {
			String message = reader.readLine();
			if (message.equals(QUESTION_HEADER)) {
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
		if (firstLine.equals(RESULTS_TERMINATOR)) {
			done = true;
			log.finer("First line was termination.");
		} else {
			if (firstLine.length() < 2 || !(firstLine.substring(0,2).equals("G:"))) {
				throw new RuntimeException("First line was neither the terminator, nor a test group header.");
			}
			log.finer("First line was: " + firstLine);
			currentTestGroup = new HashMap<String, TestResults>();
			currentTestGroupName = firstLine.substring(2);
		}
		
		// Iterate through the rest.
		while (!done) {
			String line = reader.readLine();
			log.finer("Results line (" + line + ") read.");
			if (line.equals(RESULTS_TERMINATOR)) {
				log.finest("Line detected as termination! Setting done to true.");
				done = true;
			} else {
				if (line.length() > 1) {
					String tag = line.substring(0,2);
				
				    // Test group header
				    if (tag.equals("G:")) {
				    	log.finest("Line detected as group header");
				    	out.put(currentTestGroupName, currentTestGroup);
				    	currentTestGroup = new HashMap<String, TestResults>();
				    	currentTestGroupName = line.substring(2);
				    }
				    // Tests
				    //   Success
				    else if (tag.equals("S:")) {
				    	log.finest("Line detected as test success");
                    	String testMessage = reader.readLine();
                    	log.finest("Test message was: " + testMessage);
                    	currentTestGroup.put(line.substring(2), new TestResults(TestSuccess.SUCCESS, testMessage));
				    }
                    //   Failure
				    else if (tag.equals("F:")) {
				    	log.finest("Line detected as test failure");
                    	String testMessage = reader.readLine();
                    	log.finest("Test message was: " + testMessage);
                    	currentTestGroup.put(line.substring(2), new TestResults(TestSuccess.FAIL, testMessage));
				    }
                    //   Not run
				    else if (tag.equals("N:")) {
				    	log.finest("Line detected as test notrun");
                    	String testMessage = reader.readLine();
                    	log.finest("Test message was: " + testMessage);
                    	currentTestGroup.put(line.substring(2), new TestResults(TestSuccess.NOTRUN, testMessage));
				    }
                    // Something else. Note that test messages are immediately captured after the header is received.
				    else {
					    throw new RuntimeException("Invalid item in results stream.");
				    }
				} else {
				    throw new RuntimeException("Invalid item in results stream.");
			    }
			}
		}
		
		if (currentTestGroupName != null) {
			log.finer("Adding last group (" + currentTestGroupName + ")");
			out.put(currentTestGroupName, currentTestGroup);
		}
		
		log.info("Done determining results.");
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
