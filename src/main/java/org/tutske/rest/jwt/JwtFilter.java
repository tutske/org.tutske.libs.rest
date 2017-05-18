package org.tutske.rest.jwt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tutske.rest.HttpRequest;
import org.tutske.rest.RestFilter;
import org.tutske.rest.data.RestObject;
import org.tutske.rest.data.RestStructure;
import org.tutske.rest.exceptions.InvalidJwtException;
import org.tutske.rest.internals.Chain;
import org.tutske.utils.Clock;

import java.util.Date;


/**
 * Filter that validates a jwt from a request header.
 *
 * Checks for token validity with the provided crypt object.
 * Checks for date validity (iat before now, nbt before now, exp after now).
 *
 * Setting strict will make this class throw an response exception instead of
 * not setting the principal. Non strict mode can be used when you want to use
 * the same url to serve some response to anonymous users as well.
 */
public class JwtFilter implements RestFilter {

	public static class Config {
		public Boolean strict = null;
		public Boolean forceToken = null;
		public Boolean forceEXP = null;
		public Boolean forceIAT = null;
		public Boolean forceNBT = null;

		public Config () {
		}

		private Config (String d) {
			strict = false;
			forceToken = false;
			forceEXP = true;
			forceIAT = false;
			forceNBT = false;
		}

		private void merge (Config config) {
			if ( config.strict != null ) { strict = config.strict; }
			if ( config.forceToken != null ) { forceToken = config.forceToken; }
			if ( config.forceEXP != null ) { forceEXP = config.forceEXP; }
			if ( config.forceIAT != null ) { forceIAT = config.forceIAT; }
			if ( config.forceNBT != null ) { forceNBT = config.forceNBT; }
		}
	}

	private static final Logger logger = LoggerFactory.getLogger (JwtFilter.class);

	private final Crypt crypt;
	private final Class<?> clazz;
	private final String principal;
	private final String token;
	private final String header;
	private final Config config = new Config ("default");
	private Clock clock = new Clock.SystemClock ();

	public JwtFilter (Crypt crypt, Class<?> clazz) {
		this (crypt, clazz, "principal", "token", "Authorization");
	}

	public JwtFilter (Crypt crypt, Class<?> clazz, String principal, String token, String header) {
		this.clazz = clazz;
		this.crypt = crypt;
		this.principal = principal;
		this.token = token;
		this.header = header;
	}

	public JwtFilter setStrict () {
		this.config.strict = true;
		return this;
	}

	public JwtFilter config (Config config) {
		this.config.merge (config);
		return this;
	}

	public JwtFilter setClock (Clock clock) {
		this.clock = clock;
		return this;
	}

	@Override
	public RestStructure call (HttpRequest source, Chain<HttpRequest, RestStructure> chain) throws Exception {
		String authorization = source.getHeader (header);

		if ( authorization == null || authorization.isEmpty () ) {
			if ( ! config.forceToken ) { return chain.call (source); }
			else { return cutShort ("No authorization token provided", source, chain); }
		}

		JsonWebToken token;
		try { token = JsonWebToken.fromString (authorization); }
		catch (Exception ignore) {
			logger.info ("Failed on processing {} header as jwt.", header, ignore);
			return cutShort ("Token could not be parsed", source, chain);
		}

		if ( ! crypt.validate (token) ) {
			return cutShort ("Token is not valid, hmac mismatch", source, chain);
		}

		Timed timed = token.getPayload (Timed.class);
		if ( invalidDates (timed) ) {
			return cutShort ("The iat, nbt or exp dates are invalid", source, chain);
		}

		source.context ().put (principal, token.getPayload (clazz));
		source.context ().put (this.token, token);
		return chain.call (source);
	}

	private RestStructure cutShort (String reason, HttpRequest source, Chain<HttpRequest, RestStructure> chain) throws Exception {
		if ( config.strict ) {
			String h = source.getHeader (header);
			throw new InvalidJwtException (reason, new RestObject () {{
				v ("token", h == null ? "missing" : h);
			}});
		}
		return chain.call (source);
	}

	private boolean invalidDates (Timed timed) {
		Date now = clock.now ();
		return (config.forceEXP && timed.exp == null) ||
			(config.forceIAT && timed.iat == null) ||
			(config.forceNBT && timed.nbt == null) ||
			before (now, timed.exp) ||
			after (now, timed.iat) ||
			after (now, timed.nbt);
	}

	private boolean before (Date now, Date test) {
		return test != null && test.before (now);
	}

	private boolean after (Date now, Date test) {
		return test != null && test.after (now);
	}

	private static class Timed {
		public Date exp;
		public Date iat;
		public Date nbt;
	}

}
