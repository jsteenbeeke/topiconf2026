package nl.topicus.onderwijs.steenbeeke.jeroen.topiconf2026.approach2.beans;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jetbrains.annotations.NotNull;

@ApplicationScoped
public class FruitService {
	private final AppleService appleService;

	private final OrangeService orangeService;

	@Inject
	public FruitService(AppleService appleService, OrangeService orangeService) {
		this.appleService = appleService;
		this.orangeService = orangeService;
	}

	@NotNull
	public AppleService getAppleService() {
		return appleService;
	}

	@NotNull
	public OrangeService getOrangeService() {
		return orangeService;
	}
}
