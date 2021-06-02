package Exceptions;

/************************************************************************
 Made by        PatrickSys
 Date           29/05/2021
 Package        PatrickSys
 Version        
 Description:
 ************************************************************************/

public class UserAlreadySignedUpException extends Exception{
    public UserAlreadySignedUpException(String message){
        super(message);
    }
}
