package org.tutske.rest.routes;

import static org.tutske.rest.HttpRequest.Method.GET;

import org.tutske.rest.ControllerFunction;
import org.tutske.rest.Filter;
import org.tutske.rest.HttpRequest;
import org.tutske.rest.HttpRequest.Method;
import org.tutske.rest.RestFilter;
import org.tutske.rest.RestFilterCollection;
import org.tutske.rest.Server;
import org.tutske.rest.UrlRouter;
import org.tutske.rest.data.RestStructure;

import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;


public class Routes {

	private static final AtomicLong id = new AtomicLong ();
	private static String nextId () {
		return String.valueOf (id.incrementAndGet ());
	}

	public interface RestCreator {
		void group (String identifier, String url, Consumer<RestCreator> consumer);
		default void group (String url, Consumer<RestCreator> consumer) {
			group ("", url, consumer);
		}

		void route (String identifier, String descriptor, EnumSet<Method> methods, ControllerFunction fn);
		default void route (String descriptor, EnumSet<Method> methods, ControllerFunction fn) {
			route (nextId (), descriptor, methods, fn);
		}
		default void route (String identifier, String descriptor, ControllerFunction fn) {
			route (identifier, descriptor, EnumSet.of (GET), fn);
		}
		default void route (String descriptor, ControllerFunction fn) {
			route (nextId (), descriptor, EnumSet.of (GET), fn);
		}

		void filter (String descriptor, EnumSet<Method> methods, RestFilter fn);
		default void filter (String descriptor, RestFilter fn) {
			filter (descriptor, EnumSet.allOf (Method.class), fn);
		}
	}

	public static Server api (Server server, Consumer<RestCreator> consumer) {
		return api (server, null, consumer);
	}

	public static Server api (Server server, String version, Consumer<RestCreator> consumer) {
		List<UrlRoute<ControllerFunction>> routes = new LinkedList<> ();
		List<UrlRoute<Filter<HttpRequest, RestStructure>>> filters = new LinkedList<> ();

		consumer.accept (makeRestCreator (routes, "", filters, ""));

		UrlRouter<ControllerFunction> router = new UrlRouter<> ();
		routes.forEach (router::add);

		RestFilterCollection filter = new RestFilterCollection ();
		filters.forEach (filter::add);

		if ( server != null ) {
			server.configureRoutes (router, filter);
		}

		return server;
	}

	private static RestCreator makeRestCreator (
		List<UrlRoute<ControllerFunction>> collector, String prefix,
		List<UrlRoute<Filter<HttpRequest, RestStructure>>> filters, String currentUrl
	) {
		String lead = prefix == null ? "" : prefix + "-";
		return new RestCreator () {
			@Override public void group (String id, String url, Consumer<RestCreator> consumer) {
				List<UrlRoute<ControllerFunction>> routes = new LinkedList<> ();

				String newId = lead + id;
				String newUrl = currentUrl + (url.endsWith ("/") ? url.substring (0, url.length () - 1): url);
				consumer.accept (makeRestCreator (routes, newId, filters, newUrl));

				collector.add (new GroupedRoute<> (url, routes));
			}

			@Override public void route (String id, String url, EnumSet<Method> methods, ControllerFunction fn) {
				collector.add (new SimpleRoute<> (lead + id, url, methods, fn));
			}

			@Override public void filter (String descriptor, EnumSet<Method> methods, RestFilter fn) {
				filters.add (new SimpleRoute<> ("", currentUrl + descriptor, methods, fn));
			}
		};
	}

}
