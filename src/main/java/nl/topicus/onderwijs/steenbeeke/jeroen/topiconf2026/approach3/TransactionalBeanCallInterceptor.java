package nl.topicus.onderwijs.steenbeeke.jeroen.topiconf2026.approach3;

import jakarta.transaction.Transactional;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.function.Supplier;

public class TransactionalBeanCallInterceptor {
	private static final Logger log = LoggerFactory.getLogger(TransactionalBeanCallInterceptor.class);

	@VisibleForTesting
	static int committed;

	@VisibleForTesting
	static int rolledback;

	private final Supplier<Object> lazyInitializer;

	public TransactionalBeanCallInterceptor(Supplier<Object> lazyInitializer) {
		this.lazyInitializer = lazyInitializer;
	}

	private volatile Object target;

	@RuntimeType
	public Object intercept(@Origin Method method, @AllArguments Object[] args) throws Throwable {
		if (target == null) {
			synchronized (this) {
				if (target == null) {
					target = lazyInitializer.get();
				}
			}
		}

		if (method.isAnnotationPresent(Transactional.class)) {
			return runInTransaction(() -> method.invoke(target, args));
		} else {
			return method.invoke(target, args);
		}
	}

	private Object runInTransaction(ThrowableSupplier<Object> supplier) throws Throwable {
		boolean commit = false;
		try {
			commit = startTransaction();
			return supplier.get();
		} catch (Throwable e) {
			commit = false;
			rollbackTransaction();
			throw e;
		} finally {
			if (commit) {
				commitTransaction();
			}
		}
	}

	private void commitTransaction() {
		committed++;
	}

	private void rollbackTransaction() {
		rolledback++;
	}

	private boolean startTransaction() {
		return true;
	}

	@FunctionalInterface
	private interface ThrowableSupplier<T> {
		T get() throws Throwable;
	}

}
