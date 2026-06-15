package nl.topicus.onderwijs.steenbeeke.jeroen.topiconf2026.approach3;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import nl.topicus.onderwijs.steenbeeke.jeroen.topiconf2026.DependencyInjector;
import org.jetbrains.annotations.NotNull;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static net.bytebuddy.matcher.ElementMatchers.isDeclaredBy;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.not;

public class ProxyPoweredInjector extends DependencyInjector {
	private boolean enableTransactions;

	ProxyPoweredInjector(@NotNull Package beanPackage) {
		this(beanPackage, false);
	}

	public ProxyPoweredInjector(@NotNull Package beanPackage, boolean enableTransactions) {
		this.enableTransactions = enableTransactions;

		Reflections reflections =
				new Reflections(beanPackage.getName(), Scanners.TypesAnnotated);

		Set<Class<?>> applicationScopedServices =
				reflections.getTypesAnnotatedWith(ApplicationScoped.class);

		ByteBuddy byteBuddy = new ByteBuddy();
		Objenesis objenesis = new ObjenesisStd();

		for (Class<?> serviceClass : applicationScopedServices) {
			createAndRegisterBean(serviceClass, byteBuddy, objenesis);
		}
	}

	private <T> void createAndRegisterBean(@NotNull Class<T> beanClass, @NotNull ByteBuddy byteBuddy, @NotNull Objenesis objenesis) {
		registerBean(beanClass, objenesis.newInstance(createProxyClass(byteBuddy, beanClass)));
	}

	@NotNull
	private <T> Class<? extends T> createProxyClass(@NotNull ByteBuddy byteBuddy, @NotNull Class<T> serviceClass) {
		try (DynamicType.Unloaded<T> make =
					 byteBuddy.subclass(serviceClass, ConstructorStrategy.Default.NO_CONSTRUCTORS)
							 .method(not(isDeclaredBy(Object.class)))
							 .intercept(MethodDelegation.withDefaultConfiguration()
									 .filter(named("intercept"))
									 .to(createInterceptor(serviceClass)))
							 .make()) {
			return make.load(serviceClass.getClassLoader()).getLoaded();
		}
	}

	@NotNull
	public <T> Object createInterceptor(@NotNull Class<T> serviceClass) {
		if (enableTransactions) {
			return new TransactionalBeanCallInterceptor(() -> initalizeBean(serviceClass));
		}

		return new BeanCallInterceptor(() -> initalizeBean(serviceClass));
	}

	@NotNull
	private Object initalizeBean(@NotNull Class<?> serviceClass) {
		Constructor<?> injectionConstructor = findInjectionConstructor(serviceClass);

		Object[] invocationParameters = new Object[injectionConstructor.getParameterCount()];
		Parameter[] constructorParameters = injectionConstructor.getParameters();

		for (int i = 0; i < invocationParameters.length; i++) {
			invocationParameters[i] = getBeans().get(constructorParameters[i].getType());
		}

		try {
			Object o = injectionConstructor.newInstance(invocationParameters);

			injectDependencies(o, getBeans());

			return o;
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	@NotNull
	public Constructor<?> findInjectionConstructor(@NotNull Class<?> serviceClass) {
		Set<Constructor<?>> annotatedConstructors = Arrays.stream(serviceClass.getConstructors())
				.filter(constructor -> constructor.isAnnotationPresent(Inject.class))
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
					.orElseThrow(() -> new IllegalStateException(
							"No annotated constructors and no zero-arg constructors, cannot instantiate "
									+ serviceClass.getName()));
		} else {
			return annotatedConstructors.iterator().next();
		}
	}

	public void injectDependencies(@NotNull Object target, @NotNull Map<Class<?>, Object> services)
			throws IllegalAccessException {

		Class<?> targetClass = target.getClass();

		for (Field field : targetClass.getDeclaredFields()) {
			if (field.isAnnotationPresent(Inject.class)) {
				if (services.containsKey(field.getType())) {
					field.trySetAccessible();
					field.set(target, services.get(field.getType()));
				}
			}

		}
	}
}
