package com.almahir.iti.repository;

import com.almahir.iti.model.ForgotPassword;
import org.springframework.data.repository.CrudRepository;
import java.util.Optional;

public interface ForgotPasswordRepository extends CrudRepository<ForgotPassword, String> {
    Optional<ForgotPassword> findByEmail(String email);
}