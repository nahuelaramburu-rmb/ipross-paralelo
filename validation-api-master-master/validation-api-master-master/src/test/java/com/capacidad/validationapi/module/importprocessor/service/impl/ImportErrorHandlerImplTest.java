package com.capacidad.validationapi.module.importprocessor.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.exception.GlobalExceptionHandler;
import com.capacidad.validationapi.misc.LocaleHandler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ImportErrorHandlerImplTest {

    @Mock
    private LocaleHandler localeHandler;

    @Mock
    private GlobalExceptionHandler exceptionHandler;

    @InjectMocks
    private ImportErrorHandlerImpl importErrorHandler;

    @Test
    public void testHandleImportErrorReturnsDefaultWhenUnexpectedError() {
        when(localeHandler.getLocaleMessage("import.genericErrorMessage")).thenReturn(Optional.empty());

        String result = importErrorHandler.handleImportError(new IOException(""));

        assertThat(result).isEqualTo("Error");
        verify(exceptionHandler, never()).resolveErrorMessage(any(), any());
    }

    @Test
    public void testHandleImportErrorReturnsMessageWhenObjectNotValidException() {
        ObjectNotValidException exception = new ObjectNotValidException("");
        String expectedMessage = "message";

        when(localeHandler.getLocaleMessage("import.genericErrorMessage")).thenReturn(Optional.empty());
        when(exceptionHandler.resolveErrorMessage(exception, null)).thenReturn(expectedMessage);

        String result = importErrorHandler.handleImportError(exception);

        assertThat(result).isEqualTo(expectedMessage);
        verify(exceptionHandler, times(1)).resolveErrorMessage(any(), any());
    }

    @Test
    public void testHandleImportErrorReturnsMessageWhenObjectNotFoundException() {
        ObjectNotFoundException exception = new ObjectNotFoundException("");
        String expectedMessage = "message";

        when(localeHandler.getLocaleMessage("import.genericErrorMessage")).thenReturn(Optional.empty());
        when(exceptionHandler.resolveErrorMessage(exception, null)).thenReturn(expectedMessage);

        String result = importErrorHandler.handleImportError(exception);

        assertThat(result).isEqualTo(expectedMessage);
        verify(exceptionHandler, times(1)).resolveErrorMessage(any(), any());
    }

}
