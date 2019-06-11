package org.tutske.lib.rest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.junit.Test;
import org.tutske.lib.utils.Bag;

import java.util.EnumSet;
import java.util.Map;


public class APIExtractionTest {

	/* -- for routed things -- */

	@Test
	public void it_should_extract_the_parameters_from_a_url () {
		ApiRouter<String, String> router = API.configure (api -> {
			api.route ("route", "/users/:id", name -> name);
		});

		Bag<String, String> params = router.extractMatches ("route", "/users/1", API.splitParts ("/users/1"));
		assertThat ((Map<String, String>) params, hasEntry ("id", "1"));
		assertThat (params.get ("id"), is ("1"));
	}

	@Test
	public void it_should_extract_multiple_parameters_from_a_url () {
		ApiRouter<String, String> router = API.configure (api -> {
			api.route ("route", "/books/:title/:chapter/:page", name -> name);
		});

		String [] parts = new String [] { "books", "The_book_of_love", "CH_1", "12" };
		String url = "/books/The_book_of_love/CH_1/12";

		Bag<String, String> params = router.extractMatches ("route", url, parts);
		assertThat ((Map<String, String>) params, hasEntry ("title", "The_book_of_love"));
		assertThat ((Map<String, String>) params, hasEntry ("chapter", "CH_1"));
		assertThat ((Map<String, String>) params, hasEntry ("page", "12"));
	}

	@Test
	public void it_should_extract_identically_named_parameters_from_a_url () {
		ApiRouter<String, String> router = API.configure (api -> {
			api.route ("route", "/actions/:action/:action/:action", name -> name);
		});

		String url = "/actions/walk/jump/turn-around";
		Bag<String, String> params = router.extractMatches ("route", url, API.splitParts (url));
		assertThat (params.getAll ("action"), contains ("walk", "jump", "turn-around"));
	}

	@Test
	public void it_should_put_the_trailing_path_in_the_params () {
		ApiRouter<String, String> router = API.configure (api -> {
			api.route ("route", "/files/::path", name -> name);
		});

		String url = "/files/with/long/path/to/file.ext";
		Bag<String, String> params = router.extractMatches ("route", url, API.splitParts (url));

		assertThat ((Map<String, String>) params, hasKey ("path"));
		assertThat (params.get ("path"), is ("/with/long/path/to/file.ext"));
	}

	@Test
	public void it_should_extract_traling_path_in_the_parmas_when_after_multiple_other_parts () {

		ApiRouter<String, String> router = API.configure (api -> {
			api.route ("route", "/api/files/::path", name -> name);
		});

		String url = "/api/files/with/long/path/to/file.ext";
		Bag<String, String> params = router.extractMatches ("route", url, API.splitParts (url));
		assertThat ((Map<String, String>) params, hasKey ("path"));
		assertThat (params.get ("path"), is ("/with/long/path/to/file.ext"));
	}

	/* -- for grouped things -- */

	@Test
	public void it_should_extract_parameters_with_the_sub_routes () {
		ApiRouter<String, String> router = API.configure (api -> {
			api.group ("/root", group -> {
				group.route ("route", "/:name", EnumSet.of (Request.Method.POST), name -> name);
			});
		});

		Bag params = router.extractMatches ("route", "/root/john", API.splitParts ("/root/john"));
		assertThat ((Map<String, String>) params, hasEntry ("name", "john"));
	}

	@Test
	public void it_should_extract_parameters_from_the_group () {
		ApiRouter<String, String> router = API.configure (api -> {
			api.group ("/users/:name", group -> {
				group.route ("route", "/ping", EnumSet.of (Request.Method.POST), name -> name);
			});
		});

		String url = "/users/john/ping";
		Bag params = router.extractMatches ("route", url, API.splitParts (url));
		assertThat ((Map<String, String>) params, hasEntry ("name", "john"));
	}

	@Test
	public void it_should_extract_parameters_from_both_the_group_and_the_sub () {
		ApiRouter<String, String> router = API.configure (api -> {
			api.group ("/users/:name", group -> {
				group.route ("route", "/:game", EnumSet.of (Request.Method.POST), name -> name);
			});
		});

		String url = "/users/john/pong";
		Bag params = router.extractMatches ("route", url, API.splitParts (url));
		assertThat ((Map<String, String>) params, hasEntry ("name", "john"));
		assertThat ((Map<String, String>) params, hasEntry ("game", "pong"));
	}

	@Test
	public void it_should_extract_from_subs_with_a_group_at_the_base () {
		ApiRouter<String, String> router = API.configure (api -> {
			api.group ("/", group -> {
				group.route ("route", "/sub/:key", EnumSet.of (Request.Method.POST), name -> name);
			});
		});

		Map<String, String> params = router.extractMatches ("route", "/sub/value", API.splitParts ("/sub/value"));
		assertThat ((Map<String, String>) params, hasEntry ("key", "value"));
	}

}
