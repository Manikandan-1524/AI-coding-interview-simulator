package com.aisimulator.backend;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByUsernameOrderBySubmittedAtDesc(String username);
}
