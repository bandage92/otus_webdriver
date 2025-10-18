import static factory.BrowserFactory.startBrowser;
import static factory.BrowserFactory.startHeadlessBrowser;

import annotations.Fullscreen;
import annotations.Headless;
import annotations.Kiosk;
import data.BrowserTypeData;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class HomeworkTest {
  
  private static final Logger LOGGER = LogManager.getLogger(HomeworkTest.class);
  private static final String BASE_URL =
      System.getProperty("test.base.url", "https://otus.home.kartushin.su/training.html");
  private WebDriver driver;
  private WebDriverWait wait;
  
  private static final BrowserTypeData BROWSER = getBrowserType();
  
  private static BrowserTypeData getBrowserType() {
    String browserName = System.getProperty("browser", "CHROME");
    try {
      return BrowserTypeData.valueOf(browserName.toUpperCase());
    } catch (IllegalArgumentException e) {
      LOGGER.warn("Неизвестный браузер: {}. Использую CHROME", browserName);
      return BrowserTypeData.CHROME;
    }
  }
  
  @BeforeEach
  public void startDriver(TestInfo testInfo) {
    boolean isHeadless = false;
    boolean isKiosk = false;
    boolean isFullscreen = false;
    
    try {
      Optional<Method> testMethod = testInfo.getTestMethod();
      if (testMethod.isPresent()) {
        Method method = testMethod.get();
        isHeadless = method.isAnnotationPresent(Headless.class);
        isKiosk = method.isAnnotationPresent(Kiosk.class);
        isFullscreen = method.isAnnotationPresent(Fullscreen.class);
        LOGGER.debug("Проверка аннотаций для метода {}: annotations.Headless={}, annotations.Kiosk={}, annotations.Fullscreen={}",
            method.getName(), isHeadless, isKiosk, isFullscreen);
      }
    } catch (Exception e) {
      LOGGER.warn("Ошибка при проверке аннотаций: {}", e.getMessage());
    }
    
    driver = isHeadless
        ? startHeadlessBrowser(BROWSER) :
        startBrowser(BROWSER);
    LOGGER.info("Драйвер запущен в {} режиме", isHeadless ? "headless" : "обычном");
    
    if (isKiosk) {
      driver.manage().window().fullscreen();
      LOGGER.info("Окно открыто в режиме киоска");
    } else if (isFullscreen) {
      driver.manage().window().maximize();
      LOGGER.info("Окно открыто в максимальном размере");
    }
    
    driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
    wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    
    driver.get(BASE_URL);
    LOGGER.info("Переход на страницу: {}", BASE_URL);
  }
  
  @AfterEach
  public void driverStop() {
    if (driver != null) {
      driver.close();
      LOGGER.debug("Драйвер закрыт");
    }
  }
  
  @Test
  @Headless
  @DisplayName("Открыть браузер в headless режиме")
  public void headlessView() {
    String text = "ОТУС";
    WebElement input = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("textInput")));
    
    input.sendKeys(text);
    LOGGER.info("Введен текст: '{}'", text);
    
    String actualText = input.getAttribute("value");
    
    Assertions.assertEquals(text, actualText);
    LOGGER.info("ТЕСТ ПРОЙДЕН: Введенный текст '{}' соответствует отображаемому", text);
  }
  
  @Test
  @Kiosk
  @DisplayName("Открыть браузер в режиме киоска")
  public void kioskView() {
    WebElement modalButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("openModalBtn")));
    WebElement modalWindow = driver.findElement(By.id("myModal"));
    
    String modalWindowClosed = modalWindow.getAttribute("style");
    
    modalButton.click();
    LOGGER.info("Нажата кнопка открытия модального окна");
    
    String modalWindowOpen = modalWindow.getAttribute("style");
    
    Assertions.assertNotEquals(modalWindowClosed, modalWindowOpen);
    LOGGER.info("ТЕСТ ПРОЙДЕН: Стиль модального окна изменился после открытия");
  }
  
  @Test
  @Fullscreen
  @DisplayName("Открыть браузер в режиме полного экрана")
  public void maximizeView() {
    String testName = "фыв";
    String testEmail = "asdf@sdfg.rt";
    
    WebElement inputName = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("name")));
    WebElement inputEmail = driver.findElement(By.id("email"));
    WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
    
    inputName.sendKeys(testName);
    inputEmail.sendKeys(testEmail);
    LOGGER.info("Форма заполнена: имя='{}', email='{}'", testName, testEmail);
    
    submitButton.click();
    LOGGER.info("Форма отправлена");
    
    WebElement messageBlock = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("messageBox")));
    String message = messageBlock.getText();
    
    Assertions.assertTrue(message.contains(String.format("Форма отправлена с именем: %s и email: %s", testName, testEmail)));
    LOGGER.info("ТЕСТ ПРОЙДЕН: Сообщение содержит ввёденные имя и email");
  }
}