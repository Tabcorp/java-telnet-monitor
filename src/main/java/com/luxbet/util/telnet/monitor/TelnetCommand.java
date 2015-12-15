package com.luxbet.util.telnet.monitor;

import com.luxbet.util.telnet.monitor.exceptions.InvalidArgumentException;

import java.io.BufferedReader;
import java.io.PrintWriter;

/**
 * This is the class that should be extended when wanting to add available commands to the Telnet Monitor
 *
 * TODO : Usage example, and auto validation of arguments and command help (ie "command help" -> print help for the command)
 */
abstract public class TelnetCommand {

    private String command;

    public TelnetCommand(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    /**
     *
     * @return A Short Description of the command.
     */
    abstract public String getShortDescription();

    /**
     * Execute the request and return the output when completed
     *
     * @param writer The output writer (PrintWriter) used to supply user with output (if required during task)
     * @param reader The input reader (BufferedReader) used to fetch user input (if required during task)
     * @throws InvalidArgumentException
     */
    abstract public void execute(PrintWriter writer, BufferedReader reader, String[] arguments) throws InvalidArgumentException;
}

