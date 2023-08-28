//M. M. Kuttel 2023 mkuttel@gmail.com

import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/*
 This is the basic ClubGoer Thread class, representing the patrons at the club
 */

public class Clubgoer extends Thread {

	public static ClubGrid club; // shared club

	GridBlock currentBlock;
	private Random rand;
	private int movingSpeed;

	private PeopleLocation myLocation;
	private boolean inRoom;
	private boolean thirsty;
	private boolean wantToLeave;

	private int ID; // thread ID
	// Paused allows all threads to poll whether the simulation is paused
	AtomicBoolean paused;
	// The start latch allows them to wait until the latch is opened to start
	CountDownLatch start;

	Clubgoer(int ID, PeopleLocation loc, int speed,
			AtomicBoolean paused, CountDownLatch start) {
		this.ID = ID;
		movingSpeed = speed; // range of speeds for customers
		this.myLocation = loc; // for easy lookups
		inRoom = false; // not in room yet
		thirsty = true; // thirsty when arrive
		wantToLeave = false; // want to stay when arrive
		rand = new Random();

		// These are used to control pausing and starting
		this.paused = paused;
		this.start = start;
	}

	// getter
	public boolean inRoom() {
		return inRoom;
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

	// setter

	// check to see if user pressed pause button
	private void checkPause() {
		// If paused is set to true, the threads wait for it
		// to be changed by the main thread
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
		// The thread waits for the starting latch to be opened
		try {
			start.await();
		} catch (InterruptedException e) {
		}
	}

	// get drink at bar
	private void getDrink() throws InterruptedException {
		// Wait for the current block to be notified,
		// which will happen when the barman serves it
		synchronized (currentBlock) {
			currentBlock.wait();
		}

		thirsty = false;
		System.out.println(
				"Thread " + this.ID + " got drink at bar position: " + currentBlock.getX() + " " + currentBlock.getY());
		sleep(movingSpeed * 5); // wait a bit
	}

	// --------------------------------------------------------
	// DO NOT CHANGE THE CODE BELOW HERE - it is not necessary
	// clubgoer enters club
	public void enterClub() throws InterruptedException {
		currentBlock = club.enterClub(myLocation); // enter through entrance
		inRoom = true;
		System.out.println(
				"Thread " + this.ID + " entered club at position: " + currentBlock.getX() + " " + currentBlock.getY());
		sleep(movingSpeed / 2); // wait a bit at door
	}

	// go to bar
	private void headToBar() throws InterruptedException {
		int x_mv = rand.nextInt(3) - 1; // -1,0 or 1
		int y_mv = Integer.signum(club.getBar_y() - currentBlock.getY());// -1,0 or 1
		currentBlock = club.move(currentBlock, x_mv, y_mv, myLocation); // head toward bar
		System.out.println("Thread " + this.ID + " moved toward bar to position: " + currentBlock.getX() + " "
				+ currentBlock.getY());
		sleep(movingSpeed / 2); // wait a bit
	}

	// go head towards exit
	private void headTowardsExit() throws InterruptedException {
		GridBlock exit = club.getExit();
		int x_mv = Integer.signum(exit.getX() - currentBlock.getX());// x_mv is -1,0 or 1
		int y_mv = Integer.signum(exit.getY() - currentBlock.getY());// -1,0 or 1
		currentBlock = club.move(currentBlock, x_mv, y_mv, myLocation);
		System.out.println(
				"Thread " + this.ID + " moved to towards exit: " + currentBlock.getX() + " " + currentBlock.getY());
		sleep(movingSpeed); // wait a bit
	}

	// dancing in the club
	private void dance() throws InterruptedException {
		for (int i = 0; i < 3; i++) { // sequence of 3

			int x_mv = rand.nextInt(3) - 1; // -1,0 or 1
			int y_mv = Integer.signum(1 - x_mv);

			for (int j = 0; j < 4; j++) { // do four fast dance steps
				currentBlock = club.move(currentBlock, x_mv, y_mv, myLocation);
				sleep(movingSpeed / 5);
				x_mv *= -1;
				y_mv *= -1;
			}
			checkPause();
		}
	}

	// wandering about in the club
	private void wander() throws InterruptedException {
		for (int i = 0; i < 2; i++) { //// wander for two steps
			int x_mv = rand.nextInt(3) - 1; // -1,0 or 1
			int y_mv = Integer.signum(-rand.nextInt(4) + 1); // -1,0 or 1 (more likely to head away from bar)
			currentBlock = club.move(currentBlock, x_mv, y_mv, myLocation);
			sleep(movingSpeed);
		}
	}

	// leave club
	private void leave() throws InterruptedException {
		club.leaveClub(currentBlock, myLocation);
		inRoom = false;
	}

	public void run() {
		try {
			startSim();
			checkPause();
			sleep(movingSpeed * (rand.nextInt(100) + 1)); // arriving takes a while
			checkPause();
			myLocation.setArrived();
			System.out.println("Thread " + this.ID + " arrived at club"); // output for checking
			checkPause(); // check whether have been asked to pause
			enterClub();

			while (inRoom) {
				checkPause(); // check every step
				if ((!thirsty) && (!wantToLeave)) {
					if (rand.nextInt(100) > 95)
						thirsty = true; // thirsty every now and then
					else if (rand.nextInt(100) > 98)
						wantToLeave = true; // at some point want to leave
				}

				if (wantToLeave) { // leaving overides thirsty
					sleep(movingSpeed / 5); // wait a bit
					if (currentBlock.isExit()) {
						leave();
						System.out.println("Thread " + this.ID + " left club");
					} else {
						System.out.println("Thread " + this.ID + " going to exit");
						headTowardsExit();
					}
				} else if (thirsty) {
					sleep(movingSpeed / 5); // wait a bit
					if (currentBlock.isBar()) {
						getDrink();
						System.out.println("Thread " + this.ID + " got drink ");
					} else {
						System.out.println("Thread " + this.ID + " going to getDrink ");
						headToBar();
					}
				} else {
					if (currentBlock.isDanceFloor()) {
						dance();
						System.out.println("Thread " + this.ID + " dancing ");
					}
					wander();
					System.out.println("Thread " + this.ID + " wandering about ");
				}

			}
			System.out.println("Thread " + this.ID + " is done");

		} catch (InterruptedException e1) { // do nothing
		}
	}

}
