package br.com.tecsinapse.camel.cdi;

import org.aeonbits.owner.ConfigFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;
import java.io.Serializable;

public class PropertiesProducer implements Serializable {

	private static final long serialVersionUID = 2005293064618293053L;

	@Named("envProperties")
	@ApplicationScoped
	@Produces
	public EnvProperties getEnvProps() {
		return ConfigFactory.create(EnvProperties.class);
	}

}