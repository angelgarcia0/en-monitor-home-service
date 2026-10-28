package com.energymonitor.home.infrastructure;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

/**
 * The whole filter chain, with a token signed the way the security service signs them: the API
 * needs one, the health probe does not, and anything outside {@code /api/v1} stays closed.
 */
@SpringBootTest
@AutoConfigureMockMvc
class HomeApiSecurityTest extends JwtKeyedTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void theApiRefusesARequestWithoutAToken() throws Exception {
        mvc.perform(get("/api/v1/home-types"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void theApiRefusesATokenItCannotVerify() throws Exception {
        mvc.perform(get("/api/v1/home-types")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer not-a-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void theApiAnswersATokenOfTheSecurityService() throws Exception {
        mvc.perform(get("/api/v1/home-types")
                        .header(HttpHeaders.AUTHORIZATION, bearer("use0000001")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(6));
    }

    @Test
    void theHealthProbeNeedsNoToken() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void anythingOutsideTheApiIsDenied() throws Exception {
        mvc.perform(get("/internal/devices/dev0000001")
                        .header(HttpHeaders.AUTHORIZATION, bearer("use0000001")))
                .andExpect(status().isForbidden());
    }
}
