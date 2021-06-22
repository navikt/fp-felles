package no.nav.vedtak.felles.integrasjon.rest.jersey;

import static java.util.stream.Collectors.toSet;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static org.apache.commons.lang3.reflect.ConstructorUtils.invokeConstructor;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.stream.Stream;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.GenericType;

import org.jboss.weld.exceptions.IllegalArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.exception.ManglerTilgangException;
import no.nav.vedtak.exception.VLException;

@SuppressWarnings("unchecked")
public class ExceptionTranslatingInvoker implements Invoker {

	private static final Class<? extends VLException> DEFAULT_TRANSLATED_EXCEPTION = IntegrasjonException.class;
	private static final Class<ProcessingException> DEFAULT_TRANSLATABLE_EXCEPTION = ProcessingException.class;
	private static final Class<WebApplicationException> WEBAPPLICATIONEXCEPTION = WebApplicationException.class;

	private static final Logger LOG = LoggerFactory.getLogger(ExceptionTranslatingInvoker.class);
	private final Class<? extends VLException> translatedException;
	private static final String ERRORCODE = "F-468815";

	public ExceptionTranslatingInvoker() {
		this(DEFAULT_TRANSLATED_EXCEPTION);
	}

	public ExceptionTranslatingInvoker(Class<? extends VLException> translatedException) {
		this.translatedException = translatedException;
	}

	@Override
	public void invoke(Invocation i) {
		invoke(i, DEFAULT_TRANSLATABLE_EXCEPTION);
	}

	@Override
	public <T> T invoke(Invocation i, Class<T> clazz) {
		return invoke(i, clazz, DEFAULT_TRANSLATABLE_EXCEPTION);
	}

	@SuppressWarnings("incomplete-switch")
	@Override
	public <T> T invoke(Invocation i, Class<T> clazz, Class<? extends RuntimeException>... translatables) {
		try {
			return i.invoke(clazz);
		
		} catch (RuntimeException e) {
			return handle(e, translatables);
		}
	}

	@Override
	public void invoke(Invocation i, Class<? extends RuntimeException>... translatables) {
		try {
			i.invoke();
		} catch (RuntimeException e) {
			handle(e, translatables);
		}
	}

	@Override
	public <T> T invoke(Invocation i, GenericType<T> type, Class<? extends RuntimeException>... translatables) {
		try {
			return i.invoke(type);	
		} catch (RuntimeException e) {
			return handle(e, translatables);
		}
	}

	private <T> T handle(RuntimeException e, Class<? extends RuntimeException>... translatables) {
		throw Stream.concat(Set.of(translatables).stream(), Stream.of(WEBAPPLICATIONEXCEPTION, DEFAULT_TRANSLATABLE_EXCEPTION))
		.collect(toSet())
				.stream().filter(t -> t.isAssignableFrom(e.getClass()))
				.findFirst()
				.map(t -> this.translate(e))
				.orElseGet(() -> e);
	}

	private RuntimeException translate(RuntimeException e) {
		LOG.trace("Oversetter exception {}",e.getClass().getName());
	    if (e instanceof WebApplicationException w) {
	    	return handleWebApplicationException(w);	
	    }
		return handleException(e);
	}

	private RuntimeException handleException(RuntimeException e) {
		try {
			return invokeConstructor(translatedException, "F-999999",
					"Oversatt exception " + e.getClass().getName() + " til " + translatedException.getClass().getName(),
					e);
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException
				| InstantiationException e1) {
			LOG.warn("Kunne ikke oversette {}", e.getClass().getName(), e);
			return e;
		}
	}

	private static RuntimeException handleWebApplicationException(WebApplicationException w) {
		var code = w.getResponse().getStatus();
		return switch (w.getResponse().getStatusInfo().getFamily()) {
		case CLIENT_ERROR -> {
			if (code == FORBIDDEN.getStatusCode()) {
				yield new ManglerTilgangException(ERRORCODE, "Mangler tilgang");
			}
			yield new IntegrasjonException(ERRORCODE, String.format("Uventet respons %s", code));
		}
		case SERVER_ERROR ->  new IntegrasjonException(ERRORCODE, String.format("Uventet respons %s", code));
		default -> w;
		};	
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [translatedException=" + translatedException + "]";
	}
}
