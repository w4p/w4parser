package test.model;

import com.w4.parser.annotations.W4Fetch;
import com.w4.parser.annotations.W4Parse;
import com.w4.parser.annotations.W4RegExp;
import com.w4.parser.processor.W4Select;
import lombok.Getter;

import java.util.List;

@Getter
@W4Fetch(url = "http://m.ua/m1_magazilla.php?search_=galaxy+s7")
public class Mua {
    @W4Parse(select = "//h1[class=\"oth\"]/b")
    private String searchText;

    @W4Parse(select = "//tr[class*=\"list-tr\"]", maxCount = 3)
    private List<Gadget> gadgetList;

    @Getter
    public static class Gadget {

        @W4Parse(select = "div[class*=\"list-model-title\"]/a",
                postProcess = @W4RegExp(search = "(^.*?)\\+.*$", replace = "$1"))
        private String gadgetName;

        @W4Parse(select = "div[class*=\"list-model-title\"]/a/@id",
                postProcess = @W4RegExp(search = "^.*?(\\d+)\\/.*$", replace = "$1"))
        private String gadgetId;

        @W4Fetch(href = @W4Parse(select = "//div[class=\"l-r-ic\"]/a/@href"))
        private Mua.GadgetReviewSearch reviewSearch;
    }

    @Getter
    public static class GadgetReviewSearch {

        @W4Fetch(href = @W4Parse(select = "div[class*=\"list-more-div\"]/@jsource",
                postProcess = @W4RegExp(search = "^(.*p_end_).*$", replace = "$1=200")))
        private List<GadgetReview> gadgetReviewList;
    }

    @Getter
    @W4Parse(select = "//div[class*=\"opinion\"]")
    public static class GadgetReview {

        @W4Parse(select = "@id")
        private String id;

        @W4Parse(select = "/table/tr[valign*=\"top\"]:eq(6)//span[property*=\"v:reviewer\"]/@content",
                 postProcess = @W4RegExp(search = "[\\\\']+", replace = ""))
        private String author;

        @W4Parse(select = "/table/tr:eq(0)/td[class*=\"opinion_txt\"]")
        private String positive;

        @W4Parse(select = "/table/tr:eq(2)/td[class*=\"opinion_txt\"]")
        private String negative;

        @W4Parse(select = "/table/tr:eq(4)/td[class*=\"opinion_txt\"]")
        private String comment;

        @W4Parse(select = W4Select.CURRENT_URL)
        private String url;

    }
}
