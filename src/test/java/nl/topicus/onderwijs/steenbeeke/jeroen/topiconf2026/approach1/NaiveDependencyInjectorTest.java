package nl.topicus.onderwijs.steenbeeke.jeroen.topiconf2026.approach1;

import nl.topicus.onderwijs.steenbeeke.jeroen.topiconf2026.approach1.beans.AppleService;
import nl.topicus.onderwijs.steenbeeke.jeroen.topiconf2026.approach1.beans.FruitService;
import nl.topicus.onderwijs.steenbeeke.jeroen.topiconf2026.approach1.beans.OrangeService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NaiveDependencyInjectorTest {
	@Test
	void naive_dependency_injector_yields_3_beans() {
		NaiveDependencyInjector injector = new NaiveDependencyInjector(FruitService.class.getPackage());

		AppleService appleService = injector.getBean(AppleService.class);
		OrangeService orangeService = injector.getBean(OrangeService.class);
		FruitService fruitService = injector.getBean(FruitService.class);

		assertNotNull(appleService);
		assertNotNull(orangeService);
		assertNotNull(fruitService);

		assertSame(appleService, fruitService.getAppleService());
		assertSame(orangeService, fruitService.getOrangeService());
	}

}