package br.com.tecsinapse.camel.cdi;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.Sources;

@Sources({"classpath:env.properties"})
public interface EnvProperties extends Config {

	@Key("host")
	String host();

	@Key("env")
	String env();

    @Key("branch")
    String branch();

    @Key("twitter.consumerKey")
    String twitterConsumerKey();

    @Key("twitter.consumerSecret")
    String twitterConsumerSecret();

    @Key("twitter.accessToken")
    String twitterAccessToken();

    @Key("twitter.accessTokenSecret")
    String twitterAccessTokenSecret();
}
