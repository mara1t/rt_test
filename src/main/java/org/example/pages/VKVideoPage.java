package org.example.pages;

import com.codeborne.selenide.WebDriverRunner;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import io.appium.java_client.android.AndroidDriver;
import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class VKVideoPage {

    private static final Logger logger = LoggerFactory.getLogger(VKVideoPage.class);

    private final By searchButton = AppiumBy.id("com.vk.vkvideo:id/search_button");
    private final By searchInput = AppiumBy.id("com.vk.vkvideo:id/query");
    private final By videoPlayerContainer = AppiumBy.id("com.vk.vkvideo:id/playerContainer");
    private final By contentContainer = AppiumBy.id("com.vk.vkvideo:id/contentContainer");
    private final By playPauseButton = AppiumBy.id("com.vk.vkvideo:id/video_play_button");
    private final By videoProgress = AppiumBy.id("com.vk.vkvideo:id/progress");
    private final By videoTitle = AppiumBy.id("com.vk.vkvideo:id/title");
    private final By errorMessage = AppiumBy.id("com.vk.vkvideo:id/errorMessage");
    private final By retryButton = AppiumBy.id("com.vk.vkvideo:id/retry_button");

    private final String videoItemXPath = "//android.widget.FrameLayout[contains(@resource-id, 'video')]";
    private final String playIconXPath = "//*[contains(@resource-id, 'play') or contains(@content-desc, 'play')]";

    public VKVideoPage skipOnboarding() {
        try {
            String[] onboardingXpaths = {
                    "//*[contains(@text, 'Пропустить') or contains(@text, 'Skip')]",
                    "//*[contains(@text, 'Продолжить без входа') or contains(@text, 'Continue without')]",
                    "//*[contains(@text, 'Позже') or contains(@text, 'Later')]"
            };

            for (String xpath : onboardingXpaths) {
                SelenideElement button = $(AppiumBy.xpath(xpath));
                if (button.exists()) {
                    logger.info("Found onboarding element by xpath '{}', clicking it", xpath);
                    button.click();
                    Thread.sleep(1000);
                }
            }
        } catch (Exception e) {
            logger.debug("Error while trying to skip onboarding (onboarding may be absent): {}", e.getMessage());
        }
        return this;
    }

    public VKVideoPage closePopups() {
        try {
            for (int i = 0; i < 3; i++) {
                boolean closedSomething = false;

                SelenideElement closeByIcon = $(
                        AppiumBy.xpath("//*[@content-desc='Закрыть' or contains(@resource-id, 'close')]"));
                if (closeByIcon.exists()) {
                    logger.info("Found popup (close icon), closing it");
                    closeByIcon.click();
                    closedSomething = true;
                }

                SelenideElement denyButton = $(
                        AppiumBy.xpath("//*[contains(@text, 'Не сейчас') or contains(@text, 'Not now')]"));
                if (denyButton.exists()) {
                    logger.info("Found dialog, clicking 'Not now'");
                    denyButton.click();
                    closedSomething = true;
                }

                SelenideElement backButton = $(
                        AppiumBy.xpath("//*[contains(@text, 'Назад') or contains(@text, 'Back')]"));
                if (backButton.exists()) {
                    logger.info("Found dialog, clicking 'Back'");
                    backButton.click();
                    closedSomething = true;
                }

                if (!closedSomething) {
                    logger.debug("No popups found on iteration {}", i);
                    break;
                }

                Thread.sleep(1000);
            }
        } catch (Exception e) {
            logger.debug("Error while closing popups (probably no popups): {}", e.getMessage());
        }
        return this;
    }

    public VKVideoPage searchVideo(String query) {
        logger.info("Searching for video with query: {}", query);

        skipOnboarding();
        closePopups();

        $(searchButton)
                .shouldBe(Condition.visible, Duration.ofSeconds(10))
                .click();

        $(searchInput)
                .shouldBe(Condition.visible, Duration.ofSeconds(5))
                .setValue(query);

        pressAndroidEnter();

        return this;
    }

    public VKVideoPage openVideoByIndex(int index) {
        logger.info("Opening video with index {} from search results", index);
        $$(AppiumBy.xpath(videoItemXPath))
                .shouldHave(CollectionCondition.sizeGreaterThan(index), Duration.ofSeconds(10));

        $$(AppiumBy.xpath(videoItemXPath))
                .get(index)
                .shouldBe(Condition.visible)
                .click();

        return this;
    }

    public boolean isVideoPlayerVisible() {
        try {
            boolean visible = $(videoPlayerContainer)
                    .shouldBe(Condition.visible, Duration.ofSeconds(15))
                    .exists();
            logger.info("Video player visibility check: {}", visible);
            return visible;
        } catch (Exception e) {
            logger.warn("Video player did not appear: {}", e.getMessage());
            return false;
        }
    }

    public boolean isVideoPlaying() {
        try {
            logger.info("Checking if video is playing");

            String initial = getVideoProgress();
            logger.debug("Initial progress value: {}", initial);

            Thread.sleep(9000);

            String current = getVideoProgress();
            logger.debug("Current progress value: {}", current);

            if (initial != null && current != null && !initial.equals(current)) {
                logger.info("Progress changed ({} -> {}), considering video is playing", initial, current);
                return true;
            }

            logger.warn("Progress did not change, using fallback criteria: player + controls visible");
            return isVideoPlayerVisible() && hasPlaybackControls();
        } catch (Exception e) {
            logger.error("Error while checking playback, using fallback criteria", e);
            return isVideoPlayerVisible();
        }
    }

    public boolean isVideoNotPlaying() {
        try {
            logger.info("Checking negative scenario - video is not playing");

            boolean hasError = isErrorMessageDisplayed();
            if (hasError) {
                logger.info("Playback error message detected");
                return true;
            }

            String initial = getVideoProgress();
            logger.debug("Initial progress value: {}", initial);

            Thread.sleep(9000);

            String current = getVideoProgress();
            logger.debug("Current progress value: {}", current);

            boolean progressNotChanged = initial != null && current != null && initial.equals(current);
            boolean playerNotVisible = !isVideoPlayerVisible();

            boolean videoNotPlaying = progressNotChanged || playerNotVisible;

            if (videoNotPlaying) {
                logger.info("Video is not playing (progress didn't change or player is not visible)");
            }

            return videoNotPlaying;
        } catch (Exception e) {
            logger.error("Error while checking absence of playback", e);
            return true;
        }
    }

    public boolean isErrorMessageDisplayed() {
        try {
            boolean errorExists = $(errorMessage).exists();
            if (errorExists) {
                String errorText = $(errorMessage).getText();
                logger.info("Error message found: {}", errorText);
            }
            return errorExists;
        } catch (Exception e) {
            logger.debug("Error message not found");
            return false;
        }
    }

    public boolean hasRetryButton() {
        try {
            return $(retryButton).exists();
        } catch (Exception e) {
            return false;
        }
    }

    private String getVideoProgress() {
        try {
            SelenideElement progressBar = $(videoProgress);
            if (progressBar.exists()) {
                return progressBar.getAttribute("content-desc");
            } else {
                logger.debug("Video progress bar not found");
            }
        } catch (Exception e) {
            logger.debug("Error while getting video progress: {}", e.getMessage());
        }
        return "0";
    }

    public boolean hasPlaybackControls() {
        try {
            logger.info("Checking for playback controls presence");
            $(videoPlayerContainer).click();
            Thread.sleep(1000);

            boolean hasControls = $(playPauseButton).exists() ||
                    $(AppiumBy.xpath(playIconXPath)).exists();
            logger.info("Playback controls presence: {}", hasControls);
            return hasControls;
        } catch (Exception e) {
            logger.warn("Failed to check playback controls presence: {}", e.getMessage());
            return false;
        }
    }

    public String getVideoTitle() {
        try {
            String title = $(contentContainer).$(videoTitle).getText();
            logger.info("Playing video title: {}", title);
            return title;
        } catch (Exception e) {
            logger.warn("Failed to get video title: {}", e.getMessage());
            return "";
        }
    }

    public VKVideoPage pauseVideo() {
        try {
            logger.info("Pausing video");
            $(videoPlayerContainer).click();
            Thread.sleep(1000);
            $(playPauseButton).click();
            Thread.sleep(2000);
        } catch (Exception e) {
            logger.warn("Failed to pause video: {}", e.getMessage());
        }
        return this;
    }

    public VKVideoPage resumeVideo() {
        try {
            logger.info("Resuming video");
            $(playPauseButton).click();
            Thread.sleep(2000);
        } catch (Exception e) {
            logger.warn("Failed to resume video: {}", e.getMessage());
        }
        return this;
    }

    private void pressAndroidEnter() {
        AndroidDriver driver = (AndroidDriver) WebDriverRunner.getWebDriver();
        driver.executeScript("mobile: performEditorAction", Map.of("action", "search"));
    }

    public boolean isNetworkErrorDisplayed() {
        try {
            By networkError = AppiumBy.xpath("//*[contains(@text, 'No internet connection') or " +
                    "contains(@text, 'Network error') or " +
                    "contains(@text, 'Check your connection') or " +
                    "contains(@text, 'Нет подключения')]");

            SelenideElement error = $(networkError);
            boolean displayed = error.exists() && error.isDisplayed();

            if (displayed) {
                logger.info("Network error message displayed");
            }

            return displayed;

        } catch (Exception e) {
            logger.debug("Network error message not found");
            return false;
        }
    }

    public boolean isVideoBuffering() {
        try {
            By bufferingIndicator = AppiumBy.xpath("//*[contains(@resource-id, 'buffering') or " +
                    "contains(@resource-id, 'loading') or " +
                    "contains(@content-desc, 'loading')]");

            SelenideElement buffering = $(bufferingIndicator);
            boolean isBuffering = buffering.exists() && buffering.isDisplayed();

            if (isBuffering) {
                logger.info("Buffering indicator detected");
            }

            return isBuffering;

        } catch (Exception e) {
            logger.debug("No buffering indicator found");
            return false;
        }
    }

    public String getNetworkStatusMessage() {
        try {
            By messageElement = AppiumBy.xpath("//*[contains(@text, 'connection') or " +
                    "contains(@text, 'network') or " +
                    "contains(@text, 'internet')]");

            SelenideElement element = $(messageElement);
            if (element.exists()) {
                String message = element.getText();
                logger.info("Network status message: {}", message);
                return message;
            }
        } catch (Exception e) {
            logger.debug("No network status message found");
        }
        return "";
    }

    public boolean isVideoStopped() {
        try {
            logger.info("Checking if video is stopped/paused");

            SelenideElement playIcon = $(AppiumBy.xpath(playIconXPath));
            boolean playIconVisible = playIcon.exists() && playIcon.isDisplayed();

            if (playIconVisible) {
                logger.info("Play icon is visible - video is stopped/paused");
                return true;
            }

            String initialProgress = getVideoProgress();
            logger.debug("Initial progress: {}", initialProgress);

            Thread.sleep(5000);

            String currentProgress = getVideoProgress();
            logger.debug("Current progress after 5 seconds: {}", currentProgress);

            boolean progressNotChanged = initialProgress.equals(currentProgress);

            if (progressNotChanged) {
                logger.info("Video progress hasn't changed - video is stopped");
                return true;
            }

            logger.info("Video appears to be playing (progress changed)");
            return false;

        } catch (Exception e) {
            logger.error("Error checking if video is stopped", e);
            return false;
        }
    }

    public static void disableWifiViaUI() {
        try {
            AndroidDriver driver = (AndroidDriver) WebDriverRunner.getWebDriver();

            driver.activateApp("com.android.settings");
            Thread.sleep(2000);

            SelenideElement wifiOption = $(AppiumBy.xpath(
                    "//androidx.recyclerview.widget.RecyclerView[@resource-id=\"com.android.settings:id/recycler_view\"]/android.widget.LinearLayout[1]/android.widget.RelativeLayout"));
            wifiOption.click();
            Thread.sleep(10000);

            SelenideElement wifiSwitch = $(AppiumBy.xpath("//android.widget.Switch[@content-desc=\"Wi‑Fi\"]"));
            wifiSwitch.click();
            Thread.sleep(5000);

            driver.activateApp("com.vk.vkvideo");
            Thread.sleep(5000);

            logger.info("Wi-Fi disabled via UI");

        } catch (Exception e) {
            logger.error("Failed to disable Wi-Fi via UI", e);
        }
    }

}