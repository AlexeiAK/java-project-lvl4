package hexlet.code;

import io.ebean.DB;
import io.ebean.Transaction;
import io.javalin.Javalin;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;


class AppTest {
    private static Javalin app;
    private static String baseUrl;
    private static Transaction transaction;

    @BeforeAll
    public static void beforeAll() {
        app = App.getApp();
        app.start(0);
        int port = app.port();
        baseUrl = "http://localhost:" + port;
    }

    @AfterAll
    public static void afterAll() {
        app.stop();
    }

    @BeforeEach
    void beforeEach() {
        transaction = DB.beginTransaction();
    }

    @AfterEach
    void afterEach() {
        transaction.rollback();
    }


    @Test
    void testRoot() {
        HttpResponse<String> response = Unirest
            .get(baseUrl)
            .asString();
        String content = response.getBody();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(content).contains("Анализатор страниц");
    }

    @Test
    void testUrls() {
        HttpResponse<String> response = Unirest
            .get(baseUrl + "/urls")
            .asString();
        String content = response.getBody();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(content).contains("https://gitlab.com");
        assertThat(content).contains("https://id.heroku.com");
    }

//    @Test
//    void testUrl() {
//        HttpResponse<String> response = Unirest
//            .get(baseUrl + "/urls/2")
//            .asString();
//        String content = response.getBody();
//
//        assertThat(response.getStatus()).isEqualTo(200);
//        assertThat(content).contains("https://id.heroku.com");
//    }
}
