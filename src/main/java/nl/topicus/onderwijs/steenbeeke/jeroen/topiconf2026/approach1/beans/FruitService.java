package nl.topicus.onderwijs.steenbeeke.jeroen.topiconf2026.approach1.beans;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jetbrains.annotations.NotNull;

@ApplicationScoped
public class FruitService {
	@Inject
	private AppleService appleService;

	@Inject
	private OrangeService orangeService;

	@NotNull
	public AppleService getAppleService() {
		return appleService;
	}

	@NotNull
	public OrangeService getOrangeService() {
		return orangeService;
	}
}
