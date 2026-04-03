package com.example.QLDatVe.e2e;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class FrontendSeleniumSmokeTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private String baseUrl;

    @Before
    public void setUp() {
        String browser = System.getProperty("selenium.browser", "chrome").toLowerCase();
        baseUrl = System.getProperty("selenium.baseUrl", "http://127.0.0.1:3000");
        driver = createDriver(browser);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    @After
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void homePageShouldRenderFeaturedTrips() {
        driver.get(baseUrl + "/");

        WebElement title = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='trip-results-title']")));
        List<WebElement> tripCards = wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(
                By.cssSelector("[data-testid^='trip-card-']"), 0));

        Assert.assertFalse(title.getText().isBlank());
        Assert.assertFalse(tripCards.isEmpty());
    }

    @Test
    public void cashBookingFlowShouldShowSuccessNotification() {
        driver.get(baseUrl + "/");

        WebElement selectTripButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[data-testid='trip-select-1']")));
        selectTripButton.click();

        WebElement tripTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='trip-detail-title']")));
        Assert.assertTrue(tripTitle.getText().contains("#1"));

        WebElement seatB1 = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[data-testid='seat-B1']")));
        seatB1.click();

        WebElement summary = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='selected-seats-summary']")));
        Assert.assertTrue(summary.getText().contains("B1"));

        WebElement cashOption = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[data-testid='payment-cash-option']")));
        cashOption.click();
        wait.until(ExpectedConditions.elementSelectionStateToBe(
                By.cssSelector("[data-testid='payment-cash-radio']"), true));

        WebElement bookingButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[data-testid='booking-submit-button']")));
        bookingButton.click();

        WebElement notification = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='global-notification']")));
        Assert.assertTrue(notification.getText().contains("(mock)"));
    }

    private WebDriver createDriver(String browser) {
        return switch (browser) {
            case "firefox" -> {
                FirefoxOptions options = new FirefoxOptions();
                options.addArguments("-headless");
                yield new FirefoxDriver(options);
            }
            case "edge" -> {
                EdgeOptions options = new EdgeOptions();
                options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage");
                yield new EdgeDriver(options);
            }
            default -> {
                ChromeOptions options = new ChromeOptions();
                options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage");
                yield new ChromeDriver(options);
            }
        };
    }
}
