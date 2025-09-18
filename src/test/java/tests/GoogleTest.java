package tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;

// Singleton WebDriver
class DriverManager {
    private static WebDriver driver;
    private static WebDriverWait wait;

    private DriverManager() {}

    public static WebDriver getDriver() {
        if (driver == null) {
            WebDriverManager.chromedriver().setup();
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--window-size=1280,800");
            driver = new ChromeDriver(options);
            driver.manage().window().setSize(new Dimension(1280, 800));
            wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        }
        return driver;
    }

    public static WebDriverWait getWait() {
        if (wait == null) {
            getDriver();
        }
        return wait;
    }

    public static void quitDriver() {
        if (driver != null) {
            driver.quit();
            driver = null;
            wait = null;
        }
    }
}

// Page Object para la página de Selección
class SeleccionPage {
    private WebDriver driver;
    private WebDriverWait wait;

    private By titulo = By.xpath("/html/body/app-root/header/div[1]/a/span[1]");
    private By adminLink = By.cssSelector("a.pro-chip:nth-child(2)");
    private By inputLocalidad = By.cssSelector("section.panel:nth-child(1) > div:nth-child(2) > div:nth-child(1) > input:nth-child(2)");
    private By submitButton = By.cssSelector("section.panel:nth-child(1) > div:nth-child(2) > div:nth-child(2) > button:nth-child(2)");

    public SeleccionPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public String getTitulo() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(titulo)).getText();
    }

    public void irAAdmin() {
        driver.findElement(adminLink).click();
    }

    public void crearLocalidad(String nombre) {
        driver.findElement(inputLocalidad).sendKeys(nombre);
        driver.findElement(submitButton).click();
    }
}

// Clase de tests
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GoogleTest {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    void setUp() {
        driver = DriverManager.getDriver();
        wait = DriverManager.getWait();
    }

    @Test
    void testBrowserActions() throws IOException {
        driver.get("http://157.180.19.137/seleccion");
        SeleccionPage pagina = new SeleccionPage(driver, wait);

        takeScreenshot("fullpage.png");

        // Validaciones
        Assertions.assertEquals("Tablero", pagina.getTitulo());

        // Navegación
        pagina.irAAdmin();
        pagina.crearLocalidad("Localidad nueva");

        // Info útil
        System.out.println("Título: " + driver.getTitle());
        System.out.println("URL actual: " + driver.getCurrentUrl());
        System.out.println("Ventana actual: " + driver.getWindowHandle());
    }

    @AfterEach
    void tearDown() {
        // Si quieres mantener el driver vivo entre tests, comenta esta línea
        DriverManager.quitDriver();
    }

    // Métodos utilitarios
    private void takeScreenshot(String filename) throws IOException {
        File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        Files.copy(src.toPath(), new File(filename).toPath());
    }

    private void takeScreenshotOfElement(WebElement element, String filename) throws IOException {
        File src = element.getScreenshotAs(OutputType.FILE);
        Files.copy(src.toPath(), new File(filename).toPath());
    }
}
