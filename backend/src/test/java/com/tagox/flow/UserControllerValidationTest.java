package com.tagox.flow;

import com.tagox.flow.dto.user.CreateUserRequest;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserControllerValidationTest {

    private final Validator validator =
            Validation.buildDefaultValidatorFactory()
                    .getValidator();

    @Test
    void deveRejeitarUsuarioSemNome() {

        CreateUserRequest request = new CreateUserRequest();

        request.setEmail("teste@email.com");
        request.setSenha("hash");

        Set<ConstraintViolation<CreateUserRequest>> violations =
                validator.validate(request);

        assertFalse(violations.isEmpty());

        assertTrue(
                violations.stream()
                        .anyMatch(
                                v -> v.getPropertyPath()
                                        .toString()
                                        .equals("nome")
                        )
        );
    }

    @Test
    void deveRejeitarEmailInvalido() {

        CreateUserRequest request = new CreateUserRequest();

        request.setNome("João");
        request.setEmail("email-invalido");
        request.setSenha("hash");

        Set<ConstraintViolation<CreateUserRequest>> violations =
                validator.validate(request);

        assertFalse(violations.isEmpty());

        assertTrue(
                violations.stream()
                        .anyMatch(
                                v -> v.getPropertyPath()
                                        .toString()
                                        .equals("email")
                        )
        );
    }

    @Test
    void deveRejeitarUsuarioSemSenha() {

        CreateUserRequest request = new CreateUserRequest();

        request.setNome("João");
        request.setEmail("joao@email.com");

        Set<ConstraintViolation<CreateUserRequest>> violations =
                validator.validate(request);

        assertFalse(violations.isEmpty());

        assertTrue(
                violations.stream()
                        .anyMatch(
                                v -> v.getPropertyPath()
                                        .toString()
                                        .equals("senha")
                        )
        );
    }
}