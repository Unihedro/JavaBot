package com.gmail.inverseconduit.chat;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Procrastination: I'll fix this javadoc comment later.<br>
 * ListenerPriority @ com.gmail.inverseconduit.chat
 *
 * @author Unihedron<<a href="mailto:vincentyification@gmail.com"
 *         >vincentyification@gmail.com</a>>
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Documented
@Target(value = ElementType.TYPE)
public @interface ListenerProperty {

    public enum Priority {
        HIGH,
        MEDIUM,
        DEFAULT,
        LOW;
    }

    String invocation() default "^!!";

    Priority priority() default Priority.DEFAULT;

    boolean ignoreHandled() default false;

    int timer() default 0;

}
