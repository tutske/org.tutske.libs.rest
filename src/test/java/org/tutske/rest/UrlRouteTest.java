package org.tutske.rest;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.tutske.rest.HttpRequest.Method.*;
import static org.tutske.rest.UrlRoute.SimpleRoute;

import org.junit.Test;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;


public class UrlRouteTest {

	@Test
	public void it_should_match_the_specified_urls () {
		UrlRoute<?> route = new SimpleRoute<> ("users", "/users/:id", null);
		assertThat (route.matches (GET, "/users/abc"), is (true));
	}

	@Test
	public void it_should_match_the_specified_urls_with_trailing_slash () {
		UrlRoute<?> route = new SimpleRoute<> ("users", "/users/:id", null);
		assertThat (route.matches (GET, "/users/abc/"), is (true));
	}

	@Test
	public void it_should_match_with_or_without_a_trailing_slach () {
		UrlRoute<?> route = new SimpleRoute<> ("user", "/users/:id", null);
		assertThat (route.matches (GET, "/users/abc/"), is (true));
	}

	@Test
	public void it_should_match_urls_with_the_right_http_methods () {
		UrlRoute<?> route = new SimpleRoute<> ("users", "/users/:id", EnumSet.of (POST), null);
		assertThat (route.matches (POST, "/users/abc/"), is (true));
	}

	@Test
	public void it_should_only_match_urls_with_the_right_http_methods () {
		UrlRoute<?> route = new SimpleRoute<> ("users", "/users/:id", EnumSet.of (POST), null);
		assertThat (route.matches (GET, "/users/abc/"), is (false));
	}

	@Test
	public void it_should_match_urls_with_any_of_the_right_http_methods () {
		UrlRoute<?> route = new SimpleRoute<> ("users", "/users/:id", EnumSet.of (POST, PUT), null);
		assertThat (route.matches (POST, "/users/abc/"), is (true));
		assertThat (route.matches (PUT, "/users/abc/"), is (true));
	}

	@Test
	public void it_should_match_only_urls_with_any_of_the_right_http_methods () {
		UrlRoute<?> route = new SimpleRoute<> ("users", "/users/:id", EnumSet.of (POST, PUT), null);
		assertThat (route.matches (GET, "/users/abc/"), is (false));
		assertThat (route.matches (HEAD, "/users/abc/"), is (false));
	}

	@Test
	public void it_should_only_match_urls_with_the_right_number_of_parts () {
		UrlRoute<?> route = new SimpleRoute<> ("users", "/users/:id", null);

		assertThat (route.matches (GET, "/users"), is (false));
		assertThat (route.matches (GET, "/users/"), is (false));
		assertThat (route.matches (GET, "/users/3/update"), is (false));
		assertThat (route.matches (GET, "/users/3/update/"), is (false));
	}

	@Test
	public void it_should_extract_the_parameters_from_a_url () {
		String [] parts = new String [] { "users", "abc" };
		String url = "/" + String.join ("/", parts);

		UrlRoute<?> route = new SimpleRoute<> ("users", "/users/:id", null);
		Map<String, String> params = route.extractMatches (url, parts);

		assertThat (params, hasEntry ("id", "abc"));
	}

	@Test
	public void it_should_extract_multiple_parameters_from_a_url () {
		String [] parts = new String [] { "books", "The_book_of_love", "CH_1", "12" };
		String url = "/" + String.join ("/", parts);

		UrlRoute<Object> route = new SimpleRoute<> ("page", "/books/:title/:chapter/:page", null);
		Map<String, String> params = route.extractMatches (url, parts);

		assertThat (params, hasEntry ("title", "The_book_of_love"));
		assertThat (params, hasEntry ("chapter", "CH_1"));
		assertThat (params, hasEntry ("page", "12"));
	}

	@Test (expected = RuntimeException.class)
	public void it_should_complain_when_the_descriptor_does_not_start_at_the_base () {
		new SimpleRoute<> ("user", "users/:id", null);
	}

	@Test
	public void it_should_be_able_to_generate_a_url_back_when_given_the_right_parameters () {
		UrlRoute<?> route = new SimpleRoute<> ("user", "/users/:id", null);

		Map<String, String> params = new HashMap<> ();
		params.put ("id", "abc");

		assertThat (route.linkTo (params), containsString ("/users/abc"));
	}

	@Test
	public void it_should_match_url_with_trailing_path_paths () {
		UrlRoute<?> route = new SimpleRoute<> ("", "/files/::path", null);
		assertThat (route.matches (GET, "/files/with/long/path/to/file.ext"), is (true));
	}

	@Test
	public void it_shouldSmatch_url_with_trailing_empty_paths () {
		UrlRoute<?> route = new SimpleRoute<> ("", "/files/::path", null);
		assertThat (route.matches (GET, "/files"), is (true));
	}

	@Test
	public void it_should_match_url_with_trailing_empty_paths_with_trailing_slash () {
		UrlRoute<?> route = new SimpleRoute<> ("", "/files/::path", null);
		assertThat (route.matches (GET, "/files/"), is (true));
	}

	@Test
	public void it_should_put_the_trailing_path_in_the_params () {
		UrlRoute<?> route = new SimpleRoute<> ("", "/files/::path", null);
		String url = "/files/with/long/path/to/file.ext";
		String [] parts = url.substring (1).split ("/");

		Map<String, String> params = route.extractMatches (url, parts);
		System.out.println (params);

		assertThat (params, hasKey ("path"));
		assertThat (params.get ("path"), is ("/with/long/path/to/file.ext"));
	}

	// Distinguish between get, post, put, ... request.

}
