package org.tutske.rest;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.tutske.rest.HttpRequest.Method.*;
import static org.tutske.rest.Matchers.*;

import org.junit.Test;
import org.tutske.rest.routes.GroupedRoute;
import org.tutske.rest.routes.SimpleRoute;
import org.tutske.rest.routes.UrlRoute;
import org.tutske.utils.Bag;

import java.util.EnumSet;
import java.util.Map;


public class GroupedRouteTest {

	@Test
	public void it_should_not_match_when_there_are_no_sub_routes () {
		UrlRoute<?> route = new GroupedRoute<> ("/root");

		assertThat (route, not (routesTo (GET, "/root")));
		assertThat (route, not (routesTo (POST, "/root")));
		assertThat (route, not (routesTo (PUT, "/root")));
		assertThat (route, not (routesTo (DELETE, "/root")));
	}

	@Test
	public void it_should_giva_an_identifier_when_a_sub_route_matches () {
		UrlRoute<?> route = new GroupedRoute<> ("/root",
			new SimpleRoute<> ("root-main", "/main", null)
		);

		assertThat (route, routesTo (GET, "/root/main"));
	}

	@Test
	public void it_should_to_the_right_sub_route () {
		UrlRoute<?> route = new GroupedRoute<> ("/root",
			new SimpleRoute<> ("root-one", "/first", null),
			new SimpleRoute<> ("root-two", "/second", null),
			new SimpleRoute<> ("root-third", "/three", null)
		);

		assertThat (route.toId (GET, "/root/second", new String [] { "root", "second" }), is ("root-two"));
	}

	@Test
	public void it_should_match_routes_of_different_methods () {
		UrlRoute<?> route = new GroupedRoute<> ("/root",
			new SimpleRoute<> ("root-main", "/main", EnumSet.of (POST), null)
		);

		assertThat (route, routesTo (POST, "/root/main"));
	}

	@Test
	public void it_should_only_match_on_the_right_methods () {
		UrlRoute<?> route = new GroupedRoute<> ("/root",
			new SimpleRoute<> ("root-main", "/main", EnumSet.of (POST), null)
		);

		assertThat (route, not (routesTo (GET, "/root/main")));
	}

	@Test
	public void it_should_match_sub_routes_with_different_methods () {
		UrlRoute<?> route = new GroupedRoute<> ("/root",
			new SimpleRoute<> ("post", "/post", EnumSet.of (POST), null),
			new SimpleRoute<> ("get", "/get", EnumSet.of (GET), null)
		);

		assertThat (route, routesTo (GET, "/root/get"));
		assertThat (route, routesTo (POST, "/root/post"));

		assertThat (route, not (routesTo (POST, "/root/get")));
		assertThat (route, not (routesTo (GET, "/root/post")));
	}

	@Test
	public void it_should_extract_parameters_with_the_sub_routes () {
		UrlRoute<?> route = new GroupedRoute<> ("/root",
			new SimpleRoute<> ("name", "/:name", EnumSet.of (POST), null)
		);

		Bag bag = route.extractMatches ("name", "/root/jhon", new String [] { "root", "jhon" });
		assertThat ((Map<String, String>) bag, hasEntry ("name", "jhon"));
	}

	@Test
	public void it_should_extract_parameters_from_the_group () {
		UrlRoute<?> route = new GroupedRoute<> ("/user/:name",
			new SimpleRoute<> ("user-ping", "/ping", null)
		);

		Bag bag = route.extractMatches ("user-ping", "/root/jhon/ping", new String [] { "root", "jhon" });
		assertThat ((Map<String, String>) bag, hasEntry ("name", "jhon"));
	}

	@Test
	public void it_should_extract_parameters_from_both_the_group_and_the_sub () {
		UrlRoute<?> route = new GroupedRoute<> ("/user/:name",
			new SimpleRoute<> ("user-game", "/:game", null)
		);

		Bag bag = route.extractMatches ("user-game", "/root/jhon/pong", new String [] { "root", "jhon", "pong" });

		assertThat ((Map<String, String>) bag, hasEntry ("name", "jhon"));
		assertThat ((Map<String, String>) bag, hasEntry ("game", "pong"));
	}

	@Test
	public void it_should_match_url_as_one_of_the_subs () {
		UrlRoute<Object> route = new GroupedRoute<> ("/root",
			new SimpleRoute<> ("sub", "/", null)
		);

		assertThat (route, routesTo (GET, "/root/"));
	}

	@Test
	public void it_should_match_a_group_at_the_base () {
		UrlRoute<Object> route = new GroupedRoute<> ("/",
			new SimpleRoute<> ("sub", "/sub", null)
		);

		assertThat (route, routesTo (GET, "/sub"));
	}

	@Test
	public void it_should_extract_from_subs_with_a_group_at_the_base () {
		UrlRoute<Object> route = new GroupedRoute<> ("/",
			new SimpleRoute<> ("sub", "/sub/:key", null)
		);

		Map<String, String> values = route.extractMatches ("sub", "/sub/value", new String [] { "sub", "value" });
		assertThat (values, hasEntry ("key", "value"));
	}

	@Test
	public void it_should_match_subs_from_top_toBottom () {
		UrlRoute<Object> route = new GroupedRoute<> ("/",
			new SimpleRoute<> ("key", "/sub/:key", null),
			new SimpleRoute<> ("value", "/sub/:value", null)
		);

		assertThat (route.toId (GET, "/sub/something", new String [] { "sub", "something" }), is ("key"));

		UrlRoute<Object> route2 = new GroupedRoute<> ("/",
			new SimpleRoute<> ("value", "/sub/:key", null),
			new SimpleRoute<> ("key", "/sub/:value", null)
		);

		assertThat (route2.toId (GET, "/sub/something", new String [] { "sub", "something" }), is ("value"));
	}

	@Test
	public void it_should_match_with_variables_in_the_group_part () {
		UrlRoute<Object> route = new GroupedRoute<> ("/user/:name",
			new SimpleRoute<> ("view", "/", null)
		);

		assertThat (route.toId (GET, "/user/jhon", new String [] { "user", "jhon" }), is ("view"));
	}

	@Test (expected = Exception.class)
	public void it_should_complain_about_tail_in_group_descriptor () {
		new GroupedRoute<> ("/descriptor/with/::tail",
			new SimpleRoute<> ("sub", "/sub", null)
		);
	}

}
