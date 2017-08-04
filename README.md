# W4Parser - Java HTML/XML to POJO parser

W4Parser - is a Java library for working with real-world HTML data and transform required part of data to simple java object(POJO). It provides a very convenient API for extracting data and based on popular java parser Jsoup.

## Example

Quick steps to parse HTML data to java object.

1) Prepare java object
```java
    public class BBC {
    
        @W4Parse(select = "li[class=media-list__item media-list__item--1]")
        private BBCNews mainNews;
    
        public static class BBCNews {
    
            @W4Parse(select = "h3.media__title/a")
            private String title;
    
            @W4Parse(select = "p.media__summary")
            private String desc;
        }
    }
```
2) Run parser

```java
BBC bbc = W4Parser.url("http://www.bbc.com/", BBC.class).get();
```
Well done! We already fetched data from BBC website and got BBC class object. It is very simple.

For supported "select" options in W4Parser annotation please read the jsoup [docs](https://jsoup.org/cookbook/extracting-data/selector-syntax).

## Open source
W4Parser is an open source project distributed under the liberal MIT license. 

## Status
W4Parser now under active development and has pre-release status.

## Advanced usage

For example we want to fetch additional data from external url. In this case we can use W4Fetch annotation. For example:

```java
public class BBC {

    @W4Fetch(href = @W4Parse(select = "//section.module--promo//li[class='media-list__item media-list__item--1']//a.media__link/@href"))
    private BBCNews mainNews;

    public static class BBCNews {

        @W4Parse(select = "h1.story-body__h1")
        private String title;

        @W4Parse(select = "div.story-body__inner")
        private String fulltext;
    }
}
///.......
BBC bbc = W4Parser.url("http://www.bbc.com/", BBC.class).get();
```
In this case W4Parser parse the top page and find all links with selected rules ``` @W4Parse(select = "//section.module--promo//li[class='media-list__item media-list__item--1']//a.media__link/@href")``` . Then fetch data from this link and parse page with BBCNews class.

#### or we can fecth the list of remote pages
```java
public class BBC {

    @W4Fetch(href = @W4Parse(select = "//section.module--promo//a.media__link/@href"),
            maxFetch = 5)
    private List<BBCNews> mainNews;

    public static class BBCNews {

        @W4Parse(select = "h1.story-body__h1")
        private String title;

        @W4Parse(select = "div.story-body__inner")
        private String fulltext;
    }
}
///.......
BBC bbc = W4Parser.url("http://www.bbc.com/", BBC.class).get();
```
where ```maxFetch``` - is limit for W4Parser

#### W4Parser support for predefined remote url too
```java
public class BBC {

    @W4Fetch(url="http://www.bbc.com/news/world-australia-40822310")
    private List<BBCNews> mainNews;

    public static class BBCNews {

        @W4Parse(select = "h1.story-body__h1")
        private String title;

        @W4Parse(select = "div.story-body__inner")
        private String fulltext;
    }
}
///.......
BBC bbc = W4Parser.url("http://www.bbc.com/", BBC.class).get();
```

### Also with W4Parser we can fetch remote & parse remote pages asynchronously

```java
W4QueueResult result = W4Parser
                .url("http://www.bbc.com/", BBC.class)
                .url("http://www.cnn.com/", CNN.class)
                .run();

```

or fully async implementation with promise
```java
W4Parser
    .url("http://www.bbc.com/", BBC.class)
    .url("http://www.cnn.com/", CNN.class)
    .run((result) -> {
        //Process W4Parser results.
    });
```

and what about progress of our task queue. No problem
```java
W4Parser
    .url("http://www.bbc.com/", BBC.class)
    .url("http://www.cnn.com/", CNN.class)
    .onProgress((taskResult) -> {
        //Here we can manipulate with completed task results
    })
    .run((result) -> {
         //Process W4Parser results.
     });
```