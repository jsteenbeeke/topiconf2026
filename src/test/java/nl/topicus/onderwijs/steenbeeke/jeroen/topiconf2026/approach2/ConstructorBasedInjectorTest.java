package nl.topicus.onderwijs.steenbeeke.jeroen.topiconf2026.approach2;

import nl.topicus.onderwijs.steenbeeke.jeroen.topiconf2026.approach2.beans.AppleService;
import nl.topicus.onderwijs.steenbeeke.jeroen.topiconf2026.approach2.beans.FruitService;
import nl.topicus.onderwijs.steenbeeke.jeroen.topiconf2026.approach2.beans.OrangeService;
import nl.topicus.onderwijs.steenbeeke.jeroen.topiconf2026.approach2.beans2.AService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConstructorBasedInjectorTest {
	@Test
	void injector_with_constructor_dependency_support_yields_3_beans() {
		ConstructorBasedInjector injector = new ConstructorBasedInjector(FruitService.class.getPackage());

		AppleService appleService = injector.getBean(AppleService.class);
		OrangeService orangeService = injector.getBean(OrangeService.class);
		FruitService fruitService = injector.getBean(FruitService.class);

		assertNotNull(appleService);
		assertNotNull(orangeService);
		assertNotNull(fruitService);

		assertSame(appleService, fruitService.getAppleService());
		assertSame(orangeService, fruitService.getOrangeService());
	}

	@Test
	void injector_with_constructor_dependency_support_does_not_support_circular_dependencies() {
		assertThrows(IllegalStateException.class, () -> new ConstructorBasedInjector(AService.class.getPackage()));
	}
}