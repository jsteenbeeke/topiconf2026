package nl.topicus.onderwijs.steenbeeke.jeroen.topiconf2026.approach3;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;

import java.lang.reflect.Method;
import java.util.function.Supplier;

public class BeanCallInterceptor
{
	private final Supplier<Object> lazyInitializer;

	public BeanCallInterceptor(Supplier<Object> lazyInitializer)
	{
		this.lazyInitializer = lazyInitializer;
	}

	private volatile Object target;

	@RuntimeType
	public Object intercept(@Origin Method method, @AllArguments Object[] args) throws Throwable
	{
		if (target == null)
		{
			synchronized (this)
			{
				if (target == null)
				{
					target = lazyInitializer.get();
				}
			}
		}

		return method.invoke(target, args);
	}
}
