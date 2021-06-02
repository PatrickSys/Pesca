package Exceptions;

import java.util.NoSuchElementException;

/************************************************************************
 Made by        PatrickSys
 Date           02/06/2021
 Package        Exceptions
 Description:
 ************************************************************************/
public class LineNotFoundException extends NoSuchElementException {

    public LineNotFoundException() {
    }

    public LineNotFoundException(String message){
        super(message);
    }


}
