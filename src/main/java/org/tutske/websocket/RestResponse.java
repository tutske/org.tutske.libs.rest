package org.tutske.websocket;

public class RestResponse {

	private String msg;

	public RestResponse () {
		this ("");
	}

	public RestResponse (String msg) {
		this.msg = msg;
	}

	@Override
	public String toString () {
		return msg;
	}

}
