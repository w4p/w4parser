package test;

import com.w4.parser.processor.W4Parser;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.data.TestHtmlData;
import test.model.TestPageModel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ParserTest {
    private static final Logger LOG = LoggerFactory.getLogger(ParserTest.class);

    @Test
    public void testParser() {
        TestPageModel model = W4Parser.parse(TestHtmlData.htmlReviewData(), TestPageModel.class);

        if (model == null) {
            fail( "Something wrong with parser. TestPageModel is null");
        }

        assertEquals(model.getTitle(), "Test");
        assertEquals(model.getReview().getAuthor(), "Author");
        assertEquals(model.getReview().getComment(), "Comment text");
        assertEquals(model.getLinks().size(), 5);

        assertEquals(model.getLinks().get(0).getText(), "A link 1");
        assertEquals(model.getLinks().get(1).getText(), "A link 2");
        assertEquals(model.getLinks().get(2).getText(), "A link 3");
        assertEquals(model.getLinks().get(3).getText(), "A link 4");
        assertEquals(model.getLinks().get(4).getText(), "A link 5");

        assertEquals(model.getNotfoundTitle(), null);
        assertEquals(model.getNotfoundReview(), null);


        LOG.info("Result: {}", model);

    }

}
