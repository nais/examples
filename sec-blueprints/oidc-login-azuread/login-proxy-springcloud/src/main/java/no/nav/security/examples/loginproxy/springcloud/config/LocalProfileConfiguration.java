package no.nav.security.examples.loginproxy.springcloud.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Profile("local")
@PropertySource("classpath:application-local.secrets.properties")
public class LocalProfileConfiguration {

}
