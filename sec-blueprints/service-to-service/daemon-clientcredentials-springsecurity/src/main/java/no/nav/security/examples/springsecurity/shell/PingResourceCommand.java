package no.nav.security.examples.springsecurity.shell;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.security.examples.springsecurity.client.DownstreamResourceClient;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
class PingResourceCommand {

    private final DownstreamResourceClient downstreamResourceClient;

    public PingResourceCommand(DownstreamResourceClient downstreamResourceClient) {
        this.downstreamResourceClient = downstreamResourceClient;
    }

    @ShellMethod("ping downstream api with token retrieved from the client_credentials grant")
    String ping() throws JsonProcessingException {
        String response = downstreamResourceClient.ping();
        JsonNode json = new ObjectMapper().readValue(response, JsonNode.class);
        return json.toPrettyString();
    }
}
