package com.luxbet.util.telnet.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Telnet Monitor Service (Thread) which allows users to telnet in and request/monitor internal services
 * <p>
 * Copyright © Tabcorp Pty Ltd. All rights reserved. http://www.tabcorp.com.au/
 * This code is copyrighted and is the exclusive property of Tabcorp Pty Ltd. It may not be used, copied or redistributed without the written permission of Tabcorp.
 *
 * @author Nicholas Potesta [potestani@luxbet.com]
 */
public class TelnetMonitorService extends Thread {
    protected static final Logger LOGGER = LoggerFactory.getLogger(TelnetMonitorService.class);

    protected static final String PORT_KEY = "telnet.service.port";
    protected static final String WELCOME_MESSAGE_KEY = "telnet.service.message.welcome";
    protected static final String READY_MESSAGE_KEY = "telnet.service.message.ready";

    protected static final String DEFAULT_PORT = "9999";
    protected static final String DEFAULT_WELCOME_MESSAGE = "Welcome!";
    protected static final String DEFAULT_READY_MESSAGE = "Ready for action: ";

    private String welcomeMessage;
    private String readyMessage;
    private int port;

    private Map<String, TelnetCommand> commands;
    private Map<String, TelnetMonitorHandler> handlers;

    private volatile boolean running = true;

    /**
     * Default constructor
     *
     */
    public TelnetMonitorService() throws NumberFormatException {
        welcomeMessage = System.getProperty(WELCOME_MESSAGE_KEY, DEFAULT_WELCOME_MESSAGE);
        readyMessage = System.getProperty(READY_MESSAGE_KEY, DEFAULT_READY_MESSAGE);
        port = Integer.parseInt(System.getProperty(PORT_KEY, DEFAULT_PORT));
        commands = new HashMap<>();
        handlers = new HashMap<>();
    }

    /**
     * Safely kill the thread
     */
    public void terminate() {
        // Kill all running handlers.
        handlers.entrySet().stream().forEach(h -> h.getValue().terminate(false));
        running = false;
    }

    public void removeHandler(String name) {
        LOGGER.debug("Removing Handler {}", name);
        handlers.remove(name);
    }

    /**
     * Register a Telnet command with the service.
     *
     * @param command   The Telnet Command.
     */
    public void registerCommand(TelnetCommand command) {
        this.commands.put(command.getCommand(), command);
    }

    /**
     * Match a user input to a registered command.
     *
     * @param commandInput  The user input.
     * @return A TelnetCommand is there is one, or empty.
     */
    public Optional<TelnetCommand> getRequest(String commandInput) {
        TelnetCommand command = commands.get(commandInput);

        if (command == null) {
            return Optional.empty();
        } else {
            return Optional.of(command);
        }

    }

    /**
     * @return  A list of all available commands.
     */
    public String getAvailableCommands() {
        StringBuilder builder = new StringBuilder("Available Commands : \n");
        commands.entrySet().stream().forEach(c -> builder.append(c.getValue().getCommand()).append(" : ").append(c.getValue().getShortDescription()).append("\n"));
        builder.append("help : Get available commands\n");
        builder.append("exit : Exit the shell\n");
        return builder.toString();
    }

    @Override
    public void run() {
        int socketTimeout;
        try {
            socketTimeout = Integer.parseInt(System.getProperty("telnet.service.socket.timeout"));
        } catch (Exception e) {
            LOGGER.warn("Error getting/parsing socket.timeout property, defaulting to 5000");
            socketTimeout = 5000;
        }

        try (
                ServerSocket listener = new ServerSocket(port)
        ) {
            LOGGER.info("Listening to commands on port : " + port + " ; With socket timeout : " + socketTimeout);
            listener.setSoTimeout(socketTimeout);

            while (running) {
                try {
                    TelnetMonitorHandler handler = new TelnetMonitorHandler(listener.accept(), this);
                    handlers.put(handler.getName(), handler);
                    handler.start();
                } catch (SocketTimeoutException e) {
                    // Nothing?
                }
            }
        } catch (IOException e) {
            LOGGER.error("IOException while listening to socket. {}", e, e);
        }

        LOGGER.info("Socket Listening is Done.");

    }

    /**
     * @return  The Welcome Message that is given to user on first connections.
     */
    public String getWelcomeMessage() {
        return welcomeMessage;
    }

    /**
     * @return The command prompt.
     */
    public String getReadyMessage() {
        return readyMessage;
    }
}

