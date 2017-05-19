package org.tutske.rest.jwt;

import static org.mockito.Mockito.*;

import org.junit.Test;
import org.tutske.rest.HttpRequest;
import org.tutske.rest.data.RestStructure;
import org.tutske.rest.exceptions.InvalidJwtException;
import org.tutske.rest.internals.Chain;
import org.tutske.utils.Bag;
import org.tutske.utils.Clock;

import java.util.Date;


public class JwtFilterTest {

	private static Date now = new Date (1_000_000_000);
	private static Date yesterday = new Date (now.getTime () - (24 * 60 * 60 * 1000));
	private static Date tomorrow = new Date (now.getTime () + (24 * 60 * 60 * 1000));

	private static class Auth {
		public final String sub;
		public final Date iat;
		public final Date exp;

		public Auth (String sub) {
			this (sub, yesterday, tomorrow);
		}

		public Auth (String sub, Date iat, Date exp) {
			this.sub = sub;
			this.iat = iat;
			this.exp = exp;
		}
	}

	private Bag<String, Object> context = new Bag<String, Object> ();
	private HttpRequest request = when (mock (HttpRequest.class).context ()).thenReturn (context).getMock ();
	private Clock clock = when (mock (Clock.class).now ()).thenReturn (now).getMock ();

	private Crypt crypt = new AESCrypt ("super secret key material");
	private JsonWebToken token = JsonWebToken.fromData (new Auth ("jhon.doe@example.com"));
	private JsonWebToken signed = crypt.sign (token);
	private JwtFilter filter = new JwtFilter (crypt, Auth.class).setClock (clock);

	@Test
	public void it_should_use_the_provided_clock () throws Exception {
		Clock clock = mock (Clock.class);
		when (clock.now ()).thenReturn (now);
		when (request.getHeader ("Authorization")).thenReturn (signed.toString ());

		filter.setClock (clock).call (request, mock (Chain.class));

		verify (clock, atLeast (1)).now ();
	}

	@Test
	public void it_should_call_the_rest_of_the_chain () throws Exception {
		Chain<HttpRequest, RestStructure> chain = mock (Chain.class);
		when (request.getHeader ("Authorization")).thenReturn (null);

		filter.call (request, chain);

		verify (chain).call (any ());
	}

	@Test
	public void it_should_call_the_rest_of_the_chain_if_the_token_in_not_valid () throws Exception {
		Chain<HttpRequest, RestStructure> chain = mock (Chain.class);
		when (request.getHeader ("Authorization")).thenReturn (token.toString ());

		filter.call (request, chain);

		verify (chain).call (any ());
	}

	@Test (expected = InvalidJwtException.class)
	public void it_should_not_call_the_rest_of_the_chain_if_the_Token_is_not_valid_in_strict_mode () throws Exception {
		Chain<HttpRequest, RestStructure> chain = mock (Chain.class);
		when (request.getHeader ("Authorization")).thenReturn (token.toString ());

		filter.setStrict ().call (request, chain);
	}

	@Test
	public void it_should_call_the_rest_of_the_chain_on_an_invalid_jwt_header () throws Exception {
		Chain<HttpRequest, RestStructure> chain = mock (Chain.class);
		when (request.getHeader ("Authorization")).thenReturn ("invalid");

		filter.call (request, chain);

		verify (chain).call (any ());
	}

	@Test (expected = InvalidJwtException.class)
	public void it_should_not_call_the_rest_of_the_chain_on_an_invalid_jwt_header_in_strict_mode () throws Exception {
		Chain<HttpRequest, RestStructure> chain = mock (Chain.class);
		when (request.getHeader ("Authorization")).thenReturn ("invalid");

		filter.setStrict ().call (request, chain);

		verify (chain).call (any ());
	}

	@Test
	public void it_should_use_custom_headers () throws Exception {
		filter = new JwtFilter (crypt, Auth.class, "principal", "token", "X-Custom-Authentication");
		Chain<HttpRequest, RestStructure> chain = mock (Chain.class);
		when (request.getHeader ("X-Custom-Authentication")).thenReturn (signed.toString ());

		filter.setClock (clock).setStrict ().call (request, chain);

		verify (chain).call (any ());
	}

	@Test
	public void it_should_put_the_principal_in_the_context () throws Exception {
		when (request.getHeader ("Authorization")).thenReturn (signed.toString ());

		filter.call (request, mock (Chain.class));

		verify (request, atLeast (1)).context ();
	}

	@Test (expected = InvalidJwtException.class)
	public void it_should_reject_tokens_with_an_invalid_exp () throws Exception {
		JsonWebToken token = JsonWebToken.fromData (new Auth ("jhon", yesterday, yesterday));
		when (request.getHeader ("Authorization")).thenReturn (crypt.sign (token).toString ());

		filter.setStrict ().call (request, mock (Chain.class));
	}

	@Test (expected = InvalidJwtException.class)
	public void it_should_reject_tokens_with_an_invalid_iat () throws Exception {
		JsonWebToken token = JsonWebToken.fromData (new Auth ("jhon", tomorrow, tomorrow));
		when (request.getHeader ("Authorization")).thenReturn (crypt.sign (token).toString ());

		filter.setStrict ().call (request, mock (Chain.class));
	}

	@Test (expected = InvalidJwtException.class)
	public void it_should_complain_abouth_tokens_without_iat_when_forcing_iat () throws Exception {
		JsonWebToken token = JsonWebToken.fromData (new Auth ("jhon", null, null));
		when (request.getHeader ("Authorization")).thenReturn (crypt.sign (token).toString ());

		filter.config (new JwtFilter.Config () {{ strict = true; forceIAT = true; }});
		filter.call (request, mock (Chain.class));
	}

	@Test (expected = InvalidJwtException.class)
	public void it_should_complain_abouth_tokens_without_nbt_when_forcing_nbt () throws Exception {
		JsonWebToken token = JsonWebToken.fromData (new Auth ("jhon", null, null));
		when (request.getHeader ("Authorization")).thenReturn (crypt.sign (token).toString ());

		filter.config (new JwtFilter.Config () {{ strict = true; forceIAT = true; }});
		filter.call (request, mock (Chain.class));
	}

}
