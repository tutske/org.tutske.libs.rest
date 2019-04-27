module org.tutske.libs.rest {

	exports org.tutske.lib.rest;
	exports org.tutske.lib.rest.exceptions;
	exports org.tutske.lib.rest.jwt;

	/* explicit modules */
	requires org.tutske.libs.utils;
	requires org.slf4j;

	/* named automatic modules */
	requires com.fasterxml.jackson.core;
	requires com.fasterxml.jackson.databind;
	requires com.fasterxml.jackson.datatype.jdk8;
	requires com.fasterxml.jackson.datatype.jsr310;
	requires org.eclipse.jetty.http;
	requires org.eclipse.jetty.io;
	requires org.eclipse.jetty.server;
	requires org.eclipse.jetty.servlet;
	requires org.eclipse.jetty.websocket.api;
	requires org.eclipse.jetty.websocket.server;
	requires org.eclipse.jetty.websocket.servlet;
	requires org.eclipse.jetty.xml;

	/* implicitly named automatic modules */
	requires javax.servlet.api;

}
