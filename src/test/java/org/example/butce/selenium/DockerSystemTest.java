package org.example.butce.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.UUID;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DockerSystemTest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "http://localhost:8086";

    private static final String TEST_EMAIL = "docker_user_" + UUID.randomUUID() + "@example.com";
    private static final String TEST_PASSWORD = "Password123!";

    @BeforeAll
    static void setupClass() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();

        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--start-maximized");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    static void tearDownClass() {
        if (driver != null) {
            driver.quit();
        }
    }

    /**
     * SENARYO 1: Kullanıcı Kaydı
     */
    @Test
    @Order(1)
    void testRegister() {
        driver.get(BASE_URL + "/register.html");

        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));
        emailInput.sendKeys(TEST_EMAIL);
        driver.findElement(By.id("password")).sendKeys(TEST_PASSWORD);
        driver.findElement(By.id("confirmPassword")).sendKeys(TEST_PASSWORD);

        driver.findElement(By.cssSelector("button[type='submit']")).click();

        try {
            wait.until(ExpectedConditions.alertIsPresent());
            driver.switchTo().alert().accept();
        } catch (Exception e) {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.visibilityOfElementLocated(By.id("successMessage")),
                    ExpectedConditions.urlContains("login.html")
            ));
        }
    }

    /**
     * SENARYO 2: Kullanıcı Girişi
     */
    @Test
    @Order(2)
    void testLogin() {
        driver.get(BASE_URL + "/login.html");

        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));
        emailInput.sendKeys(TEST_EMAIL);
        driver.findElement(By.id("password")).sendKeys(TEST_PASSWORD);

        driver.findElement(By.cssSelector("button[type='submit']")).click();

        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("index.html"),
                ExpectedConditions.urlContains("budgets.html")));
    }

    /**
     * SENARYO 3: Yeni Bütçe Ekleme
     */
    @Test
    @Order(3)
    void testAddBudget() {
        if (!driver.getCurrentUrl().contains("index.html") && !driver.getCurrentUrl().contains("budgets.html")) {
            testLogin();
        }

        driver.get(BASE_URL + "/budgets.html");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("budgetsList")));

        WebElement openModalBtn = wait
                .until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Yeni')]")));
        openModalBtn.click();

        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("createBudgetModal")));

        modal.findElement(By.id("budgetName")).sendKeys("Docker Test Budget");

        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].value = '2025-01-01'", modal.findElement(By.id("startDate")));
        js.executeScript("arguments[0].value = '2025-12-31'", modal.findElement(By.id("endDate")));

        modal.findElement(By.id("totalBudget")).sendKeys("15000");
        modal.findElement(By.id("monthlyLimit")).sendKeys("1250");

        modal.findElement(By.cssSelector("button[type='submit']")).click();

        wait.until(ExpectedConditions.invisibilityOf(modal));

        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("budgetsList"), "Docker Test Budget"));
    }
}
