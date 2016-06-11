package org.tutske.rest;


@FunctionalInterface
public interface ThrowingFunction<S, T> {

	public T apply (S source) throws Exception;

}
