package no.nav.security.examples.tokensupport.clientcredentials;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
class DaemonClientCredentialsTokenSupportApplication {
    public static void main(String[] args) {
        SpringApplication.run(DaemonClientCredentialsTokenSupportApplication.class, args);
    }

}
