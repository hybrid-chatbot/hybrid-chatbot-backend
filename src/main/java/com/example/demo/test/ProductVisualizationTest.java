package com.example.demo.test;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 상품 시각화 테스트
 * 
 * 네이버 쇼핑 API를 호출하여 상품 데이터를 가져오고 콘솔에서 시각화하는 테스트입니다.
 * 
 * 실행 방법:
 * 1. IDE에서 main 메서드 실행
 * 2. 터미널: javac -cp "src/main/java" src/main/java/com/example/demo/test/ProductVisualizationTest.java && java -cp "src/main/java" com.example.demo.test.ProductVisualizationTest
 */
public class ProductVisualizationTest {

    // Naver API 설정
    private static final String NAVER_SHOPPING_API_URL = "https://openapi.naver.com/v1/search/shop.json";
    private static final String CLIENT_ID = System.getenv("NAVER_CLIENT_ID");
    private static final String CLIENT_SECRET = System.getenv("NAVER_CLIENT_SECRET");
    

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
        
        System.out.println("==========================================");
        System.out.println("🎯 상품 시각화 테스트 시작");
        System.out.println("==========================================");
        
        java.util.Scanner scanner = new java.util.Scanner(System.in);
        
        System.out.println("🎮 테스트 모드 선택:");
        System.out.println("1. 자동 테스트 (기본 검색어로 테스트)");
        System.out.println("2. 수동 테스트 (직접 검색어 입력)");
        System.out.print("선택하세요 (1 또는 2): ");
        
        String choice = scanner.nextLine().trim();
        
        if ("2".equals(choice)) {
            interactiveTest(scanner);
        } else {
            automaticTest();
        }
        
        scanner.close();
    }

    /**
     * 자동 테스트 실행
     */
    private static void automaticTest() {
        String[] testQueries = {
            "나이키 운동화",
            "아디다스 신발",
            "컨버스 스니커즈"
        };
        
        for (String query : testQueries) {
            System.out.println("\n" + "=".repeat(60));
            System.out.println("🔍 검색어: " + query);
            System.out.println("=".repeat(60));
            
            try {
                String jsonResponse = fetchWithRetry(query, 10, 1, 3, 400);
                displaySearchResults(query, jsonResponse);
            } catch (Exception e) {
                System.err.println("❌ 검색 중 오류 발생: " + e.getMessage());
            }
            
            System.out.println("\n" + "-".repeat(60));
        }
        
        System.out.println("\n==========================================");
        System.out.println("🎉 자동 테스트 완료!");
        System.out.println("==========================================");
    }

    /**
     * 인터랙티브 테스트 실행
     */
    private static void interactiveTest(java.util.Scanner scanner) {
        System.out.println("\n🎮 수동 테스트 모드");
        System.out.println("💡 사용법:");
        System.out.println("   - 검색어 입력 (예: '나이키 운동화', '아디다스 신발')");
        System.out.println("   - 종료: 'exit' 또는 'quit' 입력");
        System.out.println();
        
        while (true) {
            System.out.print("🔍 검색어를 입력하세요: ");
            String query = scanner.nextLine().trim();
            
            if (query.isEmpty()) {
                System.out.println("❌ 검색어를 입력해주세요.");
                continue;
            }
            
            if (query.equalsIgnoreCase("exit") || query.equalsIgnoreCase("quit")) {
                System.out.println("👋 테스트를 종료합니다.");
                break;
            }
            
            System.out.println("\n" + "=".repeat(60));
            System.out.println("🔍 검색어: " + query);
            System.out.println("=".repeat(60));
            
            try {
                String jsonResponse = fetchWithRetry(query, 10, 1, 3, 400);
                displaySearchResults(query, jsonResponse);
            } catch (Exception e) {
                System.err.println("❌ 검색 중 오류 발생: " + e.getMessage());
            }
            
            System.out.println("\n" + "-".repeat(60));
            System.out.println();
        }
    }

    /**
     * 네이버 쇼핑 API 검색 (ProductVisualizationConsole과 동일한 방식)
     */
    private static String searchNaverShopping(String query, int display, int start) {
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
            
            System.out.println("🔍 API 디버깅 정보:");
            System.out.println("   - 요청 URL: " + url);
            System.out.println("   - HTTP 상태 코드: " + responseCode);
            
            if (responseCode == 200) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    
                    String responseBody = response.toString();
                    System.out.println("   - 응답 길이: " + responseBody.length());
                    System.out.println("   - 응답 시작 부분: " + responseBody.substring(0, Math.min(300, responseBody.length())));
                    
                    return responseBody;
                }
            } else {
                System.err.println("❌ API 호출 실패: " + responseCode);
                return null;
            }
        } catch (Exception e) {
            System.err.println("API 호출 중 오류: " + e.getMessage());
            return null;
        }
    }

    private static String fetchWithRetry(String query, int display, int start, int maxAttempts, int backoffMs) throws InterruptedException {
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            String res = searchNaverShopping(query, display, start);
            if (res != null && res.contains("\"items\":[") && res.indexOf("\"items\":[") != -1) {
                // items 길이 체크
                int itemsStart = res.indexOf("\"items\":[");
                int arrayStart = res.indexOf('[', itemsStart);
                int itemsEnd = -1;
                int bracketCount = 1; boolean inString = false; boolean escaped = false;
                for (int i = arrayStart + 1; i < res.length(); i++) {
                    char ch = res.charAt(i);
                    if (escaped) { escaped = false; continue; }
                    if (ch == '\\') { escaped = true; continue; }
                    if (ch == '"') { inString = !inString; continue; }
                    if (!inString) {
                        if (ch == '[') bracketCount++;
                        else if (ch == ']') bracketCount--;
                        if (bracketCount == 0) { itemsEnd = i; break; }
                    }
                }
                if (itemsEnd > arrayStart + 1) {
                    String itemsJson = res.substring(itemsStart + 8, itemsEnd + 1);
                    if (itemsJson.length() > 100) return res; // 데이터 충분
                }
            }
            if (attempt < maxAttempts) {
                Thread.sleep((long) backoffMs * attempt);
            }
        }
        return searchNaverShopping(query, display, start);
    }

    /**
     * 검색 결과 시각화 (ProductVisualizationConsole과 동일한 방식)
     */
    private static void displaySearchResults(String query, String jsonResponse) {
        try {
            if (jsonResponse == null || jsonResponse.isEmpty()) {
                System.out.println("❌ 검색 결과가 없습니다.");
                return;
            }
            
            // JSON에서 기본 정보 추출
            String total = extractJsonValue(jsonResponse, "total");
            String start = extractJsonValue(jsonResponse, "start");
            String display = extractJsonValue(jsonResponse, "display");
            
            // ASCII 헤더
            displayAsciiHeader();
            
            System.out.println("\n" + "=".repeat(60));
            System.out.println("상품 검색 결과 시각화");
            System.out.println("=".repeat(60));
            
            // 검색 통계
            displaySearchStatisticsBlock(query, total, start, display, jsonResponse);
            
            // 상품 목록 파싱
            List<String> products = parseProducts(jsonResponse);
            displayProductCards(products);
            
            // 요약 정보
            displaySummaryBlock(products, total);
            
        } catch (Exception e) {
            System.err.println("❌ 결과 파싱 중 오류: " + e.getMessage());
            System.err.println("🔍 디버깅 정보:");
            System.err.println("   - JSON 길이: " + jsonResponse.length());
            System.err.println("   - JSON 시작 부분: " + jsonResponse.substring(0, Math.min(200, jsonResponse.length())));
        }
    }

    /**
     * 상품 목록 파싱 및 표시
     */
    private static void parseAndDisplayProducts(String jsonResponse) {
        try {
            // items 배열 찾기
            int itemsStart = jsonResponse.indexOf("\"items\":[");
            if (itemsStart == -1) {
                System.out.println("     ❌ 상품 정보를 찾을 수 없습니다.");
                return;
            }
            
            // '[' 의 정확한 위치 찾기
            int arrayStart = jsonResponse.indexOf('[', itemsStart);
            if (arrayStart == -1) {
                System.out.println("     ❌ 상품 배열 시작을 찾을 수 없습니다.");
                return;
            }

            // 문자열/이스케이프 처리와 대괄호 카운팅으로 종료 지점 탐색
            int itemsEnd = -1;
            int bracketCount = 1;
            boolean inString = false;
            boolean escaped = false;
            for (int i = arrayStart + 1; i < jsonResponse.length(); i++) {
                char ch = jsonResponse.charAt(i);
                if (escaped) {
                    escaped = false;
                    continue;
                }
                if (ch == '\\') {
                    escaped = true;
                    continue;
                }
                if (ch == '"') {
                    inString = !inString;
                    continue;
                }
                if (!inString) {
                    if (ch == '[') bracketCount++;
                    else if (ch == ']') bracketCount--;
                    if (bracketCount == 0) { itemsEnd = i; break; }
                }
            }
            if (itemsEnd == -1) {
                System.out.println("     ❌ 상품 배열이 올바르지 않습니다.");
                return;
            }
            
            String itemsJson = jsonResponse.substring(itemsStart + 8, itemsEnd + 1);
            System.out.println("     🔍 디버깅: items JSON 길이 = " + itemsJson.length());
            
            // 더 정확한 상품 분리 방법
            List<String> products = parseProductArray(itemsJson);
            System.out.println("     🔍 디버깅: 파싱된 상품 개수 = " + products.size());
            
            int productCount = 0;
            for (String product : products) {
                if (productCount >= 10) break; // 최대 10개만 표시
                
                productCount++;
                
                // 상품 정보 추출
                String title = extractJsonValue(product, "title");
                String price = extractJsonValue(product, "lprice");
                String mallName = extractJsonValue(product, "mallName");
                String brand = extractJsonValue(product, "brand");
                
                // 상품 정보 출력
                System.out.printf("     %2d. %s%n", productCount, cleanTitle(title != null ? title : "제목 없음"));
                
                if (price != null && !price.isEmpty()) {
                    try {
                        int priceInt = Integer.parseInt(price);
                        System.out.printf("         💰 가격: %,d원%n", priceInt);
                    } catch (NumberFormatException e) {
                        System.out.printf("         💰 가격: %s원%n", price);
                    }
                }
                
                if (mallName != null && !mallName.isEmpty()) {
                    System.out.printf("         🏪 쇼핑몰: %s%n", mallName);
                }
                
                if (brand != null && !brand.isEmpty()) {
                    System.out.printf("         🏷️  브랜드: %s%n", brand);
                }
                
                System.out.println();
            }
            
            if (productCount == 0) {
                System.out.println("     ❌ 상품 정보를 찾을 수 없습니다.");
            }
            
        } catch (Exception e) {
            System.err.println("     ❌ 상품 파싱 중 오류: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Console과 유사한 카드 시각화용 래퍼
    private static List<String> parseProducts(String jsonResponse) {
        int itemsStart = jsonResponse.indexOf("\"items\":[");
        if (itemsStart == -1) return java.util.Collections.emptyList();
        int arrayStart = jsonResponse.indexOf('[', itemsStart);
        if (arrayStart == -1) return java.util.Collections.emptyList();
        int itemsEnd = -1;
        int bracketCount = 1;
        boolean inString = false;
        boolean escaped = false;
        for (int i = arrayStart + 1; i < jsonResponse.length(); i++) {
            char ch = jsonResponse.charAt(i);
            if (escaped) { escaped = false; continue; }
            if (ch == '\\') { escaped = true; continue; }
            if (ch == '"') { inString = !inString; continue; }
            if (!inString) {
                if (ch == '[') bracketCount++;
                else if (ch == ']') bracketCount--;
                if (bracketCount == 0) { itemsEnd = i; break; }
            }
        }
        if (itemsEnd == -1) return java.util.Collections.emptyList();
        String itemsJson = jsonResponse.substring(itemsStart + 8, itemsEnd + 1);
        return parseProductArray(itemsJson);
    }

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

    private static void displaySearchStatisticsBlock(String query, String total, String start, String display, String jsonResponse) {
        System.out.println("\n검색 통계");
        System.out.println("┌" + "─".repeat(58) + "┐");
        System.out.printf("│ 검색어: %-45s │\n", query);
        System.out.printf("│ 총 상품 수: %-40s │\n", (total != null ? total + "개" : "-"));
        System.out.printf("│ 시작 위치: %-40s │\n", (start != null ? start : "1"));
        // items 개수 계산
        List<String> products = parseProducts(jsonResponse);
        System.out.printf("│ 표시 상품: %-40s │\n", products.size() + "개");
        System.out.println("└" + "─".repeat(58) + "┘");
    }

    private static void displayProductCards(List<String> products) {
        System.out.println("\n상품 카드 목록");
        System.out.println("=".repeat(80));
        for (int i = 0; i < products.size() && i < 10; i++) {
            displayProductCard(i + 1, products.get(i));
            if (i < products.size() - 1 && i < 9) {
                System.out.println("\n" + "─".repeat(80));
            }
        }
    }

    private static void displayProductCard(int index, String productJson) {
        // 상품 정보 추출
        String title = extractJsonValue(productJson, "title");
        String price = extractJsonValue(productJson, "lprice");
        String hprice = extractJsonValue(productJson, "hprice");
        String mallName = extractJsonValue(productJson, "mallName");
        String brand = extractJsonValue(productJson, "brand");
        String maker = extractJsonValue(productJson, "maker");
        String link = extractJsonValue(productJson, "link");
        String image = extractJsonValue(productJson, "image");
        String category1 = extractJsonValue(productJson, "category1");

        System.out.println("\n" + "*".repeat(20));
        System.out.println("┌" + "─".repeat(78) + "┐");
        System.out.printf("│ 상품 #%d %-65s │\n", index, "");
        System.out.println("├" + "─".repeat(78) + "┤");

        // 상품명
        String clean = cleanTitle(title != null ? title : "제목 없음");
        System.out.printf("│ 상품명: %-65s │\n", truncateString(clean, 65));

        // 가격 정보
        if (price != null && !price.isEmpty()) {
            String priceInfo = safeFormatPrice(price);
            if (hprice != null && !hprice.isEmpty() && !hprice.equals(price)) {
                priceInfo += " ~ " + safeFormatPrice(hprice);
            }
            System.out.printf("│ 가격: %-65s │\n", priceInfo);
        }

        if (mallName != null && !mallName.isEmpty()) {
            System.out.printf("│ 쇼핑몰: %-65s │\n", truncateString(mallName, 65));
        }
        if (brand != null && !brand.isEmpty()) {
            System.out.printf("│ 브랜드: %-65s │\n", truncateString(brand, 65));
        }
        if (category1 != null && !category1.isEmpty()) {
            System.out.printf("│ 카테고리: %-65s │\n", truncateString(category1, 65));
        }
        if (maker != null && !maker.isEmpty()) {
            System.out.printf("│ 제조사: %-65s │\n", truncateString(maker, 65));
        }
        if (link != null && !link.isEmpty()) {
            System.out.printf("│ 링크: %-65s │\n", truncateString(link, 65));
        }
        if (image != null && !image.isEmpty()) {
            System.out.printf("│ 이미지: %-65s │\n", truncateString(image, 65));
        }

        System.out.println("└" + "─".repeat(78) + "┘");
        System.out.println("*".repeat(20));
    }

    private static void displaySummaryBlock(List<String> products, String total) {
        System.out.println("\n" + "=".repeat(20));
        System.out.println("검색 결과 요약");
        System.out.println("=".repeat(20));
        System.out.println("┌" + "─".repeat(58) + "┐");
        System.out.printf("│ 총 상품 수: %-40s │\n", (total != null ? total + "개" : "-"));
        System.out.printf("│ 표시된 상품: %-40s │\n", Math.min(products.size(), 10) + "개");
        int minPrice = Integer.MAX_VALUE;
        int maxPrice = 0;
        for (String p : products) {
            String lp = extractJsonValue(p, "lprice");
            if (lp != null) {
                try {
                    int v = Integer.parseInt(lp);
                    if (v > 0) {
                        minPrice = Math.min(minPrice, v);
                        maxPrice = Math.max(maxPrice, v);
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        if (minPrice == Integer.MAX_VALUE) minPrice = 0;
        System.out.printf("│ 최저가: %-40s │\n", formatPrice(minPrice));
        System.out.printf("│ 최고가: %-40s │\n", formatPrice(maxPrice));
        System.out.println("└" + "─".repeat(58) + "┘");
        System.out.println("\n" + "=".repeat(20));
        System.out.println("시각화 완료! 다른 상품을 검색하거나 'exit'를 입력하여 종료하세요.");
        System.out.println("=".repeat(20));
    }

    private static String truncateString(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, Math.max(0, maxLength - 3)) + "...";
    }

    private static String safeFormatPrice(String price) {
        try {
            return String.format("%,d원", Integer.parseInt(price));
        } catch (NumberFormatException e) {
            return price + "원";
        }
    }

    private static String formatPrice(int price) {
        if (price <= 0) return "가격 정보 없음";
        return String.format("%,d원", price);
    }

    /**
     * JSON 배열에서 개별 상품 객체들을 파싱
     */
    private static List<String> parseProductArray(String itemsJson) {
        List<String> products = new ArrayList<>();
        
        try {
            // JSON 배열의 시작과 끝 제거
            String content = itemsJson.trim();
            if (content.startsWith("[")) {
                content = content.substring(1);
            }
            if (content.endsWith("]")) {
                content = content.substring(0, content.length() - 1);
            }
            
            int braceCount = 0;
            int start = 0;
            boolean inString = false;
            boolean escaped = false;
            
            for (int i = 0; i < content.length(); i++) {
                char c = content.charAt(i);
                
                if (escaped) {
                    escaped = false;
                    continue;
                }
                
                if (c == '\\') {
                    escaped = true;
                    continue;
                }
                
                if (c == '"') {
                    inString = !inString;
                    continue;
                }
                
                if (!inString) {
                    if (c == '{') {
                        if (braceCount == 0) {
                            start = i;
                        }
                        braceCount++;
                    } else if (c == '}') {
                        braceCount--;
                        if (braceCount == 0) {
                            // 완전한 객체를 찾았음
                            String product = content.substring(start, i + 1);
                            products.add(product);
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("     ⚠️ 상품 배열 파싱 오류: " + e.getMessage());
        }
        
        return products;
    }

    /**
     * JSON에서 값 추출 (개선된 버전)
     */
    private static String extractJsonValue(String json, String key) {
        try {
            if (json == null || key == null) return null;
            
            String pattern = "\"" + key + "\"\\s*:";
            java.util.regex.Pattern regex = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher matcher = regex.matcher(json);
            
            if (!matcher.find()) return null;
            
            int valueStart = matcher.end();
            
            // 공백 건너뛰기
            while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
                valueStart++;
            }
            
            if (valueStart >= json.length()) return null;
            
            int valueEnd;
            if (json.charAt(valueStart) == '"') {
                // 문자열 값 - 따옴표 안의 내용 추출
                valueStart++; // 시작 따옴표 건너뛰기
                valueEnd = valueStart;
                
                // 이스케이프된 따옴표 처리
                while (valueEnd < json.length()) {
                    if (json.charAt(valueEnd) == '"' && (valueEnd == 0 || json.charAt(valueEnd - 1) != '\\')) {
                        break;
                    }
                    valueEnd++;
                }
            } else if (json.charAt(valueStart) == '{' || json.charAt(valueStart) == '[') {
                // 객체나 배열 - 중괄호 매칭
                char openChar = json.charAt(valueStart);
                char closeChar = (openChar == '{') ? '}' : ']';
                int bracketCount = 1;
                valueEnd = valueStart + 1;
                
                while (valueEnd < json.length() && bracketCount > 0) {
                    if (json.charAt(valueEnd) == openChar) {
                        bracketCount++;
                    } else if (json.charAt(valueEnd) == closeChar) {
                        bracketCount--;
                    }
                    valueEnd++;
                }
            } else {
                // 숫자나 다른 값 - 쉼표나 중괄호까지
                valueEnd = valueStart;
                while (valueEnd < json.length() && 
                       json.charAt(valueEnd) != ',' && 
                       json.charAt(valueEnd) != '}' && 
                       json.charAt(valueEnd) != ']') {
                    valueEnd++;
                }
            }
            
            if (valueEnd <= valueStart) return null;
            
            String result = json.substring(valueStart, valueEnd).trim();
            return result.isEmpty() ? null : result;
            
        } catch (Exception e) {
            System.err.println("   ⚠️ JSON 값 추출 오류 (" + key + "): " + e.getMessage());
            return null;
        }
    }


    /**
     * 제목 정리 (HTML 태그 제거)
     */
    private static String cleanTitle(String title) {
        if (title == null) return "";
        return title.replaceAll("<[^>]+>", "").trim();
    }

}
