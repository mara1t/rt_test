package org.example.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.codeborne.selenide.Selenide.$;

public class AlchemyPage {

    private static final Logger logger = LoggerFactory.getLogger(AlchemyPage.class);

    private final By playButton = AppiumBy.xpath("//androidx.compose.ui.platform.e1/android.view.View/android.view.View/android.view.View[2]/android.widget.Button");
    private final By hintsButton = AppiumBy.xpath("//androidx.compose.ui.platform.e1/android.view.View/android.view.View/android.view.View[1]/android.view.View[22]/android.view.View[1]/android.widget.Button");
    private final By getHintButton = AppiumBy.xpath("//androidx.compose.ui.platform.e1/android.view.View/android.view.View/android.view.View[2]/android.view.View/android.view.View[2]/android.view.View[2]/android.view.View/android.widget.Button");
    private final By hintsCount = AppiumBy.id("//android.widget.TextView[starts-with(@text, 'Hints')");
    private final By hintsText = AppiumBy.xpath("//android.widget.TextView[starts-with(@text, 'Hints') or starts-with(@text, 'Подсказки')]");

    // Elements for closing hints window
    private final By closeHintsWindow = AppiumBy.xpath("//*[contains(@content-desc, 'закрыть') or contains(@text, 'Закрыть') or contains(@resource-id, 'close')]");
    private final By backArrowButton = AppiumBy.xpath("//android.widget.ImageButton[@content-desc='Navigate up']");
    private final By gameAreaButton = AppiumBy.xpath("//*[contains(@resource-id, 'game_area') or contains(@class, 'GameView')]");

    private final By menuButton = AppiumBy.xpath("//*[contains(@content-desc, 'меню') or contains(@text, 'Меню') or contains(@resource-id, 'menu')]");
    private final By backButton = AppiumBy.xpath("//*[contains(@content-desc, 'назад') or contains(@text, 'Назад')]");

    public AlchemyPage clickPlayButton() {
        logger.info("Step 1: Clicking 'Play' button in main menu");

        $(playButton)
                .shouldBe(Condition.visible, Duration.ofSeconds(10))
                .click();

        logger.info("'Play' button successfully clicked");
        return this;
    }

    public AlchemyPage openHintsSection() {
        logger.info("Step 2: Opening hints section");

        $(hintsButton)
                .shouldBe(Condition.visible, Duration.ofSeconds(10))
                .click();

        logger.info("Hints section opened");

        // Handle window that appears when opening hints
        //closeHintsPanel();

        return this;
    }

    public AlchemyPage closeHintsPanel() {
        try {
            logger.info("Checking for hints window presence");
            Thread.sleep(1000);

            // Try to close hints window using different methods
            boolean closed = false;

            // Method 1: close button
            SelenideElement closeBtn = $(closeHintsWindow);
            if (closeBtn.exists() && closeBtn.isDisplayed()) {
                logger.info("Close button found, closing hints window");
                closeBtn.click();
                closed = true;
                Thread.sleep(1000);
            }

            // Method 2: back arrow
            SelenideElement backArrow = $(backArrowButton);
            if (!closed && backArrow.exists() && backArrow.isDisplayed()) {
                logger.info("Back arrow found, closing hints window");
                backArrow.click();
                closed = true;
                Thread.sleep(1000);
            }

            // Method 3: click on game area
            SelenideElement gameArea = $(gameAreaButton);
            if (!closed && gameArea.exists() && gameArea.isDisplayed()) {
                logger.info("Clicking on game area to close hints window");
                gameArea.click();
                closed = true;
                Thread.sleep(1000);
            }

            // Method 4: system back button
            if (!closed) {
                logger.info("Attempting to close hints window using system back button");
                pressBackButton();
                Thread.sleep(1000);
                closed = true;
            }

            if (closed) {
                logger.info("Hints window successfully closed");
            } else {
                logger.debug("Hints window not found or already closed");
            }
            Thread.sleep(1000);

        } catch (Exception e) {
            logger.debug("Error while closing hints window: {}", e.getMessage());
        }
        return this;
    }

    private void pressBackButton() {
        try {
            logger.debug("Pressing system back button");
            com.codeborne.selenide.WebDriverRunner.getWebDriver().navigate().back();
        } catch (Exception e) {
            logger.debug("Failed to press system back button: {}", e.getMessage());
        }
    }

    public int getCurrentHintsCount() {
        try {
            SelenideElement hintsElement = $(hintsText)
                    .shouldBe(Condition.visible, Duration.ofSeconds(5));

            String hintsTextValue = hintsElement.getText();
            logger.info("Found hints text: {}", hintsTextValue);

            int count = extractNumberFromHintsText(hintsTextValue);
            logger.info("Current hints count: {}", count);
            return count;
        } catch (Exception e) {
            logger.error("Failed to get hints count", e);
            return 0;
        }
    }

    private int extractNumberFromHintsText(String hintsText) {
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(hintsText);

        if (matcher.find()) {
            int count = Integer.parseInt(matcher.group());
            logger.debug("Extracted number {} from text '{}'", count, hintsText);
            return count;
        }

        logger.warn("Failed to extract number from text: {}", hintsText);
        return 0;
    }

    public AlchemyPage getRegularHint() {
        logger.info("Getting regular hint");


            $(getHintButton)
                    .shouldBe(Condition.visible, Duration.ofSeconds(10))
                    .click();

            logger.info("Get hint button clicked");

        closeHintsPanel();




        return this;
    }

    public AlchemyPage getMultipleHints(int count) {
        logger.info("Getting {} hints", count);

        for (int i = 0; i < count; i++) {
            logger.info("Getting hint #{}", i + 1);
            getRegularHint();
        }

        return this;
    }

    public AlchemyPage waitForHintsCount(int expectedCount, int timeoutSeconds) {
        logger.info("Waiting for hints count to increase to {}", expectedCount);

        long startTime = System.currentTimeMillis();
        long endTime = startTime + (timeoutSeconds * 1000L);

        int currentCount = getCurrentHintsCount();

        while (currentCount < expectedCount && System.currentTimeMillis() < endTime) {
            try {
                Thread.sleep(1000);
                currentCount = getCurrentHintsCount();
                logger.debug("Current hints count: {}", currentCount);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        logger.info("Final hints count: {}", currentCount);
        return this;
    }

    public boolean verifyHintsCount(int expectedCount) {
        int actualCount = getCurrentHintsCount();
        boolean isEqual = actualCount == expectedCount;

        if (isEqual) {
            logger.info("✅ Verification passed: hints count equals {}", expectedCount);
        } else {
            logger.error("❌ Verification failed: expected {}, got {}", expectedCount, actualCount);
        }

        return isEqual;
    }

    public AlchemyPage waitForHintsCountDecrease(int expectedCount, int timeoutSeconds) {
        logger.info("Waiting for hints count to decrease to {}", expectedCount);

        long startTime = System.currentTimeMillis();
        long endTime = startTime + (timeoutSeconds * 1000L);

        int currentCount = getCurrentHintsCount();

        while (currentCount > expectedCount && System.currentTimeMillis() < endTime) {
            try {
                Thread.sleep(1000);
                currentCount = getCurrentHintsCount();
                logger.debug("Current hints count: {}", currentCount);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        logger.info("Final hints count: {}", currentCount);
        return this;
    }

    public AlchemyPage returnToGame() {
        logger.info("Returning to game from hints window");
        closeHintsPanel();
        return this;
    }

    public AlchemyPage closePopups() {
        try {
            SelenideElement closeButton = $(AppiumBy.xpath("//*[contains(@text, 'Закрыть') or contains(@resource-id, 'close')]"));
            if (closeButton.exists()) {
                logger.info("Closing popup window");
                closeButton.click();
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            logger.debug("No popup windows to close");
        }
        return this;
    }
}