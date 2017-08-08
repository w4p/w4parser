package test.model;

import com.w4p.parser.annotations.W4Fetch;
import com.w4p.parser.annotations.W4Parse;

import java.util.List;

public class BBC {

    @W4Fetch(href = @W4Parse(select = "//section.module--promo//a.media__link/@href"),
            maxFetch = 5)
    private List<BBCNews> mainNews;

    public static class BBCNews {

        @W4Parse(select = "h1.story-body__h1")
        private String title;

        @W4Parse(select = "div.story-body__inner")
        private String fulltext;
    }
}
