package com.foxtrotparser;

import java.util.HashMap;
import java.util.Map;

public class FoxtrotProductInfo
{
    public String name;
    public Float oldPrice;
    public Float currentPrice;
    public Float discount;
    public String url;
    public Map<String, String> characteristics;


    public FoxtrotProductInfo(){
        characteristics = new HashMap<>();
        oldPrice = 0f;
        currentPrice = 0f;
        discount = 0f;
    }

}
