package com.codeforge.compiler.exception;

public class ApiException extends RuntimeException {
  public ApiException(String message) {
    super(message);
  }
}