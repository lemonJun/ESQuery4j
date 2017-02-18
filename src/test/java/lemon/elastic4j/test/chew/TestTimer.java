package lemon.elastic4j.test.chew;

import java.util.Timer;
import java.util.TimerTask;

public class TestTimer {
    public static void main(String args[]) {
        System.out.println("About to schedule task.");
        new Reminder(3);
        System.out.println("Task scheduled.");
    }

    public static class Reminder {
        Timer timer;

        public Reminder(int sec) {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                public void run() {
                    System.out.println("Time's up!");
                    //timer.cancel();
                }
            }, sec * 1000);
        }
    }
}
