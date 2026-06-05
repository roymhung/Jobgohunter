package vn.proy.jobgohunter.util.error;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import vn.proy.jobgohunter.domain.response.RestResponse;

@RestControllerAdvice
public class GlobalException {
    @ExceptionHandler(value = {IdInvalidException.class, UsernameNotFoundException.class,
            BadCredentialsException.class})

    public ResponseEntity<RestResponse<Object>> handleIdInvalidException(
            IdInvalidException idException) {

        RestResponse<Object> res = new RestResponse<Object>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setError(idException.getMessage());
        res.setMessage("Exception occurs....");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RestResponse<Object>> validationError(
            MethodArgumentNotValidException ex) {

        BindingResult bindingResult = ex.getBindingResult();
        final List<FieldError> fieldErrors = bindingResult.getFieldErrors();

        RestResponse<Object> res = new RestResponse<Object>();

        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setError(ex.getBody().getDetail());

        List<String> errorMessages =
                fieldErrors.stream().map(f -> f.getDefaultMessage()).collect(Collectors.toList());
        res.setMessage(errorMessages.size() > 1 ? errorMessages : errorMessages.get(0));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    @ExceptionHandler(value = {NoResourceFoundException.class})
    public ResponseEntity<RestResponse<Object>> handleNotFoundException(Exception ex) {

        RestResponse<Object> res = new RestResponse<Object>();
        res.setStatusCode(HttpStatus.NOT_FOUND.value());
        res.setError(ex.getMessage());
        res.setMessage("404 Not Found. URL may not exist....");

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
    }
}
