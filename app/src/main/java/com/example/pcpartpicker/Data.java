package com.example.pcpartpicker;

import java.util.List;

public class Data {
    public class Part {
        public String name;
        public String url;
        public String price;

    }

    public class Product {
        public String name;
        public List<Spec> specs;
        public List<Price> priceList;
    }

    public class Spec {
        public String key;
        public String value;
    }

    public class Price {
        public String seller;
        public double value;
    }
}
