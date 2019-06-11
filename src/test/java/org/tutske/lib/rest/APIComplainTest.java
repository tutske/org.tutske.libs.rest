package org.tutske.lib.rest;

import org.junit.Test;


public class APIComplainTest {

	@Test (expected = Exception.class)
	public void complain_when_a_route_does_not_start_with_a_slash () {
		API.<String, String>configure (api -> {
			api.route ("path/to/resources", name -> name);
		});
	}

	@Test (expected = Exception.class)
	public void complain_when_a_route_ends_in_a_slash () {
		API.<String, String>configure (api -> {
			api.route ("/path/to/resources/", name -> name);
		});
	}

	@Test (expected = Exception.class)
	public void complain_when_a_group_does_not_start_with_a_slash () {
		API.<String, String>configure (api -> {
			api.group ("path/to/resources", resource -> {
				resource.route ("/", name -> name);
			});
		});
	}

	@Test (expected = Exception.class)
	public void complain_when_a_group_ends_in_a_slash () {
		API.<String, String>configure (api -> {
			api.group ("/path/to/resources/", resource -> {
				resource.route ("/", name -> name);
			});
		});
	}

	@Test (expected = Exception.class)
	public void it_should_complain_when_configureing_routes_with_the_same_id () {
		API.<String, String>configure (api -> {
			api.route ("id", "/path/to/resource", name -> name);
			api.route ("id", "/path/to/objects", name -> name);
		});
	}

	@Test (expected = Exception.class)
	public void it_should_complain_when_a_route_has_a_part_after_the_tail () {
		API.<String, String>configure (api -> {
			api.route ("/files/::path/:id", name -> name);
		});
	}

}
