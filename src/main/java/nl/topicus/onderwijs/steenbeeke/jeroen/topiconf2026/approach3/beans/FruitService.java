package nl.topicus.onderwijs.steenbeeke.jeroen.topiconf2026.approach3.beans;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class FruitService {
	private final AppleService appleService;

	private final OrangeService orangeService;

	@Inject
	public FruitService(AppleService appleService, OrangeService orangeService) {
		this.appleService = appleService;
		this.orangeService = orangeService;
	}

	public AppleService getAppleService() {
		return appleService;
	}

	public OrangeService getOrangeService() {
		return orangeService;
	}
}
