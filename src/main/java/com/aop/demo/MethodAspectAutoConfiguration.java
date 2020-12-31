package com.aop.demo;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;

@Configuration
@EnableAspectJAutoProxy
public class MethodAspectAutoConfiguration {

	@Component
	@Aspect
	public static class MethodAspect{
		@Around(value="execution(* *Service(..))")
		public Object around(ProceedingJoinPoint joinPoint)throws Throwable{
			Signature signature = joinPoint.getSignature();
			MethodSignature methodSignature = (MethodSignature) signature;
			Method method = methodSignature.getMethod();

			Timer timer = Metrics.timer("method.cost.time", "method.name",method.getName());
			ThrowableHolder holder = new ThrowableHolder() ;
			Object result = timer.recordCallable(() -> { //記錄數據
				long st = System.currentTimeMillis();
				Object obj = null ;
				try {
					obj = joinPoint.proceed();
				}catch(Throwable e) {
					holder.t = e ;
				}finally{
					long ed = System.currentTimeMillis();
					System.out.printf("execute method: %s, time: %d ms.", method.getName(),(ed-st)) ;
				}
				return obj ;
			});
			if(holder.t !=null) throw holder.t ;
			return result ;
		}
		private class ThrowableHolder{
			Throwable t ;
		}

	}

}
