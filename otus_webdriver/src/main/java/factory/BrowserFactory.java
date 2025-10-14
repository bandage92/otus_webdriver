package factory;

import data.BrowserTypeData;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

public class BrowserFactory {
  
  public static WebDriver startBrowser(BrowserTypeData type) {
    return switch (type) {
      case CHROME -> new ChromeDriver();
      case FIREFOX -> new FirefoxDriver();
      case EDGE -> new EdgeDriver();
    };
  }
  
  public static WebDriver startHeadlessBrowser(BrowserTypeData type) {
    return switch (type) {
      case CHROME -> {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("headless");
        yield new ChromeDriver(options);
      }
      case FIREFOX -> {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("-headless");
        yield new FirefoxDriver(options);
      }
      case EDGE -> {
        EdgeOptions options = new EdgeOptions();
        options.addArguments("headless");
        yield new EdgeDriver(options);
      }
    };
  }
}