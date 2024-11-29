package org.vasquez.junit5app.ejemplos;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.vasquez.junit5app.ejemplos.exceptions.DineroInsuficienteException;
import org.vasquez.junit5app.ejemplos.models.Banco;
import org.vasquez.junit5app.ejemplos.models.Cuenta;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CuentaTest {
    Cuenta cuenta;
    private TestInfo testInfo;
    private TestReporter testReporter;

    @BeforeEach
    void initMetodoTest(TestInfo testInfo, TestReporter testReporter) {
        this.cuenta = new Cuenta("Renzo", new BigDecimal("1000.2354"));
        this.testInfo = testInfo;
        this.testReporter = testReporter;
        System.out.println("iniciando el método.");
        testReporter.publishEntry(" ejecutando: " + testInfo.getDisplayName() + " " + testInfo.getTestMethod().orElse(null)
                .getName() + " " + "con las etiquetas " + testInfo.getTags());
    }

    @AfterEach
    void tearDown() {
        System.out.println("finalizando el método prueba.");
    }

    @BeforeAll
    static void beforeAll() {
        System.out.println("inicializando el test");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("finalizando el test");
    }

    @Tag("cuenta")
    @Nested
    @DisplayName("probando atributos de cuenta corriente")
    class CuentaTestNombreSaldo {
        @Test
        @DisplayName("el nombre")
        void test_nombre_cuenta() { //no se usa camelCase normalmente en TEST
            System.out.println(testInfo.getTags());
            if (testInfo.getTags().contains("cuenta")) {
                System.out.println("hacer algo con la etiqueta cuenta");
            }
            cuenta = new Cuenta("Renzo", new BigDecimal("1000.2354"));
            //cuenta.setPersona("Renzo");
            String esperado = "Renzo";
            String real = cuenta.getPersona();
            assertNotNull(real, () -> "La cuenta no puede ser nula");
            assertEquals(esperado, real,() -> "El nombre de la cuenta no es la esperaba: se esperaba " + esperado
                    + " sin embargo fue " + real);
            assertTrue(real.equals("Renzo"),() -> "Nombre cuenta esperada debe ser igual a la real");
        }

        @Test
        @DisplayName("probando el saldo de la cuenta corriente, que no sea null, mayor que 0, valor esperado")
        void test_saldo_cuenta() {
            //cuenta = new Cuenta("Renzo", new BigDecimal("1000.2354"));
            assertEquals(1000.2354, cuenta.getSaldo().doubleValue());
            assertFalse(cuenta.getSaldo().compareTo(BigDecimal.ZERO) < 0);
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
        }

        @Test
        @DisplayName("testeando referencias que sean iguales con el método equals")
        void test_referencia_cuenta() {
            cuenta = new Cuenta("Pepito Rojas", new BigDecimal("8900.9997"));
            Cuenta cuenta2 = new Cuenta("Pepito Rojas", new BigDecimal("8900.9997"));

            //assertNotEquals(cuenta,cuenta2);
            assertEquals(cuenta,cuenta2);


        }
    }

    @Nested
    class CuentaOperacionesTest {
        @Tag("cuenta")
        @Test
        void test_debito_cuenta() {
            cuenta = new Cuenta("Renzo", new BigDecimal("1000.2354"));
            cuenta.debito(new BigDecimal(100));
            assertNotNull(cuenta.getSaldo());
            assertEquals(900,cuenta.getSaldo().intValue());
            assertEquals("900.2354",cuenta.getSaldo().toPlainString());
        }

        @Tag("cuenta")
        @Test
        void test_credito_cuenta() {
            Cuenta cuenta = new Cuenta("Renzo", new BigDecimal("1000.2354"));
            cuenta.credito(new BigDecimal(100));
            assertNotNull(cuenta.getSaldo());
            assertEquals(1100,cuenta.getSaldo().intValue());
            assertEquals("1100.2354",cuenta.getSaldo().toPlainString());
        }

        @Tag("cuenta")
        @Tag("banco")
        @Test
        void testTransferirDineroCuentas() {
            Cuenta cuenta1 = new Cuenta("Pepito", new BigDecimal("2500"));
            Cuenta cuenta2 = new Cuenta("Renzo V", new BigDecimal("1500.8989"));

            Banco banco = new Banco();
            banco.setNombre("Banco del Estado");
            banco.transferir(cuenta2,cuenta1, new BigDecimal(500));
            assertEquals("3000", cuenta1.getSaldo().toPlainString());
            assertEquals("1000.8989",cuenta2.getSaldo().toPlainString());

        }
    }

    @Tag("cuenta")
    @Tag("error")
    @Test
    void testDineroInsuficienteExceptionCuenta() {
        Cuenta cuenta = new Cuenta("Renzo", new BigDecimal("1000.2354"));
        Exception exception = assertThrows(DineroInsuficienteException.class,() -> {
            cuenta.debito(new BigDecimal(1500));
        });
        String actual = exception.getMessage();
        String esperado = "Dinero Insuficiente";
        assertEquals(esperado, actual);
    }

    @Tag("cuenta")
    @Tag("banco")
    @Test
    //@Disabled
    @DisplayName("probando relaciones entre cuentas y el banco con assertAll")
    void testRelacionBancoCuentas() {
        //fail();
        Cuenta cuenta1 = new Cuenta("Pepito", new BigDecimal("2500"));
        Cuenta cuenta2 = new Cuenta("Renzo V", new BigDecimal("1500.8989"));

        Banco banco = new Banco();
        banco.addCuenta(cuenta1);
        banco.addCuenta(cuenta2);
        banco.setNombre("Banco del Estado");
        banco.transferir(cuenta2,cuenta1, new BigDecimal(500));

        assertAll(()->{assertEquals("3000", cuenta1.getSaldo().toPlainString(),
                        () -> "el valor de la cuenta 1 no es lo esperado.");},
        () -> {assertEquals("1000.8989",cuenta2.getSaldo().toPlainString(),
                () -> "el saldo de la cuenta 2 no es lo esperado.");},
        () -> {assertEquals(2,banco.getCuentas().size(),
                () -> "el banco no tiene las cuentas esperadas");},
        () -> {assertEquals("Banco del Estado", cuenta1.getBanco().getNombre());},
        () -> {assertEquals("Renzo V", banco.getCuentas().stream()
                .filter(c -> c.getPersona().equals("Renzo V"))
                .findFirst().get().getPersona());},
        () -> {assertTrue(banco.getCuentas().stream().anyMatch(c -> c.getPersona().equals("Renzo V")));});

    }

    @Nested
    class SitemaOperativoTest {
        @Test
        @EnabledOnOs(OS.WINDOWS)
        void testSoloWindows() {

        }

        @Test
        @EnabledOnOs({OS.LINUX,OS.MAC})
        void testSoloLinuxMac() {
        }

        @Test
        @DisabledOnOs(OS.WINDOWS)
        void testNoWindows() {
        }
    }

    @Nested
    class JavaVersionTest {
        @Test
        @EnabledOnJre(JRE.JAVA_8)
        void soloJdk8() {
        }

        @Test
        @EnabledOnJre(JRE.JAVA_15)
        void soloJdk15() {
        }

        @Test
        @DisabledOnJre(JRE.JAVA_15)
        void testNoJDK15() {
        }
    }

    @Nested
    class SistemPropertiesTest {
        @Test
        void imprimirSystemProperties() {
            Properties properties = System.getProperties();
            properties.forEach((k,v) -> System.out.println(k + " : " + v));
        }

        @Test
        @EnabledIfSystemProperty(named = "java.version", matches = "15.0.2")
        void testJavaVersion() {
        }

        @Test
        @DisabledIfSystemProperty(named = "os.arch", matches = ".*32.*")
        void testSolo64() {
        }

        @Test
        @EnabledIfSystemProperty(named = "os.arch", matches = ".*32.*")
        void testNo64() {
        }

        @Test
        @EnabledIfSystemProperty(named = "user.name", matches = "Renzo")
        void testUsername() {
        }

        @Test
        @EnabledIfSystemProperty(named = "ENV", matches = "dev") //para que funcione se debe editar el runner
        void testDev() {
        }
    }

    @Nested
    class variableAmbiente {
        @Test
        void imprimirVariablesAmbiente() {
            Map<String,String> getenv =System.getenv();
            getenv.forEach((k,v) -> System.out.println(k + " " + v));
        }

        @Test
        @EnabledIfEnvironmentVariable(named = "JAVA_HOME", matches = ".*jdk-21.0.4.7-hotspot.*")
        void testJAVAHOME() {
        }

        @Test
        @EnabledIfEnvironmentVariable(named = "NUMBER_OF_PROCESSORS", matches = "12")
        void testProcesadores() {
        }

        @Test
        @EnabledIfEnvironmentVariable(named = "ENVIRONMENT", matches = "dev")
        void testEnv() {
        }

        @Test
        @DisabledIfEnvironmentVariable(named = "ENVIRONMENT", matches = "prod")
        void testEnvProdDisabled() {
        }
    }

    @Test
    @DisplayName("test Saldo Cuenta Dev")
    void test_saldo_cuenta_dev() {
        boolean esDev = "dev".equals(System.getProperty("ENV"));
        assumeTrue(esDev);
        //cuenta = new Cuenta("Renzo", new BigDecimal("1000.2354"));
        assertEquals(1000.2354, cuenta.getSaldo().doubleValue());
        assertFalse(cuenta.getSaldo().compareTo(BigDecimal.ZERO) < 0);
        assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("test Saldo Cuenta Dev 2")
    void test_saldo_cuenta_dev2() {
        boolean esDev = "dev".equals(System.getProperty("ENV"));
        assumingThat(esDev, () -> {
            //cuenta = new Cuenta("Renzo", new BigDecimal("1000.2354"));
            assertEquals(1000.2354, cuenta.getSaldo().doubleValue());
            assertFalse(cuenta.getSaldo().compareTo(BigDecimal.ZERO) < 0);
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
        });
    }
    @DisplayName("Probando ando")
    @RepeatedTest(value = 4, name = "{displayName} -  Repetecion {currentRepetition} de {totalRepetitions}")
    void test_debito_cuenta_repetir(RepetitionInfo info) {
        if (info.getCurrentRepetition() == 3) {
            System.out.println("Ya repitrimos 3 veces");
        }
        cuenta = new Cuenta("Renzo", new BigDecimal("1000.2354"));
        cuenta.debito(new BigDecimal(100));
        assertNotNull(cuenta.getSaldo());
        assertEquals(900,cuenta.getSaldo().intValue());
        assertEquals("900.2354",cuenta.getSaldo().toPlainString());
    }

    @Tag("param")
    @Nested
    class PruebasParametrizadasTest {
        @ParameterizedTest(name = "numero {index} ejecutando con valor {0} - {argumentsWithNames}")
        @ValueSource(strings = {"100","200","300","500","700","1000.2354"})
        void test_debito_cuenta_parametros_strings(String monto) {
            cuenta.debito(new BigDecimal(monto));
            assertNotNull(cuenta.getSaldo());
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
        }

        @ParameterizedTest(name = "numero {index} ejecutando con valor {0} - {argumentsWithNames}")
        @ValueSource(doubles = {100,200,300,500,700,1000})
        void test_debito_cuenta_parametros_ints(double monto) {
            cuenta.debito(new BigDecimal(monto));
            assertNotNull(cuenta.getSaldo());
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
        }

        @ParameterizedTest(name = "numero {index} ejecutando con valor {0} - {argumentsWithNames}")
        @CsvSource({"1,100","2,200","3,300","4,500","5,700","6,1000.2354"})
        void test_debito_cuenta_csv_source(String index,String monto) {
            System.out.println(index + " -> " + monto);
            cuenta.debito(new BigDecimal(monto));
            assertNotNull(cuenta.getSaldo());
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
        }

        @ParameterizedTest(name = "numero {index} ejecutando con valor {0} - {argumentsWithNames}")
        @CsvSource({"200,100,JD,LIZU","250,200,PEPITO,PEPITO","300,300,maria,Maria","510,500,Lucas,Luca","750,700,PEPA,PEPA","1000.2354,1000.2354,Joel,Joel"})
        void test_debito_cuenta_csv_source2(String saldo,String monto,String esperado, String actual) {
            System.out.println(saldo + " -> " + monto);
            cuenta.setSaldo(new BigDecimal(saldo));
            cuenta.debito(new BigDecimal(monto));
            cuenta.setPersona(actual);
            assertNotNull(cuenta.getSaldo());
            assertNotNull(cuenta.getPersona());
            assertEquals(esperado, actual);
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
        }

        @ParameterizedTest(name = "numero {index} ejecutando con valor {0} - {argumentsWithNames}")
        @CsvFileSource(resources = "/data.csv")
        void test_debito_cuenta_csv_file_source(String monto) {
            cuenta.debito(new BigDecimal(monto));
            assertNotNull(cuenta.getSaldo());
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
        }

        @ParameterizedTest(name = "numero {index} ejecutando con valor {0} - {argumentsWithNames}")
        @CsvFileSource(resources = "/data2.csv")
        void test_debito_cuenta_csv_file_source2(String saldo,String monto,String esperado, String actual) {
            cuenta.setSaldo(new BigDecimal(saldo));
            cuenta.debito(new BigDecimal(monto));
            cuenta.setPersona(actual);
            assertNotNull(cuenta.getSaldo());
            assertNotNull(cuenta.getPersona());
            assertEquals(esperado, actual);
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
        }
    }


    @Tag("param")
    @ParameterizedTest(name = "numero {index} ejecutando con valor {0} - {argumentsWithNames}")
    @MethodSource("montoList")
    void test_debito_cuenta_method_source(String monto) {
        cuenta.debito(new BigDecimal(monto));
        assertNotNull(cuenta.getSaldo());
        assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
    }

    static List<String> montoList() {
        return Arrays.asList("100","200","300","500","700","1000.2354");
    }

    @Nested
    @Tag("timeout")
    class EjemploTimeoutTest {
        @Test
        @Timeout(3)
        void pruebaTimeout() throws InterruptedException {
            TimeUnit.SECONDS.sleep(2);
        }

        @Test
        @Timeout(value = 1000,unit = TimeUnit.MILLISECONDS)
        void pruebaTimeout2() throws InterruptedException {
            TimeUnit.MILLISECONDS.sleep(900);
        }

        @Test
        void testTimeoutAssertions() throws InterruptedException {
            assertTimeout(Duration.ofSeconds(5),() -> {
                TimeUnit.MILLISECONDS.sleep(4000);
            });
        }
    }

}