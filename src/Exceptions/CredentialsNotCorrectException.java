package Exceptions;

import javax.naming.AuthenticationException;
import java.nio.file.AccessDeniedException;

/************************************************************************
 Made by        PatrickSys
 Date           02/06/2021
 Package        Exceptions
 Description:
 ************************************************************************/
public class CredentialsNotCorrectException extends AuthenticationException {
    public CredentialsNotCorrectException(String message){
        super(message);
    }
}
