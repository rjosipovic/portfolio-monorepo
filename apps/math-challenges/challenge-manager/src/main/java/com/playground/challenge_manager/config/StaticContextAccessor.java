package com.playground.challenge_manager.config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * A bridge to access Spring beans from non-Spring-managed classes (like JPA Converters).
 */
@Component
public class StaticContextAccessor implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    public static <T> T getBean(Class<T> clazz) {
        if (Objects.isNull(context)) {
            throw new IllegalStateException("ApplicationContext is not initialized yet.");
        }
        return context.getBean(clazz);
    }
}
