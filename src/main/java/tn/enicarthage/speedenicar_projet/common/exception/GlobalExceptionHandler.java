package tn.enicarthage.speedenicar_projet.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Cette méthode va attraper TOUTES les erreurs de ton application
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllExceptions(Exception ex) {
        ex.printStackTrace(); // Affichera toute l'erreur en rouge dans ta console

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "LA VRAIE ERREUR : " + ex.getMessage());
        response.put("type_erreur", ex.getClass().getSimpleName());

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
