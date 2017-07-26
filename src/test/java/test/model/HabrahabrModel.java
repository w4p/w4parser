package test.model;

import com.w4.parser.annotations.W4Fetch;
import com.w4.parser.annotations.W4Parse;
import lombok.Getter;

import java.util.List;

@Getter
public class HabrahabrModel {

    @W4Parse(xpath = "a[class*=\"nav-links__item-link_current\"]")
    private String title;

    @W4Fetch(href = @W4Parse(xpath = "//a[class*=\"nav-links__item-link\"]/@href"), maxDepth = 2, maxFetch = 2)
    private List<HabrahabrModel> habrahabrModelList;

    @Override
    public String toString() {
        return "HabrahabrModel{" +
                "title='" + title + '\'' +
                ", habrahabrModelList=" + habrahabrModelList +
                '}';
    }
}
