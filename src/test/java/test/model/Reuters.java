package test.model;

import com.w4.parser.annotations.W4Fetch;
import com.w4.parser.annotations.W4Parse;
import lombok.Getter;

import java.util.List;

@Getter
@W4Fetch(url = "http://feeds.feedburner.com/Mobilecrunch")
public class Reuters {


    @W4Parse(xpath = "//item")
    private List<ReuterNews> newsList;


    @Getter
    public static class ReuterNews {

        @W4Parse(xpath = "title")
        private String title;

        @W4Parse(xpath = "description")
        private String description;

        @W4Parse(xpath = "link")
        private String link;


    }
}
