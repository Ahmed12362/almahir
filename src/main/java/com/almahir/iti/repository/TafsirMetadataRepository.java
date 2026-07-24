package com.almahir.iti.repository;

import com.almahir.iti.model.TafsirMetadata;
import com.almahir.iti.model.enums.TafsirBuildStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TafsirMetadataRepository extends JpaRepository<TafsirMetadata, UUID> {

    List<TafsirMetadata> findByStatus(TafsirBuildStatus status);

    Optional<TafsirMetadata> findByTafsirKeyAndLanguage(String tafsirKey, String language);
}