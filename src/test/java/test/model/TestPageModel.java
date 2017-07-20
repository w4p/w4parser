package test.model;

import com.w4.parser.annotations.W4RegExp;
import com.w4.parser.annotations.W4Xpath;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@W4Xpath(path = "/body")
public class TestPageModel {

    @W4Xpath(path = "//h1:eq(0)", postProcess = {@W4RegExp(search = "^(\\w+)\\s.*$", replace = "$1")})
    private String title;

    @W4Xpath(path = "//h1:eq(0)/@class")
    private String titleClass;

    @W4Xpath(path = "//h1:eq(1)/@class", defaultValue = "default-class")
    private String defaultTitleClass;

    @W4Xpath(path = "//h2:eq(1)")
    private String notfoundTitle;

    @W4Xpath(path = "//a", maxCount = 5)
    private List<TestLink> links;

    @W4Xpath(path = "/body//div[class='review']")
    private TestReview review;

    @W4Xpath(path = "/body//div[class='review-notfound']")
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

        @W4Xpath(path = "@text()")
        private String text;

        @W4Xpath(path = "@href")
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

        @W4Xpath(path = "/h3")
        private String author;

        @W4Xpath(path = "/div[class='comment']")
        private String comment;

        @W4Xpath(path = "/span[class=\"rating\"]")
        private int rating;

        @W4Xpath(path = "/span[class=\"boolean\"]")
        private boolean enabled;

        @W4Xpath(path = "/span[class=\"float\"]")
        private float floatVal;

        @W4Xpath(path = "/span[class*=\"floatValValid\"]")
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
