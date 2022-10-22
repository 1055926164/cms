package com.xxxx.cms.aspect;

import com.xxxx.cms.annotaions.RequirePermission;
import com.xxxx.cms.exceptions.AuthException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Method;
import java.util.List;

@Component
@Aspect
public class PermissionProxy {
    @Resource
    private HttpSession session;

    @Around(value = "@annotation(com.xxxx.cms.annotaions.RequirePermission)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        Object result=null;
        List<String> permissions= (List<String>) session.getAttribute("permissions");
        if (null==permissions||permissions.size()<1){
            throw new AuthException();
        }
        MethodSignature methodSignature= (MethodSignature) pjp.getSignature();
        RequirePermission requirePermission=methodSignature.getMethod().getDeclaredAnnotation(RequirePermission.class);
        if (!(permissions.contains(requirePermission.code()))){
            throw new AuthException();
        }
        result=pjp.proceed();
        System.out.println(">>>>>>>>>>>>>>>>");
        return result;
    }
}
