package test.model;

import com.w4.parser.annotations.W4Parse;
import com.w4.parser.annotations.W4RegExp;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@W4Parse(select = "/body")
public class TestPageModel {

    @W4Parse(select = "//h1:eq(0)", postProcess = {@W4RegExp(search = "^(\\w+)\\s.*$", replace = "$1")})
    private String title;

    @W4Parse(select = "//h1:eq(0)/@class")
    private String titleClass;

    @W4Parse(select = "//h1:eq(1)/@class", defaultValue = "default-class")
    private String defaultTitleClass;

    @W4Parse(select = "//h2:eq(1)")
    private String notfoundTitle;

    @W4Parse(select = "//a", maxCount = 5)
    private List<TestLink> links;

    @W4Parse(select = "/body//div[class='review']")
    private TestReview review;

    @W4Parse(select = "/body//div[class='review-notfound']")
    private TestReview notfoundReview;

    @Override
    public String toString() {
        return "TestPageModel{" +
                "title='" + title + '\'' +
                ", titleClass='" + titleClass + '\'' +
                ", defaultTitleClass='" + defaultTitleClass + '\'' +
                ", links=" + links +
                ", review=" + review +
                '}';
    }

    @Getter
    @Setter
    public static class TestLink {

        @W4Parse(select = "@text()")
        private String text;

        @W4Parse(select = "@href")
        private String href;

        @Override
        public String toString() {
            return "TestLink{" +
                    "text='" + text + '\'' +
                    ", href='" + href + '\'' +
                    '}';
        }
    }

    @Getter
    @Setter
    public static class TestReview {

        @W4Parse(select = "/h3")
        private String author;

        @W4Parse(select = "/div[class='comment']")
        private String comment;

        @W4Parse(select = "/span[class=\"rating\"]")
        private int rating;

        @W4Parse(select = "/span[class=\"boolean\"]")
        private boolean enabled;

        @W4Parse(select = "/span[class=\"float\"]")
        private float floatVal;

        @W4Parse(select = "/span[class*=\"floatValValid\"]")
        private float floatValValid;

        @Override
        public String toString() {
            return "TestReview{" +
                    "author='" + author + '\'' +
                    ", comment='" + comment + '\'' +
                    ", rating=" + rating +
                    ", enabled=" + enabled +
                    ", floatVal=" + floatVal +
                    ", floatValValid=" + floatValValid +
                    '}';
        }
    }
}
