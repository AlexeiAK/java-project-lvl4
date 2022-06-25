package hexlet.code.controllers;

import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;
import io.ebean.PagedList;
import io.javalin.http.Handler;
import io.javalin.http.NotFoundResponse;

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

        PagedList<Url> pagedArticles = new QUrl()
            .setFirstRow(offset)
            .setMaxRows(rowsPerPage)
            .orderBy().id.asc()
            .findPagedList();
        List<Url> urls = pagedArticles.getList();

        int lastPage = pagedArticles.getTotalPageCount() + 1;
        int currentPage = pagedArticles.getPageIndex() + 1;
        List<Integer> pages = IntStream
            .range(1, lastPage)
            .boxed()
            .collect(Collectors.toList());

        ctx.attribute("urls", urls);
        ctx.attribute("pages", pages);
        ctx.attribute("currentPage", currentPage);

        ctx.render("urls/urls.html");
    };


    public static Handler addUrl = ctx -> {
        String urlFromForm = ctx.formParam("url");
        String hostName = null;

        try {
            URL parsedUrl = new URL(urlFromForm);
            hostName = parsedUrl.getAuthority();

            Url url = new QUrl()
                .name.equalTo(hostName)
                .findOne();

            if (url != null) {
                ctx.sessionAttribute("flash", "Страница уже существует");
                ctx.sessionAttribute("flash-type", "info");
            } else {
                Url newUrl = new Url(hostName);
                newUrl.save();

                ctx.sessionAttribute("flash", "Страница успешно добавлена");
                ctx.sessionAttribute("flash-type", "success");
            }
        } catch (MalformedURLException e) {
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.redirect("/");
            return;
        }

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

        ctx.attribute("url", url);
        ctx.render("urls/show.html");
    };

}
