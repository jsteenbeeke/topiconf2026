package nl.topicus.onderwijs.steenbeeke.jeroen.topiconf2026;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public abstract class DependencyInjector {
	private final Map<Class<?>, Object> beans = new HashMap<>();

	protected <T> void registerBean(@NotNull Class<T> beanClass, @NotNull T bean) {
		beans.put(beanClass, bean);
	}

	@NotNull
	public Map<Class<?>, Object> getBeans() {
		return beans;
	}

	@NotNull
	@SuppressWarnings("unchecked")
	public final <T> T getBean(Class<T> clazz) {
		if (!beans.containsKey(clazz)) {
			throw new IllegalArgumentException("Bean not found for class: " + clazz.getName());
		}

		return (T) beans.get(clazz);
	}
}
