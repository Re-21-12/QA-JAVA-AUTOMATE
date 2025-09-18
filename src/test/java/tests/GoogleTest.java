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

@TestInstance(TestInstance.Lifecycle.PER_CLASS) // Permite reutilizar el driver si quieres
class GoogleTest {

    private WebDriver driver;
    private WebDriverWait wait; // ✅ WebDriverWait para sincronización
//setup
    @BeforeEach
    void setUp() {
        // ✅ WebDriverManager se encarga de descargar y configurar el driver correcto
        WebDriverManager.chromedriver().setup();

        // ✅ Configuración de opciones de Chrome
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--window-size=1280,800");
        // options.addArguments("--headless"); // Usar en CI/CD si no necesitas interfaz gráfica

        driver = new ChromeDriver(options);

        // ✅ Uso de WebDriverWait para evitar errores por carga lenta
        // se puede usar con waiting strategies
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // ✅ Mejor práctica: iniciar siempre en tamaño conocido (consistencia en capturas)
        driver.manage().window().setSize(new Dimension(1280, 800));
    }

    @Test
    void testBrowserActions() throws IOException {
        driver.get("http://157.180.19.137/seleccion");



        // ✅ Uso de ExpectedConditions para elementos dinámicos
        // se usa de la mano con una espera explicita``
        WebElement elemento = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("/html/body/app-root/header/div[1]/a/span[1]")));
        WebElement elementoPorCssSelector = driver.findElement(By.cssSelector(".pro-brand__title"));

        takeScreenshot("fullpage.png");   // Captura de pantalla general
        takeScreenshotOfElement(elemento, "h1.png");

        Assertions.assertEquals("Tablero", elemento.getText(), "El texto del elemento no es el esperado.");
        Assertions.assertEquals("Tablero", elementoPorCssSelector.getText(), "El texto del elemento no es el esperado.");

        // es el titulo del tab
        System.out.println("Título de la página: " + driver.getTitle());

        System.out.println("URL actual: " + driver.getCurrentUrl());

        // ✅ Navegación segura
        driver.navigate().refresh();
        driver.navigate().back();
        driver.navigate().forward();

        WebElement admin = driver.findElement(By.cssSelector("a.pro-chip:nth-child(2)"));
        Assertions.assertEquals("Admin", "Navegador click con exito");
        admin.click();

        WebElement inputLocalidad = driver.findElement(By.cssSelector("section.panel:nth-child(1) > div:nth-child(2) > div:nth-child(1) > input:nth-child(2)"));
        inputLocalidad.sendKeys("Localidad nueva");

        WebElement submitButton = driver.findElement(By.cssSelector("section.panel:nth-child(1) > div:nth-child(2) > div:nth-child(2) > button:nth-child(2)"));
        submitButton.click();
        // ✅ Mostrar el handle de la ventana
        System.out.println("Ventana actual: " + driver.getWindowHandle());
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // ✅ Métodos utilitarios para mantener el test limpio
    private void takeScreenshot(String filename) throws IOException {
        File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        Files.copy(src.toPath(), new File(filename).toPath());
    }

    private void takeScreenshotOfElement(WebElement element, String filename) throws IOException {
        File src = element.getScreenshotAs(OutputType.FILE);
        Files.copy(src.toPath(), new File(filename).toPath());
    }
}
