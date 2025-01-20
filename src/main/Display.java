package main;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.DimensionUIResource;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.html.HTMLDocument;

public class Display {

	private Logger log;
    private final String imgUrl = "/img";
    private ImageIcon[] flowcharts;
	public Display(Logger log) {
		flowcharts = new ImageIcon[6];
		for (int i = 0; i < flowcharts.length; i++) {
			flowcharts[i] = new StretchIcon(getClass().getResource(imgUrl + "/AmmeterClientFlowchart" + i + ".jpg"));
		}
		this.log = log;
	}
	
	public static enum TestSuccess {
		NOTRUN,
		FAIL,
		SUCCESS;
	}

	public static TestSuccess testSuccessFromBool(Boolean bool) {
		if (bool == true) {
			return TestSuccess.SUCCESS;
		} else if (bool == false) {
			return TestSuccess.FAIL;
		} else {
			return TestSuccess.NOTRUN;
		}
	}

	/**
	 * Stores the results of a test, that being whether it succeeded and any other message
	 * it may have provided.
	 * 
	 * @author H!
	 */
	public static class TestResults {
		public TestSuccess m_successResult;
		public String m_message;

		public TestResults(TestSuccess successResult, String message) {
			m_successResult = successResult;
			m_message = message;
		}

		public TestResults(TestSuccess successResult) {
			this(successResult, "");
		}
	}
	
	private class TextPaneConsole extends Handler {
		
		private JTextPane textPane;
		
		public TextPaneConsole(JTextPane textPane) {
			this.textPane = textPane;
		}

		@Override
		public void publish(LogRecord record) {
		    appendToPane(
		    	textPane, 
		    	record.getLevel().getLocalizedName(), 
		    	colorFromLevel(record.getLevel())
		    );
		    String fullMillisTime = Long.toString(record.getMillis());
		    appendToPane(
			    textPane, 
			    "\t:" + fullMillisTime.substring(fullMillisTime.length() - 6) + ":\t", 
			    new Color(0, 0, 0)
			);
		    appendToPane(
				textPane, 
				record.getMessage() + "\n", 
				new Color(50, 50, 50)
			);
		}

		@Override
		public void flush() {}

		@Override
		public void close() throws SecurityException {}
	}
	
	public Handler displayMainWindow() {
		// Initialize elements
		JFrame frame = new JFrame();
		
		GridBagConstraints constraints = new GridBagConstraints();
		
		frame.setLayout(new GridLayout(1, 2));
		
		Container leftPane = new Container();
		leftPane.setLayout(new GridBagLayout());
		
		Container leftUpperPane = new Container();
		GroupLayout leftUpperLayout = new GroupLayout(leftUpperPane);
		leftUpperPane.setLayout(leftUpperLayout);
		
		JLabel targetLabel = new JLabel("Target:");
		JLabel targetNotes = new JLabel("<html>Target describes the host that will be connected to. For simulation, this is 'localhost'. For regular control, this is either 10.##.##.2 or 'roboRIO-####-FRC.local', where #### is your team number.</html>");
		JLabel portLabel = new JLabel("Port:");
		JLabel portNotes = new JLabel("The port is normally 6001.");
		
		JTextField targetField = new JTextField("localhost");
		JTextField portField = new JTextField("6001");
		
		Container leftLowerPane = new Container();
		leftLowerPane.setLayout(new GridLayout(1, 2));
		JButton connectButton = new JButton("Connect");
		JToggleButton autoToggleButton = new JToggleButton("Auto connect");
		
		Container rightPane = new Container();
		rightPane.setLayout(new GridBagLayout());
		
		Container rightUpperPane = new Container();
		rightUpperPane.setLayout(new BoxLayout(rightUpperPane, BoxLayout.Y_AXIS));
		
		JLabel connectionDisplay = new JLabel("Disconnected", JLabel.CENTER);
		JLabel statusImage = new JLabel(flowcharts[0], JLabel.CENTER);
		
		ScrollPane rightLowerPane = new ScrollPane();
		
		JTextPane console = new JTextPane();
		
		// Configure elements
		targetField.setPreferredSize(new Dimension(100, 10));
		portField.setPreferredSize(new Dimension(100, 10));
		targetField.setMaximumSize(new Dimension(99999, 10));
		portField.setMaximumSize(new Dimension(99999, 10));
		
		console.setBorder(new EmptyBorder(new Insets(5, 5, 5, 5)));
		
		rightLowerPane.setPreferredSize(new Dimension(400, 400));
		
		statusImage.setMinimumSize(new Dimension(1896 / 4, 481 / 4));
		statusImage.setPreferredSize(new Dimension(1896 / 4, 481 / 4));
		statusImage.setMaximumSize(new Dimension(18960, 4810));
		
		
		
		// Glue elements
		// TODO
		leftUpperLayout.setVerticalGroup(
			leftUpperLayout.createSequentialGroup()
			    .addGroup(leftUpperLayout.createParallelGroup()
			    	.addComponent(targetLabel)
			    	.addComponent(targetField))
			    .addComponent(targetNotes)
			    .addGroup(leftUpperLayout.createParallelGroup()
				    .addComponent(portLabel)
				    .addComponent(portField))
			    .addComponent(portNotes)
		);
		leftUpperLayout.setHorizontalGroup(
			leftUpperLayout.createSequentialGroup()
			    .addGroup(leftUpperLayout.createParallelGroup()
			    	.addComponent(targetLabel)
			    	.addComponent(targetNotes)
			    	.addComponent(portLabel)
			    	.addComponent(portNotes))
			    .addGroup(leftUpperLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
				    .addComponent(targetField)
				    .addComponent(portField))
		);
		
		leftLowerPane.add(connectButton);
		
		leftLowerPane.add(autoToggleButton);
		
		constraints.fill = GridBagConstraints.BOTH;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1;
		constraints.weighty = 9;
		leftPane.add(leftUpperPane, constraints);
		
		constraints.fill = GridBagConstraints.BOTH;
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.weightx = 1;
		constraints.weighty = 1;
		leftPane.add(leftLowerPane, constraints);
		
		rightUpperPane.add(connectionDisplay);
		rightUpperPane.add(statusImage);
		
		rightLowerPane.add(console);
		
		constraints.fill = GridBagConstraints.BOTH;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1;
		constraints.weighty = 1;
		rightPane.add(rightUpperPane, constraints);
		
		constraints.fill = GridBagConstraints.BOTH;
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.weightx = 1;
		constraints.weighty = 4;
		rightPane.add(rightLowerPane, constraints);
		
		frame.add(leftPane, constraints);
		
		frame.add(rightPane, constraints);
		
		frame.pack();
		
		frame.setVisible(true);
		
		return new TextPaneConsole(console);
	}
	
	/** The format used to generate the HTML group headers in the test results @author H! */
	protected static final String groupFormat = "<li><h2 class='%2$s'>%1$s | %3$s %4$s </h2> <p><em class='success'>%5$d/%7$d/%8$d Success</em> | <em class='fail'>%6$d/%7$d/%8$d Fails</em></p><ul></ul></li>";
	/**
	 * A utility method for getting the proper HTML to make a subsystem test result wrapper be displayed
	 * 
	 * @param resultEntry One entry of the map corresponding to the test group to display
	 * @return A {@link String} with the HTML in plaintext representing the group header
	 * 
	 * @author H!
	 */
	protected String getGroupHTMLElement(Entry<String, Map<String, TestResults>> resultEntry) {
		int successCount = 0;
		int performedCount = 0;
		int totalCount = resultEntry.getValue().size();

		for (TestResults testResult : resultEntry.getValue().values()) {
			if (testResult.m_successResult == TestSuccess.SUCCESS) {
				successCount++;
				performedCount++;
			} else if (testResult.m_successResult != TestSuccess.NOTRUN) {
				performedCount++;
			}
		}

		return String.format(
			groupFormat, 
			resultEntry.getKey(), 
			successCount == performedCount ? "success" : "fail",
			successCount == performedCount ? "✔" : "✗",
			performedCount == totalCount ? "" : "*",
			successCount,
			performedCount - successCount,
			performedCount,
			totalCount
		);
	}

	/** The format used to generate the HTML for individual tests in the test results @author H! */
	protected static final String testFormat = "<li><h3 class='%2$s'>%1$s | %3$s</h3>%4$s</li>";
	/**A utility method for getting the proper HTML to make a test result be displayed
	 * 
	 * @param testEntry One entry of the map corresponding to the test to display
	 * @return A {@link String} with the HTML in plaintext representing the test result
	 * 
	 * @author H!
	 */
	protected static String getTestHTMLFormat(Entry<String, TestResults> testEntry) {
		String cssClass = "";
		String resultIcon = "";
		if		(testEntry.getValue().m_successResult == TestSuccess.SUCCESS) {
			cssClass = "success";
			resultIcon = "✔";
		} else if (testEntry.getValue().m_successResult == TestSuccess.FAIL) {
			cssClass = "fail";
			resultIcon = "✗";
		} else if (testEntry.getValue().m_successResult == TestSuccess.NOTRUN) {
			cssClass = "notRun";
			resultIcon = "-";
		}

		return String.format(
			testFormat, 
			testEntry.getKey(), 
			cssClass,
			resultIcon,
			testEntry.getValue().m_message.equals("") ? "" : "<p>" + testEntry.getValue().m_message + "</p>"
		);
	}

	/**Displays the latest results of the integrated tests in a Swing dialog
	 * @author H!
	 */
	public void displayTestResults(Map<String, Map<String, TestResults>> results) {
		JTextPane textPane = new JTextPane();
		textPane.setContentType("text/html");
		textPane.setText("<!DOCTYPE html><html><head><style>.fail{color:red}.success{color:green}.notRun{color:grey}</style></head><body><h1>Integrated Test Results</h1><ul id=testGroupList></ul></body></html>");
		HTMLDocument doc = (HTMLDocument) textPane.getDocument();

		for (Entry<String, Map<String, TestResults>> groupEntry : results.entrySet()) {
			try {
				doc.insertAfterStart(doc.getElement("testGroupList"), getGroupHTMLElement(groupEntry));
				
				for (Entry<String, TestResults> testResultEntry : groupEntry.getValue().entrySet()) {
					doc.insertAfterStart(doc.getElement("testGroupList").getElement(0).getElement(2), getTestHTMLFormat(testResultEntry));
				}


			} catch (Exception e) {
				System.err.println("Test result display generation failed:");
				e.printStackTrace();
			}
		}
		
		JScrollPane scrollPane = new JScrollPane(textPane);

		
		scrollPane.setPreferredSize(new DimensionUIResource(
			600, 
			500
		));
		
		JOptionPane.showMessageDialog(
			new JTextPane(), 
			scrollPane,
			"Integrated Test Results",
			JOptionPane.PLAIN_MESSAGE
		);
	}


	public boolean[] showTestGroupSelection(List<String> testGroupNames) {
		// Root component
		JFrame frame = new JFrame();
		frame.setLayout(new GridBagLayout());
		// Constraints to be used to arrange the button and list properly
		GridBagConstraints constraints = new GridBagConstraints();
																																																																																																																											
		// Holds the checkboxes
		Container groupList = new Container();
		groupList.setLayout(new GridLayout(testGroupNames.size(), 1));
		ScrollPane groupListPane = new ScrollPane();
		groupListPane.add(groupList);
		// Add all checkboxes
		for (String name : testGroupNames) {
			JCheckBox checkBox = new JCheckBox(name, true);
			groupList.add(checkBox);
		}
		
		boolean[] selections = new boolean[testGroupNames.size()];

		// Confirm button
		CompletableFuture<Boolean> isDone = new CompletableFuture<>();
		JButton button = new JButton("Start Tests");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.log(Level.FINEST, "User test group selection button clicked.");
				for (int i = 0; i < groupList.getComponentCount(); i++) {
					selections[i] = ((JCheckBox) groupList.getComponent(i)).isSelected();
				}
				frame.dispose();
				isDone.complete(true);
			}
		});
		
		// Assemble components into frame
		constraints.fill = GridBagConstraints.BOTH;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1;
		constraints.weighty = 9;
		frame.add(groupListPane, constraints);

		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.weightx = 1;
		constraints.weighty = 1;
		frame.add(button, constraints);

		frame.pack();

		frame.setVisible(true);
		
		log.log(Level.FINEST, "User test group selection displayed.");
		
		isDone.join();
		
		log.log(Level.FINEST, "User test group selection returned.");
		
		return selections;
	}
	
	public Boolean askUserQuestion(String question, String yesOption, String noOption) {
		Object[] options = {
			yesOption,
			noOption
		};

		switch (JOptionPane.showOptionDialog(new JFrame(), question, "Test Input", 2, JOptionPane.QUESTION_MESSAGE, null, options, options[0])) {
			case (JOptionPane.CLOSED_OPTION):
				 return null;
				
			case 0:
				return true;
				
			case 1:
				return false;
			
			default:
				return null;
		}
	}
	
	
	// Thank you to nIcE cOw (https://stackoverflow.com/questions/9650992/how-to-change-text-color-in-the-jtextarea)
	private static void appendToPane(JTextPane tp, String msg, Color c)
    {
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);

        aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Lucida Console");
        aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_LEFT);

        int len = tp.getDocument().getLength();
        tp.setCaretPosition(len);
        tp.setCharacterAttributes(aset, false);
        tp.replaceSelection(msg);
    }
	
	private static Color colorFromLevel(Level level) {
		if (level.intValue() >= Level.SEVERE.intValue()) {
			return new Color(128, 10, 10);
		}
		if (level.intValue() >= Level.WARNING.intValue()) {
			return new Color(200, 130, 10);
		}
		if (level.intValue() >= Level.INFO.intValue()) {
			return new Color(130, 180, 130);
		}
		return new Color(65, 65, 65);
	}

}
