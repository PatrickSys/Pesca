package Exceptions;

import java.io.File;
import java.io.IOException;

/************************************************************************
 Made by        PatrickSys
 Date           02/06/2021
 Package        Exceptions
 Description:
 ************************************************************************/
public class FileIsEmptyException extends IOException {
    public FileIsEmptyException(){
        super();
    }
    public FileIsEmptyException(String message){
        super(message);
    }
}
