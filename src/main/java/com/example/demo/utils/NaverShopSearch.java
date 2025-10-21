package com.example.demo.utils;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;


public class NaverShopSearch {
    public String search() {
        RestTemplate rest = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Naver-Client-Id", "cvVzeOuNwyDim95CZdhk");
        headers.add("X-Naver-Client-Secret", "zzGRQfZOH3");
        String body = "";

        HttpEntity<String> requestEntity = new HttpEntity<String>(body, headers);
        String searchKeyword = "청바지";
        // searchKeyword로 검색하도록
        String url = "https://openapi.naver.com/v1/search/shop.json?query=" + searchKeyword;
        ResponseEntity<String> responseEntity = rest.exchange(url, HttpMethod.GET, requestEntity, String.class);
        HttpStatusCode httpStatus = responseEntity.getStatusCode();
        int status = httpStatus.value(); 
        String response = responseEntity.getBody(); // JSON 형태로 반환
        System.out.println("Response status: " + status);
        System.out.println(response);
        return response;
    }

    public static void main(String[] args) {
        NaverShopSearch naverShopSearch = new NaverShopSearch();
        naverShopSearch.search();
    }
}