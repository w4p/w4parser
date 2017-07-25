package test.model;

import com.w4.parser.annotations.W4Fetch;
import com.w4.parser.annotations.W4Xpath;

import java.util.List;

@W4Fetch(url = "http://feeds.feedburner.com/Mobilecrunch")
public class Reuters {


    @W4Xpath(path = "//item")
    private List<ReuterNews> newsList;


    public static class ReuterNews {

        @W4Xpath(path = "title")
        private String title;

        @W4Xpath(path = "description")
        private String description;

        @W4Xpath(path = "link")
        private String link;


    }
}
