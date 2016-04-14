package org.tutske.rest;


@FunctionalInterface
public interface SocketFunction {

	WebSocketListener apply (SocketRequest request);

}
