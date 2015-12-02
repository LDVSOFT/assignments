package ru.spbau.mit;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.*;


public class Injector {

    private static Object initializeInContext(
            String interfaceName,
            Map<String, Object> context,
            List<String> implementationClassNames) throws Exception {
        //Search for implementation
        Class<?> targetClass = Class.forName(interfaceName);
        Class<?> solutionClass = null;
        for (String candidate : implementationClassNames) {
            Class<?> candidateClass = Class.forName(candidate);
            if (!targetClass.isAssignableFrom(candidateClass)) {
                continue;
            }
            if (solutionClass != null) {
                throw new AmbiguousImplementationException();
            }
            solutionClass = candidateClass;
        }
        if (solutionClass == null) {
            throw new ImplementationNotFoundException();
        }
        String className = solutionClass.getName();

        //Check if it was already created
        if (context.containsKey(className)) {
            Object instance = context.get(className);
            if (instance == null) {
                throw new InjectionCycleException();
            }
            return instance;
        }

        // To prevent cycles
        context.put(className, null);

        // Gather parameters recursively
        Constructor<?> constructor = solutionClass.getConstructors()[0];
        List<Object> params = new ArrayList<>();
        for (Class<?> paramClass : constructor.getParameterTypes()) {
            params.add(initializeInContext(paramClass.getCanonicalName(), context, implementationClassNames));
        }

        // Create instance
        Object instance = constructor.newInstance(params.toArray());
        context.put(className, instance);
        return instance;
    }

    /**
     * Create and initialize object of `rootClassName` class using classes from
     * `implementationClassNames` for concrete dependencies.
     */
    public static Object initialize(String rootClassName, List<String> implementationClassNames) throws Exception {
        List<String> classNames = new ArrayList<>(implementationClassNames);
        classNames.add(rootClassName);
        return initializeInContext(rootClassName, new HashMap<String, Object>(), classNames);
    }
}