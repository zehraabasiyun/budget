package org.example.butce.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.openqa.selenium.JavascriptExecutor;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

import org.springframework.test.context.ActiveProfiles;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SeleniumSystemTest {

    @LocalServerPort
    private int port;

    private static WebDriver driver;
    private static WebDriverWait wait;
    private String baseUrl;

    private static String TEST_EMAIL = "selenium_" + UUID.randomUUID() + "@example.com";
    private static String TEST_PASSWORD = "password123";

    @BeforeAll
    static void setupClass() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");

        driver = new ChromeDriver(options);
        driver.manage().window().maximize();

        // Elementlerin yüklenmesi için 10 saniyeye kadar bekleme süresi
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    static void tearDownClass() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    void setup() {
        baseUrl = "http://localhost:" + port;
    }

    /**
     * TEST 1: Kullanıcı Kaydı (Başarılı)
     */
    @Test
    @Order(1)
    void testRegisterSuccess() {
        driver.get(baseUrl + "/register.html");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email"))).sendKeys(TEST_EMAIL);
        driver.findElement(By.id("password")).sendKeys(TEST_PASSWORD);
        driver.findElement(By.id("confirmPassword")).sendKeys(TEST_PASSWORD);
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("successMessage")));
        } catch (Exception e) {
            wait.until(ExpectedConditions.urlContains("login.html"));
        }
    }

    /**
     * TEST 2: Kullanıcı Kaydı (Hata - Mükerrer Email)
     */
    @Test
    @Order(2)
    void testRegisterFailureDuplicate() {
        driver.get(baseUrl + "/register.html");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email"))).sendKeys(TEST_EMAIL);
        driver.findElement(By.id("password")).sendKeys(TEST_PASSWORD);
        driver.findElement(By.id("confirmPassword")).sendKeys(TEST_PASSWORD);
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        WebElement errorDiv = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("errorMessage")));
        assertTrue(errorDiv.isDisplayed());
    }

    /**
     * TEST 3: Kullanıcı Girişi (Başarılı)
     */
    @Test
    @Order(3)
    void testLoginSuccess() {
        driver.get(baseUrl + "/login.html");

        // Eğer zaten giriş yapılmışsa (authToken cookie duruyorsa), auth.js bizi
        // budgets.html'e atar.
        try {
            wait.until(ExpectedConditions.urlContains("budgets.html"));
        } catch (Exception e) {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email"))).sendKeys(TEST_EMAIL);
            driver.findElement(By.id("password")).sendKeys(TEST_PASSWORD);
            driver.findElement(By.cssSelector("button[type='submit']")).click();

            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlContains("index.html"),
                    ExpectedConditions.urlContains("budgets.html")));
        }
    }

    /**
     * TEST 4: Kullanıcı Girişi (Hatalı Şifre)
     */
    @Test
    @Order(4)
    void testLoginFailure() {
        ensureLogout();

        driver.get(baseUrl + "/login.html");

        WebElement emailComp = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));
        emailComp.clear();
        emailComp.sendKeys(TEST_EMAIL);

        driver.findElement(By.id("password")).sendKeys("wrong");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        WebElement errorDiv = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("errorMessage")));
        assertTrue(errorDiv.isDisplayed());
    }

    /**
     * TEST 5: Anasayfa Kontrolü
     */
    @Test
    @Order(5)
    void testHomePage() {
        ensureLogin();

        driver.get(baseUrl + "/index.html");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        String pageSource = driver.getPageSource();
        assertTrue(pageSource.contains("Bütçe Takip Sistemi"));
    }

    /**
     * TEST 6: Bütçe Sayfasına Geçiş
     */
    @Test
    @Order(6)
    void testBudgetPageNavigation() {
        ensureLogin();

        driver.get(baseUrl + "/budgets.html");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("budgetsList")));
        assertTrue(driver.getCurrentUrl().contains("budgets.html"));
    }

    /**
     * TEST 7: Yeni Bütçe Ekleme
     */
    @Test
    @Order(7)
    void testAddBudget() {
        ensureLogin();
        driver.get(baseUrl + "/budgets.html");

        // Sayfanın tam yüklenmesini bekle
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("budgetsList")));

        WebElement openModalBtn = wait
                .until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Yeni')]")));
        openModalBtn.click();

        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("createBudgetModal")));

        modal.findElement(By.id("budgetName")).sendKeys("Selenium Budget");


        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebElement startDate = modal.findElement(By.id("startDate"));
        WebElement endDate = modal.findElement(By.id("endDate"));

        js.executeScript("arguments[0].value = '2024-01-01'", startDate);
        js.executeScript("arguments[0].value = '2024-12-31'", endDate);

        modal.findElement(By.id("totalBudget")).sendKeys("5000");
        modal.findElement(By.id("monthlyLimit")).sendKeys("1000");


        WebElement submitBtn = modal.findElement(By.cssSelector("button[type='submit']"));
        submitBtn.click();

        wait.until(ExpectedConditions.invisibilityOf(modal));

        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("budgetsList"), "Selenium Budget"));
    }

    /**
     * TEST 8: Harcamalar Sayfasına Geçiş
     */
    @Test
    @Order(8)
    void testExpensePageNavigation() {
        ensureLogin();
        driver.get(baseUrl + "/budgets.html");

        try {
            WebElement firstBudget = wait
                    .until(ExpectedConditions.elementToBeClickable(By.cssSelector(".budget-card-header h3")));
            firstBudget.click();
        } catch (Exception e) {
            testAddBudget();
            WebElement firstBudget = wait
                    .until(ExpectedConditions.elementToBeClickable(By.cssSelector(".budget-card-header h3")));
            firstBudget.click();
        }

        wait.until(ExpectedConditions.urlContains("expenses.html"));
    }

    /**
     * TEST 9: Yeni Harcama Ekleme UI
     */
    @Test
    @Order(9)
    void testAddExpenseUI() {

        if (!driver.getCurrentUrl().contains("expenses.html")) {
            testExpensePageNavigation();
        }

        WebElement addExpenseBtn = wait
                .until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Harcama Ekle')]")));
        addExpenseBtn.click();

        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("addExpenseModal")));
        assertTrue(modal.isDisplayed());

        // Kapat
        modal.findElement(By.className("close")).click();
        wait.until(ExpectedConditions.invisibilityOf(modal));
    }

    /**
     * TEST 10: Çıkış Yapma
     */
    @Test
    @Order(10)
    void testLogout() {
        ensureLogout();
        wait.until(ExpectedConditions.urlContains("login.html"));
    }

    // Yardımcı Metodlar

    private void ensureLogout() {
        if (!driver.getCurrentUrl().contains("login.html")) {
            driver.get(baseUrl + "/budgets.html");
        }

        try {

            wait.until(ExpectedConditions.or(
                    ExpectedConditions.visibilityOfElementLocated(By.id("email")),
                    ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[contains(text(),'Çıkış')]"))));

            if (!driver.findElements(By.id("email")).isEmpty() && driver.findElement(By.id("email")).isDisplayed()) {
            } else {
                driver.findElement(By.xpath("//button[contains(text(),'Çıkış')]")).click();
                wait.until(ExpectedConditions.urlContains("login.html"));
            }
        } catch (Exception e) {

            driver.manage().deleteAllCookies();
            driver.get(baseUrl + "/login.html");
        }
    }

    private void ensureLogin() {

        if (!driver.getCurrentUrl().contains("login.html") && !driver.getPageSource().contains("Giriş Yap")) {
            driver.get(baseUrl + "/budgets.html");
            try {
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("budgetsList")));
                return;
            } catch (Exception e) {
            }
        }

        driver.get(baseUrl + "/login.html");

        try {
            WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));
            emailInput.clear();
            emailInput.sendKeys(TEST_EMAIL);
            driver.findElement(By.id("password")).sendKeys(TEST_PASSWORD);
            driver.findElement(By.cssSelector("button[type='submit']")).click();

            wait.until(ExpectedConditions.urlContains("budgets.html"));
        } catch (Exception e) {
            System.out.println("Login Failed: " + e.getMessage());
        }
    }
}
