package hexlet.code.controllers;

import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;
import io.ebean.PagedList;
import io.javalin.http.Handler;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

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

        ctx.attribute("urls", urls);
        ctx.attribute("page", page);

        ctx.render("urls/urls.html");
    };

    public static Handler createUrl = ctx -> {
        String urlFromForm = ctx.formParam("url");

        URL parsedUrl = null;

        try {
            parsedUrl = new URL(urlFromForm);
            String hostName = parsedUrl.getAuthority();

            Url url = new QUrl()
                .name.equalTo(hostName)
                .findOne();

            if (url != null) {
                ctx.sessionAttribute("flash", "Страница уже существует");
            } else {
                Url newUrl = new Url(hostName);
                newUrl.save();

                ctx.sessionAttribute("flash", "Страница успешно добавлена");
            }
        } catch (MalformedURLException e) {
            ctx.sessionAttribute("flash", "Некорректный URL");
        }

        ctx.redirect("/urls");
    };

    public static Handler showUrl = ctx -> {
        long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);

        Url url = new QUrl()
            .id.equalTo(id)
            .findOne();

        ctx.sessionAttribute("url", url);
        ctx.render("urls/show.html");
    };

}
