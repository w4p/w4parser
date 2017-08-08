package test.model;

import com.w4p.parser.annotations.W4Fetch;
import com.w4p.parser.annotations.W4Parse;
import lombok.Getter;

import java.util.List;

@Getter
public class HabrahabrModel {

    @W4Parse(select = "a[class*=\"nav-links__item-link_current\"]")
    private String title;

    @W4Fetch(href = @W4Parse(select = "//a[class*=\"nav-links__item-link\"]/@href"), depth = 2, maxFetch = 2)
    private List<HabrahabrModel> habrahabrModelList;

    @Override
    public String toString() {
        return "HabrahabrModel{" +
                "title='" + title + '\'' +
                ", habrahabrModelList=" + habrahabrModelList +
                '}';
    }
}
