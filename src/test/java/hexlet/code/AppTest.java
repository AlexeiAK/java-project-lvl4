package hexlet.code;

import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;
import io.ebean.DB;
import io.ebean.Transaction;
import io.javalin.Javalin;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;


class AppTest {
    private static Javalin app;
    private static String baseUrl;
    private static Transaction transaction;
    private static MockWebServer mockWebServer;
    private final static Dispatcher dispatcher = new Dispatcher() {
        @Override
        public MockResponse dispatch (RecordedRequest request) throws InterruptedException {
            String pageWithAllParameters;
            String pageWithSomeParameters;
            try {
                pageWithAllParameters = Files.readString(Path.of("src/test/resources/mockPage.html"));
//                pageWithSomeParameters = Files.readString(Path.of("src/test/resources/mockPage.html"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }


            return switch (request.getPath()) {
                case "/v1/page01/" -> new MockResponse().setResponseCode(200).setBody(pageWithAllParameters);
//                case "/v1/page02/" -> new MockResponse().setResponseCode(200).setBody(pageWithSomeParameters);
                default -> new MockResponse().setResponseCode(404);
            };
        }
    };

    @BeforeAll
    public static void beforeAll() throws IOException {
        app = App.getApp();
        app.start(0);
        int port = app.port();
        baseUrl = "http://localhost:" + port;

        mockWebServer = new MockWebServer();
//        Path filePath = Path.of("src/test/resources/mockPage.html");
//        String pageWithAllParameters = Files.readString(filePath);
//        mockWebServer.enqueue(new MockResponse().setBody(html));
        mockWebServer.setDispatcher(dispatcher);
        mockWebServer.start();
    }

    @AfterAll
    public static void afterAll() throws IOException {
        app.stop();
        mockWebServer.shutdown();
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

    @Test
    void testChecks() throws MalformedURLException {
        HttpUrl url01 = mockWebServer.url("/v1/page01/");
        HttpUrl url02 = mockWebServer.url("/v1/page02/");

        String mockSiteUrl = mockWebServer.url(url01.toString()).toString();
        URL parsedUrl = new URL(mockSiteUrl);
        String hostName = parsedUrl.getProtocol() + "://" + parsedUrl.getAuthority();

        Unirest
            .post(baseUrl + "/urls")
            .field("url", mockSiteUrl)
            .asString();

        Url actualUrl = new QUrl()
            .name.equalTo(hostName)
            .findOne();

        // Check that mock site exists
        HttpResponse<String> response = Unirest
            .get(baseUrl + "/urls/" + actualUrl.getId())
            .asString();
        String content = response.getBody();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(content).contains(hostName);

        // Check that UrlController.checkUrl works
        // if all HTML parameters exist
        Unirest
            .post(baseUrl + "/urls/" + actualUrl.getId() + "/checks")
            .asString();

        String pageOfMockSiteCheck = Unirest
            .get(baseUrl + "/urls/" + actualUrl.getId())
            .asString()
            .getBody();

        final String expectedDescription = "Description 01";
        final String expectedTitle = "Title 01";
        final String expectedH1 = "The h1 01";

        assertThat(pageOfMockSiteCheck).contains(expectedDescription);
        assertThat(pageOfMockSiteCheck).contains(expectedTitle);
        assertThat(pageOfMockSiteCheck).contains(expectedH1);
    }

}
