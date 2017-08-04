package test.model;

import com.w4.parser.annotations.W4Fetch;
import com.w4.parser.annotations.W4Parse;

public class BBC {

    @W4Parse(select = "li[class=media-list__item media-list__item--1]")
    private BBCNews mainNews;

    public static class BBCNews {

        @W4Parse(select = "h3.media__title/a")
        private String title;

        @W4Parse(select = "p.media__summary")
        private String desc;
    }
}
