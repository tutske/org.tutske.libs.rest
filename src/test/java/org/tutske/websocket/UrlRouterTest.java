package org.tutske.websocket;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.tutske.websocket.HttpRequest.Method.GET;

import org.junit.Test;
import org.tutske.websocket.UrlRoute.SimpleRoute;


public class UrlRouterTest {

	@Test
	public void it_should_find_routes_back_by_name () {
		UrlRoute all_users = new SimpleRoute ("all users", "/users", null);
		UrlRoute one_user = new SimpleRoute ("one users", "/users/:id", null);

		UrlRouter router = new UrlRouter ();
		router.add (all_users, one_user);

		assertThat (router.find ("all users"), is (all_users));
		assertThat (router.find ("one users"), is (one_user));
	}

	@Test
	public void it_should_know_what_route_to_take_for_a_url () {
		UrlRouter router = new UrlRouter ();
		router.add (
			new SimpleRoute ("all users", "/users", null),
			new SimpleRoute ("one user", "/users/:id", null),
			new SimpleRoute ("user groups", "/users/:id/groups", null),
			new SimpleRoute ("all groups", "/groups", null)
		);

		assertThat (router.route (GET, "/users/abc"), is (router.find ("one user")));
	}

}
