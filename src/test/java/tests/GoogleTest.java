package tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.assertj.core.api.SoftAssertions; // ✅ para soft assert

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;

// ----------------------------
// Singleton WebDriver
// ----------------------------
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
        if (wait == null) getDriver();
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

// ----------------------------
// Page Object: Selección
// ----------------------------
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

    // ----------------------------
    // Acciones de la página
    // ----------------------------
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

    // ----------------------------
    // Validaciones soft assert (verify)
    // ----------------------------
    public void verifyTitulo(String expected, SoftAssertions soft) {
        String actual = getTitulo();
        soft.assertThat(actual)
                .as("Verificando título de la página")
                .isEqualTo(expected);
    }
}

// ----------------------------
// Builder para crear Localidad (ejemplo)
// ----------------------------
class Localidad {
    private String nombre;

    private Localidad(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() { return nombre; }

    public static class Builder {
        private String nombre;

        public Builder setNombre(String nombre) {
            this.nombre = nombre;
            return this;
        }

        public Localidad build() {
            return new Localidad(nombre);
        }
    }
}

// ----------------------------
// Test Class
// ----------------------------
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GoogleTest {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    void setUp() {
        driver = DriverManager.getDriver();
        wait = DriverManager.getWait();
    }

    // ----------------------------
    // Data-Driven ejemplo
    // ----------------------------
    @ParameterizedTest
    @ValueSource(strings = {"Localidad A", "Localidad B"})
    void testBrowserActions(String nombreLocalidad) throws IOException {

        driver.get("http://157.180.19.137/seleccion");
        SeleccionPage pagina = new SeleccionPage(driver, wait);

        // ----------------------------
        // Soft Assertions (Verify)
        // ----------------------------
        SoftAssertions soft = new SoftAssertions();
        pagina.verifyTitulo("Tablero", soft);

        // ----------------------------
        // Assert crítico: detiene test si falla
        // ----------------------------
        Assertions.assertTrue(driver.getTitle().contains("Tablero"), "Título de pestaña no esperado");

        // Captura de pantalla
        takeScreenshot("fullpage.png");

        // ----------------------------
        // Navegación y creación de Localidad (Builder)
        // ----------------------------
        pagina.irAAdmin();
        Localidad localidad = new Localidad.Builder()
                .setNombre(nombreLocalidad)
                .build();
        pagina.crearLocalidad(localidad.getNombre());

        // ----------------------------
        // Info útil en logs
        // ----------------------------
        System.out.println("Título: " + driver.getTitle());
        System.out.println("URL actual: " + driver.getCurrentUrl());
        System.out.println("Ventana actual: " + driver.getWindowHandle());

        // ----------------------------
        // Reporta fallos de soft asserts
        // ----------------------------
        soft.assertAll();
    }

    @AfterEach
    void tearDown() {
        DriverManager.quitDriver();
    }

    // ----------------------------
    // Métodos utilitarios
    // ----------------------------
    private void takeScreenshot(String filename) throws IOException {
        File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        Files.copy(src.toPath(), new File(filename).toPath());
    }

    private void takeScreenshotOfElement(WebElement element, String filename) throws IOException {
        File src = element.getScreenshotAs(OutputType.FILE);
        Files.copy(src.toPath(), new File(filename).toPath());
    }
}
