package nl.topicus.onderwijs.steenbeeke.jeroen.topiconf2026.approach1;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import nl.topicus.onderwijs.steenbeeke.jeroen.topiconf2026.DependencyInjector;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;

public class NaiveDependencyInjector extends DependencyInjector {
	public NaiveDependencyInjector(@NotNull Package beanPackage) {
		Reflections reflections =
				new Reflections(beanPackage.getName(), Scanners.TypesAnnotated);

		Set<Class<?>> applicationScopedServices =
				reflections.getTypesAnnotatedWith(ApplicationScoped.class);

		for (Class<?> serviceClass : applicationScopedServices) {
			createAndRegisterBean(serviceClass);
		}

		for (Object bean : getBeans().values()) {
			injectDependencies(bean, getBeans());
		}
	}

	private void injectDependencies(@NotNull Object target, @NotNull Map<Class<?>, Object> services) {
		Class<?> targetClass = target.getClass();

		for (Field field : targetClass.getDeclaredFields()) {
			if (field.isAnnotationPresent(Inject.class)) {
				if (services.containsKey(field.getType())) {
					field.trySetAccessible();
					try {
						field.set(target, services.get(field.getType()));
					} catch (IllegalAccessException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
	}


	private <T> void createAndRegisterBean(@NotNull Class<T> beanClass) {
		try {
			T instance = beanClass.getConstructor().newInstance();

			registerBean(beanClass, instance);
		} catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
		         InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

}
