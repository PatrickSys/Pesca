package Exceptions;

import java.util.NoSuchElementException;

/************************************************************************
 Made by        PatrickSys
 Date           28/05/2021
 Package        PatrickSys
 Version        
 Description:
 ************************************************************************/

public class UserNotFoundException extends NoSuchElementException {

    public UserNotFoundException (){
        super();
    }
    public UserNotFoundException(String message){
        super(message);
    }
}
