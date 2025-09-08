package com.example.navershopping.dto;

import lombok.Data;
import java.util.List;

@Data
public class NaverShoppingResponse {
    private String lastBuildDate;
    private int total;
    private int start;
    private int display;
    private List<Item> items;

    @Data
    public static class Item {
        private String title;
        private String link;
        private String image;
        private int lprice;
        private int hprice;
        private String mallName;
        private String productId;
        private String productType;
        private String brand;
        private String maker;
        private String category1;
        private String category2;
        private String category3;
        private String category4;
    }
}