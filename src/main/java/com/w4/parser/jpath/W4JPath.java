package com.w4.parser.jpath;

import com.w4.parser.annotations.W4Parse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class W4JPath {

    private String path;
    private String attr;

    private W4Parse xpath;

    public W4JPath(W4Parse w4Parse, String xpath) {
        this.xpath = w4Parse;
        String xpathValid = xpath.replace("//", " ").trim();
        String[] pathNodes = xpathValid.split("\\/");
        StringBuilder sb = new StringBuilder();
        for (String s : pathNodes) {
            if (s != null || !s.isEmpty()) {
                if (s.startsWith("@")) {
                    //Found attribute
                    if (!s.equalsIgnoreCase("@text()")) {
                        this.attr = s.substring(1);
                    }
                } else {
                    if (sb.length() > 0) {
                        sb.append(" > ");
                    }
                    sb.append(s);
                }
            }
        }
        this.path = sb.toString();
    }

    @Override
    public String toString() {
        return "W4JPath{" +
                "xpath='" + path + '\'' +
                ", attr='" + attr + '\'' +
                ", href='" + xpath + '\'' +
                '}';
    }
}
