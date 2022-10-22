package com.xxxx.cms.annotaions;


import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {
    // 权限码
    String code() default "";
}
