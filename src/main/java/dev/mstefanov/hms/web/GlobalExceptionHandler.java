package dev.mstefanov.hms.web;

import dev.mstefanov.hms.exception.ConflictException;
import dev.mstefanov.hms.exception.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Maps exceptions raised by the Thymeleaf controllers in this package to the HTML error pages under
 * {@code templates/error/}. Security exceptions are deliberately not handled here so that Spring Security's
 * {@code ExceptionTranslationFilter} keeps producing the login redirect / access-denied page.
 * REST controllers ({@code dev.mstefanov.hms.api}) have their own RFC 7807 handler.
 */
@ControllerAdvice(basePackages = "dev.mstefanov.hms.web")
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView notFound(NotFoundException ex, HttpServletRequest request) {
        return errorView(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class,
            IllegalArgumentException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView badRequest(Exception ex, HttpServletRequest request) {
        return errorView(HttpStatus.BAD_REQUEST, "The request could not be processed.", request);
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ModelAndView conflict(ConflictException ex, HttpServletRequest request) {
        return errorView(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView serverError(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception for {} {}", request.getMethod(), request.getRequestURI(), ex);
        return errorView(HttpStatus.INTERNAL_SERVER_ERROR, null, request);
    }

    private static ModelAndView errorView(HttpStatus status, String message, HttpServletRequest request) {
        String view = switch (status) {
            case NOT_FOUND -> "error/404";
            case FORBIDDEN -> "error/403";
            case INTERNAL_SERVER_ERROR -> "error/500";
            default -> "error/4xx";
        };
        ModelAndView mav = new ModelAndView(view, status);
        mav.addObject("status", status.value());
        mav.addObject("error", status.getReasonPhrase());
        mav.addObject("message", message);
        mav.addObject("path", request.getRequestURI());
        return mav;
    }
}
