package test;

import com.google.gson.Gson;
import com.w4.parser.W4Parser;
import com.w4.parser.client.W4QueueResult;
import com.w4.parser.client.queue.ReturnType;
import com.w4.parser.client.queue.W4TaskResult;
import com.w4.parser.processor.W4Processor;
import org.eclipse.jetty.http.HttpHeader;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.data.TestHtmlData;
import test.model.*;
import test.result.TestResult;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

@Ignore
public class ParserTest {
    private static final Logger LOG = LoggerFactory.getLogger(ParserTest.class);

    @Test
    public void bbc() {
        BBC bbc = W4Parser.url("http://www.bbc.com/", BBC.class).debug(true).get();
        LOG.info(new Gson().toJson(bbc));
    }

    @Test
    public void bbc() {
        W4QueueResult result = W4Parser
                .url("http://www.bbc.com/", BBC.class)
                .url("http://www.cnn.com/", CNN.class)
                .run();
        LOG.info(new Gson().toJson(result));
    }

    @Test
    public void parseClass() {
        try {
            Habrahabr habrahabr = W4Parser.agent("W4P user agent").parse(Habrahabr.class).threads(5).get();
            assertNotEquals(habrahabr, null);
            assertEquals(habrahabr.getArticleList().size(), 2);
            assertNotEquals(habrahabr.getWbbUser(), null);
            assertEquals(habrahabr.getWbbUser().getUsername(), "wbb");
            assertEquals(habrahabr.getUrl(), "https://habrahabr.ru/top/");
            assertEquals(habrahabr.getUa(), "W4P user agent");
            LOG.info("Result: {}", new Gson().toJson(habrahabr));
        } catch (Throwable e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testList() {
        List<HabraArticle> habrahabr = W4Parser
                .agent("W4P user agent")
                .url("https://habrahabr.ru/top/", HabraArticle.class).threads(5).getList();
        LOG.info("Result: {}", new Gson().toJson(habrahabr));
    }

    @Test
    public void parseAdvancedSelect() {
        try {
            Mua mua = W4Parser.agent("W4P user agent").parse(Mua.class).threads(5).get();
            assertNotEquals(mua, null);
            assertNotEquals(mua.getGadgetList(), null);
            assertEquals(mua.getGadgetList().size(), 3);
            assertNotEquals(mua.getGadgetList().get(0).getGadgetName(), null);
            assertNotEquals(mua.getGadgetList().get(0).getReviewSearch(), null);
            assertNotEquals(mua.getGadgetList().get(0).getReviewSearch().getGadgetReviewList(), null);
            LOG.info("Result: {}", new Gson().toJson(mua));
        } catch (Throwable e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void parsePagination() {
        HabraTitleList habrahabr = W4Parser.parse(HabraTitleList.class).get();
        LOG.info("Result: {}", new Gson().toJson(habrahabr));

        assertNotEquals(habrahabr, null);
        assertEquals(habrahabr.getTitleLists().size(), 30);
        assertNotEquals(habrahabr.getTitleLists().get(0), null);
//        assertEquals(habrahabr.getTitleLists().get(0).getTitles().size(), 10);
    }

    @Test
    public void reutersClass() {
        Reuters reuters = W4Parser.parse(Reuters.class).threads(5).get();
        LOG.info("Result: {}", new Gson().toJson(reuters));

        assertNotEquals(reuters, null);
        assertNotEquals(reuters.getNewsList(), null);
        assertNotEquals(reuters.getNewsList().get(0).getTitle(), null);
        assertNotEquals(reuters.getNewsList().get(0).getDescription(), null);
        assertNotEquals(reuters.getNewsList().get(0).getLink(), null);
    }




    ////////////////////////////////////


    @Test
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

    @Test
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

    @Test
    public void queue() {
        String url1 = "https://habrahabr.ru/users/";
        String url2 = "https://habrahabr.ru/hubs/";
        W4QueueResult result = W4Processor
                                    .url(url1, HabrahabrModel.class)
//                                    .url(url2, HabrahabrModel.class)
                                    .onProgress((taskResult) -> {
                                        LOG.info("Complete process: {}, model: {}",
                                                taskResult.getTask().getW4Request().getUrl(),
                                                taskResult.getOne());
                                    })
                                .run();
        LOG.debug("W4Queue result: {}", result);

        for (Iterator<W4TaskResult<HabrahabrModel>> it = result.iterator(); it.hasNext();) {
            HabrahabrModel m = it.next().getOne();
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
                    .onProgress((taskResult) -> {
                        LOG.info("Complete process: {}, model: {}",
                                taskResult.getTask().getW4Request().getUrl(),
                                taskResult.getOne());
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
