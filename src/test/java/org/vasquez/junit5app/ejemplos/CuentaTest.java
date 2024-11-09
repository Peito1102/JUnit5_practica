package org.vasquez.junit5app.ejemplos;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.*;
import org.vasquez.junit5app.ejemplos.exceptions.DineroInsuficienteException;
import org.vasquez.junit5app.ejemplos.models.Banco;
import org.vasquez.junit5app.ejemplos.models.Cuenta;

import java.math.BigDecimal;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CuentaTest {
    Cuenta cuenta;

    @BeforeEach
    void initMetodoTest() {
        this.cuenta = new Cuenta("Renzo", new BigDecimal("1000.2354"));
        System.out.println("iniciando el método.");
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

    @Test
    @DisplayName("probando el nombre de la cuenta corriente!")
    void test_nombre_cuenta() { //no se usa camelCase normalmente en TEST
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

    @Test
    void test_debito_cuenta() {
            cuenta = new Cuenta("Renzo", new BigDecimal("1000.2354"));
        cuenta.debito(new BigDecimal(100));
        assertNotNull(cuenta.getSaldo());
        assertEquals(900,cuenta.getSaldo().intValue());
        assertEquals("900.2354",cuenta.getSaldo().toPlainString());
    }

    @Test
    void test_credito_cuenta() {
        Cuenta cuenta = new Cuenta("Renzo", new BigDecimal("1000.2354"));
        cuenta.credito(new BigDecimal(100));
        assertNotNull(cuenta.getSaldo());
        assertEquals(1100,cuenta.getSaldo().intValue());
        assertEquals("1100.2354",cuenta.getSaldo().toPlainString());
    }

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