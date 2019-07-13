module org.tutske.libs.rest {

	exports org.tutske.lib.rest;

	/* explicit modules */
	requires org.slf4j;
	requires org.tutske.libs.api;
	requires org.tutske.libs.utils;

	/* named automatic modules */
	requires transitive com.fasterxml.jackson.core;
	requires transitive com.fasterxml.jackson.databind;
	requires transitive com.fasterxml.jackson.datatype.jdk8;
	requires transitive com.fasterxml.jackson.datatype.jsr310;
	requires transitive org.eclipse.jetty.http;
	requires transitive org.eclipse.jetty.io;
	requires transitive org.eclipse.jetty.server;
	requires transitive org.eclipse.jetty.servlet;
	requires transitive org.eclipse.jetty.util;
	requires transitive org.eclipse.jetty.websocket.api;
	requires transitive org.eclipse.jetty.websocket.server;
	requires transitive org.eclipse.jetty.websocket.servlet;
	requires transitive org.eclipse.jetty.xml;

	/* implicitly named automatic modules */
	requires transitive javax.servlet.api;

}
