package org.tutske.rest;

import org.tutske.rest.internals.Chain;

import java.util.Arrays;


@FunctionalInterface
public interface Filter<S, T> {

	public static <S, T> Filter<S, T> combine (Filter<S, T> ... filters) {
		return (source, c) -> {
			return new Chain<> (c::call, Arrays.asList (filters)).call (source);
		};
	}

	T call (S source, Chain<S, T> chain) throws Exception;

}
