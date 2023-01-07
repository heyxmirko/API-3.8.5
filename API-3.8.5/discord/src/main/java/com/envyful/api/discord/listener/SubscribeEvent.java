package com.envyful.api.discord.listener;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface SubscribeEvent {
    boolean receiveAcknowledged() default false;
}
