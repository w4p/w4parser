package test.model;

import com.w4.parser.annotations.W4Fetch;
import com.w4.parser.annotations.W4Xpath;
import lombok.Getter;

import java.util.List;

@Getter
public class HabrahabrModel {

    @W4Xpath(path = "a[class*=\"nav-links__item-link_current\"]")
    private String title;

    @W4Fetch(path = @W4Xpath(path = "//a[class*=\"nav-links__item-link\"]/@href"), maxDepth = 2, maxFetch = 2)
    private List<HabrahabrModel> habrahabrModelList;

    @Override
    public String toString() {
        return "HabrahabrModel{" +
                "title='" + title + '\'' +
                ", habrahabrModelList=" + habrahabrModelList +
                '}';
    }
}
