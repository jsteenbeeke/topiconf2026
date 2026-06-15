package nl.topicus.onderwijs.steenbeeke.jeroen.topiconf2026.approach3.beans;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class OrangeService {
	@Transactional
	public void buy() {
		throw new UnsupportedOperationException("Oranges are not for sale!");
	}
}
