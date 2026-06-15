package nl.topicus.onderwijs.steenbeeke.jeroen.topiconf2026.approach3.beans;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class AppleService {
	private static final Logger log = LoggerFactory.getLogger(AppleService.class);

	@Transactional
	public void buy() {
		log.info("Buying apple");
	}
}
