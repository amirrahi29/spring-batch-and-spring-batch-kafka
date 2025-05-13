package com.bqs.main.repositories;

import com.bqs.main.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientRepository extends JpaRepository<Patient,Long> {
    boolean existsByEmailAndMerchantNumber(String email, String merchantNumber);
}
