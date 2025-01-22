# Ammeter Client
... is the client GUI run on the driver station to interact with Ammeter Live Tester.
The Ammeter Live Tester can be accessed at https://github.com/AMES-Robotics-3243-Amperes/ammeter-live-tester
## What does it do?
The Ammeter Client connects to the TCP server hosted by the Ammeter Live Tester running on an FRC robot, or in simulation. This is used to display relevant information to the user, along with prompts and dialog boxes. The Ammeter Live Tester requires a client program to function.
## Protocol
The protocol used by Ammeter for TCP communication between the client and tester is fairly simple. In fact, 
because of its simplicity, and its use of solely UTF text in communication, a simple TCP connection program 
such as netcat can be used in place of either of end of the connection.

Communications are newline separated. Communication proceeds as follows:
| rio ALT | TCP Communication | Client |
|:--------|:-----------------:|-------:|
| Robot code loaded               |                                | Client started                        |
| Opens TCP listener on port 5809 |                                | Waits for user to command connection  |
|                                 | &#8592; Initial TCP connection | User commanded connection             |
| Holds connection                |                                |                                       |
| Tests start                     | "TestGroup1" &#8594;           |                                       |
|                                 | "TestGroup2" &#8594;           |                                       |
|                                 | "TestGroup3" &#8594;           |                                       |
|                                 | "END_SELECTION" &#8594;        |                                       |
|                                 |                                | Queries user for test group selection |
|                                 | &#8592; "TTF"                  | User gives selection                  |
|                                 | (In order, T for "run this group", F for "don't run this group") |     |
| Begins tests                    |                                |                                       |
| Test requests user input        | "BEGIN_QUESTION" &#8594;       |                                       |
|                                 | "Your question here?" &#8594;  |                                       |
|                                 | "True option" &#8594;          |                                       |
|                                 | "False option" &#8594;         |                                       |
|                                 |                                | Queries user with provided question   |
|                                 | &#8592; "T" or "F"             | User gives selection                  |
|                                 | (Only binary questions are allowed currently) |                        |
| Tests finish                    |                                |                                       |
| Results generated               | "G:TestGroup1" ("G:" followed by group name) &#8594; |                 |
|                                 | "S:SucceedingTest" &#8594;     |                                       |
|                                 | "That test's detail message, if present" &#8594; |                     |
|                                 | "F:FailingTest" &#8594;        |                                       |
|                                 | "" (No detail message) &#8594; |                                       |
|                                 | "G:TestGroup2" (Empty groups are allowed) &#8594; |                    |
|                                 | "G:TestGroup3" &#8594;         |                                       |
|                                 | "N:NotRunTest" &#8594;         |                                       |
|                                 | "Dependencies not correct" (Common detail message) &#8594; |           |
|                                 | "END_RESULTS" &#8594;          |                                       |
|                                 |                                | Displays results to user              |
|                                 |                                | Resets to beginning state             |
| Test mode disabled              |                                |                                       |
| Server shutdown and restarted   |                                |                                       |

## Contributing
Contributions are very welcome! You can contribute by...
* Solving issues or making improvements and submitting a pull request.
* Adding issues for problems you encounter or features you would like.

If you would like to know more about the project, or how you can contribute, contact
hydrogenhone+ammeter@gmail.com or
ames.amperes@gmail.com

Please share any improvements you make! Together we can build better tools for FIRST!
Note that the GNU GPLv3 license that this program is under prohibits the distribution of
closed source versions of the project.

## Planned features
* Improved documentation
* Improved error handling
* Saving test results to file

## Acknowledgements
### Creator
The Ammeter client and Ammeter Live Tester were created by Hale Barber, of team 3243, the AMES Amperes.
### Contributors
### Other Sources
* Thank you to Darryl for publishing the StretchIcon class for Java Swing, which is used by the Ammeter client. (https://tips4java.wordpress.com/2012/03/31/stretch-icon/)
* Thank you to nIcE cOw for giving code and information about formatting with Java Swing's JTextPane. (https://stackoverflow.com/questions/9650992/how-to-change-text-color-in-the-jtextarea)