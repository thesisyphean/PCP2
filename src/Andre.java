//M. M. Kuttel 2023 mkuttel@gmail.com

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/*
 This is the basic ClubGoer Thread class, representing the patrons at the club
 */

public class Andre extends Thread {

	public static ClubGrid club; // shared club

	GridBlock currentBlock;
	private int movingSpeed;

	private PeopleLocation myLocation;
	private int direction = 1;
	// Andre always wants to leave the club...
	private boolean wantToLeave = true;

	AtomicBoolean paused;
	CountDownLatch start;

	Andre(PeopleLocation loc, int speed,
			AtomicBoolean paused, CountDownLatch start, GridBlock gb) {
		movingSpeed = speed; // range of speeds for customers
		this.myLocation = loc; // for easy lookups

		this.paused = paused;
		this.start = start;

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

	// getter
	public int getSpeed() {
		return movingSpeed;
	}

	// check to see if user pressed pause button
	private void checkPause() {
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
		try {
			start.await();
		} catch (InterruptedException e) {
		}
	}

	private void move() {
		if (!club.inGrid(currentBlock.getX() + direction, currentBlock.getY())) {
			direction *= -1;
		}

		try {
			currentBlock = club.move(currentBlock, direction, 0, myLocation);
		} catch (InterruptedException e) {
		}
	}

	private void serveDrink() {
		// if there's anyone in front of him, he serves them
		GridBlock opposite = club.whichBlock(currentBlock.getX(), currentBlock.getY() - 1);
		synchronized (opposite) {
			opposite.notifyAll();
		}
	}

	public void run() {
		startSim();
		checkPause();
		// myLocation.setArrived();

		while (true) {
			checkPause(); // check every step

			move();
			serveDrink();

			try {
				sleep(500);
			} catch (InterruptedException e) {}
		}
	}

}
