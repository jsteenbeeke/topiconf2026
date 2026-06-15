package nl.topicus.onderwijs.steenbeeke.jeroen.topiconf2026.approach3.beans2;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class BService {
	private final AService aService;

	@Inject
	public BService(AService aService) {
		this.aService = aService;
	}
}
