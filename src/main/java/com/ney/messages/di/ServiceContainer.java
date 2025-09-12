package com.ney.messages.di;

import java.util.*;

public class ServiceContainer {

    private final Map<Class<?>, Object> singletons = new HashMap<>();
    private final Map<Class<?>, List<Object>> multiBindings = new HashMap<>();

    public <T> void bind(Class<T> type, T instance) {
        singletons.put(type, instance);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> type) {
        return (T) singletons.get(type);
    }

    public <T> void bindMulti(Class<T> type, T instance) {
        multiBindings.computeIfAbsent(type, k -> new ArrayList<>()).add(instance);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getAll(Class<T> type) {
        return (List<T>) multiBindings.getOrDefault(type, Collections.emptyList());
    }

    public void clear() {
        singletons.clear();
        multiBindings.clear();
    }
}