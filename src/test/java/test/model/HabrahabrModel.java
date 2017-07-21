package test.model;

import com.w4.parser.annotations.W4Xpath;
import lombok.Getter;

@Getter
public class HabrahabrModel {

    @W4Xpath(path = "div[class=\"page-header__title\"]")
    private String title;

    @Override
    public String toString() {
        return "HabrahabrModel{" +
                "title='" + title + '\'' +
                '}';
    }
}
