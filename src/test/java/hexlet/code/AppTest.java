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

    @Test
    void testUrl() {
        HttpResponse<String> response = Unirest
            .get(baseUrl + "/urls/2")
            .asString();
        String content = response.getBody();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(content).contains("https://id.heroku.com");
    }


    @Test
    void testAddSuccessAndAlreadyAdd() {
        String requestUrl01 = "https://www.youtube.com/c/google";
        String parsedUrl01 = "www.youtube.com";

        HttpResponse response01 = Unirest
            .post(baseUrl + "/urls")
            .field("url", requestUrl01)
            .asEmpty();

        assertThat(response01.getStatus()).isEqualTo(302);
        assertThat(response01.getHeaders().getFirst("Location")).isEqualTo("/urls");


        String requestUrl02 = "https://www.youtube.com:800";
        String parsedUrl02 = "www.youtube.com:800";

        HttpResponse response02 = Unirest
            .post(baseUrl + "/urls")
            .field("url", requestUrl02)
            .asEmpty();


        HttpResponse<String> responseWithSuccessAdd = Unirest
            .get(baseUrl + "/urls")
            .asString();
        String body01 = responseWithSuccessAdd.getBody();

        assertThat(body01).contains(parsedUrl01);
        assertThat(body01).contains(parsedUrl02);
        assertThat(body01).contains("Страница успешно добавлена");


        HttpResponse response03 = Unirest
            .post(baseUrl + "/urls")
            .field("url", requestUrl01)
            .asEmpty();

        HttpResponse<String> responseWithAlreadyAdd = Unirest
            .get(baseUrl + "/urls")
            .asString();
        String body02 = responseWithAlreadyAdd.getBody();

        assertThat(body02).contains("Страница уже существует");
    }

    @Test
    void testIncorrectAdd() {
        String requestUrl01 = "youtube.com/c/google";

        HttpResponse response01 = Unirest
            .post(baseUrl + "/urls")
            .field("url", requestUrl01)
            .asEmpty();

        assertThat(response01.getHeaders().getFirst("Location")).isEqualTo("/");

        HttpResponse<String> responseWithIncorrectAdd = Unirest
            .get(baseUrl)
            .asString();
        String body01 = responseWithIncorrectAdd.getBody();

        assertThat(body01).contains("Некорректный URL");
    }

}
