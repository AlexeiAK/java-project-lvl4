package hexlet.code.controllers;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.query.QUrl;
import hexlet.code.domain.query.QUrlCheck;
import io.ebean.PagedList;
import io.javalin.http.Handler;
import io.javalin.http.NotFoundResponse;
import kong.unirest.core.HttpResponse;
import kong.unirest.core.Unirest;
import kong.unirest.core.UnirestException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class UrlController {
    public static Handler showUrls = ctx -> {
        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
        int rowsPerPage = 10;
        int offset = (page - 1) * rowsPerPage;

        PagedList<Url> pagedUrls = new QUrl()
            .setFirstRow(offset)
            .setMaxRows(rowsPerPage)
            .orderBy().id.asc()
            .findPagedList();
        List<Url> urls = pagedUrls.getList();

        int lastPage = pagedUrls.getTotalPageCount() + 1;
        int currentPage = pagedUrls.getPageIndex() + 1;
        List<Integer> pages = IntStream
            .range(1, lastPage)
            .boxed()
            .collect(Collectors.toList());

        ctx.attribute("urls", urls);
        ctx.attribute("pages", pages);
        ctx.attribute("currentPage", currentPage);

        ctx.render("urls/index.html");
    };


    public static Handler addUrl = ctx -> {
        String inputUrl = ctx.formParam("url");
        URL parsedUrl;

        try {
            parsedUrl = new URL(inputUrl);
        } catch (MalformedURLException e) {
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.redirect("/");
            return;
        }

        String hostName = parsedUrl.getProtocol() + "://" + parsedUrl.getAuthority();
        Url url = new QUrl()
            .name.equalTo(hostName)
            .findOne();

        if (url != null) {
            ctx.sessionAttribute("flash", "Страница уже существует");
            ctx.sessionAttribute("flash-type", "info");
            ctx.redirect("/urls");
            return;
        }

        Url newUrl = new Url(hostName);
        newUrl.save();

        ctx.sessionAttribute("flash", "Страница успешно добавлена");
        ctx.sessionAttribute("flash-type", "success");
        ctx.redirect("/urls");
    };


    public static Handler showUrl = ctx -> {
        long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);

        Url url = new QUrl()
            .id.equalTo(id)
            .findOne();

        if (url == null) {
            throw new NotFoundResponse();
        }

        List<UrlCheck> urlsChecks = new QUrlCheck()
            .url.id.equalTo(id)
            .orderBy().id.desc()
            .findList();

        ctx.attribute("url", url);
        ctx.attribute("urlChecks", urlsChecks);
        ctx.render("urls/show.html");
    };

    public static Handler checkUrl = ctx -> {
        long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);
        Url url = new QUrl().id.equalTo(id).findOne();

        try {
            HttpResponse<String> response = Unirest.get(url.getName()).asString();
            Document doc = Jsoup.parse(response.getBody());

            int statusCode = response.getStatus();
            String title = doc.title();

            Element descriptionElement = doc.selectFirst("meta[name=description]");
            String description = descriptionElement == null ? "" : descriptionElement.attr("content");

            Element h1Element = doc.selectFirst("h1");
            String h1 = h1Element == null ? "" : h1Element.text();


            UrlCheck urlCheck = new UrlCheck(statusCode, title, h1, description, url);
            urlCheck.save();

            ctx.sessionAttribute("flash", "Страница успешно проверена");
            ctx.sessionAttribute("flash-type", "success");
        } catch (UnirestException e) {
            ctx.sessionAttribute("flash", "Страница недоступна");
            ctx.sessionAttribute("flash-type", "danger");
        }

        ctx.redirect("/urls/" + id);
    };

}
