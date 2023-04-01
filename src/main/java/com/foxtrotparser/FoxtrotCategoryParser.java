package com.foxtrotparser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
public class FoxtrotCategoryParser {
    private String categoryUrl;
    public FoxtrotCategoryParser(){
        categoryUrl = "https://www.foxtrot.com.ua/uk/shop/mobilnye_telefony_smartfon.html";
    }
    public FoxtrotCategoryParser(String url){
        categoryUrl = url;
    }
    public ArrayList<FoxtrotProductInfo> parse() throws IOException {
        ArrayList<FoxtrotProductInfo> products = new ArrayList<>();
        Document mainDoc = getDocument(categoryUrl);
        List<Integer> intArray = new ArrayList<>();
        IntStream
                .rangeClosed(1,
                        Integer.parseInt(mainDoc.select("nav.listing__pagination")
                                .attr("data-pages-count") == "" ? "1" :
                                mainDoc.select("nav.listing__pagination")
                                .attr("data-pages-count")))
                .forEach(i->{
                    intArray.add(i);
                });
        intArray.stream().parallel().forEach((i) ->{
            Document _doc;
            if (i == 1){
                _doc = mainDoc;
            }
            else {
                try {
                    _doc = getDocument(categoryUrl + "?page=" + i);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            Elements articles = _doc
                    .select("div.listing__body-wrap")
                    .get(0)
                    .getElementsByTag("section")
                    .get(0)
                    .getElementsByTag("article");
            articles.forEach((article) ->{
                var productInfo = new FoxtrotProductInfo();
                var _mainDiv = article.getElementsByTag("div").get(0);
                var _div2 = _mainDiv.getElementsByTag("div").get(1);
                productInfo.name = _div2.attr("data-title");
                productInfo.url = _div2.getElementsByTag("a").get(0).attr("href");
                var _oldPrice = _mainDiv.select("div.card__price-discount");
                if (!_oldPrice.isEmpty()){
                    productInfo.oldPrice = Float.parseFloat(_oldPrice.get(0).getElementsByTag("p").get(0).text().replaceAll("[^0-9]", ""));
                    productInfo.discount = Float.parseFloat(_oldPrice.get(0).getElementsByTag("label").get(0).text().replaceAll("[^0-9]", ""));
                }
                var _currentPrice = _mainDiv.select("div.card-price");
                if (!_currentPrice.isEmpty()){
                    productInfo.currentPrice = Float.parseFloat(_currentPrice.get(0).text().replaceAll("[^0-9]", ""));
                }
                String str = downloadString("https://www.foxtrot.com.ua/uk/products/getshortproperties/" + _mainDiv.attr("data-id") + "?classId=" + _mainDiv.attr("data-classid")).replace("null", "");
                var _characteristicElements = Objects.requireNonNull(_mainDiv.selectFirst("table.prop-main")).append(str).getElementsByTag("tr");
                if (!_characteristicElements.isEmpty()){
                    _characteristicElements.forEach((characteristic)->{
                        var _td_s= characteristic.getElementsByTag("td");
                        productInfo.characteristics.put(_td_s.get(0).text().replace(":", "").trim(), _td_s.get(1).text().trim());
                    });
                }

                products.add(productInfo);
            });
        });
        return products;
    }
    //String str = downloadString("https://www.foxtrot.com.ua/uk/products/getshortproperties/" + _mainDiv.attr("data-id") + "?classId=" + _mainDiv.attr("data-classid")).replace("null", "");
    private Document getDocument(String url) throws IOException {
        return Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows; Windows NT 10.5; Win64; x64; en-US) AppleWebKit/601.18 (KHTML, like Gecko) Chrome/48.0.1479.228 Safari/602")
                .header("accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
                .header("accept-language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7,uk;q=0.6,be;q=0.5")
                .get();
    }
    private String downloadString(String url) {
        String s = null;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
            String ss;
            while ((ss = reader.readLine()) != null)
                s += ss;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return s;
    }
}
