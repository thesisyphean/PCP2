import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/*
 This is a special Thread class that serves drinks to the patrons at the club
 */

public class Andre extends Thread {

	public static ClubGrid club; // shared club

	GridBlock currentBlock;
	private PeopleLocation myLocation;
	// this controls which way he is moving
	private int direction = 1;
	// Andre always wants to leave the club...
	private boolean wantToLeave = true;

	// these enable waiting to start and pausing
	AtomicBoolean paused;
	CountDownLatch start;

	Andre(PeopleLocation loc, int speed,
			AtomicBoolean paused, CountDownLatch start, GridBlock gb) {
		myLocation = loc; // for easy lookups

		this.paused = paused;
		this.start = start;

		// everything ClubGrid normally does in enterClub
		// ensures that the location and gridblock match
		try {gb.get(myLocation.getID());} catch (InterruptedException e) {};
		myLocation.setLocation(gb);
		currentBlock = gb;
	}

	// getter
	public int getX() {
		return currentBlock.getX();
	}

	// getter
	public int getY() {
		return currentBlock.getY();
	}

	// check to see if user pressed pause button
	private void checkPause() {
		// Everytime this is called, if paused is true,
		// the thread will wait to be notified of its change
		synchronized (paused) {
			try {
				while (paused.get()) {
					paused.wait();
				}
			} catch (InterruptedException e) {
			}
		}
	}

	private void startSim() {
		// The thread waits for the latch to be opened by the main thread
		try {
			start.await();
		} catch (InterruptedException e) {
		}
	}

	private void move() {
		// If he hits the end of the club, he turns around
		if (!club.inGrid(currentBlock.getX() + direction, currentBlock.getY())) {
			direction *= -1;
		}

		// He moves back and forth against the bar
		try {
			currentBlock = club.move(currentBlock, direction, 0, myLocation);
		} catch (InterruptedException e) {
		}
	}

	private void serveDrink() {
		// if there's anyone in front of him, he serves them
		GridBlock opposite = club.whichBlock(currentBlock.getX(), currentBlock.getY() - 1);
		// This allows the patron to move from the bar since they have been served
		synchronized (opposite) {
			opposite.notifyAll();
		}
	}

	public void run() {
		startSim();
		checkPause();

		// He continually moves and serves
		while (true) {
			checkPause(); // check every step

			move();
			serveDrink();

			try {
				sleep(400);
			} catch (InterruptedException e) {}
		}
	}

}
