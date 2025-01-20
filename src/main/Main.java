package main;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import main.Display.ProgramState;

public class Main {

	public static void main(String[] args) {
		Logger log = Logger.getGlobal();
        Handler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
		log.addHandler(handler);
		
		Display display = new Display(log);
		log.log(Level.INFO, "Display initialzed");
		
		Handler mainWindowHandler = display.displayMainWindow();
		mainWindowHandler.setLevel(Level.INFO);
		log.addHandler(mainWindowHandler);
		
		log.setLevel(Level.INFO);
		
		try (RobotConnection robotConnection = new RobotConnection(log, display)) {
			while (true) {
				try {
				    log.log(Level.INFO, "Beginning client connection");
				    robotConnection.start("localhost", 6001);
			    } catch (Exception e) {
			    	log.log(Level.WARNING, e.toString());
			    	display.setCurrentState(ProgramState.DISCONNECTED);
			    	try {
			    		log.log(Level.FINE, LocalDateTime.now().toString());
			    		Thread.sleep(2000);
			    	} catch (InterruptedException e1) {
			    		e1.printStackTrace();
			    	}
			    }
			}
		}
	}

}
