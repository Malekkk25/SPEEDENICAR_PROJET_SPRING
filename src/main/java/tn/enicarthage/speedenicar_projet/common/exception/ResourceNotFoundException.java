package tn.enicarthage.speedenicar_projet.common.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException  extends RuntimeException{
    public ResourceNotFoundException(String resource ,String field ,Object value){
        super(String.format("%s intouvale avec %s : %s " ,resource,field,value));
    }

    public ResourceNotFoundException(String message){
        super(message);
    }
}
