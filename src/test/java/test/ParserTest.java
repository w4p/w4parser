package test;

import com.w4.parser.processor.W4Parser;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.data.TestHtmlData;
import test.model.RemoteTestModel;
import test.model.TestPageModel;

import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ParserTest {
    private static final Logger LOG = LoggerFactory.getLogger(ParserTest.class);

    @Test
    public void testParser() {
        TestPageModel model = W4Parser.data(TestHtmlData.htmlReviewData()).parse(TestPageModel.class);

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

        assertEquals(model.getReview().isEnabled(), true);
        assertEquals(model.getReview().getRating(), 4);
        assertEquals(model.getReview().getFloatVal(), 0.0f, 0f);
        assertEquals(model.getReview().getFloatValValid(), 4.007f, 0f);


        LOG.info("Result: {}", model);

    }

    @Test
    public void testParserAsync() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        W4Parser.data(TestHtmlData.htmlReviewData()).parseAsync(TestPageModel.class, (model) -> {
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

            assertEquals(model.getReview().isEnabled(), true);
            assertEquals(model.getReview().getRating(), 4);
            assertEquals(model.getReview().getFloatVal(), 0.0f, 0f);
            assertEquals(model.getReview().getFloatValValid(), 4.007f, 0f);

            LOG.info("Result: {}", model);
            latch.countDown();
        });
        LOG.info("Waiting for parsing result.");
        latch.await();
    }

//    @Test
    public void remoteSync() {
        String url = "https://www.ebay.com/sch/Cell-Phones-Smartphones-/9355/i.html";
        RemoteTestModel model = W4Parser.url(url).parse(RemoteTestModel.class);

        LOG.info("Remote result: {}", model);
    }

    @Test
    public void remoteAsync() {
        CountDownLatch latch = new CountDownLatch(1);
        String url = "https://www.ebay.com/sch/Cell-Phones-Smartphones-/9355/i.html";
        W4Parser.url(url).parseAsync(RemoteTestModel.class, (remoteTestModel -> {
            LOG.info("Fetched model by async method: {}", remoteTestModel);
            latch.countDown();
        }));

        LOG.info("Async request sended. Wait for result.");
        try {
            latch.await();
            LOG.info("Completed");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
