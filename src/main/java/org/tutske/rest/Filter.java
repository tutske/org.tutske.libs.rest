package org.tutske.rest;


import org.tutske.rest.internals.Chain;


@FunctionalInterface
public interface Filter<S, T> {

	T call (S source, Chain<S, T> chain) throws Exception;

}
