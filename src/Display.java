import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.plaf.DimensionUIResource;
import javax.swing.text.html.HTMLDocument;

public class Display {

	public Display() {
		// TODO Auto-generated constructor stub
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
        if        (testEntry.getValue().m_successResult == TestSuccess.SUCCESS) {
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


    public void showTestGroupSelection(List<String> testGroupNames) {
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

        // Confirm button
        JButton button = new JButton("Start Tests");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO
                frame.dispose();
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
    }

}
