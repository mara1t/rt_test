package org.example.tests;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import io.appium.java_client.android.AndroidDriver;
import org.example.config.Config;
import org.example.pages.AlchemyPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.*;

public class AlchemyHintsTest {

    private static final Logger logger = LoggerFactory.getLogger(AlchemyHintsTest.class);

    private AlchemyPage alchemyPage;

    @BeforeClass
    public void setupClass() {
        logger.info("Initializing Selenide/Appium configuration for Alchemy puzzle app");
        Configuration.browser = Config.class.getName();
        Configuration.browserSize = null;
        Configuration.timeout = 15000;
        Configuration.pageLoadTimeout = 30000;
        Configuration.screenshots = true;
        Configuration.savePageSource = false;
    }

    @BeforeMethod
    public void setup() {
        logger.info("Opening Alchemy puzzle app");
        Selenide.open();

        try {
            if (WebDriverRunner.hasWebDriverStarted()) {
                AndroidDriver driver = (AndroidDriver) WebDriverRunner.getWebDriver();
                driver.activateApp("com.ilyin.alchemy");
                logger.info("Alchemy puzzle app activated");
                Thread.sleep(2000);
            }
        } catch (Exception e) {
            logger.warn("Failed to activate Alchemy app: {}", e.getMessage());
        }

        alchemyPage = new AlchemyPage();

        alchemyPage.closePopups();
    }

    @Test(description = "Test for getting regular hint")
    public void testGetRegularHint() throws InterruptedException {
        logger.info("========================================");
        logger.info("TEST START: Getting regular hint");
        logger.info("========================================");

        logger.info("Step 1: Click 'Play' button in menu");
        alchemyPage.clickPlayButton();
        Thread.sleep(3000);
        int initialHintsCount = alchemyPage.getCurrentHintsCount();
        logger.info("Initial hints count: {}", initialHintsCount);
        logger.info("Step 2: Click on hints section (window will be closed automatically)");
        alchemyPage.openHintsSection();
        Thread.sleep(3000);

        logger.info("Step 3: Get regular hint (hints window will be closed automatically)");
        alchemyPage.getRegularHint();
        Thread.sleep(3000);

        int expectedCount = initialHintsCount + 2;
        logger.info("Expected hints count after getting hint: {}", expectedCount);

        logger.info("Step 4: Verify that hints count increased to {}", expectedCount);
        alchemyPage.waitForHintsCount(expectedCount, 10);

        boolean hintsCountIncreased = alchemyPage.verifyHintsCount(expectedCount);

        Assert.assertTrue(hintsCountIncreased,
                String.format("Hints count did not increase. Was: %d, expected: %d",
                        initialHintsCount, expectedCount));

        logger.info("TEST SUCCESSFULLY COMPLETED: hints count increased from {} to {}",
                initialHintsCount, expectedCount);
        logger.info("========================================");
    }

    @AfterMethod
    public void tearDown() {
        try {
            logger.info("Saving test result screenshot");
            Selenide.screenshot("alchemy_test_result");
        } catch (Exception e) {
            logger.warn("Failed to take test result screenshot", e);
        }

        try {
            logger.info("Closing Alchemy puzzle app");
            if (WebDriverRunner.hasWebDriverStarted()) {
                AndroidDriver driver = (AndroidDriver) WebDriverRunner.getWebDriver();
                driver.terminateApp("com.ilyin.alchemy");
                logger.info("Alchemy puzzle app successfully closed");
                Thread.sleep(2000);
            }
        } catch (Exception e) {
            logger.warn("Failed to close Alchemy puzzle app", e);
        }
    }

    @AfterClass
    public void tearDownClass() {
        logger.info("Shutting down WebDriver");
        if (WebDriverRunner.hasWebDriverStarted()) {
            Selenide.closeWebDriver();
        }
    }
}