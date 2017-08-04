# W4Parser - Java HTML/XML to POJO parser

W4Parser - is a Java library for working with real-world HTML data and transform required part of data to simple java object(POJO). It provides a very convenient API for extracting data and based on popular java parser Jsoup.

##Example
Quick steps to parse HTML data to java object.
1) Prepare java object

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

2) Run parser

    BBC bbc = W4Parser.url("http://www.bbc.com/", BBC.class).get();

Well done! WE already fetched data from BBC website and got BBC class object. It is very simple.

## Open source
W4Parser is an open source project distributed under the liberal MIT license. 

## Status
W4Parser now under active developent and has pre-release status.
