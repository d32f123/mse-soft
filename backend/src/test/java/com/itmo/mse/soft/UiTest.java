package com.itmo.mse.soft;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.WebDriver;
import org.junit.jupiter.api.Test;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class UiTest {
    private final String firefoxDriver = ConfProperties.getProperty("firefox_driver");
    private final String indexUrl = ConfProperties.getProperty("index_page");
    private final String groomer_login = ConfProperties.getProperty("groomer_login");
    private final String groomer_password = ConfProperties.getProperty("groomer_password");
    private final String pig_master_login = ConfProperties.getProperty("pig_master_login");
    private final String pig_master_password = ConfProperties.getProperty("pig_master_password");

    WebDriver get_driver(){
        System.setProperty("webdriver.firefox.driver", firefoxDriver);
        WebDriver driver = new FirefoxDriver();
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
        driver.manage().window().maximize();
        return driver;
    }

    @Test
    void loginGroomer(){
        WebDriver driver = get_driver();
        driver.get(indexUrl);

        WebElement usernameElement = driver.findElement(By.id("username"));
        usernameElement.clear();
        usernameElement.sendKeys(groomer_login);

        WebElement passwordElement = driver.findElement(By.id("password"));
        passwordElement.clear();
        passwordElement.sendKeys(groomer_password);

        driver.findElement(By.id("submit")).click();
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

        String titleText = driver.findElement(By.tagName("h1")).getText();

        assertThat(titleText).isEqualTo("Домашняя страничка Грумера");

        driver.close();
    }

    @Test
    void loginPigMaster(){
        WebDriver driver = get_driver();
        driver.get(indexUrl);

        WebElement usernameElement = driver.findElement(By.id("username"));
        usernameElement.clear();
        usernameElement.sendKeys(pig_master_login);

        WebElement passwordElement = driver.findElement(By.id("password"));
        passwordElement.clear();
        passwordElement.sendKeys(pig_master_password);

        driver.findElement(By.id("submit")).click();
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

        String titleText = driver.findElement(By.tagName("h1")).getText();

        assertThat(titleText).isEqualTo("Домашняя страничка Мастера свиней");

        driver.close();
    }
}
