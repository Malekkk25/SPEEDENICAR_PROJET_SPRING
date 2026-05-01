package tn.enicarthage.speedenicar_projet.common.aspects;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    // 1. Définir le point d'intersection : toutes les méthodes dans les packages "service" ou "controller"
    @Pointcut("within(tn.enicarthage.speedenicar_projet..service..*) || within(tn.enicarthage.speedenicar_projet..controller..*)")
    public void applicationPackagePointcut() {
        // Pointcut vide, sert juste de référence
    }

    // 2. Intercepter les erreurs (Exceptions) automatiquement
    @AfterThrowing(pointcut = "applicationPackagePointcut()", throwing = "e")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
        log.error("Exception dans {}.{}() avec cause = {}",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(),
                e.getCause() != null ? e.getCause() : "NULL");
    }

    // 3. Autour de la méthode : Loguer l'entrée, la sortie et le temps d'exécution
    @Around("applicationPackagePointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        if (log.isDebugEnabled()) {
            log.debug("Entrée : {}.{}() avec argument[s] = {}",
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(),
                    Arrays.toString(joinPoint.getArgs()));
        }

        long start = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed(); // Exécute la vraie méthode

            long elapsedTime = System.currentTimeMillis() - start;
            if (log.isDebugEnabled()) {
                log.debug("Sortie : {}.{}() avec résultat = {}, Temps d'exécution = {} ms",
                        joinPoint.getSignature().getDeclaringTypeName(),
                        joinPoint.getSignature().getName(),
                        result,
                        elapsedTime);
            }
            return result;

        } catch (IllegalArgumentException e) {
            log.error("Argument illégal : {} dans {}.{}()",
                    Arrays.toString(joinPoint.getArgs()),
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName());
            throw e;
        }
    }
}
