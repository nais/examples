package no.nav.security.examples.loginproxy.springcloud;

import no.nav.security.examples.mock.MockResourceServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;

@SpringBootApplication
public class LoginProxyMockApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(LoginProxyMockApplication.class);
        app.setAdditionalProfiles("local");
        app.run(args);
    }
}

@Configuration
class MockResourceServerConfiguration {

    private final MockResourceServer mockResourceServer;

    MockResourceServerConfiguration(@Value("${mockresourceserver.port}") int port) {
        this.mockResourceServer = new MockResourceServer(port);
        this.mockResourceServer.start();
    }

    @PreDestroy
    void shutdown(){
        this.mockResourceServer.shutdown();
    }
}
