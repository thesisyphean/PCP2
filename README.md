# PCP2

### About
This program simulates an artificial club with patrons, a dance floor, a bar, and a bartender. This involves a lot of concurrent programming as patrons and the bartender are all seperate threads. The program abides by the following behaviour rules:
* The Start button initiates the simulation.
* The Pause button pauses/resumes the simulation.
* The Quit button terminates the simulation (it does).
* Patrons enter through the entrance door and exit through the exit doors.
* The entrance and exit doors are accessed by one patron at a time.
* The maximum number of patrons inside the club must not exceed a specified limit.
* Patrons must wait if the club limit is reached or the entrance door is occupied.
* Inside the club, patrons maintain a realistic distance from each other (one per grid block).
* Patrons move block by block and simultaneously to ensure liveness.
* The simulation must be free from deadlocks.

### Running
A Makefile has been provided to ease the running of the program. `make run` can be used to compile and run the program, and `make clean` can be used to remove the compiled classes.

### Notes
Swing sometimes causes issues with certain window managers. If you get a black white screen when running the program, you might need to set the following variable in your terminal: `_JAVA_AWT_WM_NONREPARENTING=1`.