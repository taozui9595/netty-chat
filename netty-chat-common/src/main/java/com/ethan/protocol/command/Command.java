package com.ethan.protocol.command;

/**
 * @author tmpil9
 * @version 1.0
 * @date 22/01/2019
 */
public interface Command {
    Byte LOGIN_REQUEST = 1;
    Byte LOGIN_RESPONSE = 2;

    Byte MESSAGE_REQUEST = 3;
    Byte MESSAGE_RESPONSE = 4;


}
