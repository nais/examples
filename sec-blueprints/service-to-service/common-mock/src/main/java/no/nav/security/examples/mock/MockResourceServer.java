package no.nav.security.examples.mock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.ParseException;
import java.util.Optional;

@Slf4j
public class MockResourceServer {

    private final int port;
    private final MockWebServer server;

    public MockResourceServer(int port) {
        this.port = port;
        this.server = new MockWebServer();
    }

    public URL getServerUrl(){
        try {
            return URI.create("http://localhost:" + server.getPort()).toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public void start() {
        this.server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                log.info("received request on url={} with headers={}", request.getRequestUrl(), request.getHeaders());
                return mockResponse(request);
            }
        });
        try {
            this.server.start(port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void shutdown() {
        try {
            this.server.shutdown();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private MockResponse mockResponse(RecordedRequest request) {
        return new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json;charset=UTF-8")
                .setBody(jsonResponse(parseBearerToken(request)));
    }

    private JWTClaimsSet parseBearerToken(RecordedRequest request) {
        String token = Optional.ofNullable(request.getHeader("Authorization"))
                .map(s -> s.split(" "))
                .filter(pair -> pair.length == 2)
                .filter(pair -> pair[0].trim().equalsIgnoreCase("BEARER"))
                .map(pair -> pair[1].trim())
                .orElse(null);
        try {
            return token != null ? JWTParser.parse(token).getJWTClaimsSet() : null;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private String jsonResponse(JWTClaimsSet jwtClaimsSet) {
        try {
            return new ObjectMapper().writeValueAsString(
                    new Response("pong", jwtClaimsSet));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Data
    @AllArgsConstructor
    class Response {
        private String ping;
        private JWTClaimsSet accessToken;
    }
}
