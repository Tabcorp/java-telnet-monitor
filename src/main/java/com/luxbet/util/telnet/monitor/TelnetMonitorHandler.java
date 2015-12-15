package com.luxbet.util.telnet.monitor;

import com.github.lalyos.jfiglet.FigletFont;
import com.luxbet.util.telnet.monitor.exceptions.InvalidArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.Optional;

/**
 * Created by erwan on 6/10/15.
 *
 * Handles the commands...
 */
public class TelnetMonitorHandler extends Thread {

    private final static Logger LOGGER = LoggerFactory.getLogger(TelnetMonitorHandler.class);

    volatile static int threadId = 0;
    synchronized static public int getNextThreadId() {
        ++threadId;
        return threadId;
    }

    private Socket socket;
    private TelnetMonitorService service;
    private boolean running = true;

    private PrintWriter writer;


    public TelnetMonitorHandler(Socket socket, TelnetMonitorService service) {
        this.socket = socket;
        this.service = service;
        this.setName("TelnetMonitorHandler-" + TelnetMonitorHandler.getNextThreadId());
    }

    @Override
    public void run() {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)
        ) {
            this.writer = writer;

            //use Figlet to convert our welcome message to an ascii art banner (like all cool telnet services must have!)
            writer.println(FigletFont.convertOneLine(service.getWelcomeMessage()));

            while (running && socket.isConnected()) {
                writer.print(service.getReadyMessage()+" ");
                writer.flush();
                String userInput = reader.readLine();
                LOGGER.debug("Received command: {}", userInput);

                if (userInput != null) {
                    try {
                        String[] args = userInput.split(" ");
                        String[] comArguments = (args.length > 1) ? Arrays.copyOfRange(args, 1, args.length) : new String[]{};
                        String command = args[0];

                        switch (command) {
                            case "exit":
                                terminate(true);
                                break;

                            case "help":
                            case "?":
                                writer.println(service.getAvailableCommands());
                                break;

                            default:
                                Optional<TelnetCommand> oRequest = service.getRequest(command);

                                if (oRequest.isPresent()) {
                                    // Split the arguments.
                                    oRequest.get().execute(writer, reader, comArguments); //and away it goes!

                                    writer.println("Request completed!");
                                } else {
                                    writer.println("No such command.");
                                }
                                break;
                        }
                    } catch (IllegalArgumentException e) {
                        writer.println("Invalid command supplied");
                        LOGGER.debug("Unable to find corresponding enum/request for command supplied", e); //not really something we want to trigger as an error - it's more of a user error
                    } catch (InvalidArgumentException e) {
                        writer.println(e.getMessage()); //present the user with the supplied error message
                        LOGGER.debug("Unable to complete request. Not enough arguments", e);
                    }

                    writer.println("."); //create a separator/line between requests so the screen doesn't get cluttered
                }
            }

        } catch (IOException e) {
            LOGGER.warn("Connection has been closed.", e);
            terminate(true); //terminate the thread/do-not continue. Something has gone awry or the user has closed to connection (I.e. exit command)!
        }

        LOGGER.info("Done");
    }

    /**
     * Terminate this handler.
     */
    public void terminate(boolean removeFromService) {
        writer.println("Terminating...");

        if (removeFromService) {
            service.removeHandler(this.getName());
        }

        running = false;
    }

//    /**
//     * Send data to this handler's connection... maybe?
//     */
//    public void send(String data) {
//        this.writer.println(data);
//        this.writer.flush();
//    }

}
