package dev.eventmanager;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.eventmanager.config.MapperConfig;
import dev.eventmanager.users.domain.UserRole;
import dev.eventmanager.users.UserTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;

import java.security.SecureRandom;

@AutoConfigureMockMvc
@SpringBootTest
public class RootTest {

    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected UserTestUtils userTestUtils;
    @Autowired
    protected MapperConfig mapperConfig;

    protected final SecureRandom secureRandom = new SecureRandom();

    private static volatile boolean isSharedSetupDone = false;

    public static PostgreSQLContainer<?> POSTGRES_CONTAINER =
            new PostgreSQLContainer<>("postgres:15.3")
                    .withDatabaseName("postgres")
                    .withUsername("postgres")
                    .withPassword("root");

    static {
        if (!isSharedSetupDone) {
            POSTGRES_CONTAINER.start();
            isSharedSetupDone = true;
        }
    }

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("test.postgres.port", POSTGRES_CONTAINER::getFirstMappedPort);
    }

    @EventListener(ContextStoppedEvent.class)
    public void stopContainer(ContextStoppedEvent event) {
        POSTGRES_CONTAINER.stop();
    }

    public String getAuthorizationHeader(UserRole userRole) {
        return "Bearer " + userTestUtils.getJwtToken(userRole);
    }

    public int getRandomInt() {
        return secureRandom.nextInt();
    }
}
