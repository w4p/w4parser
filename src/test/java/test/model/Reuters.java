package test.model;

import com.w4p.parser.annotations.W4Fetch;
import com.w4p.parser.annotations.W4Parse;
import lombok.Getter;

import java.util.List;

@Getter
@W4Fetch(url = "http://feeds.feedburner.com/Mobilecrunch")
public class Reuters {


    @W4Parse(select = "//item")
    private List<ReuterNews> newsList;


    @Getter
    public static class ReuterNews {

        @W4Parse(select = "title")
        private String title;

        @W4Parse(select = "description")
        private String description;

        @W4Parse(select = "link")
        private String link;


    }
}
