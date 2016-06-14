package org.tutske.rest.data;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Ignore;
import org.junit.Test;


public class RestObjectTest {

	Gson gson = new GsonBuilder ().create ();

	@Test
	public void it_should_allow_Strings_as_properties () {
		RestObject object = new RestObject () {{
			v ("key", "value");
		}};

		String json = gson.toJson (object.asJson ());
		assertThat (json, is ("{\"key\":\"value\"}"));
	}

	@Test
	public void it_should_allow_Numbers_as_properties () {
		RestObject object = new RestObject () {{
			v ("zero", 0);
			v ("one", 1L);
			v ("two", 2.0F);
			v ("three", 3.0D);
		}};

		String json = gson.toJson (object.asJson ());
		assertThat (json, is ("{\"zero\":0,\"one\":1,\"two\":2.0,\"three\":3.0}"));
	}

	@Test
	public void it_should_allow_Booleans_as_properties () {
		RestObject object = new RestObject () {{
			v ("success", true);
			v ("faulty", false);
		}};

		String json = gson.toJson (object.asJson ());
		assertThat (json, is ("{\"success\":true,\"faulty\":false}"));
	}

	@Test
	public void it_should_allow_RestObjects_as_properties () {
		RestObject object = new RestObject () {{
			v ("shallow", new RestObject () {{
				v ("deep", "value");
			}});
		}};

		String json = gson.toJson (object.asJson ());
		assertThat (json, is ("{\"shallow\":{\"deep\":\"value\"}}"));
	}

	@Test
	public void it_should_allow_lists_as_properties () {
		RestObject object = new RestObject () {{
			v ("key", list ());
		}};

		String json = gson.toJson (object.asJson ());
		assertThat (json, is ("{\"key\":[]}"));
	}

	@Test
	public void it_should_allow_lists_with_values () {
		RestObject object = new RestObject () {{
			v ("key", list (1, 2, 3));
		}};

		String json = gson.toJson (object.asJson ());
		assertThat (json, is ("{\"key\":[1,2,3]}"));
	}

	@Test (expected = RuntimeException.class)
	public void it_should_complain_when_the_value_is_not_primitive_and_not_rest () {
		new RestObject () {{
			v ("key", new Object ());
		}};
	}

	@Test
	public void it_should_merge_two_rest_objects () {
		RestObject target = new RestObject () {{
			v ("first", 1);
		}};
		RestObject source = new RestObject () {{
			v ("second", 2);
		}};

		String json = gson.toJson (target.merge (source).asJson ());
		assertThat (json, is ("{\"first\":1,\"second\":2}"));
	}

	@Test
	public void it_should_merge_down_deeper_objects () {
		RestObject target = new RestObject () {{
			v ("object", new RestObject () {{
				v ("first", 1);
			}});
		}};
		RestObject source = new RestObject () {{
			v ("object", new RestObject () {{
				v ("second", 2);
			}});
		}};

		String json = gson.toJson (target.merge (source).asJson ());
		assertThat (json, is ("{\"object\":{\"first\":1,\"second\":2}}"));
	}

	@Test
	public void it_should_merge_objects_in_deep_arrays () {
		RestArray target = new RestArray () {{
		}};
		RestArray source = new RestArray () {{
			v (new RestObject () {{
				v ("key", "value");
			}});
		}};

		String json = gson.toJson (target.merge (source).asJson ());
		assertThat (json, is ("[{\"key\":\"value\"}]"));
	}

	@Test
	public void it_should_merge_deep_arrays () {
		RestArray target = new RestArray () {{
		}};
		RestArray source = new RestArray () {{
			v (new RestArray () {{
				v ("first");
			}});
		}};

		String json = gson.toJson (target.merge (source).asJson ());
		assertThat (json, is ("[[\"first\"]]"));
	}

	@Test
	public void it_should_merge_lists () {
		RestObject target = new RestObject () {{
			v ("list", list (1, 2));
		}};
		RestObject source = new RestObject () {{
			v ("list", list (3, 4));
		}};

		String json = gson.toJson (target.merge (source).asJson ());
		assertThat (json, is ("{\"list\":[1,2,3,4]}"));
	}

	@Test
	public void it_should_keep_the_order_in_which_the_keys_where_added () {
		RestObject first_second = new RestObject () {{
			v ("first", 1);
			v ("second", 2);
		}};

		RestObject second_first = new RestObject () {{
			v ("second", 2);
			v ("first", 1);
		}};

		assertThat (
			gson.toJson (first_second.asJson ()),
			is ("{\"first\":1,\"second\":2}")
		);
		assertThat (
			gson.toJson (second_first.asJson ()),
			is ("{\"second\":2,\"first\":1}")
		);
	}

	@Test
	public void it_should_give_us_back_the_value () {
		RestObject subject = new RestObject () {{
			v ("first", 1);
		}};

		assertThat (subject.getNumber ("first"), is (1));
	}

	@Test
	public void it_should_give_us_back_deeper_values () {
		RestObject subject = new RestObject () {{
			v ("first", new RestObject () {{
				v ("second", "value");
			}});
		}};

		assertThat (subject.getString ("first", "second"), is ("value"));
	}

	@Test
	public void it_should_marshall_an_array_with_arrays () {
		RestArray array = new RestArray () {{
			attribute ("length", 1);
			v ("John Doe");
		}};
		String json = gson.toJson (array.asJson ());
		assertThat (json, is ("{\"$attributes\":{\"length\":1},\"items\":[\"John Doe\"]}" ));
	}

}
