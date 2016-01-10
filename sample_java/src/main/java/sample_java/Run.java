package sample_java;

import java.util.Scanner;

public class Run {

    public static void main(String args[]) {
        App app = new App();

        while (!app.close()) {
            app.printInstructionsForAvailableOptions();
            int input = Integer.valueOf(new Scanner(System.in).nextLine());
            app.processNewInput(input);
        }
    }
}
