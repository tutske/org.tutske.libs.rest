package org.tutske.websocket;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.tutske.websocket.HttpRequest.Method.GET;
import static org.tutske.websocket.UrlRoute.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;


@RunWith (Parameterized.class)
public class UrlRouterRoutingTest {

	@Parameterized.Parameters (name = "{index} route url: `{0}` to `{1}`")
	public static Collection<Object []> data () {
		return Arrays.asList (new Object[][] {
			{ "/users/abc", "one user" },
			{ "/users/abc/", "one user" },
			{ "/users/", "all users" },
			{ "/users/abc/groups", "user groups" },
			{ "/users/abc/groups/", "user groups" },
			{ "/teams/america/the_eagels", "named teams by country" }
		});
	}

	private final String url;
	private final String identifier;
	private UrlRouter router;

	public UrlRouterRoutingTest (String url, String identifier) {
		this.url = url;
		this.identifier = identifier;
	}

	@Before
	public void setup () {
		router = new UrlRouter ();
		router.add (
			new SimpleRoute ("all users", "/users", null),
			new SimpleRoute ("one user", "/users/:id", null),
			new SimpleRoute ("user groups", "/users/:id/groups", null),
			new SimpleRoute ("all groups", "/groups/:id", null),
			new SimpleRoute ("one group", "/groups", null),
			new SimpleRoute ("all teams", "/teams", null),
			new SimpleRoute ("teams by country", "/teams/:country", null),
			new SimpleRoute ("named teams by country", "/teams/:country/:name", null),
			new SimpleRoute ("book segment pages", "/books/:id/:section/:page", null)
		);
	}

	@Test
	public void it_should_route_to_the_right_url_route () {
		assertThat (router.route (GET, url), is (router.find (identifier)));
	}

}
