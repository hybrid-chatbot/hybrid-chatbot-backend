package com.example.demo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

/**
 * 네이버 쇼핑 콘솔 검색기 (스프링 없이 순수 자바)
 * 
 * 이 클래스는 Spring Boot 없이도 실행 가능한 독립적인 네이버 쇼핑 검색기입니다.
 * 
 * 실행 방법:
 * 1. javac NaverShoppingConsole.java
 * 2. java NaverShoppingConsole
 * 3. 또는 IDE에서 NaverShoppingConsole.main() 실행
 */
public class NaverShoppingConsole {

    // 네이버 쇼핑 API 설정
    private static final String NAVER_SHOPPING_API_URL = "https://openapi.naver.com/v1/search/shop.json";
    private static final String CLIENT_ID = "MX1_wyfeo9eBuPfVTCSA";  // 실제 클라이언트 ID로 변경 필요
    private static final String CLIENT_SECRET = "MdiPTZAHE0";  // 실제 클라이언트 시크릿으로 변경 필요
    
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("🛍️  네이버 쇼핑 콘솔 검색기 (순수 자바)");
        System.out.println("=".repeat(80));
        System.out.println("✅ 애플리케이션이 시작되었습니다!");
        System.out.println("🔍 검색할 상품을 입력해주세요 (종료: 'exit' 또는 'quit')");
        System.out.println("=".repeat(80));
        
        Scanner scanner = new Scanner(System.in);
        
        while (true) {
            System.out.print("\n🔍 검색어 입력: ");
            String query = scanner.nextLine().trim();
            
            if (query.equalsIgnoreCase("exit") || query.equalsIgnoreCase("quit")) {
                System.out.println("👋 검색을 종료합니다. 안녕히 가세요!");
                break;
            }
            
            if (query.isEmpty()) {
                System.out.println("❌ 검색어를 입력해주세요.");
                continue;
            }
            
            try {
                // 네이버 쇼핑 검색 실행
                searchAndPrintProducts(query);
            } catch (Exception e) {
                System.out.println("❌ 검색 중 오류가 발생했습니다: " + e.getMessage());
            }
        }
        
        scanner.close();
    }

    /**
     * 상품 검색 및 콘솔 출력
     * 
     * @param query 검색 키워드
     */
    private static void searchAndPrintProducts(String query) {
        System.out.println("\n🔍 검색 중... '" + query + "'");
        
        try {
            // 1. 네이버 쇼핑 API 호출
            NaverShoppingResponse response = callNaverShoppingAPI(query, 10, 1);
            
            if (response == null || response.getItems() == null) {
                System.out.println("❌ API 응답이 없습니다.");
                return;
            }
            
            List<NaverShoppingResponse.Item> products = response.getItems();
            
            // 2. 검색 결과 출력
            printSearchResults(query, products);
            
            // 3. 검색 통계 출력
            printSearchStatistics(query, products);
            
        } catch (Exception e) {
            System.out.println("❌ 네이버 쇼핑 API 호출 실패: " + e.getMessage());
        }
    }

    /**
     * 네이버 쇼핑 API 호출
     * 
     * @param query 검색 키워드
     * @param display 검색 결과 출력 건수
     * @param start 검색 시작 위치
     * @return API 응답 객체
     */
    private static NaverShoppingResponse callNaverShoppingAPI(String query, int display, int start) {
        try {
            // URL 인코딩
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            
            // API URL 구성
            String apiUrl = NAVER_SHOPPING_API_URL + 
                          "?query=" + encodedQuery + 
                          "&display=" + display + 
                          "&start=" + start + 
                          "&sort=sim";
            
            // HTTP 연결 설정
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
            // 요청 헤더 설정
            connection.setRequestMethod("GET");
            connection.setRequestProperty("X-Naver-Client-Id", CLIENT_ID);
            connection.setRequestProperty("X-Naver-Client-Secret", CLIENT_SECRET);
            connection.setRequestProperty("Content-Type", "application/json");
            
            // 응답 읽기
            int responseCode = connection.getResponseCode();
            
            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)
                );
                
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                
                // JSON 파싱
                return objectMapper.readValue(response.toString(), NaverShoppingResponse.class);
                
            } else {
                System.out.println("❌ API 호출 실패: HTTP " + responseCode);
                return null;
            }
            
        } catch (Exception e) {
            System.out.println("❌ API 호출 중 오류: " + e.getMessage());
            return null;
        }
    }

    /**
     * 검색 결과를 콘솔에 출력
     * 
     * @param query 검색 키워드
     * @param products 검색된 상품 목록
     */
    private static void printSearchResults(String query, List<NaverShoppingResponse.Item> products) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("🛍️  네이버 쇼핑 검색 결과");
        System.out.println("=".repeat(80));
        System.out.printf("🔍 검색어: %s\n", query);
        System.out.printf("📊 검색 결과: %d개 상품\n", products.size());
        System.out.println("=".repeat(80));
        
        if (products.isEmpty()) {
            System.out.println("❌ 검색 결과가 없습니다.");
            return;
        }
        
        // 각 상품 정보 출력
        for (int i = 0; i < products.size(); i++) {
            NaverShoppingResponse.Item product = products.get(i);
            printProductInfo(i + 1, product);
            System.out.println("-".repeat(80));
        }
    }

    /**
     * 개별 상품 정보를 콘솔에 출력
     * 
     * @param index 상품 순번
     * @param product 상품 정보
     */
    private static void printProductInfo(int index, NaverShoppingResponse.Item product) {
        System.out.printf("📦 [%d] %s\n", index, product.getTitle());
        System.out.printf("   💰 가격: %s원", formatPrice(product.getLprice()));
        if (product.getHprice() != product.getLprice()) {
            System.out.printf(" ~ %s원", formatPrice(product.getHprice()));
        }
        System.out.println();
        
        System.out.printf("   🏪 쇼핑몰: %s\n", product.getMallName() != null ? product.getMallName() : "정보 없음");
        System.out.printf("   🏷️  브랜드: %s\n", product.getBrand() != null ? product.getBrand() : "정보 없음");
        
        if (product.getCategory1() != null) {
            System.out.printf("   📂 카테고리: %s", product.getCategory1());
            if (product.getCategory2() != null) {
                System.out.printf(" > %s", product.getCategory2());
            }
            System.out.println();
        }
        
        if (product.getMaker() != null) {
            System.out.printf("   🏭 제조사: %s\n", product.getMaker());
        }
        
        if (product.getProductType() != null) {
            System.out.printf("   📋 상품타입: %s\n", product.getProductType());
        }
        
        System.out.printf("   🔗 링크: %s\n", product.getLink());
        
        if (product.getImage() != null) {
            System.out.printf("   🖼️  이미지: %s\n", product.getImage());
        }
    }

    /**
     * 검색 통계 정보를 콘솔에 출력
     * 
     * @param query 검색 키워드
     * @param products 검색된 상품 목록
     */
    private static void printSearchStatistics(String query, List<NaverShoppingResponse.Item> products) {
        if (products.isEmpty()) {
            return;
        }
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("📊 검색 통계 정보");
        System.out.println("=".repeat(80));
        
        // 가격 통계
        int minPrice = products.stream()
                .mapToInt(NaverShoppingResponse.Item::getLprice)
                .min()
                .orElse(0);
        
        int maxPrice = products.stream()
                .mapToInt(NaverShoppingResponse.Item::getLprice)
                .max()
                .orElse(0);
        
        double avgPrice = products.stream()
                .mapToInt(NaverShoppingResponse.Item::getLprice)
                .average()
                .orElse(0.0);
        
        System.out.printf("💰 가격 범위: %s원 ~ %s원\n", formatPrice(minPrice), formatPrice(maxPrice));
        System.out.printf("💰 평균 가격: %s원\n", formatPrice((int) avgPrice));
        
        // 쇼핑몰 통계
        long mallCount = products.stream()
                .filter(p -> p.getMallName() != null)
                .map(NaverShoppingResponse.Item::getMallName)
                .distinct()
                .count();
        
        System.out.printf("🏪 참여 쇼핑몰: %d개\n", mallCount);
        
        // 브랜드 통계
        long brandCount = products.stream()
                .filter(p -> p.getBrand() != null)
                .map(NaverShoppingResponse.Item::getBrand)
                .distinct()
                .count();
        
        System.out.printf("🏷️  참여 브랜드: %d개\n", brandCount);
        
        // 카테고리 통계
        long categoryCount = products.stream()
                .filter(p -> p.getCategory1() != null)
                .map(NaverShoppingResponse.Item::getCategory1)
                .distinct()
                .count();
        
        System.out.printf("📂 카테고리: %d개\n", categoryCount);
        
        System.out.println("=".repeat(80));
        System.out.println("✅ 검색 완료! 다른 상품을 검색하거나 'exit'를 입력하여 종료하세요.");
    }

    /**
     * 가격을 천 단위 구분자와 함께 포맷팅
     * 
     * @param price 가격
     * @return 포맷된 가격 문자열
     */
    private static String formatPrice(Integer price) {
        if (price == null) return "정보 없음";
        return String.format("%,d", price);
    }

    /**
     * 네이버 쇼핑 API 응답 DTO
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NaverShoppingResponse {
        @JsonProperty("lastBuildDate")
        private String lastBuildDate;
        
        @JsonProperty("total")
        private int total;
        
        @JsonProperty("start")
        private int start;
        
        @JsonProperty("display")
        private int display;
        
        @JsonProperty("items")
        private List<Item> items;

        // Getters and Setters
        public String getLastBuildDate() { return lastBuildDate; }
        public void setLastBuildDate(String lastBuildDate) { this.lastBuildDate = lastBuildDate; }
        
        public int getTotal() { return total; }
        public void setTotal(int total) { this.total = total; }
        
        public int getStart() { return start; }
        public void setStart(int start) { this.start = start; }
        
        public int getDisplay() { return display; }
        public void setDisplay(int display) { this.display = display; }
        
        public List<Item> getItems() { return items; }
        public void setItems(List<Item> items) { this.items = items; }

        /**
         * 개별 상품 정보 DTO
         */
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Item {
            @JsonProperty("title")
            private String title;
            
            @JsonProperty("link")
            private String link;
            
            @JsonProperty("image")
            private String image;
            
            @JsonProperty("lprice")
            private int lprice;
            
            @JsonProperty("hprice")
            private int hprice;
            
            @JsonProperty("mallName")
            private String mallName;
            
            @JsonProperty("productId")
            private String productId;
            
            @JsonProperty("productType")
            private String productType;
            
            @JsonProperty("brand")
            private String brand;
            
            @JsonProperty("maker")
            private String maker;
            
            @JsonProperty("category1")
            private String category1;
            
            @JsonProperty("category2")
            private String category2;
            
            @JsonProperty("category3")
            private String category3;
            
            @JsonProperty("category4")
            private String category4;

            // Getters and Setters
            public String getTitle() { return title; }
            public void setTitle(String title) { this.title = title; }
            
            public String getLink() { return link; }
            public void setLink(String link) { this.link = link; }
            
            public String getImage() { return image; }
            public void setImage(String image) { this.image = image; }
            
            public int getLprice() { return lprice; }
            public void setLprice(int lprice) { this.lprice = lprice; }
            
            public int getHprice() { return hprice; }
            public void setHprice(int hprice) { this.hprice = hprice; }
            
            public String getMallName() { return mallName; }
            public void setMallName(String mallName) { this.mallName = mallName; }
            
            public String getProductId() { return productId; }
            public void setProductId(String productId) { this.productId = productId; }
            
            public String getProductType() { return productType; }
            public void setProductType(String productType) { this.productType = productType; }
            
            public String getBrand() { return brand; }
            public void setBrand(String brand) { this.brand = brand; }
            
            public String getMaker() { return maker; }
            public void setMaker(String maker) { this.maker = maker; }
            
            public String getCategory1() { return category1; }
            public void setCategory1(String category1) { this.category1 = category1; }
            
            public String getCategory2() { return category2; }
            public void setCategory2(String category2) { this.category2 = category2; }
            
            public String getCategory3() { return category3; }
            public void setCategory3(String category3) { this.category3 = category3; }
            
            public String getCategory4() { return category4; }
            public void setCategory4(String category4) { this.category4 = category4; }
        }
    }
}
