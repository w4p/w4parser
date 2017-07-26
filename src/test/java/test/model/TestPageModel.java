package test.model;

import com.w4.parser.annotations.W4Parse;
import com.w4.parser.annotations.W4RegExp;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@W4Parse(xpath = "/body")
public class TestPageModel {

    @W4Parse(xpath = "//h1:eq(0)", postProcess = {@W4RegExp(search = "^(\\w+)\\s.*$", replace = "$1")})
    private String title;

    @W4Parse(xpath = "//h1:eq(0)/@class")
    private String titleClass;

    @W4Parse(xpath = "//h1:eq(1)/@class", defaultValue = "default-class")
    private String defaultTitleClass;

    @W4Parse(xpath = "//h2:eq(1)")
    private String notfoundTitle;

    @W4Parse(xpath = "//a", maxCount = 5)
    private List<TestLink> links;

    @W4Parse(xpath = "/body//div[class='review']")
    private TestReview review;

    @W4Parse(xpath = "/body//div[class='review-notfound']")
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

        @W4Parse(xpath = "@text()")
        private String text;

        @W4Parse(xpath = "@href")
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

        @W4Parse(xpath = "/h3")
        private String author;

        @W4Parse(xpath = "/div[class='comment']")
        private String comment;

        @W4Parse(xpath = "/span[class=\"rating\"]")
        private int rating;

        @W4Parse(xpath = "/span[class=\"boolean\"]")
        private boolean enabled;

        @W4Parse(xpath = "/span[class=\"float\"]")
        private float floatVal;

        @W4Parse(xpath = "/span[class*=\"floatValValid\"]")
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
