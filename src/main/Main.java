package main;

public class Main {

	public static void main(String[] args) {
		Display display = new Display();
		try (RobotConnection robotConnection = new RobotConnection(display, "localhost", 6001)) {
			robotConnection.start();
		}
	}

}
