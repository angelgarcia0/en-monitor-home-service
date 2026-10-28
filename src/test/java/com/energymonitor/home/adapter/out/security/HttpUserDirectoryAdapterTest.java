package com.energymonitor.home.adapter.out.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.energymonitor.home.application.port.out.UserDirectoryPort.UserSummary;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class HttpUserDirectoryAdapterTest {

    private MockRestServiceServer security;
    private HttpUserDirectoryAdapter directory;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("http://security:8082")
                .defaultHeader("X-Internal-Token", "the-token");
        security = MockRestServiceServer.bindTo(builder).build();
        directory = new HttpUserDirectoryAdapter(builder.build());
    }

    @Test
    void readsTheProfilesOfTheUsersInOneRequest() {
        security.expect(requestTo("http://security:8082/internal/users/profiles"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-Internal-Token", "the-token"))
                .andExpect(content().json("""
                        {"userIds":["use0000001","use0000002"]}
                        """))
                .andRespond(withSuccess("""
                        {"profiles":[
                          {"userId":"use0000001","name":"Ana","lastName":"Rojas","email":"ana@example.com","profileImage":"data:image/png;base64,AAAA"}
                        ]}
                        """, MediaType.APPLICATION_JSON));

        Map<String, UserSummary> found = directory.findByIds(List.of("use0000001", "use0000002"));

        assertThat(found).containsOnlyKeys("use0000001");
        assertThat(found.get("use0000001"))
                .isEqualTo(new UserSummary("Ana", "Rojas", "ana@example.com",
                        "data:image/png;base64,AAAA"));
        security.verify();
    }

    @Test
    void listsTheMembersWithoutNamesWhenTheSecurityServiceFails() {
        security.expect(requestTo("http://security:8082/internal/users/profiles"))
                .andRespond(withServerError());

        assertThat(directory.findByIds(List.of("use0000001"))).isEmpty();
    }

    @Test
    void asksNothingForNoUsers() {
        assertThat(directory.findByIds(List.of())).isEmpty();
        security.verify();
    }
}
