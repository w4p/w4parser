package test.model;

import com.w4.parser.annotations.W4Fetch;
import com.w4.parser.annotations.W4Xpath;

import java.util.List;

@W4Fetch(url = "https://habrahabr.ru/top/")
public class HabraTitleList {

    @W4Xpath(path = "//div[class*=\"post\"]")
    @W4Fetch(href = @W4Xpath(path = "//a[class*=\"toggle-menu__item-link_pagination\"]/@href"), maxDepth = 2)
    private List<HabraItem> titles;

    public static class HabraItem {

        @W4Xpath(path = "//a[class=\"post__title_link\"]")
        private List<String> title;
    }

}
