package com.luxbet.util.telnet.monitor.exceptions;

/**
 * Created by erwan on 6/10/15.
 *
 * Invalid Command given to the Telnet Monitor
 */
public class InvalidCommandException extends Exception {
    public String command;

    public InvalidCommandException(String command) {
        this.command = command;
    }

    @Override
    public String getMessage() {
        return "Invalid Command : " + command;
    }
}
