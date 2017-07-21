package test.model;

import com.w4.parser.annotations.W4Client;
import com.w4.parser.annotations.W4RegExp;
import com.w4.parser.annotations.W4Xpath;

import java.util.List;

@W4Xpath(followURL = "//a")
//@W4Client(timeout = 15000)
public class RemoteTestModel {

    @W4Xpath(path = "/h1[class=\"bcrs\"]//b")
    private String category;

    @W4Xpath(path = "//*[class*=\"sresult\"]")
    private List<Item> items;

    @Override
    public String toString() {
        return "RemoteTestModel{" +
                "category='" + category + '\'' +
                ", items=" + items +
                '}';
    }

    public static class Item {

        @W4Xpath(path = "//h3[class=\"lvtitle\"]/a")
        private String name;

        @W4Xpath(
                path = {"//span[class=\"prRange\"]", "li[class*=\"lvprice\"]/span"},
                postProcess = @W4RegExp(search = "^\\$([0-9]*\\.?[0-9]+).*$", replace = "$1"))
        private Float minPrice;

        @Override
        public String toString() {
            return "Item{" +
                    "name='" + name + '\'' +
                    ", minPrice=" + minPrice +
                    '}';
        }
    }
}
