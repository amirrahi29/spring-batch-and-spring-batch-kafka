package com.bqs.main.repositories;

import com.bqs.main.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientRepository extends JpaRepository<Patient,Long> {
    // For D/DY Dupe Check
    boolean existsByFirstNameAndLastNameAndZipCodeAndPhoneNumberAndSsn(
            String firstName, String lastName, String zip, String phone, String ssn);
    boolean existsByFirstNameAndLastNameAndZipCodeAndPhoneNumberAndDob(
            String firstName, String lastName, String zip, String phone, String dob);
    boolean existsByFirstNameAndLastNameAndZipCodeAndPhoneNumber(
            String firstName, String lastName, String zip, String phone);
    // For Existing Account Holder (future use)
    boolean existsBySsnEndingWithAndZipCodeAndFirstNameAndLastName(
            String last4ssn, String zip, String firstName, String lastName);
    boolean existsBySsnAndZipCodeAndFirstNameAndLastName(
            String fullSsn, String zip, String firstName, String lastName);
    boolean existsByFirstNameAndLastNameAndPhoneNumber(
            String firstName, String lastName, String phoneNumber);
}
