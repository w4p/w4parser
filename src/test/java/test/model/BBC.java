package test.model;

import com.w4.parser.annotations.W4Fetch;
import com.w4.parser.annotations.W4Parse;

public class BBC {

    @W4Fetch(href = @W4Parse(select = "//section.module--promo//li[class='media-list__item media-list__item--1']//a.media__link/@href"))
    private BBCNews mainNews;

    public static class BBCNews {

        @W4Parse(select = "h1.story-body__h1")
        private String title;

        @W4Parse(select = "div.story-body__inner")
        private String fulltext;
    }
}
