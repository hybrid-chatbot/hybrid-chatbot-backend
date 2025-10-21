package com.example.demo.test;

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
 * 상품 정보 시각화 콘솔 애플리케이션
 * 
 * 네이버 쇼핑 API를 호출하여 상품 정보를 검색하고,
 * 검색된 상품들을 시각적으로 보기 좋게 출력합니다.
 * 
 * 주요 기능:
 * - 네이버 쇼핑 API 호출
 * - 상품 정보 파싱 및 저장
 * - 시각적 상품 카드 출력
 * - 통계 정보 표시
 */
public class ProductVisualizationConsole {

    // 네이버 쇼핑 API 설정
    private static final String NAVER_SHOPPING_API_URL = "https://openapi.naver.com/v1/search/shop.json";
    private static final String CLIENT_ID = System.getenv("NAVER_CLIENT_ID");
    private static final String CLIENT_SECRET = System.getenv("NAVER_CLIENT_SECRET");
    
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        // 콘솔 인코딩 설정
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("console.encoding", "UTF-8");
        
        // 환경변수 검증
        if (CLIENT_ID == null || CLIENT_SECRET == null) {
            System.err.println("❌ 오류: 네이버 API 자격증명이 설정되지 않았습니다.");
            System.err.println("   다음 환경변수를 설정해주세요:");
            System.err.println("   - NAVER_CLIENT_ID");
            System.err.println("   - NAVER_CLIENT_SECRET");
            System.err.println("\n   PowerShell에서 설정하는 방법:");
            System.err.println("   $env:NAVER_CLIENT_ID=\"your_client_id\"");
            System.err.println("   $env:NAVER_CLIENT_SECRET=\"your_client_secret\"");
            System.exit(1);
        }
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("상품 정보 시각화 콘솔");
        System.out.println("=".repeat(80));
        System.out.println("애플리케이션이 시작되었습니다!");
        System.out.println("검색할 상품을 입력해주세요 (종료: 'exit' 또는 'quit')");
        System.out.println("=".repeat(80));
        
        Scanner scanner = new Scanner(System.in);
        
        while (true) {
            System.out.print("\n검색어 입력: ");
            String query = scanner.nextLine().trim();
            
            if (query.equalsIgnoreCase("exit") || query.equalsIgnoreCase("quit")) {
                System.out.println("검색을 종료합니다. 안녕히 가세요!");
                break;
            }
            
            if (query.isEmpty()) {
                System.out.println("검색어를 입력해주세요.");
                continue;
            }
            
            // 상품 검색 및 시각화
            searchAndVisualizeProducts(query);
        }
        
        scanner.close();
    }

    /**
     * 상품 검색 및 시각화
     */
    private static void searchAndVisualizeProducts(String query) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("'" + query + "' 검색 중...");
        System.out.println("=".repeat(50));
        
        try {
            // 네이버 쇼핑 API 호출
            NaverShoppingResponse response = callNaverShoppingAPIWithRetry(query, 10, 1, 3, 400);
            
            if (response == null || response.getItems() == null || response.getItems().isEmpty()) {
                System.out.println("❌ 검색 결과가 없습니다.");
                return;
            }
            
            // 시각화 출력
            displayProductVisualization(query, response);
            
        } catch (Exception e) {
            System.err.println("검색 중 오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 상품 정보 시각화 출력
     */
    private static void displayProductVisualization(String query, NaverShoppingResponse response) {
        // ASCII 아트 헤더
        displayAsciiHeader();
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("상품 검색 결과 시각화");
        System.out.println("=".repeat(60));
        
        // 검색 통계 정보
        displaySearchStatistics(query, response);
        
        // 상품 카드들 출력
        displayProductCards(response.getItems());
        
        // 요약 정보
        displaySummary(response);
    }

    /**
     * ASCII 아트 헤더 출력
     */
    private static void displayAsciiHeader() {
        System.out.println("\n");
        System.out.println("    ╔══════════════════════════════════════════════════════════════╗");
        System.out.println("    ║                    SHOPPING MALL                             ║");
        System.out.println("    ║                                                                  ║");
        System.out.println("    ║  [상점] [장바구니] [카드] [상품] [선물] [돈] [태그] [검색] [통계] ║");
        System.out.println("    ║                                                                  ║");
        System.out.println("    ║                    PRODUCT VISUALIZATION                       ║");
        System.out.println("    ╚══════════════════════════════════════════════════════════════╝");
        System.out.println();
    }

    /**
     * 검색 통계 정보 출력
     */
    private static void displaySearchStatistics(String query, NaverShoppingResponse response) {
        System.out.println("\n검색 통계");
        System.out.println("┌" + "─".repeat(58) + "┐");
        System.out.printf("│ 검색어: %-45s │\n", query);
        System.out.printf("│ 총 상품 수: %-40s │\n", response.getTotal() + "개");
        System.out.printf("│ 검색 시간: %-40s │\n", response.getLastBuildDate());
        System.out.printf("│ 표시 상품: %-40s │\n", response.getItems().size() + "개");
        System.out.println("└" + "─".repeat(58) + "┘");
    }

    /**
     * 상품 카드들 출력
     */
    private static void displayProductCards(List<NaverShoppingResponse.Item> items) {
        System.out.println("\n상품 카드 목록");
        System.out.println("=".repeat(80));
        
        for (int i = 0; i < items.size(); i++) {
            NaverShoppingResponse.Item item = items.get(i);
            displayProductCard(i + 1, item);
            
            if (i < items.size() - 1) {
                System.out.println("\n" + "─".repeat(80));
            }
        }
    }

    /**
     * 개별 상품 카드 출력
     */
    private static void displayProductCard(int index, NaverShoppingResponse.Item item) {
        // 상품 카드 헤더
        System.out.println("\n" + "*".repeat(20));
        System.out.println("┌" + "─".repeat(78) + "┐");
        System.out.printf("│ 상품 #%d %-65s │\n", index, "");
        System.out.println("├" + "─".repeat(78) + "┤");
        
        // 상품명
        String title = cleanHtmlTags(item.getTitle());
        System.out.printf("│ 상품명: %-65s │\n", truncateString(title, 65));
        
        // 가격 정보
        if (item.getLprice() != null) {
            String priceInfo = formatPrice(item.getLprice());
            if (item.getHprice() != null && !item.getLprice().equals(item.getHprice())) {
                priceInfo += " ~ " + formatPrice(item.getHprice());
            }
            System.out.printf("│ 가격: %-65s │\n", priceInfo);
        }
        
        // 쇼핑몰 정보
        if (item.getMallName() != null && !item.getMallName().isEmpty()) {
            System.out.printf("│ 쇼핑몰: %-65s │\n", truncateString(item.getMallName(), 65));
        }
        
        // 브랜드 정보
        if (item.getBrand() != null && !item.getBrand().isEmpty()) {
            System.out.printf("│ 브랜드: %-65s │\n", truncateString(item.getBrand(), 65));
        }
        
        // 카테고리 정보
        if (item.getCategory1() != null && !item.getCategory1().isEmpty()) {
            System.out.printf("│ 카테고리: %-65s │\n", truncateString(item.getCategory1(), 65));
        }
        
        // 제조사 정보
        if (item.getMaker() != null && !item.getMaker().isEmpty()) {
            System.out.printf("│ 제조사: %-65s │\n", truncateString(item.getMaker(), 65));
        }
        
        // 링크 정보
        if (item.getLink() != null && !item.getLink().isEmpty()) {
            System.out.printf("│ 링크: %-65s │\n", truncateString(item.getLink(), 65));
        }
        
        // 이미지 정보
        if (item.getImage() != null && !item.getImage().isEmpty()) {
            System.out.printf("│ 이미지: %-65s │\n", truncateString(item.getImage(), 65));
        }
        
        System.out.println("└" + "─".repeat(78) + "┘");
        System.out.println("*".repeat(20));
    }

    /**
     * 요약 정보 출력
     */
    private static void displaySummary(NaverShoppingResponse response) {
        System.out.println("\n" + "=".repeat(20));
        System.out.println("검색 결과 요약");
        System.out.println("=".repeat(20));
        System.out.println("┌" + "─".repeat(58) + "┐");
        System.out.printf("│ 총 상품 수: %-40s │\n", response.getTotal() + "개");
        System.out.printf("│ 표시된 상품: %-40s │\n", response.getItems().size() + "개");
        
        // 가격 통계
        if (!response.getItems().isEmpty()) {
            int minPrice = response.getItems().stream()
                .filter(item -> item.getLprice() != null)
                .mapToInt(NaverShoppingResponse.Item::getLprice)
                .min().orElse(0);
            
            int maxPrice = response.getItems().stream()
                .filter(item -> item.getHprice() != null)
                .mapToInt(NaverShoppingResponse.Item::getHprice)
                .max().orElse(0);
            
            System.out.printf("│ 최저가: %-40s │\n", formatPrice(minPrice));
            System.out.printf("│ 최고가: %-40s │\n", formatPrice(maxPrice));
        }
        
        System.out.println("└" + "─".repeat(58) + "┘");
        
        // 완료 메시지
        System.out.println("\n" + "=".repeat(20));
        System.out.println("시각화 완료! 다른 상품을 검색하거나 'exit'를 입력하여 종료하세요.");
        System.out.println("=".repeat(20));
    }

    /**
     * 네이버 쇼핑 API 호출
     */
    private static NaverShoppingResponse callNaverShoppingAPI(String query, int display, int start) {
        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = String.format("%s?query=%s&display=%d&start=%d&sort=sim", 
                NAVER_SHOPPING_API_URL, encodedQuery, display, start);
            
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("X-Naver-Client-Id", CLIENT_ID);
            connection.setRequestProperty("X-Naver-Client-Secret", CLIENT_SECRET);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Accept-Charset", "UTF-8");
            connection.setRequestProperty("Accept-Language", "ko-KR,ko;q=0.9,en;q=0.8");
            
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    return objectMapper.readValue(response.toString(), NaverShoppingResponse.class);
                }
            } else {
                System.err.println("API 호출 실패: " + responseCode);
                return null;
            }
        } catch (Exception e) {
            System.err.println("API 호출 중 오류: " + e.getMessage());
            return null;
        }
    }

    private static NaverShoppingResponse callNaverShoppingAPIWithRetry(String query, int display, int start, int maxAttempts, int backoffMs) {
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            NaverShoppingResponse res = callNaverShoppingAPI(query, display, start);
            if (res != null && res.getItems() != null && !res.getItems().isEmpty()) {
                return res;
            }
            if (attempt < maxAttempts) {
                try { Thread.sleep((long) backoffMs * attempt); } catch (InterruptedException ignored) {}
            }
        }
        return callNaverShoppingAPI(query, display, start);
    }

    /**
     * HTML 태그 제거
     */
    private static String cleanHtmlTags(String text) {
        if (text == null) return "";
        return text.replaceAll("<[^>]*>", "").trim();
    }

    /**
     * 문자열 자르기
     */
    private static String truncateString(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }

    /**
     * 가격 포맷팅
     */
    private static String formatPrice(Integer price) {
        if (price == null) return "가격 정보 없음";
        return String.format("%,d원", price);
    }

    /**
     * 네이버 쇼핑 API 응답 클래스
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NaverShoppingResponse {
        @JsonProperty("total")
        private int total;
        
        @JsonProperty("lastBuildDate")
        private String lastBuildDate;
        
        @JsonProperty("items")
        private List<Item> items;

        // Getters and Setters
        public int getTotal() { return total; }
        public void setTotal(int total) { this.total = total; }
        
        public String getLastBuildDate() { return lastBuildDate; }
        public void setLastBuildDate(String lastBuildDate) { this.lastBuildDate = lastBuildDate; }
        
        public List<Item> getItems() { return items; }
        public void setItems(List<Item> items) { this.items = items; }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Item {
            @JsonProperty("title")
            private String title;
            
            @JsonProperty("link")
            private String link;
            
            @JsonProperty("image")
            private String image;
            
            @JsonProperty("lprice")
            private Integer lprice;
            
            @JsonProperty("hprice")
            private Integer hprice;
            
            @JsonProperty("mallName")
            private String mallName;
            
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
            
            public Integer getLprice() { return lprice; }
            public void setLprice(Integer lprice) { this.lprice = lprice; }
            
            public Integer getHprice() { return hprice; }
            public void setHprice(Integer hprice) { this.hprice = hprice; }
            
            public String getMallName() { return mallName; }
            public void setMallName(String mallName) { this.mallName = mallName; }
            
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
