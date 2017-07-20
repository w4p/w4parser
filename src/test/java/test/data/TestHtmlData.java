package test.data;

public class TestHtmlData {

    public static String htmlReviewData() {
        return "<html>" +
                "<body>" +
                    "<div class=\"testPage\">" +
                        "<h1 class=\"title-class class-title\">Test Page</h1>" +
                        "<h1>Another Test Page title</h1>" +
                        "<div class=\"review\">" +
                            "<h3>Author</h3>" +
                            "<div class=\"comment\">Comment text</div>" +
                            "<div class=\"comment\">Another Comment text</div>" +
                            "<span class=\"rating\">4</span>" +
                            "<span class=\"boolean\">true</span>" +
                            "<span class=\"float\">4s.007</span>" +
                            "<span class=\"float floatValValid\">4.007</span>" +
                            "<a href=\"http://www.google.com\">A link 1</a>" +
                            "<a href=\"http://www.google.com2\">A link 2</a>" +
                            "<a href=\"http://www.google.com3\">A link 3</a>" +
                            "<a href=\"http://www.google.com4\">A link 4</a>" +
                            "<a href=\"http://www.google.com5\">A link 5</a>" +
                            "<a href=\"http://www.google.com6\">A link 6</a>" +
                        "</div>" +
                        "<div class=\"review\">" +
                            "<h3>Another Author</h3>" +
                            "<div class=\"comment\">Another Comment text</div>" +
                            "<div class=\"comment\">Second Another Comment text</div>" +
                        "</div>" +
                    "</div>" +
                "</body>" +
                "</html>";
    }
}
