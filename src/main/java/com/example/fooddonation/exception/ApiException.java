// filename: src/main/java/com/example/fooddonation/exception/ApiException.java
package com.example.fooddonation.exception;

public class ApiException extends RuntimeException {
    public ApiException(String message) { super(message); }
}