package com.itmo.mse.soft;

import okhttp3.*;
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
    private final String hydra_orders_url = ConfProperties.getProperty("hydra_orders_url");

    WebDriver get_driver(){
        System.setProperty("webdriver.firefox.driver", firefoxDriver);
        WebDriver driver = new FirefoxDriver();
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
        driver.manage().window().maximize();
        return driver;
    }

    void sendPostRequest(String url, String content){
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, content);
        Request request = new Request.Builder()
                .url(url)
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();
        try {
            Response response = client.newCall(request).execute();
        } catch (java.io.IOException e) {}
    }

    void login(WebDriver driver, String login, String password){
        driver.get(indexUrl);

        WebElement usernameElement = driver.findElement(By.id("username"));
        usernameElement.clear();
        usernameElement.sendKeys(login);

        WebElement passwordElement = driver.findElement(By.id("password"));
        passwordElement.clear();
        passwordElement.sendKeys(password);

        driver.findElement(By.id("submit")).click();
    }

    @Test
    void viewSheduleGroomer1(){
        WebDriver driver = get_driver();

        login(driver, groomer_login, groomer_password);
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

        String taskType = driver.findElement(By.xpath("//thead/tr[1]/th[1]")).getText();
        String fromTime = driver.findElement(By.xpath("//thead/tr[1]/th[2]")).getText();
        String toTime = driver.findElement(By.xpath("//thead/tr[1]/th[3]")).getText();

        assertThat(taskType).isEqualTo("Тип задачи");
        assertThat(fromTime).isEqualTo("Время начала");
        assertThat(toTime).isEqualTo("Время окончания");

        driver.close();
    }

    @Test
    void viewShedulePigMaster1(){
        WebDriver driver = get_driver();

        login(driver, pig_master_login, pig_master_password);
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

        String taskType = driver.findElement(By.xpath("//thead/tr[1]/th[1]")).getText();
        String fromTime = driver.findElement(By.xpath("//thead/tr[1]/th[2]")).getText();
        String toTime = driver.findElement(By.xpath("//thead/tr[1]/th[3]")).getText();
        String lawNumber = driver.findElement(By.xpath("//thead/tr[1]/th[4]")).getText();
        String pigCount = driver.findElement(By.xpath("//thead/tr[1]/th[5]")).getText();
        String feedingTime = driver.findElement(By.xpath("//thead/tr[1]/th[6]")).getText();

        assertThat(taskType).isEqualTo("Тип задачи");
        assertThat(fromTime).isEqualTo("Время начала");
        assertThat(toTime).isEqualTo("Время окончания");
        assertThat(lawNumber).isEqualTo("Номер загона");
        assertThat(pigCount).isEqualTo("Число свиней в загоне");
        assertThat(feedingTime).isEqualTo("Время последнего кормления");

        driver.close();
    }

    @Test
    void loginGroomer2(){
        WebDriver driver = get_driver();

        login(driver, groomer_login, groomer_password);
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

        String titleText = driver.findElement(By.tagName("h1")).getText();
        assertThat(titleText).isEqualTo("Домашняя страничка Грумера");

        driver.close();
    }

    @Test
    void loginPigMaster2(){
        WebDriver driver = get_driver();

        login(driver, pig_master_login, pig_master_password);
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

        String titleText = driver.findElement(By.tagName("h1")).getText();
        assertThat(titleText).isEqualTo("Домашняя страничка Мастера свиней");

        driver.close();
    }

    @Test
    void logoutGroomer(){
        WebDriver driver = get_driver();

        login(driver, groomer_login, groomer_password);
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

        String titleText = driver.findElement(By.tagName("h1")).getText();
        assertThat(titleText).isEqualTo("Домашняя страничка Грумера");

        driver.findElement(By.id("id-logout")).click();

        titleText = driver.findElement(By.tagName("h1")).getText();
        assertThat(titleText).isEqualTo("Добро пожаловать");

        driver.close();
    }

    @Test
    void logoutPigMaster(){
        WebDriver driver = get_driver();

        login(driver, pig_master_login, pig_master_password);
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

        String titleText = driver.findElement(By.tagName("h1")).getText();
        assertThat(titleText).isEqualTo("Домашняя страничка Мастера свиней");

        driver.findElement(By.id("id-logout")).click();

        titleText = driver.findElement(By.tagName("h1")).getText();
        assertThat(titleText).isEqualTo("Добро пожаловать");

        driver.close();
    }

    @Test
    void auth1() {
        WebDriver driver = get_driver();

        login(driver, groomer_login, "123456");
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

        String titleText = driver.findElement(By.tagName("h1")).getText();
        assertThat(titleText).isNotEqualTo("Домашняя страничка Грумера");

        driver.close();
    }

    @Test
    void auth2() {
        WebDriver driver = get_driver();

        login(driver, groomer_login, "qwerty");
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

        String titleText = driver.findElement(By.tagName("h1")).getText();
        assertThat(titleText).isNotEqualTo("Домашняя страничка Грумера");

        driver.close();
    }

    @Test
    void auth3() {
        WebDriver driver = get_driver();

        login(driver, "sdasd", "asdasd");
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

        String titleText = driver.findElement(By.tagName("h1")).getText();
        assertThat(titleText).isNotEqualTo("Домашняя страничка Грумера");

        driver.close();
    }

//    @Test
//    void Feed(){
//        sendPostRequest(
//                hydra_orders_url,
//                "{\"paymentAmount\": 100,\"pickupInstant\": \"1990-10-10T07:10:10.000+00:00\"}"
//        );
//    }
}
