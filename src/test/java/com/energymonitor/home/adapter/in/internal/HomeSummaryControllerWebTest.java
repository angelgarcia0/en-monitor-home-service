package com.energymonitor.home.adapter.in.internal;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.energymonitor.home.application.port.in.DescribeHome;
import com.energymonitor.home.application.port.in.DescribeHome.HomeSummary;
import com.energymonitor.home.infrastructure.JwtKeyedTest;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * The home and its members energy-monitor-backend asks: who may call it, and what it answers. The
 * token is the one of the test profile; MockMvc requests come from 127.0.0.1.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Home summary")
class HomeSummaryControllerWebTest extends JwtKeyedTest {

    private static final String HOME = "/internal/homes/hom0000001";
    private static final String TOKEN = "test-only-internal-token";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private DescribeHome describeHome;

    @Test
    @DisplayName("200 with the home and its members")
    void answers() throws Exception {
        given(describeHome.describe("hom0000001")).willReturn(Optional.of(new HomeSummary(
                "hom0000001", "Casa", "hous000001", "Calle 1",
                Instant.parse("2026-01-15T10:30:00Z"), List.of("use0000001", "use0000002"))));

        mvc.perform(get(HOME).header("X-Internal-Token", TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.homeId").value("hom0000001"))
                .andExpect(jsonPath("$.name").value("Casa"))
                .andExpect(jsonPath("$.memberIds.length()").value(2));
    }

    @Test
    @DisplayName("404 for a missing home")
    void missingHome() throws Exception {
        given(describeHome.describe("hom0000001")).willReturn(Optional.empty());

        mvc.perform(get(HOME).header("X-Internal-Token", TOKEN))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("refused without the token")
    void withoutToken() throws Exception {
        mvc.perform(get(HOME)).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("a user's access token does not open it")
    void bearerIsNotEnough() throws Exception {
        mvc.perform(get(HOME).header("Authorization", bearer("use0000001")))
                .andExpect(status().isForbidden());
    }
}
