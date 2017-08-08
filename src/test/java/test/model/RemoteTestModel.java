package test.model;

import com.w4p.parser.annotations.W4Fetch;
import com.w4p.parser.annotations.W4Parse;
import com.w4p.parser.annotations.W4RegExp;
import lombok.Getter;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Getter
@W4Fetch(timeout = 8000, timeUnit = TimeUnit.MILLISECONDS)
public class RemoteTestModel {

    @W4Parse(select = "/h1[class=\"bcrs\"]//b")
    private String category;

    @W4Parse(select = "//*[class*=\"sresult\"]")
    private List<Item> items;

    @W4Fetch(url = "https://habrahabr.ru/users/")
    private HabrahabrModel habrahabrModel;

    @Override
    public String toString() {
        return "RemoteTestModel{" +
                "category='" + category + '\'' +
                ", items=" + items +
                ", habrahabrModel=" + habrahabrModel +
                '}';
    }

    public static class Item {

        @W4Parse(select = "//h3[class=\"lvtitle\"]/a")
        private String name;

        @W4Parse(
                select = {"//span[class=\"prRange\"]", "li[class*=\"lvprice\"]/span"},
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
