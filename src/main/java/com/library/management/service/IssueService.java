package com.library.management.service;

import com.library.management.model.Book;
import com.library.management.model.Issue;
import com.library.management.repository.BookRepository;
import com.library.management.repository.IssueRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class IssueService {

    private final IssueRepository issueRepository;
    private final BookRepository bookRepository;

    private static final double FINE_PER_DAY = 10.0;

    public IssueService(IssueRepository issueRepository,
                        BookRepository bookRepository) {
        this.issueRepository = issueRepository;
        this.bookRepository = bookRepository;
    }

    @Transactional
    public Issue issueBook(Long userId, Long bookId) {

        Book book = bookRepository.findById(bookId).orElse(null);
        if (book == null || book.getQuantity() <= 0) {
            return null;
        }

        // Prevent duplicate active issue for the same user + book
        boolean alreadyIssued = issueRepository
                .findByUserIdAndBookIdAndStatus(userId, bookId, "ISSUED")
                .isPresent();
        if (alreadyIssued) {
            throw new IllegalStateException(
                "You already have an active issue for this book.");
        }

        book.setQuantity(book.getQuantity() - 1);
        bookRepository.save(book);

        Issue issue = new Issue();
        issue.setUserId(userId);
        issue.setBookId(bookId);
        issue.setIssueDate(LocalDate.now());
        issue.setDueDate(LocalDate.now().plusDays(7));
        issue.setStatus("ISSUED");
        issue.setFine(0);

        return issueRepository.save(issue);
    }

    @Transactional(readOnly = true)
    public List<Issue> getAllIssues() {
        return issueRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Issue> getUserIssues(Long userId) {
        return issueRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<Issue> getOverdueIssues() {
        return issueRepository.findByStatus("ISSUED").stream()
                .filter(i -> LocalDate.now().isAfter(i.getDueDate()))
                .toList();
    }

    @Transactional
    public Issue returnBook(Long issueId) {

        Issue issue = issueRepository.findById(issueId).orElse(null);
        if (issue == null) {
            return null;
        }

        if ("RETURNED".equals(issue.getStatus())) {
            return issue;
        }

        Book book = bookRepository.findById(issue.getBookId()).orElse(null);
        if (book != null) {
            book.setQuantity(book.getQuantity() + 1);
            bookRepository.save(book);
        }

        LocalDate returnDate = LocalDate.now();
        issue.setReturnDate(returnDate);
        issue.setStatus("RETURNED");

        if (returnDate.isAfter(issue.getDueDate())) {
            long lateDays = ChronoUnit.DAYS.between(issue.getDueDate(), returnDate);
            issue.setFine(lateDays * FINE_PER_DAY);
        } else {
            issue.setFine(0);
        }

        return issueRepository.save(issue);
    }
}
