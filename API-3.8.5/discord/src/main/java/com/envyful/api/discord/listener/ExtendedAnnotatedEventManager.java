package com.envyful.api.discord.listener;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.hooks.IEventManager;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.utils.ClassWalker;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ExtendedAnnotatedEventManager implements IEventManager {

    protected final Set<Object> listeners = ConcurrentHashMap.newKeySet();
    protected final Map<Class<?>, Map<Object, List<AnnotatedEventListener>>> methods = new ConcurrentHashMap<>();

    @Override
    public void register(@Nonnull Object listener) {
        if (this.listeners.add(listener)) {
            this.updateMethods();
        }
    }

    @Override
    public void unregister(@Nonnull Object listener) {
        if (this.listeners.remove(listener)) {
            this.updateMethods();
        }
    }

    @Nonnull
    @Override
    public List<Object> getRegisteredListeners() {
        return Collections.unmodifiableList(Lists.newArrayList(this.listeners));
    }

    @Override
    public void handle(@Nonnull GenericEvent event) {
        for (Class<?> eventClass : ClassWalker.walk(event.getClass())) {
            Map<Object, List<AnnotatedEventListener>> listeners = this.methods.getOrDefault(eventClass, ImmutableMap.of());

            for (Map.Entry<Object, List<AnnotatedEventListener>> entry : listeners.entrySet()) {
                for (AnnotatedEventListener method : entry.getValue()) {
                    if (event instanceof GenericInteractionCreateEvent) {
                        if (!method.receiveAcknowledged && ((GenericInteractionCreateEvent)event).isAcknowledged()) {
                            return;
                        }
                    }

                    try {
                        method.method.setAccessible(true);
                        method.method.invoke(entry.getKey(), event);
                    } catch (IllegalAccessException | InvocationTargetException e1) {
                        JDAImpl.LOG.error("Couldn't access annotated EventListener method", e1);
                    } catch (Throwable throwable) {
                        JDAImpl.LOG.error("One of the EventListeners had an uncaught exception", throwable);
                        if (throwable instanceof Error)
                            throw (Error)throwable;
                    }
                }
            }
        }
    }

    private void updateMethods() {
        this.methods.clear();

        for (Object listener : this.listeners) {
            boolean isClass = listener instanceof Class;
            Class<?> c = isClass ? (Class) listener : listener.getClass();

            for (Method m : c.getMethods()) {
                if (!m.isAnnotationPresent(SubscribeEvent.class) || (isClass && !Modifier.isStatic(m.getModifiers()))) {
                    continue;
                }

                Class<?>[] parameterTypes = m.getParameterTypes();
                if (parameterTypes.length != 1 || !GenericEvent.class.isAssignableFrom(parameterTypes[0])) {
                    continue;
                }

                Class<?> eventClass = parameterTypes[0];
                boolean receiveAcknowledged = m.getAnnotation(SubscribeEvent.class).receiveAcknowledged();

                this.methods.computeIfAbsent(eventClass, ___ -> Maps.newConcurrentMap())
                        .computeIfAbsent(listener, ___ -> new CopyOnWriteArrayList<>())
                        .add(new AnnotatedEventListener(m, receiveAcknowledged));
            }
        }
    }

    private static class AnnotatedEventListener {

        protected final Method method;
        protected final boolean receiveAcknowledged;

        private AnnotatedEventListener(Method method, boolean receiveAcknowledged) {
            this.method = method;
            this.receiveAcknowledged = receiveAcknowledged;
        }

        public Method getMethod() {
            return this.method;
        }

        public boolean isReceiveAcknowledged() {
            return this.receiveAcknowledged;
        }
    }
}
