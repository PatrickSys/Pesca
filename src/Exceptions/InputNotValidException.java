package Exceptions;

import java.util.InputMismatchException;

/************************************************************************
 Made by        PatrickSys
 Date           02/06/2021
 Package        Exceptions
 Description:
 ************************************************************************/


public class InputNotValidException extends InputMismatchException {
    public InputNotValidException(String message){
        super(message);
    }
}
