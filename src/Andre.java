//M. M. Kuttel 2023 mkuttel@gmail.com

import java.util.Random;
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
			AtomicBoolean paused, CountDownLatch start) {
		movingSpeed = speed; // range of speeds for customers
		this.myLocation = loc; // for easy lookups

		this.paused = paused;
		this.start = start;
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
			club.move(currentBlock, direction, 0, myLocation);
		} catch (InterruptedException e) {
		}
	}

	private void serveDrink() {
		// TODO
	}

	public void run() {
		startSim();
		checkPause();
		// myLocation.setArrived();

		while (true) {
			checkPause(); // check every step

			move();
			serveDrink();
		}
	}

}
