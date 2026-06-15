package nl.topicus.onderwijs.steenbeeke.jeroen.topiconf2026.approach1.beans;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class FruitService {
	@Inject
	private AppleService appleService;

	@Inject
	private OrangeService orangeService;

	public AppleService getAppleService() {
		return appleService;
	}

	public OrangeService getOrangeService() {
		return orangeService;
	}
}
