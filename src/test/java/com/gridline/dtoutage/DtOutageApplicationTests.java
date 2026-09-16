package com.gridline.dtoutage;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test: the application context should wire up cleanly (entities,
 * repositories, security config, role hierarchy, etc). JwtDecoder is mocked
 * out since there's no real Entra ID tenant reachable in CI.
 */
@SpringBootTest
@ActiveProfiles("test")
class DtOutageApplicationTests {

    @MockBean
    JwtDecoder jwtDecoder;

    @Test
    void contextLoads() {
    }
}
