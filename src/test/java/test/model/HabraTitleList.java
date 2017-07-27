package test.model;

import com.w4.parser.annotations.W4Fetch;
import com.w4.parser.annotations.W4Parse;
import lombok.Getter;

import java.util.List;

@Getter
@W4Fetch(url = "https://habrahabr.ru/top/")
public class HabraTitleList {


    @W4Parse(select = "//a[class*=\"post__title_link\"]")
    @W4Fetch(href = @W4Parse(select = "//a[class*=\"toggle-menu__item-link_pagination\"]/@href"),
             maxDepth = 2, maxFetch = 2)
    private List<String> titleLists;

//    @Getter
//    public static class TitleList {
//
//        @W4Parse(select = "//a[class*=\"post__title_link\"]")
//        private List<String> titles;
//    }
}
