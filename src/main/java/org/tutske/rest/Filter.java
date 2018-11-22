package org.tutske.rest;


@FunctionalInterface
public interface Filter<S, T> {

	T call (S source, Chain<S, T> chain) throws Exception;

}
