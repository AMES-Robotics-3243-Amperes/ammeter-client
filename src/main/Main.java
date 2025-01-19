package main;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

	public static void main(String[] args) {
		Logger log = Logger.getGlobal();
        Handler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
		log.addHandler(handler);
		log.setLevel(Level.ALL);
		
		Display display = new Display(log);
		log.log(Level.INFO, "Display initialzed");
		try (RobotConnection robotConnection = new RobotConnection(log, display, "localhost", 6001)) {
			log.log(Level.INFO, "Beginning client connection");
			robotConnection.start();
		}
	}

}
