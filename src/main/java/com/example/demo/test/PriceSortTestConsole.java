package com.example.demo.test;

import com.example.demo.dto.ShoppingMessageResponse;
import com.example.demo.service.SimpleShoppingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.util.List;

/**
 * 가격순 정렬 기능 테스트 콘솔
 * 
 * 이 클래스는 가격순 정렬 기능을 콘솔에서 직접 테스트할 수 있도록 합니다.
 * Spring Boot 애플리케이션으로 실행하여 데이터베이스와 연동된 테스트를 수행합니다.
 * 
 * 실행 방법:
 * 1. IDE에서 main 메서드 실행
 * 2. 터미널: mvn spring-boot:run -Dspring-boot.run.main-class=com.example.demo.test.PriceSortTestConsole
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.example.demo", "com.example.navershopping"})
public class PriceSortTestConsole implements CommandLineRunner {

    @Autowired
    private SimpleShoppingService simpleShoppingService;
    private static final String CLIENT_ID = System.getenv("NAVER_CLIENT_ID");
    private static final String CLIENT_SECRET = System.getenv("NAVER_CLIENT_SECRET");
    public static void main(String[] args) {
        // 환경변수 설정 (Naver API 키)
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("console.encoding", "UTF-8");
        
        SpringApplication.run(PriceSortTestConsole.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("==========================================");
        System.out.println("🎯 가격순 정렬 기능 테스트 시작");
        System.out.println("==========================================");
        
        try {
            // 1. 더미 데이터 생성
            System.out.println("\n📦 1단계: 더미 데이터 생성");
            System.out.println("------------------------------------------");
            int dummyCount = simpleShoppingService.createDummyNikeShoes();
            System.out.println("✅ 더미 데이터 " + dummyCount + "개 생성 완료");
            
            // 2. 사용자 입력 테스트
            System.out.println("\n🔍 2단계: 사용자 입력 테스트");
            System.out.println("------------------------------------------");
            interactiveSearchTest();
            
        } catch (Exception e) {
            System.err.println("❌ 테스트 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 인터랙티브 검색 테스트
     */
    private void interactiveSearchTest() {
        java.util.Scanner scanner = new java.util.Scanner(System.in);
        
        System.out.println("🎮 사용자 입력 테스트 모드");
        System.out.println("💡 사용법:");
        System.out.println("   - 일반 검색: '나이키 운동화 검색해줘'");
        System.out.println("   - 최저가순: '나이키 운동화 최저가순으로 검색해줘'");
        System.out.println("   - 최고가순: '나이키 운동화 최고가순으로 검색해줘'");
        System.out.println("   - 종료: 'exit' 또는 'quit' 입력");
        System.out.println();
        
        while (true) {
            System.out.print("🔍 검색어를 입력하세요: ");
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty()) {
                System.out.println("❌ 검색어를 입력해주세요.");
                continue;
            }
            
            if (input.equalsIgnoreCase("exit") || input.equalsIgnoreCase("quit")) {
                System.out.println("👋 테스트를 종료합니다.");
                break;
            }
            
            System.out.println("\n" + "=".repeat(50));
            System.out.println("🔍 검색 실행: '" + input + "'");
            System.out.println("=".repeat(50));
            
            try {
                ShoppingMessageResponse response = simpleShoppingService.searchProducts(input);
                
                // 검색 결과 출력
                System.out.println("📊 검색 결과:");
                System.out.println("   - 상품 개수: " + response.getProducts().size() + "개");
                System.out.println("   - 정렬 정보: " + (response.getSortOrder() != null ? response.getSortOrder() : "없음"));
                System.out.println("   - 정렬 타입: " + (response.getSortType() != null ? response.getSortType() : "없음"));
                System.out.println("   - 응답 메시지: " + response.getResponse());
                
                if (response.getProducts().size() > 0) {
                    // 정렬 검증
                    if (response.getSortOrder() != null) {
                        boolean isSorted = verifyPriceSort(response.getProducts(), "asc".equals(response.getSortOrder()));
                        String sortType = "asc".equals(response.getSortOrder()) ? "오름차순" : "내림차순";
                        System.out.println("   - 정렬 검증: " + (isSorted ? "✅ 올바른 " + sortType + " 정렬" : "❌ 정렬 오류"));
                    }
                    
                    // 상품 목록 출력 (최대 10개)
                    int displayCount = Math.min(10, response.getProducts().size());
                    System.out.println("   - 상품 목록 (상위 " + displayCount + "개):");
                    
                    for (int i = 0; i < displayCount; i++) {
                        ShoppingMessageResponse.ProductCard product = response.getProducts().get(i);
                        System.out.printf("     %2d. %-50s - %s%n", 
                            i + 1, 
                            truncateString(product.getTitle(), 50), 
                            product.getPriceFormatted());
                    }
                    
                    if (response.getProducts().size() > 10) {
                        System.out.println("     ... 및 " + (response.getProducts().size() - 10) + "개 더");
                    }
                } else {
                    System.out.println("   - 검색 결과가 없습니다.");
                }
                
            } catch (Exception e) {
                System.err.println("❌ 검색 중 오류 발생: " + e.getMessage());
            }
            
            System.out.println("\n" + "-".repeat(50));
            System.out.println();
        }
        
        scanner.close();
    }

    /**
     * 가격 정렬 검증
     * 
     * @param products 상품 목록
     * @param ascending true면 오름차순, false면 내림차순 검증
     * @return 정렬이 올바른지 여부
     */
    private boolean verifyPriceSort(List<ShoppingMessageResponse.ProductCard> products, boolean ascending) {
        if (products.size() < 2) return true;
        
        for (int i = 0; i < products.size() - 1; i++) {
            int currentPrice = products.get(i).getLprice();
            int nextPrice = products.get(i + 1).getLprice();
            
            if (ascending) {
                if (currentPrice > nextPrice) {
                    return false;
                }
            } else {
                if (currentPrice < nextPrice) {
                    return false;
                }
            }
        }
        
        return true;
    }

    /**
     * 문자열 자르기 (긴 제목을 표시용으로 자름)
     * 
     * @param str 원본 문자열
     * @param maxLength 최대 길이
     * @return 잘린 문자열
     */
    private String truncateString(String str, int maxLength) {
        if (str == null) return "";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }
}
