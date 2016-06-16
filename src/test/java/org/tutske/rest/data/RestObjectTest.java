package org.tutske.rest.data;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Test;


public class RestObjectTest {

	Gson gson = new GsonBuilder ().create ();

	@Test
	public void it_should_allow_Strings_as_properties () {
		RestObject object = new RestObject () {{
			v ("key", "value");
		}};

		String json = gson.toJson (object.asRestStructure ());
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

		String json = gson.toJson (object.asRestStructure ());
		assertThat (json, is ("{\"zero\":0,\"one\":1,\"two\":2.0,\"three\":3.0}"));
	}

	@Test
	public void it_should_allow_Booleans_as_properties () {
		RestObject object = new RestObject () {{
			v ("success", true);
			v ("faulty", false);
		}};

		String json = gson.toJson (object.asRestStructure ());
		assertThat (json, is ("{\"success\":true,\"faulty\":false}"));
	}

	@Test
	public void it_should_allow_RestObjects_as_properties () {
		RestObject object = new RestObject () {{
			v ("shallow", new RestObject () {{
				v ("deep", "value");
			}});
		}};

		String json = gson.toJson (object.asRestStructure ());
		assertThat (json, is ("{\"shallow\":{\"deep\":\"value\"}}"));
	}

	@Test
	public void it_should_allow_lists_as_properties () {
		RestObject object = new RestObject () {{
			v ("key", list ());
		}};

		String json = gson.toJson (object.asRestStructure ());
		assertThat (json, is ("{\"key\":[]}"));
	}

	@Test
	public void it_should_allow_lists_with_values () {
		RestObject object = new RestObject () {{
			v ("key", list (1, 2, 3));
		}};

		String json = gson.toJson (object.asRestStructure ());
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

		String json = gson.toJson (target.merge (source).asRestStructure ());
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

		String json = gson.toJson (target.merge (source).asRestStructure ());
		assertThat (json, is ("{\"object\":{\"first\":1,\"second\":2}}"));
	}

	@Test
	public void it_should_overwrite_existing_properties_when_merging_objects () {
		RestObject target = new RestObject () {{
			v ("key", "value");
		}};
		RestObject source = new RestObject () {{
			v ("key", "new value");
		}};

		String json = gson.toJson (target.merge (source).asRestStructure ());
		assertThat (json, is ("{\"key\":\"new value\"}"));
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

		String json = gson.toJson (target.merge (source).asRestStructure ());
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

		String json = gson.toJson (target.merge (source).asRestStructure ());
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

		String json = gson.toJson (target.merge (source).asRestStructure ());
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
			gson.toJson (first_second.asRestStructure ()),
			is ("{\"first\":1,\"second\":2}")
		);
		assertThat (
			gson.toJson (second_first.asRestStructure ()),
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
	public void it_should_marshall_an_array_with_attributes () {
		RestArray array = new RestArray () {{
			attribute ("length", 1);
			v ("John Doe");
		}};
		String json = gson.toJson (array.asRestStructure ());
		assertThat (json, is ("{\"$attributes\":{\"length\":1},\"items\":[\"John Doe\"]}" ));
	}

	@Test
	public void it_should_keep_the_original_tag_name_when_merging_arrays () {
		RestArray target = new RestArray ("users");
		RestArray source = new RestArray ();

		target.merge (source);

		assertThat (target.getTag (), is ("users"));
	}

	@Test
	public void it_should_keep_the_original_tag_name_when_merging_objects () {
		RestObject target = new RestObject ("response");
		RestObject source = new RestObject ();

		target.merge (source);

		assertThat (target.getTag (), is ("response"));
	}

	@Test
	public void it_should_keep_the_original_child_tag_name_when_merging_arrays () {
		RestArray target = new RestArray ("users", "user");
		RestArray source = new RestArray ("modified");

		target.merge (source);

		assertThat (target.getChildTag (), is ("user"));
	}

	@Test
	public void it_should_keep_the_original_child_tag_when_merging_an_array_with_dot_child_tag () {
		RestArray target = new RestArray ("users", "user");
		RestArray source = new RestArray ("modified", ".");

		target.merge (source);

		assertThat (target.getChildTag (), is ("user"));
	}

	@Test
	public void it_should_keep_the_new_tag_name_when_merging_arrays () {
		RestArray target = new RestArray ("original");
		RestArray source = new RestArray ("modified");

		target.merge (source);

		assertThat (target.getTag (), is ("modified"));
	}

	@Test
	public void it_should_keep_the_new_tag_name_when_merging_objects () {
		RestObject target = new RestObject ("original");
		RestObject source = new RestObject ("modified");

		target.merge (source);

		assertThat (target.getTag (), is ("modified"));
	}

	@Test
	public void it_should_keep_the_new_child_tag_name_when_merging_arrays () {
		RestArray target = new RestArray ("users", "user");
		RestArray source = new RestArray ("modified", "modifiedChildTag");

		target.merge (source);

		assertThat (target.getChildTag (), is ("modifiedChildTag"));
	}

	@Test
	public void it_should_merge_attributes_on_objects () {
		RestObject target = new RestObject () {{
			attribute ("key", "value");
		}};
		RestObject source = new RestObject () {{
			attribute ("key", "new value");
			attribute ("second", "second value");
		}};

		target.merge (source);

		assertThat (target.getAttributes (), hasEntry ("key", "new value"));
		assertThat (target.getAttributes (), hasEntry ("second", "second value"));
		assertThat (target.getAttributes ().entrySet (), hasSize (2));
	}

	@Test
	public void it_should_merge_attributes_on_arrays () {
		RestArray target = new RestArray () {{
			attribute ("key", "value");
		}};
		RestArray source = new RestArray () {{
			attribute ("key", "new value");
			attribute ("second", "second value");
		}};

		target.merge (source);

		assertThat (target.getAttributes (), hasEntry ("key", "new value"));
		assertThat (target.getAttributes (), hasEntry ("second", "second value"));
		assertThat (target.getAttributes ().entrySet (), hasSize (2));
	}

	@Test (expected = RuntimeException.class)
	public void it_should_complain_when_an_attribute_on_array_is_not_primitive () {
		new RestArray () {{
			attribute ("length", new RestObject ());
		}};
	}

	@Test (expected = RuntimeException.class)
	public void it_should_complain_when_an_attribute_on_object_is_not_primitive () {
		new RestObject () {{
			attribute ("max", new RestArray ());
		}};
	}

}
