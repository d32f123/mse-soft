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
    private final String currentDate = "2020-12-11T19:41:00.000+00:00";
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

    WebElement getUncompletedTask(WebDriver driver, String login, String password, String Name) {
        login(driver, login, password);
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

        for (WebElement elem: driver.findElements(By.name(Name))) {
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
    void pickupGroomer3() {
        sendPostRequest(
            hydra_orders_url,
            "{\"paymentAmount\": 100,\"pickupInstant\": \""+ currentDate + "\"}"
        );

        WebDriver driver = get_driver();

        //
        WebElement pickup = getUncompletedTask(driver, groomer_login, groomer_password, "PICKUP");
        if (Objects.isNull(pickup)) {
            logout(driver);
            pickup = getUncompletedTask(driver, groomer2_login, groomer2_password, "PICKUP");

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
    void takeBodyFromOwner4() {
        sendPostRequest(
                hydra_orders_url,
                "{\"paymentAmount\": 100,\"pickupInstant\": \""+ currentDate + "\"}"
        );

        WebDriver driver = get_driver();

        //
        WebElement pickup = getUncompletedTask(driver, groomer_login, groomer_password, "PICKUP");
        if (Objects.isNull(pickup)) {
            logout(driver);
            pickup = getUncompletedTask(driver, groomer2_login, groomer2_password, "PICKUP");

            if (Objects.isNull(pickup)) {
                assertThat("There is no PICKUP task.").isEqualTo("");
            }
        }
        pickup.click();

        // Статус: ожидает принятия
        pause(1000);
        String corpsStatus = driver.findElement(By.name("corps-status")).getText();
        assertThat("AWAITING_RECEIVAL").isEqualTo(corpsStatus);

        // Отмечаем PICKUP_FROM_CUSTOMER
        driver.findElement(By.name("PICKUP_FROM_CUSTOMER")).click();

        // Выполняем подзадачу
        driver.findElement(By.id("btnCompleteSubTask")).click();
        pause(300);

        // Статус трупа IN_RECEIVAL
        corpsStatus = driver.findElement(By.name("corps-status")).getText();
        assertThat("IN_RECEIVAL").isEqualTo(corpsStatus);

        driver.close();

    }

    @Test
    void printBarcode5() {
        sendPostRequest(
                hydra_orders_url,
                "{\"paymentAmount\": 100,\"pickupInstant\": \""+ currentDate + "\"}"
        );

        WebDriver driver = get_driver();

        //
        WebElement pickup = getUncompletedTask(driver, groomer_login, groomer_password, "PICKUP");
        if (Objects.isNull(pickup)) {
            logout(driver);
            pickup = getUncompletedTask(driver, groomer2_login, groomer2_password, "PICKUP");

            if (Objects.isNull(pickup)) {
                assertThat("There is no PICKUP task.").isEqualTo("");
            }
        }
        pickup.click();

        // Статус: ожидает принятия
        pause(1000);
        String corpsStatus = driver.findElement(By.name("corps-status")).getText();
        assertThat("AWAITING_RECEIVAL").isEqualTo(corpsStatus);

        // Отмечаем PICKUP_FROM_CUSTOMER
        driver.findElement(By.name("PICKUP_FROM_CUSTOMER")).click();

        // Выполняем подзадачу
        driver.findElement(By.id("btnCompleteSubTask")).click();
        pause(300);

        // Отмечаем PRINT_BARCODE
        driver.findElement(By.name("PRINT_BARCODE")).click();

        // Выполняем подзадачу
        driver.findElement(By.id("btnCompleteSubTask")).click();
        pause(300);

        // Статус трупа IN_RECEIVAL
        corpsStatus = driver.findElement(By.name("corps-status")).getText();
        assertThat("IN_RECEIVAL").isEqualTo(corpsStatus);

        driver.close();
    }

    @Test
    void putInFridge6() {
        sendPostRequest(
                hydra_orders_url,
                "{\"paymentAmount\": 100,\"pickupInstant\": \""+ currentDate + "\"}"
        );

        WebDriver driver = get_driver();

        //
        WebElement pickup = getUncompletedTask(driver, groomer_login, groomer_password, "PICKUP");
        if (Objects.isNull(pickup)) {
            logout(driver);
            pickup = getUncompletedTask(driver, groomer2_login, groomer2_password, "PICKUP");

            if (Objects.isNull(pickup)) {
                assertThat("There is no PICKUP task.").isEqualTo("");
            }
        }
        pickup.click();

        // Статус: ожидает принятия
        pause(1000);
        String corpsStatus = driver.findElement(By.name("corps-status")).getText();
        assertThat("AWAITING_RECEIVAL").isEqualTo(corpsStatus);

        // Выполняем PICKUP_FROM_CUSTOMER
        driver.findElement(By.name("PICKUP_FROM_CUSTOMER")).click();
        driver.findElement(By.id("btnCompleteSubTask")).click();
        pause(300);

        // Выполняем PRINT_BARCODE
        driver.findElement(By.name("PRINT_BARCODE")).click();
        driver.findElement(By.id("btnCompleteSubTask")).click();
        pause(300);

        // Выполняем PUT_IN_FRIDGE
        driver.findElement(By.name("PUT_IN_FRIDGE")).click();
        driver.findElement(By.id("btnCompleteSubTask")).click();
        pause(300);

        // Статус трупа RECEIVED
        corpsStatus = driver.findElement(By.name("corps-status")).getText();
        assertThat("RECEIVED").isEqualTo(corpsStatus);

        driver.close();
    }

    @Test
    void takeFromFridge7() {
        sendPostRequest(
                hydra_orders_url,
                "{\"paymentAmount\": 100,\"pickupInstant\": \""+ currentDate + "\"}"
        );

        WebDriver driver = get_driver();

        // Выполняем PICKUP
        WebElement pickup = getUncompletedTask(driver, groomer_login, groomer_password, "PICKUP");
        if (Objects.isNull(pickup)) {
            logout(driver);
            pickup = getUncompletedTask(driver, groomer2_login, groomer2_password, "PICKUP");

            if (Objects.isNull(pickup)) {
                assertThat("There is no PICKUP task.").isEqualTo("");
            }
        }
        pickup.click();
        pause(300);
        driver.findElement(By.id("btnCompleteTask")).click();

        // Возвращаемся к расписанию
        driver.findElement(By.id("schedule")).click();

        // Переходим в GROOM
        WebElement groom = getUncompletedTask(driver, groomer_login, groomer_password, "GROOM");
        if (Objects.isNull(groom)) {
            logout(driver);
            groom = getUncompletedTask(driver, groomer2_login, groomer2_password, "GROOM");

            if (Objects.isNull(groom)) {
                assertThat("There is no GROOM task.").isEqualTo("");
            }
        };
        groom.click();

        // Выполняем TAKE_FROM_FRIDGE
        driver.findElement(By.name("TAKE_FROM_FRIDGE")).click();
        driver.findElement(By.id("btnCompleteSubTask")).click();
        pause(300);

        // Статус трупа IN_GROOMING
        String corpsStatus = driver.findElement(By.name("corps-status")).getText();
        assertThat("IN_GROOMING").isEqualTo(corpsStatus);

        driver.close();
    }

    @Test
    void groom8() {
        sendPostRequest(
                hydra_orders_url,
                "{\"paymentAmount\": 100,\"pickupInstant\": \""+ currentDate + "\"}"
        );

        WebDriver driver = get_driver();

        // Выполняем PICKUP
        WebElement pickup = getUncompletedTask(driver, groomer_login, groomer_password, "PICKUP");
        if (Objects.isNull(pickup)) {
            logout(driver);
            pickup = getUncompletedTask(driver, groomer2_login, groomer2_password, "PICKUP");

            if (Objects.isNull(pickup)) {
                assertThat("There is no PICKUP task.").isEqualTo("");
            }
        }
        pickup.click();
        pause(300);
        driver.findElement(By.id("btnCompleteTask")).click();

        // Возвращаемся к расписанию
        driver.findElement(By.id("schedule")).click();

        // Переходим в GROOM
        WebElement groom = getUncompletedTask(driver, groomer_login, groomer_password, "GROOM");
        if (Objects.isNull(groom)) {
            logout(driver);
            groom = getUncompletedTask(driver, groomer2_login, groomer2_password, "GROOM");

            if (Objects.isNull(groom)) {
                assertThat("There is no GROOM task.").isEqualTo("");
            }
        };
        groom.click();

        // Выполняем TAKE_FROM_FRIDGE
        driver.findElement(By.name("TAKE_FROM_FRIDGE")).click();
        driver.findElement(By.id("btnCompleteSubTask")).click();
        pause(300);

        // Выполняем TAKE_OUT_TEETH
        driver.findElement(By.name("TAKE_OUT_TEETH")).click();
        driver.findElement(By.id("btnCompleteSubTask")).click();
        pause(300);

        // Выполняем SHAVE
        driver.findElement(By.name("SHAVE")).click();
        driver.findElement(By.id("btnCompleteSubTask")).click();
        pause(300);

        // Выполняем BUTCHER
        driver.findElement(By.name("BUTCHER")).click();
        driver.findElement(By.id("btnCompleteSubTask")).click();
        pause(300);

        // Статус трупа IN_GROOMING
        String corpsStatus = driver.findElement(By.name("corps-status")).getText();
        assertThat("IN_GROOMING").isEqualTo(corpsStatus);

        driver.close();
    }

    @Test
    void putInFridge9() {
        sendPostRequest(
                hydra_orders_url,
                "{\"paymentAmount\": 100,\"pickupInstant\": \""+ currentDate + "\"}"
        );

        WebDriver driver = get_driver();

        // Выполняем PICKUP
        WebElement pickup = getUncompletedTask(driver, groomer_login, groomer_password, "PICKUP");
        if (Objects.isNull(pickup)) {
            logout(driver);
            pickup = getUncompletedTask(driver, groomer2_login, groomer2_password, "PICKUP");

            if (Objects.isNull(pickup)) {
                assertThat("There is no PICKUP task.").isEqualTo("");
            }
        }
        pickup.click();
        pause(300);
        driver.findElement(By.id("btnCompleteTask")).click();

        // Возвращаемся к расписанию
        driver.findElement(By.id("schedule")).click();

        // Переходим в GROOM
        WebElement groom = getUncompletedTask(driver, groomer_login, groomer_password, "GROOM");
        if (Objects.isNull(groom)) {
            logout(driver);
            groom = getUncompletedTask(driver, groomer2_login, groomer2_password, "GROOM");

            if (Objects.isNull(groom)) {
                assertThat("There is no GROOM task.").isEqualTo("");
            }
        };
        groom.click();

        // Выполняем TAKE_FROM_FRIDGE
        driver.findElement(By.name("TAKE_FROM_FRIDGE")).click();
        driver.findElement(By.id("btnCompleteSubTask")).click();
        pause(300);

        // Выполняем TAKE_OUT_TEETH
        driver.findElement(By.name("TAKE_OUT_TEETH")).click();
        driver.findElement(By.id("btnCompleteSubTask")).click();
        pause(300);

        // Выполняем SHAVE
        driver.findElement(By.name("SHAVE")).click();
        driver.findElement(By.id("btnCompleteSubTask")).click();
        pause(300);

        // Выполняем BUTCHER
        driver.findElement(By.name("BUTCHER")).click();
        driver.findElement(By.id("btnCompleteSubTask")).click();
        pause(300);

        // Выполняем PUT_IN_FRIDGE
        driver.findElement(By.name("PUT_IN_FRIDGE")).click();
        driver.findElement(By.id("btnCompleteSubTask")).click();
        pause(300);

        // Статус трупа GROOMED
        String corpsStatus = driver.findElement(By.name("corps-status")).getText();
        assertThat("GROOMED").isEqualTo(corpsStatus);

        driver.close();
    }

    @Test
    void takeFromFridge10() {
        sendPostRequest(
                hydra_orders_url,
                "{\"paymentAmount\": 100,\"pickupInstant\": \""+ currentDate + "\"}"
        );

        WebDriver driver = get_driver();

        // Выполняем PICKUP
        WebElement pickup = getUncompletedTask(driver, groomer_login, groomer_password, "PICKUP");
        if (Objects.isNull(pickup)) {
            logout(driver);
            pickup = getUncompletedTask(driver, groomer2_login, groomer2_password, "PICKUP");

            if (Objects.isNull(pickup)) {
                assertThat("There is no PICKUP task.").isEqualTo("");
            }
        }
        pickup.click();
        pause(300);
        driver.findElement(By.id("btnCompleteTask")).click();

        // Возвращаемся к расписанию
        driver.findElement(By.id("schedule")).click();

        // Выполняем GROOM
        WebElement groom = getUncompletedTask(driver, groomer_login, groomer_password, "GROOM");
        if (Objects.isNull(groom)) {
            logout(driver);
            groom = getUncompletedTask(driver, groomer2_login, groomer2_password, "GROOM");

            if (Objects.isNull(groom)) {
                assertThat("There is no GROOM task.").isEqualTo("");
            }
        };
        groom.click();
        pause(300);
        driver.findElement(By.id("btnCompleteTask")).click();

        // Выходим из системы
        driver.findElement(By.id("schedule")).click();
        logout(driver);
        pause(300);

        // Входим как PigMaster и находим FEED
        WebElement feed = getUncompletedTask(driver, pig_master_login, pig_master_password, "FEED");
        if (Objects.isNull(feed)) {
            assertThat("There is no FEED task.").isEqualTo("");
        }
        feed.click();

        // Выполняем TAKE_FROM_FRIDGE
        driver.findElement(By.name("TAKE_FROM_FRIDGE")).click();
        driver.findElement(By.id("btnCompleteSubTask")).click();
        pause(300);

        // Статус трупа IN_FEEDING
        String corpsStatus = driver.findElement(By.name("corps-status")).getText();
        assertThat("IN_FEEDING").isEqualTo(corpsStatus);

        driver.close();
    }

    @Test
    void feed11() {
        sendPostRequest(
                hydra_orders_url,
                "{\"paymentAmount\": 100,\"pickupInstant\": \""+ currentDate + "\"}"
        );

        WebDriver driver = get_driver();

        // Выполняем PICKUP
        WebElement pickup = getUncompletedTask(driver, groomer_login, groomer_password, "PICKUP");
        if (Objects.isNull(pickup)) {
            logout(driver);
            pickup = getUncompletedTask(driver, groomer2_login, groomer2_password, "PICKUP");

            if (Objects.isNull(pickup)) {
                assertThat("There is no PICKUP task.").isEqualTo("");
            }
        }
        pickup.click();
        pause(300);
        driver.findElement(By.id("btnCompleteTask")).click();

        // Возвращаемся к расписанию
        driver.findElement(By.id("schedule")).click();

        // Выполняем GROOM
        WebElement groom = getUncompletedTask(driver, groomer_login, groomer_password, "GROOM");
        if (Objects.isNull(groom)) {
            logout(driver);
            groom = getUncompletedTask(driver, groomer2_login, groomer2_password, "GROOM");

            if (Objects.isNull(groom)) {
                assertThat("There is no GROOM task.").isEqualTo("");
            }
        };
        groom.click();
        pause(300);
        driver.findElement(By.id("btnCompleteTask")).click();

        // Выходим из системы
        driver.findElement(By.id("schedule")).click();
        logout(driver);
        pause(300);

        // Входим как PigMaster и находим FEED
        WebElement feed = getUncompletedTask(driver, pig_master_login, pig_master_password, "FEED");
        if (Objects.isNull(feed)) {
            assertThat("There is no FEED task.").isEqualTo("");
        }
        feed.click();

        // Выполняем TAKE_FROM_FRIDGE
        driver.findElement(By.name("TAKE_FROM_FRIDGE")).click();
        driver.findElement(By.id("btnCompleteSubTask")).click();
        pause(300);

        // Выполняем FEED
        driver.findElement(By.name("FEED")).click();
        driver.findElement(By.id("btnCompleteSubTask")).click();
        pause(300);

        // Статус трупа FED
        String corpsStatus = driver.findElement(By.name("corps-status")).getText();
        assertThat("FED").isEqualTo(corpsStatus);

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
                "{\"paymentAmount\": 100,\"pickupInstant\": \""+ currentDate + "\"}"
        );
    }
}
