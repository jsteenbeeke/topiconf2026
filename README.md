# Dependency Injection Demystified — Topiconf 2026

Example code accompanying my Topiconf talk on the basics of **dependency injection (DI)**.

This project builds three increasingly
capable DI containers by hand. Each one scans a package for beans annotated with
`@ApplicationScoped`, wires them together, and exposes them through a shared
`DependencyInjector` base class. By walking from the simplest possible implementation to
a proxy-based one, the examples show *why* real frameworks (Spring, CDI, Guice, …) work
the way they do.

## Requirements

- **JDK 21** (the project compiles with `--release 21`)
- **Maven 3.9+**

## Running the examples

The examples are expressed as JUnit 5 tests — each test constructs an injector, asks it
for some beans, and asserts that they were wired correctly. Running the test suite *is*
running the examples:

```bash
# Run every approach
mvn test

# Run a single approach
mvn test -Dtest=NaiveDependencyInjectorTest        # approach 1
mvn test -Dtest=ConstructorBasedInjectorTest       # approach 2
mvn test -Dtest=ProxyPoweredInjectorTest           # approach 3
```

## Project layout

Each approach lives in its own package and ships with a matching set of demo beans:

```
src/main/java/.../topiconf2026/
├── DependencyInjector.java          # shared bean registry + getBean()
├── approach1/  NaiveDependencyInjector + beans/
├── approach2/  ConstructorBasedInjector + beans/ + beans2/
└── approach3/  ProxyPoweredInjector + interceptors + beans/ + beans2/
```

The demo beans are deliberately tiny: a `FruitService` that depends on an `AppleService`
and an `OrangeService` (the happy path), plus an `AService`/`BService` pair that depend on
each other to exercise **circular dependencies**.

## The three approaches

### 1. Naive — field injection (`approach1`)

The simplest thing that could possibly work, in two phases:

1. Instantiate every `@ApplicationScoped` class using its **no-arg constructor**.
2. Walk each bean's fields and, for every `@Inject` field, set it to the matching
   registered bean.

Because dependencies are injected *after* construction, circular references are not a
problem. The price is that beans **must** have a no-arg constructor and mutable fields —
there is no way to express a dependency as a constructor parameter, and nothing guarantees
a field is populated before it is used.

### 2. Constructor-based injection (`approach2`)

Moves dependencies into the constructor, which is the style most modern frameworks prefer
(final fields, no half-constructed objects). To make this work the injector must
instantiate beans **in dependency order**:

1. For each bean, find its injection constructor (`@Inject`-annotated, or the no-arg one)
   and record which types it needs.
2. Repeatedly instantiate any bean whose dependencies are already available, looping until
   everything is built.
3. If a full pass adds no new beans, the remaining beans form a **circular dependency** and
   an `IllegalStateException` is thrown (with a description of what couldn't be resolved).

This buys cleaner, immutable beans — but at the cost of being unable to support circular
dependencies, as the dedicated test demonstrates.

### 3. Proxy-powered injection (`approach3`)

The most powerful approach, and roughly how real containers behave. Instead of registering
the bean itself, the injector registers a **dynamically generated proxy subclass** (built
with [Byte Buddy](https://bytebuddy.net/) and instantiated with
[Objenesis](https://objenesis.org/), bypassing constructors). Every method call on the
proxy is routed through an interceptor that:

- **Lazily creates the real bean on first use**, resolving its constructor and field
  dependencies on demand. Because nothing is instantiated up front, *circular dependencies
  work again* — the proxy for `B` can be handed to `A` before `B` is actually built.
- **Adds cross-cutting behaviour** around method calls. With transactions enabled
  (`new ProxyPoweredInjector(pkg, true)`), the `TransactionalBeanCallInterceptor` wraps any
  `@Transactional` method in a begin/commit/rollback cycle — committing on success and
  rolling back when the method throws.

This is the payoff of proxies: lazy wiring solves the ordering/cycle problem, and method
interception is exactly the hook that frameworks use to implement transactions, security,
caching, and logging without touching the bean's own code.

## Summary

|                          | Approach 1 — Naive | Approach 2 — Constructor | Approach 3 — Proxy                 |
|--------------------------|--------------------|--------------------------|------------------------------------|
| Injection style          | `@Inject` fields   | `@Inject` constructor    | constructor + fields               |
| Bean creation            | eager, no-arg ctor | eager, dependency order  | lazy, on first call                |
| Circular dependencies    | ✅ supported        | ❌ rejected               | ✅ supported                        |
| Immutable (final) fields | ❌                  | ✅                        | ✅                                  |
| Cross-cutting concerns   | ❌                  | ❌                        | ✅ (`@Transactional`)               |
| Key libraries            | Reflections        | Reflections              | Reflections, Byte Buddy, Objenesis |
