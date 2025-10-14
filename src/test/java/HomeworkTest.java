import static data.BrowserTypeData.CHROME;
import static factory.BrowserFactory.startBrowser;
import static factory.BrowserFactory.startHeadlessBrowser;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Optional;
import io.github.bonigarcia.wdm.WebDriverManager;
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
  private static final String BASE_URL = "https://otus.home.kartushin.su/training.html";
  private WebDriver driver;
  private WebDriverWait wait;
  
  @BeforeAll
  public static void driverSetup() {
    WebDriverManager.chromedriver().setup();
    LOGGER.info("WebDriver Manager настроен");
  }
  
  @BeforeEach
  public void startDriver(TestInfo testInfo) {
    boolean isHeadless = false;
    
    try {
      Optional<Method> testMethod = testInfo.getTestMethod();
      if (testMethod.isPresent()) {
        Method method = testMethod.get();
        isHeadless = method.isAnnotationPresent(Headless.class);
        LOGGER.debug("Проверка аннотации Headless для метода {}: {}", method.getName(), isHeadless);
      }
    } catch (Exception e) {
      LOGGER.warn("Ошибка при проверке аннотации Headless: {}", e.getMessage());
    }
    
    driver = isHeadless
        ? startHeadlessBrowser(CHROME) :
        startBrowser(CHROME);
    LOGGER.info("Драйвер запущен в {} режиме", isHeadless ? "headless" : "обычном");
    
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
  @DisplayName("Открыть браузер в режиме киоска")
  public void kioskView() {
    driver.manage().window().fullscreen();
    LOGGER.info("Окно открыто в режиме киоска");
    
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
  @DisplayName("Открыть браузер в режиме полного экрана")
  public void maximizeView() {
    driver.manage().window().maximize();
    LOGGER.info("Окно открыто в максимальном размере");
    
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