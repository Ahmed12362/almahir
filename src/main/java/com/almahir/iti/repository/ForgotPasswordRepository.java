package com.almahir.iti.repository;

import com.almahir.iti.model.ForgotPassword;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ForgotPasswordRepository extends CrudRepository<ForgotPassword, String> {
    Optional<ForgotPassword> findByEmail(String email);
}