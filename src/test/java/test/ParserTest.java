package test;

import com.google.gson.Gson;
import com.w4.parser.W4Parser;
import com.w4.parser.client.W4QueueResult;
import com.w4.parser.processor.W4Processor;
import org.eclipse.jetty.http.HttpHeader;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.data.TestHtmlData;
import test.model.Habrahabr;
import test.model.HabrahabrModel;
import test.model.RemoteTestModel;
import test.model.TestPageModel;
import test.result.TestResult;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

public class ParserTest {
    private static final Logger LOG = LoggerFactory.getLogger(ParserTest.class);

    @Test
    public void parseClass() {
        Habrahabr habrahabr = W4Parser.parse(Habrahabr.class);
        LOG.info("Result: {}", new Gson().toJson(habrahabr));

        assertNotEquals(habrahabr, null);
    }




    ////////////////////////////////////


//    @Test
    public void testParser() {
        TestPageModel model = W4Processor.data(TestHtmlData.htmlReviewData(), TestPageModel.class).get();

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


        LOG.debug("Result: {}", model);
        LOG.info("Sync parsing test passed.");

    }

//    @Test
    public void testParserAsync() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        W4Processor.data(TestHtmlData.htmlReviewData(), TestPageModel.class).get((TestPageModel model) -> {
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

            LOG.debug("Result: {}", model);
            latch.countDown();
        });
        LOG.debug("Waiting for parsing result.");
        latch.await();
        LOG.info("Async parsing test passed.");
    }

//    @Test
    public void remoteSync() {
        String url = "https://www.ebay.com/sch/Cell-Phones-Smartphones-/9355/i.html";
        RemoteTestModel model = W4Processor.url(url, RemoteTestModel.class).get();

        LOG.info("Remote result: {}", model);
    }

//    @Test
    public void remoteAsync() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final TestResult testResult = new TestResult();
//        final TestResult<List> testResult2 = new TestResult();
        String url = "https://www.ebay.com/sch/Cell-Phones-Smartphones-/9355/i.html";
        W4Processor.url(url, RemoteTestModel.class).run((result -> {
            RemoteTestModel remoteTestModel = (RemoteTestModel) result.getFirst();
            LOG.info("Fetched model by async method: {}", new Gson().toJson(remoteTestModel));
            if (remoteTestModel != null) {
                testResult.setResult(remoteTestModel.getHabrahabrModel());
            }
            latch.countDown();
        }));

        LOG.debug("Async request sended. Wait for result.");
        latch.await();
        assertNotEquals(testResult.getResult(), null);
//        assertNotEquals(testResult2.getResult(), null);
//        assertEquals(testResult2.getResult().size(), 2);
        LOG.info("Remote async parse test passed.");
    }

//    @Test
    public void queue() {
        String url1 = "https://habrahabr.ru/users/";
        String url2 = "https://habrahabr.ru/hubs/";
        W4QueueResult result = W4Processor
                                    .url(url1, HabrahabrModel.class)
//                                    .url(url2, HabrahabrModel.class)
                                    .onProgress((task, model) -> {
                                        LOG.debug("Complete process: {}, model: {}", task.getW4Request().getUrl(), model);
                                    })
                                .run();
        LOG.debug("W4Queue result: {}", result);
        for (Iterator<HabrahabrModel> it = result.iterator(); it.hasNext();) {
            HabrahabrModel m = it.next();
            LOG.info("Res: {}", new Gson().toJson(m));
        }

        LOG.info("Queue test passed");
    }

//    @Test
    public void queueAsync() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        String url3 = "https://habrahabr.ru/companies/";
        String url4 = "https://www.ebay.com/sch/Cell-Phones-Smartphones-/9355/i.html";

        W4Processor
                .queue()
                    .threads(5)
                    .timeout(3000, TimeUnit.MILLISECONDS)
                    .url(url4, RemoteTestModel.class)
                        .setup()
                            .header(HttpHeader.ACCEPT_CHARSET, "*/*")
                            .timeout(400, TimeUnit.MILLISECONDS)
                            .agent("My UserAgent")
                        .done()
                    .url(url3, HabrahabrModel.class)
                    .onProgress((task, model) -> {
                        LOG.info("Complete process: {}, model: {}", task.getW4Request().getUrl(), model);
                    })
                .run((result -> {
                    latch.countDown();
                    LOG.debug("W4Queue result: {}", result);
                    for (Iterator it = result.iterator(); it.hasNext();) {
                        Object m = it.next();
                        LOG.debug("Res: {}", m.toString());
                    }
                }));

        LOG.debug("Waiting for async result.");
        latch.await();
        LOG.info("Async queue test passed.");
    }

}
