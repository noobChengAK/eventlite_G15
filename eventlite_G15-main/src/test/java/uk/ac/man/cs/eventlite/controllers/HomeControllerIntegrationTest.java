
package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.web.reactive.server.WebTestClient;
import uk.ac.man.cs.eventlite.EventLite;



@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class HomeControllerIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {

    @LocalServerPort
    private int port;

    private WebTestClient client;

    
    @BeforeEach
    public void setup() {
        client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
    }

    @Test
    public void testGetIndex() {
        client.get().uri("/").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk()
        .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML).expectBody(String.class).consumeWith(res-> {
			assertThat(res.getResponseBody(), containsString("Upcoming 3 Events"));
			assertThat(res.getResponseBody(), containsString("Most Used Venues"));
		});;
    }
}