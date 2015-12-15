package com.luxbet.util.telnet.monitor.exceptions;

/**
 * Created by erwan on 6/10/15.
 *
 * When arguments given are invalid. (not enough, too many, etc)
 */
public class InvalidArgumentException extends Exception {
    public InvalidArgumentException(String message) {
        super(message);
    }
}
