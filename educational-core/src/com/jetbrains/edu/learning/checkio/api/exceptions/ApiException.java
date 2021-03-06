package com.jetbrains.edu.learning.checkio.api.exceptions;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * Is used as marker for all exceptions related to API errors
 *
 * @see HttpException
 * @see NetworkException
 * @see ParseException
 * */
public abstract class ApiException extends Exception {
  public ApiException(@NotNull @Nls(capitalization = Nls.Capitalization.Sentence) String message) {
    super(message);
  }

  public ApiException(@NotNull Throwable cause) {
    super(cause);
  }
}
