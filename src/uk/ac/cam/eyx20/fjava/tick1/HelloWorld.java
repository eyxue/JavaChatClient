package uk.ac.cam.eyx20.fjava.tick1;

import java.util.Scanner;

/**
 * Hello world class. Greets the world or a single given argument
 * @author Elise
 *
 */
public class HelloWorld {
    
    /**
     * Prints a greeting on the console
     * @param args: a single argument on the command line, 
     *      says hello to the argument.
     *      If no argument is given, prints "Hello, world".
     */
    public static void main(String[] args) {
        if (args.length == 1) {
            System.out.println("Hello, " + args[0]);
            return;
        }
        else {
            System.out.println("Hello, world");
            return;
        }
    }
}
