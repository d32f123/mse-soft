package com.itmo.mse.soft;

import okhttp3.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.WebDriver;
import org.junit.jupiter.api.Test;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class UiTest {
    private final String firefoxDriver = ConfProperties.getProperty("firefox_driver");
    private final String indexUrl = ConfProperties.getProperty("index_page");
    private final String groomer_login = ConfProperties.getProperty("groomer_login");
    private final String groomer_password = ConfProperties.getProperty("groomer_password");
    private final String groomer2_login = ConfProperties.getProperty("groomer2_login");
    private final String groomer2_password = ConfProperties.getProperty("groomer2_password");
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

    void logout(WebDriver driver){
        driver.findElement(By.id("id-logout")).click();

        String titleText = driver.findElement(By.tagName("h1")).getText();
        assertThat(titleText).isEqualTo("Добро пожаловать");
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

    WebElement getPickup(WebDriver driver, String login, String password) {
        login(driver, login, password);
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

        for (WebElement elem: driver.findElements(By.name("PICKUP"))) {
            WebElement parent = elem.findElement(By.xpath("./.."));

            if (!parent.getAttribute("class").equals("_complete")) {
                return elem;
            }
        }
        return null;
    }

    void pause(long millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception e) {};
    }

    @Test
    void pickupGroomer3() throws InterruptedException {
        sendPostRequest(
            hydra_orders_url,
            "{\"paymentAmount\": 100,\"pickupInstant\": \"2020-12-11T19:41:00.000+00:00\"}"
        );

        WebDriver driver = get_driver();

        //
        WebElement pickup = getPickup(driver, groomer_login, groomer_password);
        if (Objects.isNull(pickup)) {
            logout(driver);
            pickup = getPickup(driver, groomer2_login, groomer2_password);

            if (Objects.isNull(pickup)) {
                assertThat("There is no PICKUP task.").isEqualTo("");
            }
        }
        pickup.click();

        // Статус: ожидает принятия
        pause(1000);
        String corpsStatus = driver.findElement(By.name("corps-status")).getText();
        assertThat("AWAITING_RECEIVAL").isEqualTo(corpsStatus);

        // Выполняем задачу
        driver.findElement(By.id("btnCompleteTask")).click();

        // Цвет заголовка зеленый
        WebElement title = driver.findElement(By.id("bodyTitle"));
        assertThat(title.getAttribute("class")).isEqualTo("alert alert-success");

        // Возвращаемся к расписанию
        driver.findElement(By.id("schedule")).click();

        driver.close();
    }

    @Test
    void logoutEmployee(){
        WebDriver driver = get_driver();

        login(driver, groomer_login, groomer_password);
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
        logout(driver);

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

    @Test
    void Feed(){
        sendPostRequest(
                hydra_orders_url,
                "{\"paymentAmount\": 100,\"pickupInstant\": \"2020-12-11T07:10:10.000+00:00\"}"
        );
    }
}
