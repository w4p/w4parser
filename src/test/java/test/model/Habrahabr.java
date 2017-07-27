package test.model;

import com.w4.parser.annotations.W4Fetch;
import com.w4.parser.annotations.W4Parse;
import lombok.Getter;

import java.util.List;

@Getter
@W4Fetch(url = "https://habrahabr.ru/top/", timeout = 60000)
public class Habrahabr {

    @W4Parse(select = "a[class*=\"nav-links__item-link_current\"]")
    private String title;

    @W4Fetch(url = "https://habrahabr.ru/users/wbb/")
    private HabrahabrUser wbbUser;

    @W4Fetch(href = @W4Parse(select = "//a[class=\"post__title_link\"]/@href"), maxFetch = 2)
    private List<HabrahabrArticle> articleList;


    @Getter
    public static class HabrahabrUser {

        @W4Parse(select = {"a[class=\"page-header__nickname\"]", "a[class=\"page-header__info-title\"]"})
        private String username;
    }

    @Getter
    public static class HabrahabrArticle {

        @W4Parse(select = "//*[class=\"post__title\"]")
        private String title;

        @W4Fetch(href = @W4Parse(select = {"//a[class=\"author-info__nickname\"]/@href", "//a[class=\"page-header__info-title\"]/@href"}))
        private HabrahabrUser author;

//        @W4Parse(select = "//div[class*=\"post__body_full\"]", html = false)
//        private String text;


    }

}
