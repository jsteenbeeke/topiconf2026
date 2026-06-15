package nl.topicus.onderwijs.steenbeeke.jeroen.topiconf2026.approach3.beans2;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AService {
	private final BService bService;

	@Inject
	public AService(BService bService) {
		this.bService = bService;
	}
}
