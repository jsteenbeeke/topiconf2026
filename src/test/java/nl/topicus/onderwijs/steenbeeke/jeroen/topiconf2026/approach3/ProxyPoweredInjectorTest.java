package nl.topicus.onderwijs.steenbeeke.jeroen.topiconf2026.approach3;

import nl.topicus.onderwijs.steenbeeke.jeroen.topiconf2026.approach3.beans.AppleService;
import nl.topicus.onderwijs.steenbeeke.jeroen.topiconf2026.approach3.beans.FruitService;
import nl.topicus.onderwijs.steenbeeke.jeroen.topiconf2026.approach3.beans.OrangeService;
import nl.topicus.onderwijs.steenbeeke.jeroen.topiconf2026.approach3.beans2.AService;
import nl.topicus.onderwijs.steenbeeke.jeroen.topiconf2026.approach3.beans2.BService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

class ProxyPoweredInjectorTest {
	@Test
	void test_all_fruit_services_available() {
		ProxyPoweredInjector injector = new ProxyPoweredInjector(AppleService.class.getPackage());

		assertNotNull(injector.getBean(AppleService.class));
		assertNotNull(injector.getBean(OrangeService.class));
		assertNotNull(injector.getBean(FruitService.class));
	}

	@Test
	void test_all_circular_services_available() {
		ProxyPoweredInjector injector = new ProxyPoweredInjector(AService.class.getPackage());

		assertNotNull(injector.getBean(AService.class));
		assertNotNull(injector.getBean(BService.class));
	}

	@Test
	void test_transactions_work() {
		ProxyPoweredInjector injector = new ProxyPoweredInjector(FruitService.class.getPackage(), true);

		AppleService apples = injector.getBean(AppleService.class);
		OrangeService oranges = injector.getBean(OrangeService.class);

		assertNotNull(apples);
		assertNotNull(oranges);

		assertEquals(0, TransactionalBeanCallInterceptor.committed);
		assertEquals(0, TransactionalBeanCallInterceptor.rolledback);
		apples.buy();

		assertEquals(1, TransactionalBeanCallInterceptor.committed);
		assertEquals(0, TransactionalBeanCallInterceptor.rolledback);

		assertThrows(InvocationTargetException.class, oranges::buy);

		assertEquals(1, TransactionalBeanCallInterceptor.committed);
		assertEquals(1, TransactionalBeanCallInterceptor.rolledback);
	}
}