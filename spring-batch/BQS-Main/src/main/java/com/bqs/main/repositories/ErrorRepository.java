package com.bqs.main.repositories;

import com.bqs.main.model.PatientError;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ErrorRepository extends JpaRepository<PatientError,Long> {
}
