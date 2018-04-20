package org.tutske.rest;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.tutske.rest.HttpRequest.Method.GET;

import org.junit.Test;
import org.tutske.rest.routes.UrlRoute;
import org.tutske.rest.routes.SimpleRoute;


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

		assertThat (router.route (GET, "/users/abc"), is ("one user"));
	}

	@Test
	public void it_should_find_the_right_route () {
		UrlRouter router = new UrlRouter ();
		UrlRoute one_user = new SimpleRoute ("one user", "/users/:id", null);

		router.add (
			new SimpleRoute ("all users", "/users", null),
			one_user,
			new SimpleRoute ("user groups", "/users/:id/groups", null),
			new SimpleRoute ("all groups", "/groups", null)
		);

		assertThat (router.find ("one user"), is (one_user));
	}

}
