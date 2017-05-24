package org.tutske.rest.data;

import java.util.Collections;
import java.util.Map;


public class RestRaw implements RestStructure {

	private final String mime;
	private final byte [] bytes;

	public RestRaw (byte [] bytes) {
		this ("application/octet-stream", bytes);
	}

	public RestRaw (String mime, byte [] bytes) {
		this.mime = mime;
		this.bytes = bytes;
	}

	public String mime () {
		return mime;
	}

	public byte [] content () {
		return bytes;
	}

	@Override
	public String getTag () {
		return "raw";
	}

	@Override
	public Map<String, Object> getAttributes () {
		return Collections.emptyMap ();
	}

	@Override
	public RestStructure asRestStructure () {
		return this;
	}

}
