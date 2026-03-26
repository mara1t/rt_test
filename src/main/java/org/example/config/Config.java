package org.example.config;

import com.codeborne.selenide.WebDriverProvider;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.net.MalformedURLException;
import java.net.URI;
import java.time.Duration;

public class Config implements WebDriverProvider {

    private static final Logger logger = LoggerFactory.getLogger(Config.class);

    private static final String APPIUM_SERVER_URL = "http://127.0.0.1:4723";
    private static final String PLATFORM_NAME = "Android";
    private static final String PLATFORM_VERSION = "11.0";
    private static final String DEVICE_NAME = "emulator-5554";

    @Nonnull
    @Override
    public WebDriver createDriver(@Nonnull Capabilities capabilities) {
        logger.info("Initializing AndroidDriver for Appium. Device: {}, platform: {} {}",
                DEVICE_NAME, PLATFORM_NAME, PLATFORM_VERSION);

        UiAutomator2Options options = new UiAutomator2Options();

        options.setPlatformName(PLATFORM_NAME);
        options.setPlatformVersion(PLATFORM_VERSION);
        options.setDeviceName(DEVICE_NAME);
        options.setAutomationName("UiAutomator2");

        options.setNoReset(true);
        options.setFullReset(false);
        options.setNewCommandTimeout(Duration.ofSeconds(300));

        options.setCapability("autoGrantPermissions", true);
        options.setCapability("isHeadless", false);

        try {
            AndroidDriver driver = new AndroidDriver(URI.create(APPIUM_SERVER_URL).toURL(), options);
            logger.info("AndroidDriver successfully created and connected to Appium at {}", APPIUM_SERVER_URL);
            return driver;
        } catch (MalformedURLException e) {
            logger.error("Error creating URL for Appium server: {}", APPIUM_SERVER_URL, e);
            throw new RuntimeException("Error creating URL for Appium server", e);
        }
    }
}