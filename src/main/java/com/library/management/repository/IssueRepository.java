package com.library.management.repository;

import com.library.management.model.Issue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IssueRepository extends JpaRepository<Issue, Long> {

    List<Issue> findByUserId(Long userId);

    List<Issue> findByBookId(Long bookId);

    List<Issue> findByStatus(String status);

    Optional<Issue> findByUserIdAndBookIdAndStatus(Long userId, Long bookId, String status);
}
