package org.example.tests;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.example.config.Config;
import org.example.pages.VKVideoPage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.*;

import static com.codeborne.selenide.Selenide.$;
import static org.example.pages.VKVideoPage.disableWifiViaUI;

public class VKVideoPlaybackTest {

    private static final Logger logger = LoggerFactory.getLogger(VKVideoPlaybackTest.class);

    private VKVideoPage vkVideoPage;

    @BeforeClass
    public void setupClass() {
        logger.info("Initializing Selenide / Appium configuration");
        Configuration.browser = Config.class.getName();
        Configuration.browserSize = null;
        Configuration.timeout = 15000;
        Configuration.pageLoadTimeout = 30000;
        Configuration.screenshots = true;
        Configuration.savePageSource = false;
    }

    @BeforeMethod
    public void setup() {
        logger.info("Opening VK Video app and preparing the page");
        Selenide.open();

        try {
            if (WebDriverRunner.hasWebDriverStarted()) {
                AndroidDriver driver = (AndroidDriver) WebDriverRunner.getWebDriver();
                driver.activateApp("com.vk.vkvideo");
                logger.info("VK Video app activated");
                Thread.sleep(2000);
            }
        } catch (Exception e) {
            logger.warn("Failed to activate VK Video app, continuing: {}", e.getMessage());
        }

        vkVideoPage = new VKVideoPage();

        vkVideoPage.skipOnboarding()
                .closePopups();
    }

    @Test(priority = 1, description = "Positive test: video plays successfully")
    public void testVideoPlaysSuccessfully() {
        logger.info("Positive test: verifying successful video playback");

        vkVideoPage.searchVideo("luntik");

        try {
            vkVideoPage.openVideoByIndex(0);

            Thread.sleep(5000);

            boolean playerVisible = vkVideoPage.isVideoPlayerVisible();

            Assert.assertTrue(playerVisible, "Video player did not appear on screen");
            logger.info("Video player is displayed");

            boolean isPlaying = vkVideoPage.isVideoPlaying();
            Assert.assertTrue(isPlaying, "Video is not playing (progress does not change)");
            logger.info("Video is playing");

            boolean hasControls = vkVideoPage.hasPlaybackControls();
            Assert.assertTrue(hasControls, "Playback controls not found");
            logger.info("Playback controls are present");

            String videoTitle = vkVideoPage.getVideoTitle();
            Assert.assertFalse(videoTitle.isEmpty(), "Video title should not be empty");
            logger.info("Playing video: {}", videoTitle);

        } catch (Exception e) {
            logger.error("Exception while verifying video playback", e);
            Assert.fail("Error while verifying video playback: " + e.getMessage());
        }
    }


    @Test(priority = 2, description = "Test: video buffering during Wi-Fi interruption")
    public void testVideoBufferingDuringWifiDisconnect() {
        logger.info("========================================");
        logger.info("TEST START: Video buffering during Wi-Fi interruption");
        logger.info("========================================");

        // Включаем Wi-Fi и запускаем видео

        vkVideoPage.searchVideo("nature relaxation");

        try {
            vkVideoPage.openVideoByIndex(0);
            Thread.sleep(5000);

            logger.info("Disabling Wi-Fi during playback");
            disableWifiViaUI();
            Thread.sleep(3000);
            // Ждем начала воспроизведения
            boolean playing = vkVideoPage.isVideoPlaying();
            Assert.assertTrue(playing, "Video should start playing");
            logger.info("✓ Video started playing");

            // Отключаем Wi-Fi во время воспроизведения


            // Проверяем что видео буферизирует или остановилось
            boolean isBuffering = vkVideoPage.isVideoBuffering();
            boolean hasError = vkVideoPage.isNetworkErrorDisplayed();
            boolean stopped = vkVideoPage.isVideoStopped();

            logger.info("Video buffering: {}", isBuffering);
            logger.info("Network error: {}", hasError);
            logger.info("Video stopped: {}", stopped);

            Assert.assertTrue(isBuffering || hasError || stopped,
                    "Video should buffer or show error after Wi-Fi disconnect");
            logger.info("✓ Video entered buffering/error state");

            // Восстанавливаем Wi-Fi
            logger.info("Re-enabling Wi-Fi");
            //NetworkUtils.enableWifi();
            Thread.sleep(5000);

            // Проверяем восстановление
            boolean resumed = vkVideoPage.isVideoPlaying();
            Assert.assertTrue(resumed, "Video should resume after Wi-Fi reconnection");
            logger.info("✓ Video resumed playing after Wi-Fi reconnection");

            logger.info("✅ TEST SUCCESSFULLY COMPLETED: Video handles Wi-Fi interruption correctly");

        } catch (Exception e) {
            logger.error("Exception during Wi-Fi interruption test", e);
            Assert.fail("Error: " + e.getMessage());
        }

        logger.info("========================================");
    }

    @AfterMethod
    public void tearDown() {
        try {
            logger.info("Saving test result screenshot");
            Selenide.screenshot("test_result");
        } catch (Exception e) {
            logger.warn("Failed to take test result screenshot", e);
        }

        try {
            logger.info("Closing VK Video app");
            if (WebDriverRunner.hasWebDriverStarted()) {
                AndroidDriver driver = (AndroidDriver) WebDriverRunner.getWebDriver();
                driver.terminateApp("com.vk.vkvideo");
                logger.info("VK Video app successfully closed");

                Thread.sleep(3000);
                logger.debug("Pause after closing app completed");
            }
        } catch (Exception e) {
            logger.warn("Failed to close VK Video app", e);
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