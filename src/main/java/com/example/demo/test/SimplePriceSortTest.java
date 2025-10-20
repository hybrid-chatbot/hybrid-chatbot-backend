package com.example.demo.test;

import java.util.ArrayList;
import java.util.List;

/**
 * 간단한 가격순 정렬 테스트
 * 
 * Spring Boot 없이도 실행할 수 있는 독립적인 테스트입니다.
 * 가격순 정렬 로직만을 테스트합니다.
 * 
 * 실행 방법:
 * 1. IDE에서 main 메서드 실행
 * 2. 터미널: javac -cp "src/main/java" src/main/java/com/example/demo/test/SimplePriceSortTest.java && java -cp "src/main/java" com.example.demo.test.SimplePriceSortTest
 */
public class SimplePriceSortTest {

    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("🎯 간단한 가격순 정렬 테스트");
        System.out.println("==========================================");
        
        java.util.Scanner scanner = new java.util.Scanner(System.in);
        
        System.out.println("🎮 테스트 모드 선택:");
        System.out.println("1. 자동 테스트 (기존 테스트 실행)");
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
        // 1. 테스트 데이터 생성
        System.out.println("\n📦 1단계: 테스트 데이터 생성");
        System.out.println("------------------------------------------");
        List<TestProduct> products = createTestProducts();
        System.out.println("✅ 테스트 상품 " + products.size() + "개 생성 완료");
        
        // 2. 정렬 전 상태 출력
        System.out.println("\n📋 2단계: 정렬 전 상품 목록");
        System.out.println("------------------------------------------");
        printProducts(products, "정렬 전");
        
        // 3. 오름차순 정렬 테스트
        System.out.println("\n💰 3단계: 오름차순 정렬 테스트");
        System.out.println("------------------------------------------");
        List<TestProduct> ascendingSorted = sortProductsByPrice(products, true);
        printProducts(ascendingSorted, "오름차순 정렬 후");
        verifySort(ascendingSorted, true);
        
        // 4. 내림차순 정렬 테스트
        System.out.println("\n💎 4단계: 내림차순 정렬 테스트");
        System.out.println("------------------------------------------");
        List<TestProduct> descendingSorted = sortProductsByPrice(products, false);
        printProducts(descendingSorted, "내림차순 정렬 후");
        verifySort(descendingSorted, false);
        
        // 5. 키워드 감지 테스트
        System.out.println("\n🎨 5단계: 정렬 키워드 감지 테스트");
        System.out.println("------------------------------------------");
        testKeywordDetection();
        
        System.out.println("\n==========================================");
        System.out.println("🎉 모든 테스트 완료!");
        System.out.println("==========================================");
    }

    /**
     * 인터랙티브 테스트 실행
     */
    private static void interactiveTest(java.util.Scanner scanner) {
        System.out.println("\n🎮 수동 테스트 모드");
        System.out.println("💡 사용법:");
        System.out.println("   - 정렬 테스트: '오름차순' 또는 '내림차순' 입력");
        System.out.println("   - 키워드 테스트: 검색 메시지 입력 (예: '나이키 운동화 최저가순으로 검색해줘')");
        System.out.println("   - 종료: 'exit' 또는 'quit' 입력");
        System.out.println();
        
        List<TestProduct> products = createTestProducts();
        System.out.println("✅ 테스트 상품 " + products.size() + "개 준비 완료");
        System.out.println();
        
        while (true) {
            System.out.print("🔍 명령을 입력하세요: ");
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty()) {
                System.out.println("❌ 명령을 입력해주세요.");
                continue;
            }
            
            if (input.equalsIgnoreCase("exit") || input.equalsIgnoreCase("quit")) {
                System.out.println("👋 테스트를 종료합니다.");
                break;
            }
            
            if (input.equals("오름차순")) {
                System.out.println("\n💰 오름차순 정렬 테스트");
                System.out.println("------------------------------------------");
                List<TestProduct> sorted = sortProductsByPrice(products, true);
                printProducts(sorted, "오름차순 정렬 후");
                verifySort(sorted, true);
                
            } else if (input.equals("내림차순")) {
                System.out.println("\n💎 내림차순 정렬 테스트");
                System.out.println("------------------------------------------");
                List<TestProduct> sorted = sortProductsByPrice(products, false);
                printProducts(sorted, "내림차순 정렬 후");
                verifySort(sorted, false);
                
            } else {
                System.out.println("\n🎨 키워드 감지 테스트");
                System.out.println("------------------------------------------");
                String sortOrder = detectPriceSortRequest(input);
                String cleanQuery = removeSortKeywords(input);
                
                System.out.println("🔍 입력 메시지: '" + input + "'");
                System.out.println("   - 감지된 정렬: " + (sortOrder != null ? sortOrder : "없음"));
                System.out.println("   - 정리된 검색어: '" + cleanQuery + "'");
                
                if (sortOrder != null) {
                    System.out.println("\n📊 정렬 적용 결과:");
                    List<TestProduct> sorted = sortProductsByPrice(products, "asc".equals(sortOrder));
                    printProducts(sorted, sortOrder + " 정렬 후");
                    verifySort(sorted, "asc".equals(sortOrder));
                }
            }
            
            System.out.println("\n" + "-".repeat(50));
            System.out.println();
        }
    }

    /**
     * 테스트 상품 데이터 생성
     */
    private static List<TestProduct> createTestProducts() {
        List<TestProduct> products = new ArrayList<>();
        
        products.add(new TestProduct("나이키 에어맥스 90", 59000));
        products.add(new TestProduct("나이키 에어포스 1", 89000));
        products.add(new TestProduct("나이키 조던 1", 129000));
        products.add(new TestProduct("나이키 덩크 로우", 109000));
        products.add(new TestProduct("나이키 에어맥스 270", 139000));
        products.add(new TestProduct("나이키 프리런 5.0", 79000));
        products.add(new TestProduct("나이키 리액트 엘리먼트 55", 99000));
        products.add(new TestProduct("나이키 에어맥스 97", 149000));
        products.add(new TestProduct("나이키 코르테즈", 69000));
        products.add(new TestProduct("나이키 블레이저 미드", 89000));
        
        return products;
    }

    /**
     * 상품 목록 출력
     */
    private static void printProducts(List<TestProduct> products, String title) {
        System.out.println("📋 " + title + ":");
        for (int i = 0; i < products.size(); i++) {
            TestProduct product = products.get(i);
            System.out.printf("   %2d. %-30s - %,d원%n", 
                i + 1, product.getName(), product.getPrice());
        }
    }

    /**
     * 가격순 정렬
     * 
     * @param products 정렬할 상품 목록
     * @param ascending true면 오름차순, false면 내림차순
     * @return 정렬된 상품 목록
     */
    private static List<TestProduct> sortProductsByPrice(List<TestProduct> products, boolean ascending) {
        List<TestProduct> sorted = new ArrayList<>(products);
        
        sorted.sort((a, b) -> {
            int comparison = Integer.compare(a.getPrice(), b.getPrice());
            return ascending ? comparison : -comparison;
        });
        
        return sorted;
    }

    /**
     * 정렬 검증
     */
    private static void verifySort(List<TestProduct> products, boolean ascending) {
        boolean isCorrect = true;
        
        for (int i = 0; i < products.size() - 1; i++) {
            int currentPrice = products.get(i).getPrice();
            int nextPrice = products.get(i + 1).getPrice();
            
            if (ascending && currentPrice > nextPrice) {
                isCorrect = false;
                break;
            } else if (!ascending && currentPrice < nextPrice) {
                isCorrect = false;
                break;
            }
        }
        
        String sortType = ascending ? "오름차순" : "내림차순";
        System.out.println("   - 정렬 검증: " + (isCorrect ? "✅ 올바른 " + sortType + " 정렬" : "❌ 정렬 오류"));
    }

    /**
     * 정렬 키워드 감지 테스트
     */
    private static void testKeywordDetection() {
        String[] testMessages = {
            "나이키 운동화 최저가순으로 검색해줘",
            "나이키 운동화 최고가순으로 검색해줘",
            "나이키 운동화 낮은가격순으로 검색해줘",
            "나이키 운동화 높은가격순으로 검색해줘",
            "나이키 운동화 저렴한 가격순으로 검색해줘",
            "나이키 운동화 비싼 가격순으로 검색해줘",
            "나이키 운동화 검색해줘"
        };
        
        for (String message : testMessages) {
            System.out.println("🔍 메시지: '" + message + "'");
            String sortOrder = detectPriceSortRequest(message);
            String cleanQuery = removeSortKeywords(message);
            
            System.out.println("   - 감지된 정렬: " + (sortOrder != null ? sortOrder : "없음"));
            System.out.println("   - 정리된 검색어: '" + cleanQuery + "'");
            System.out.println();
        }
    }

    /**
     * 가격순 정렬 요청 감지 (실제 서비스와 동일한 로직)
     */
    private static String detectPriceSortRequest(String message) {
        if (message == null) return null;
        
        String lowerMessage = message.toLowerCase();
        
        // 오름차순 (낮은 가격순) 키워드
        if (lowerMessage.contains("최저가순") || lowerMessage.contains("최저가 순") ||
            lowerMessage.contains("낮은가격순") || lowerMessage.contains("낮은 가격순") ||
            lowerMessage.contains("저렴한가격순") || lowerMessage.contains("저렴한 가격순") ||
            lowerMessage.contains("싼가격순") || lowerMessage.contains("싼 가격순") ||
            lowerMessage.contains("오름차순")) {
            return "asc";
        }
        
        // 내림차순 (높은 가격순) 키워드
        if (lowerMessage.contains("최고가순") || lowerMessage.contains("최고가 순") ||
            lowerMessage.contains("높은가격순") || lowerMessage.contains("높은 가격순") ||
            lowerMessage.contains("비싼가격순") || lowerMessage.contains("비싼 가격순") ||
            lowerMessage.contains("내림차순")) {
            return "desc";
        }
        
        return null;
    }

    /**
     * 정렬 키워드 제거 (실제 서비스와 동일한 로직)
     */
    private static String removeSortKeywords(String message) {
        if (message == null) return null;
        
        String result = message;
        
        String[] sortKeywords = {
            "가격순으로", "가격 순으로", "가격순", "가격 순",
            "낮은가격순", "낮은 가격순", "저렴한가격순", "저렴한 가격순", 
            "싼가격순", "싼 가격순", "최저가순", "최저가 순", "오름차순",
            "높은가격순", "높은 가격순", "비싼가격순", "비싼 가격순", 
            "최고가순", "최고가 순", "내림차순",
            "정렬해줘", "정렬해", "정렬", "sort"
        };
        
        for (String keyword : sortKeywords) {
            result = result.replaceAll("(?i)" + keyword, "").trim();
        }
        
        return result.isEmpty() ? message : result;
    }

    /**
     * 테스트용 상품 클래스
     */
    static class TestProduct {
        private String name;
        private int price;

        public TestProduct(String name, int price) {
            this.name = name;
            this.price = price;
        }

        public String getName() {
            return name;
        }

        public int getPrice() {
            return price;
        }
    }
}
