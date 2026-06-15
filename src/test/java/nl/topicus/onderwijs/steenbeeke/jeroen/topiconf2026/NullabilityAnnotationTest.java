package nl.topicus.onderwijs.steenbeeke.jeroen.topiconf2026;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaParameter;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

public class NullabilityAnnotationTest {
	@Test
	void all_method_parameters_should_have_nullability_annotations() {
		var rule = methods()
				.should(haveParametersWithNullabilityAnnotations())
				.allowEmptyShould(true)
				.because("this helps static code analysis tools do their job");

		JavaClasses javaClasses = new ClassFileImporter()
				.importPackages(NullabilityAnnotationTest.class.getPackageName());

		rule.check(javaClasses);
	}


	@Test
	void all_return_types_should_have_nullability_annotations() {
		var rule = methods().that(doNotHaveVoidReturnType())
				.and(doNotReturnPrimitives())
				.should()
				.beAnnotatedWith(Nullable.class)
				.orShould()
				.beAnnotatedWith(NotNull.class)
				.allowEmptyShould(true)
				.because("this helps static code analysis tools do their job");

		JavaClasses javaClasses = new ClassFileImporter()
				.importPackages(NullabilityAnnotationTest.class.getPackageName());

		rule.check(javaClasses);
	}

	@NotNull
	private DescribedPredicate<? super JavaMethod> doNotHaveVoidReturnType() {
		return new DescribedPredicate<>("do not have a void return type") {
			@Override
			public boolean test(@NotNull JavaMethod javaMethod) {
				return !javaMethod.getRawReturnType().reflect().equals(void.class);
			}
		};
	}

	@NotNull
	private DescribedPredicate<? super JavaMethod> doNotReturnPrimitives() {
		return new DescribedPredicate<>("do not return primitives") {
			@Override
			public boolean test(@NotNull JavaMethod javaMethod) {
				return !javaMethod.getRawReturnType().isPrimitive();
			}
		};
	}

	@NotNull
	private static ArchCondition<JavaMethod> haveParametersWithNullabilityAnnotations() {
		return new ArchCondition<>("have nullability annotations on their parameters") {
			@Override
			public void check(@NotNull JavaMethod item, @NotNull ConditionEvents events) {
				for (JavaParameter parameter : item.getParameters()) {
					if (!parameter.isAnnotatedWith(Nullable.class) && !parameter.isAnnotatedWith(NotNull.class)) {
						String message = String.format(
								"Method %s has a parameter %d without nullability annotation",
								item.getFullName(), parameter.getIndex());
						events.add(SimpleConditionEvent.violated(item, message));
					}
				}
			}
		};
	}
}
