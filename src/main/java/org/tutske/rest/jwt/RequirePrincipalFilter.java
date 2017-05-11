package org.tutske.rest.jwt;

import org.tutske.rest.HttpRequest;
import org.tutske.rest.RestFilter;
import org.tutske.rest.data.RestStructure;
import org.tutske.rest.exceptions.AuthorizationFailure;
import org.tutske.rest.internals.Chain;


public class RequirePrincipalFilter implements RestFilter {

	private final String principal;

	public RequirePrincipalFilter () {
		this ("principal");
	}

	public RequirePrincipalFilter (String principal) {
		this.principal = principal;
	}

	@Override
	public RestStructure call (HttpRequest source, Chain<HttpRequest, RestStructure> chain) throws Exception {
		if ( ! source.context ().containsKey (principal) ) {
			throw new AuthorizationFailure ();
		}
		return chain.call (source);
	}

}
