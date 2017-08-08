package test.model;

import com.w4p.parser.annotations.W4Fetch;
import com.w4p.parser.annotations.W4Parse;

@W4Parse(select = "div.post", maxCount = 3)
public class HabraArticle {

        @W4Parse(select = "//*[class=\"post__title\"]")
        private String title;

        @W4Fetch(href = @W4Parse(select = {"//a.post-author__link/@href"}))
        private Habrahabr.HabrahabrUser author;
}
