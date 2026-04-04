package com.example.QLDatVe.e2e;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class FrontendKnownIssueEvidenceTest {

    private WebDriver primaryDriver;
    private WebDriver secondaryDriver;
    private WebDriverWait primaryWait;
    private WebDriverWait secondaryWait;
    private String baseUrl;
    private String browserName;
    private String apiBaseUrl;
    private int screenshotCounter;

    @Rule
    public TestName currentTestName = new TestName();

    @Rule
    public TestWatcher screenshotOnFailure = new TestWatcher() {
        @Override
        protected void failed(Throwable error, Description description) {
            captureFailureScreenshots(description.getMethodName());
        }
    };

    @Before
    public void setUp() throws Exception {
        String browser = System.getProperty("selenium.browser", "chrome").toLowerCase();
        browserName = browser;
        screenshotCounter = 1;
        baseUrl = System.getProperty("selenium.baseUrl", "http://127.0.0.1:3000");
        apiBaseUrl = System.getProperty("selenium.apiBaseUrl", "http://127.0.0.1:8081/api");

        primaryDriver = createDriver(browser);
        secondaryDriver = createDriver(browser);

        configureDriver(primaryDriver);
        configureDriver(secondaryDriver);

        primaryWait = new WebDriverWait(primaryDriver, Duration.ofSeconds(20));
        secondaryWait = new WebDriverWait(secondaryDriver, Duration.ofSeconds(20));
    }

    @After
    public void tearDown() {
        if (primaryDriver != null) {
            primaryDriver.quit();
        }
        if (secondaryDriver != null) {
            secondaryDriver.quit();
        }
    }

    @Test
    public void tc03_overlapSeatCash_shouldRejectOneBooking() throws Exception {
        resetMockState();
        List<EvidenceShot> evidenceShots = new ArrayList<>();

        openTripAndSeedUser(primaryDriver, primaryWait, 1, "tc03-user-a", "tc03a@example.com");
        openTripAndSeedUser(secondaryDriver, secondaryWait, 2, "tc03-user-b", "tc03b@example.com");

        selectSeats(primaryDriver, primaryWait, "D1", "D2");
        selectSeats(secondaryDriver, secondaryWait, "D2", "C2");
        choosePaymentMethod(primaryDriver, primaryWait, "cash");
        choosePaymentMethod(secondaryDriver, secondaryWait, "cash");

        evidenceShots.add(captureScenarioScreenshot(primaryDriver, "TC03", "booking", "user-a", "before-submit",
                "User A | seats D1,D2 | CASH | before submit"));
        evidenceShots.add(captureScenarioScreenshot(secondaryDriver, "TC03", "booking", "user-b", "before-submit",
                "User B | seats D2,C2 | CASH | before submit"));

        boolean firstUserSucceeded = submitCashBookingAndDetectSuccess(primaryDriver, primaryWait);
        boolean secondUserSucceeded = submitCashBookingAndDetectSuccess(secondaryDriver, secondaryWait);

        evidenceShots.add(captureScenarioScreenshot(primaryDriver, "TC03", "booking", "user-a", "after-submit",
                "User A | CASH | after submit"));
        evidenceShots.add(captureScenarioScreenshot(secondaryDriver, "TC03", "booking", "user-b", "after-submit",
                "User B | CASH | after submit"));

        createEvidenceBundle(
                "TC03",
                "booking",
                "Hai user dat tap ghe giao nhau bang CASH",
                "Expected: chi 1 booking duoc chap nhan vi trung ghe D2.",
                "Observed: ca hai booking deu thanh cong neu first=true va second=true. first="
                        + firstUserSucceeded + ", second=" + secondUserSucceeded,
                evidenceShots
        );

        Assert.assertFalse(
                "TC03 failed as expected: both bookings containing overlapping seat D2 were accepted.",
                firstUserSucceeded && secondUserSucceeded
        );
    }

    @Test
    public void tc02_sameSeatMomo_shouldRejectOneBooking() throws Exception {
        resetMockState();
        List<EvidenceShot> evidenceShots = new ArrayList<>();

        openTripAndSeedUser(primaryDriver, primaryWait, 3, "tc02-user-a", "tc02a@example.com");
        openTripAndSeedUser(secondaryDriver, secondaryWait, 4, "tc02-user-b", "tc02b@example.com");

        selectSeats(primaryDriver, primaryWait, "A2");
        selectSeats(secondaryDriver, secondaryWait, "A2");
        choosePaymentMethod(primaryDriver, primaryWait, "momo");
        choosePaymentMethod(secondaryDriver, secondaryWait, "momo");

        evidenceShots.add(captureScenarioScreenshot(primaryDriver, "TC02", "payment", "user-a", "before-submit",
                "User A | seat A2 | MOMO | before submit"));
        evidenceShots.add(captureScenarioScreenshot(secondaryDriver, "TC02", "payment", "user-b", "before-submit",
                "User B | seat A2 | MOMO | before submit"));

        boolean firstUserReachedSuccess = submitMomoBookingAndReachPaymentSuccess(primaryDriver, primaryWait);
        boolean secondUserReachedSuccess = submitMomoBookingAndReachPaymentSuccess(secondaryDriver, secondaryWait);

        evidenceShots.add(captureScenarioScreenshot(primaryDriver, "TC02", "payment", "user-a", "after-submit",
                "User A | MOMO | payment success"));
        evidenceShots.add(captureScenarioScreenshot(secondaryDriver, "TC02", "payment", "user-b", "after-submit",
                "User B | MOMO | payment success"));

        createEvidenceBundle(
                "TC02",
                "payment",
                "Hai user dat cung 1 ghe bang MOMO",
                "Expected: chi 1 user duoc thanh toan thanh cong cho ghe A2.",
                "Observed: ca hai user deu toi payment success neu first=true va second=true. first="
                        + firstUserReachedSuccess + ", second=" + secondUserReachedSuccess,
                evidenceShots
        );

        Assert.assertFalse(
                "TC02 failed as expected: both users reached MOMO success for the same seat A2.",
                firstUserReachedSuccess && secondUserReachedSuccess
        );
    }

    @Test
    public void tc24_differentSeatsCash_shouldAllowBothBookings() throws Exception {
        resetMockState();
        List<EvidenceShot> evidenceShots = new ArrayList<>();

        openTripAndSeedUser(primaryDriver, primaryWait, 5, "tc24-user-a", "tc24a@example.com");
        openTripAndSeedUser(secondaryDriver, secondaryWait, 6, "tc24-user-b", "tc24b@example.com");

        selectSeats(primaryDriver, primaryWait, "A2");
        selectSeats(secondaryDriver, secondaryWait, "C1");
        choosePaymentMethod(primaryDriver, primaryWait, "cash");
        choosePaymentMethod(secondaryDriver, secondaryWait, "cash");

        evidenceShots.add(captureScenarioScreenshot(primaryDriver, "TC24", "booking", "user-a", "before-submit",
                "User A | seat A2 | CASH | before submit"));
        evidenceShots.add(captureScenarioScreenshot(secondaryDriver, "TC24", "booking", "user-b", "before-submit",
                "User B | seat C1 | CASH | before submit"));

        boolean firstUserSucceeded = submitCashBookingAndDetectSuccess(primaryDriver, primaryWait);
        boolean secondUserSucceeded = submitCashBookingAndDetectSuccess(secondaryDriver, secondaryWait);

        evidenceShots.add(captureScenarioScreenshot(primaryDriver, "TC24", "booking", "user-a", "after-submit",
                "User A | CASH | after submit"));
        evidenceShots.add(captureScenarioScreenshot(secondaryDriver, "TC24", "booking", "user-b", "after-submit",
                "User B | CASH | after submit"));

        createEvidenceBundle(
                "TC24",
                "booking",
                "Hai user dat dong thoi nhung khac ghe bang CASH",
                "Expected: ca hai user deu dat thanh cong vi khong trung ghe.",
                "Observed: first=" + firstUserSucceeded + ", second=" + secondUserSucceeded,
                evidenceShots
        );

        Assert.assertTrue(
                "TC24 should pass: two users booking different seats must both succeed.",
                firstUserSucceeded && secondUserSucceeded
        );
    }

    private void configureDriver(WebDriver driver) {
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
    }

    private WebDriver createDriver(String browser) {
        String remoteUrl = System.getProperty("selenium.remoteUrl");

        return switch (browser) {
            case "firefox" -> {
                FirefoxOptions options = new FirefoxOptions();
                options.addArguments("-headless");
                yield remoteUrl != null && !remoteUrl.isBlank()
                        ? buildRemoteDriver(remoteUrl, options)
                        : new FirefoxDriver(options);
            }
            case "edge" -> {
                EdgeOptions options = new EdgeOptions();
                options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage");
                yield remoteUrl != null && !remoteUrl.isBlank()
                        ? buildRemoteDriver(remoteUrl, options)
                        : new EdgeDriver(options);
            }
            default -> {
                ChromeOptions options = new ChromeOptions();
                options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage");
                yield remoteUrl != null && !remoteUrl.isBlank()
                        ? buildRemoteDriver(remoteUrl, options)
                        : new ChromeDriver(options);
            }
        };
    }

    private WebDriver buildRemoteDriver(String remoteUrl, Object options) {
        try {
            return new RemoteWebDriver(new URL(remoteUrl), (org.openqa.selenium.Capabilities) options);
        } catch (MalformedURLException exception) {
            throw new IllegalArgumentException("Invalid selenium.remoteUrl: " + remoteUrl, exception);
        }
    }

    private void resetMockState() throws Exception {
        HttpURLConnection connection = (HttpURLConnection) URI.create(apiBaseUrl + "/test/reset").toURL().openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.connect();

        int responseCode = connection.getResponseCode();
        if (responseCode < 200 || responseCode >= 300) {
            throw new IllegalStateException("Unable to reset mock state. HTTP " + responseCode);
        }

        connection.disconnect();
    }

    private void openTripAndSeedUser(WebDriver driver, WebDriverWait wait, int userId, String username, String email) {
        driver.get(baseUrl + "/");
        seedLoggedInUser(driver, userId, username, email);
        driver.navigate().to(baseUrl + "/trips/1");

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='trip-detail-title']")));
    }

    private void seedLoggedInUser(WebDriver driver, int userId, String username, String email) {
        ((JavascriptExecutor) driver).executeScript(
                "window.localStorage.setItem('user', arguments[0]);" +
                "window.localStorage.setItem('token', arguments[1]);",
                String.format("{\"userId\":%d,\"username\":\"%s\",\"email\":\"%s\"}", userId, username, email),
                "mock-token-" + userId);
    }

    private void selectSeats(WebDriver driver, WebDriverWait wait, String... seatNumbers) {
        for (String seatNumber : seatNumbers) {
            wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("[data-testid='seat-" + seatNumber + "']"))).click();
            wait.until(ExpectedConditions.textToBePresentInElementLocated(
                    By.cssSelector("[data-testid='selected-seats-summary']"), seatNumber));
        }
    }

    private void choosePaymentMethod(WebDriver driver, WebDriverWait wait, String paymentMethod) {
        String optionSelector = "cash".equalsIgnoreCase(paymentMethod)
                ? "[data-testid='payment-cash-option']"
                : "[data-testid='payment-momo-option']";
        String radioSelector = "cash".equalsIgnoreCase(paymentMethod)
                ? "[data-testid='payment-cash-radio']"
                : "[data-testid='payment-momo-radio']";

        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(optionSelector))).click();
        wait.until(ExpectedConditions.elementSelectionStateToBe(By.cssSelector(radioSelector), true));
    }

    private boolean submitCashBookingAndDetectSuccess(WebDriver driver, WebDriverWait wait) {
        wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[data-testid='booking-submit-button']"))).click();

        String notificationText = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='global-notification']"))).getText().toLowerCase();

        return notificationText.contains("thành công")
                || notificationText.contains("thanh cong")
                || notificationText.contains("mock")
                || notificationText.contains("success");
    }

    private boolean submitMomoBookingAndReachPaymentSuccess(WebDriver driver, WebDriverWait wait) {
        wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[data-testid='booking-submit-button']"))).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='payment-success-title']")));
        wait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='payment-success-alert']")),
                ExpectedConditions.urlContains("/my-bookings")
        ));

        return true;
    }

    private void captureFailureScreenshots(String testName) {
        String scenario = inferScenarioFromTestName(testName);
        String component = inferComponentFromTestName(testName);
        captureScreenshot(primaryDriver, "fail", scenario, component, testName + "-user-a-FAIL");
        captureScreenshot(secondaryDriver, "fail", scenario, component, testName + "-user-b-FAIL");
    }

    private EvidenceShot captureScenarioScreenshot(WebDriver driver,
                                                   String scenario,
                                                   String component,
                                                   String actor,
                                                   String checkpoint,
                                                   String humanLabel) {
        String label = String.format("%02d-%s-%s-%s", screenshotCounter++, scenario, actor, checkpoint);
        Path savedPath = captureScreenshot(driver, "pass", scenario, component, label);
        return new EvidenceShot(savedPath, humanLabel);
    }

    private Path captureScreenshot(WebDriver driver, String status, String scenario, String component, String label) {
        if (driver == null || !(driver instanceof TakesScreenshot)) {
            return null;
        }

        try {
            Path screenshotDirectory = Path.of(
                    "target",
                    "selenium-screenshots",
                    sanitizeForFileName(status),
                    sanitizeForFileName(scenario),
                    sanitizeForFileName(component));
            Files.createDirectories(screenshotDirectory);

            String safeBrowser = sanitizeForFileName(browserName == null || browserName.isBlank() ? "unknown" : browserName);
            String safeLabel = sanitizeForFileName(label);
            String fileName = safeBrowser + "-" + safeLabel + "-" + System.currentTimeMillis() + ".png";
            Path screenshotPath = screenshotDirectory.resolve(fileName);

            Path tempScreenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE).toPath();
            Files.copy(tempScreenshot, screenshotPath, StandardCopyOption.REPLACE_EXISTING);

            System.out.println("Saved Selenium screenshot to: " + screenshotPath.toAbsolutePath());
            return screenshotPath;
        } catch (Exception exception) {
            System.err.println("Unable to capture Selenium screenshot: " + exception.getMessage());
            return null;
        }
    }

    private String sanitizeForFileName(String value) {
        return value.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private String inferComponentFromTestName(String testName) {
        if (testName == null || testName.isBlank()) {
            return "booking";
        }
        String normalized = testName.toLowerCase();
        if (normalized.contains("momo") || normalized.contains("payment")) {
            return "payment";
        }
        return "booking";
    }

    private String inferScenarioFromTestName(String testName) {
        if (testName == null || testName.isBlank()) {
            return "unknown";
        }
        String normalized = testName.toLowerCase();
        if (normalized.contains("tc02")) {
            return "TC02";
        }
        if (normalized.contains("tc03")) {
            return "TC03";
        }
        if (normalized.contains("tc24")) {
            return "TC24";
        }
        return "unknown";
    }

    private void createEvidenceBundle(String scenario,
                                      String component,
                                      String title,
                                      String expected,
                                      String observed,
                                      List<EvidenceShot> shots) throws Exception {
        Path evidenceDirectory = Path.of("target", "selenium-screenshots", "evidence", scenario, component);
        Files.createDirectories(evidenceDirectory);

        StringBuilder summary = new StringBuilder();
        summary.append("Scenario: ").append(scenario).append(System.lineSeparator());
        summary.append("Title: ").append(title).append(System.lineSeparator());
        summary.append("Expected: ").append(expected).append(System.lineSeparator());
        summary.append("Observed: ").append(observed).append(System.lineSeparator());
        summary.append("Screenshots:").append(System.lineSeparator());

        for (EvidenceShot shot : shots) {
            if (shot != null && shot.path != null) {
                summary.append("- ").append(shot.label).append(" => ").append(shot.path.getFileName()).append(System.lineSeparator());
            }
        }

        Files.writeString(
                evidenceDirectory.resolve(scenario + "-summary.txt"),
                summary.toString(),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
        );

        createCollageImage(evidenceDirectory.resolve(scenario + "-collage.png"), scenario, title, expected, observed, shots);
    }

    private void createCollageImage(Path outputPath,
                                    String scenario,
                                    String title,
                                    String expected,
                                    String observed,
                                    List<EvidenceShot> shots) throws Exception {
        int headerHeight = 180;
        int labelHeight = 36;
        int tileWidth = 620;
        int tileHeight = 360;
        int columns = 2;
        int rows = Math.max(1, (int) Math.ceil(shots.size() / 2.0));
        int canvasWidth = columns * tileWidth;
        int canvasHeight = headerHeight + rows * (tileHeight + labelHeight);

        BufferedImage canvas = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = canvas.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, canvasWidth, canvasHeight);

        graphics.setColor(new Color(28, 43, 56));
        graphics.setFont(new Font("Arial", Font.BOLD, 26));
        graphics.drawString(scenario + " - " + title, 20, 40);

        graphics.setFont(new Font("Arial", Font.PLAIN, 18));
        graphics.drawString(expected, 20, 78);
        graphics.drawString(observed, 20, 108);
        graphics.drawString("Proof bundle: user A + user B, before/after submit, same scenario.", 20, 138);

        for (int index = 0; index < shots.size(); index++) {
            EvidenceShot shot = shots.get(index);
            if (shot == null || shot.path == null || !Files.exists(shot.path)) {
                continue;
            }

            int row = index / columns;
            int col = index % columns;
            int x = col * tileWidth;
            int y = headerHeight + row * (tileHeight + labelHeight);

            BufferedImage image = ImageIO.read(shot.path.toFile());
            if (image == null) {
                continue;
            }

            graphics.setColor(new Color(245, 245, 245));
            graphics.fillRect(x, y, tileWidth, tileHeight + labelHeight);
            graphics.drawImage(image, x + 10, y + 10, tileWidth - 20, tileHeight - 20, null);

            graphics.setColor(new Color(28, 43, 56));
            graphics.setFont(new Font("Arial", Font.BOLD, 16));
            graphics.drawString(shot.label, x + 12, y + tileHeight + 20);
        }

        graphics.dispose();
        ImageIO.write(canvas, "png", outputPath.toFile());
    }

    private static class EvidenceShot {
        private final Path path;
        private final String label;

        private EvidenceShot(Path path, String label) {
            this.path = path;
            this.label = label;
        }
    }
}
