package nl.topicus.onderwijs.steenbeeke.jeroen.topiconf2026.approach2;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import nl.topicus.onderwijs.steenbeeke.jeroen.topiconf2026.DependencyInjector;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ConstructorBasedInjector extends DependencyInjector {
	public ConstructorBasedInjector(@NotNull Package beanPackage) {
		Reflections reflections =
				new Reflections(beanPackage.getName(), Scanners.TypesAnnotated);

		Set<Class<?>> applicationScopedServices =
				reflections.getTypesAnnotatedWith(ApplicationScoped.class);

		Map<Class<?>, Set<Class<?>>> beanDependencies = new HashMap<>();

		for (Class<?> serviceClass : applicationScopedServices) {
			Constructor<?> injectionConstructor = findInjectionConstructor(serviceClass);

			beanDependencies.put(serviceClass,
					Arrays.stream(injectionConstructor.getParameters())
							.map(Parameter::getType)
							.collect(Collectors.toSet()));
		}

		int lastIteration = getBeans().size();
		while (getBeans().size() < beanDependencies.size()) {
			instantiateServices(beanDependencies);

			if (getBeans().size() == lastIteration) {
				// No new services added
				throw new IllegalStateException(
						"Could not resolve dependencies for remaining services (possibly circular dependencies): "
								+ describeUnresolvedDependencies(beanDependencies)

				);
			}

			lastIteration = getBeans().size();
		}

		for (Object service : getBeans().values()) {
			injectDependencies(service, getBeans());
		}
	}

	private void instantiateServices(@NotNull Map<Class<?>, Set<Class<?>>> serviceDependencies) {
		for (var serviceAndDependencies : serviceDependencies.entrySet()) {
			Class<?> serviceClass = serviceAndDependencies.getKey();
			Set<Class<?>> dependencies = serviceAndDependencies.getValue();

			if (getBeans().containsKey(serviceClass)) {
				continue;
			}

			if (getBeans().keySet().containsAll(dependencies)) {
				createAndRegisterBean(serviceClass);
			}
		}
	}

	private <T> void createAndRegisterBean(@NotNull Class<T> beanClass) {
		try {
			Constructor<T> injectionConstructor = findInjectionConstructor(beanClass);

			Object[] invocationParameters =
					new Object[injectionConstructor.getParameterCount()];
			Parameter[] constructorParameters = injectionConstructor.getParameters();

			for (int i = 0; i < invocationParameters.length; i++) {
				invocationParameters[i] =
						getBeans().get(constructorParameters[i].getType());
			}

			registerBean(beanClass,
					injectionConstructor.newInstance(invocationParameters));
		} catch (InstantiationException | IllegalAccessException |
		         InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@NotNull
	private <T> Constructor<T> findInjectionConstructor(@NotNull Class<T> serviceClass) {
		Set<Constructor<T>> annotatedConstructors = Arrays.stream(serviceClass.getConstructors())
				.filter(constructor -> constructor.isAnnotationPresent(Inject.class))
				.map(ctor -> (Constructor<T>) ctor)
				.collect(Collectors.toSet());

		if (annotatedConstructors.size() > 1) {
			throw new IllegalStateException(
					"Multiple constructors annotated with @Inject on class " + serviceClass.getName());
		}

		if (annotatedConstructors.isEmpty()) {
			// No explicitly marked constructor, look for no-arg constructor
			return Arrays.stream(serviceClass.getConstructors())
					.filter(constructor -> constructor.getParameterCount() == 0)
					.findFirst()
					.map(ctor -> (Constructor<T>) ctor)
					.orElseThrow(() -> new IllegalStateException(
							"No annotated constructors and no zero-arg constructors, cannot instantiate "
									+ serviceClass.getName()));
		} else {
			return annotatedConstructors.iterator().next();
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

	@NotNull
	private String
	describeUnresolvedDependencies(@NotNull Map<Class<?>, Set<Class<?>>> serviceDependencies) {
		return serviceDependencies.keySet()
				.stream()
				.filter(serviceClass -> !getBeans().containsKey(serviceClass))
				.flatMap(serviceClass -> serviceDependencies.get(serviceClass).stream())
				.map(Class::getName)
				.collect(Collectors.joining(", "));
	}
}
