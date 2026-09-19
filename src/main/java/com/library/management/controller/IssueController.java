package com.library.management.controller;

import com.library.management.dto.IssueDTO;
import com.library.management.model.Book;
import com.library.management.model.Issue;
import com.library.management.model.User;
import com.library.management.repository.BookRepository;
import com.library.management.repository.UserRepository;
import com.library.management.service.IssueService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/issues")
public class IssueController {

    private final IssueService issueService;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public IssueController(IssueService issueService,
                           BookRepository bookRepository,
                           UserRepository userRepository) {
        this.issueService = issueService;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    // -------------------------------------------------------
    // Issue a book (called by a USER for themselves)
    // -------------------------------------------------------
    @PostMapping("/issue")
    public ResponseEntity<?> issueBook(
            @RequestParam Long userId,
            @RequestParam Long bookId) {

        Book book = bookRepository.findById(bookId).orElse(null);
        if (book == null) {
            return ResponseEntity.badRequest().body("Book not found with id: " + bookId);
        }
        if (book.getQuantity() <= 0) {
            return ResponseEntity.badRequest()
                    .body("Book '" + book.getTitle() + "' is currently not available.");
        }
        if (!userRepository.existsById(userId)) {
            return ResponseEntity.badRequest().body("User not found with id: " + userId);
        }

        try {
            Issue issued = issueService.issueBook(userId, bookId);
            if (issued == null) {
                return ResponseEntity.internalServerError()
                        .body("Failed to issue book. Please try again.");
            }
            return ResponseEntity.ok(issued);
        } catch (IllegalStateException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    // -------------------------------------------------------
    // All issues — ADMIN only (SecurityConfig enforces /admin/**)
    // -------------------------------------------------------
    @GetMapping
    public List<IssueDTO> getAllIssues() {
        return toIssueDTOList(issueService.getAllIssues());
    }

    // -------------------------------------------------------
    // Issues for a specific user
    // -------------------------------------------------------
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserIssues(
            @PathVariable Long userId,
            Authentication auth) {

        // A USER may only see their own issues; ADMIN can see anyone's
        boolean isAdmin = auth.getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

        if (!isAdmin) {
            User caller = userRepository.findByEmail(auth.getName()).orElse(null);
            if (caller == null || !caller.getId().equals(userId)) {
                return ResponseEntity.status(403)
                        .body("Access denied: you can only view your own issues.");
            }
        }

        return ResponseEntity.ok(toIssueDTOList(issueService.getUserIssues(userId)));
    }

    // -------------------------------------------------------
    // Return a book
    // -------------------------------------------------------
    @PutMapping("/return/{issueId}")
    public ResponseEntity<?> returnBook(
            @PathVariable Long issueId,
            Authentication auth) {

        Issue issue = issueService.getAllIssues().stream()
                .filter(i -> i.getId().equals(issueId))
                .findFirst().orElse(null);

        if (issue == null) {
            return ResponseEntity.notFound().build();
        }

        // A USER may only return their own book
        boolean isAdmin = auth.getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

        if (!isAdmin) {
            User caller = userRepository.findByEmail(auth.getName()).orElse(null);
            if (caller == null || !caller.getId().equals(issue.getUserId())) {
                return ResponseEntity.status(403)
                        .body("Access denied: you can only return your own books.");
            }
        }

        Issue returned = issueService.returnBook(issueId);
        if (returned == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(returned);
    }

    // -------------------------------------------------------
    // Helper
    // -------------------------------------------------------
    private List<IssueDTO> toIssueDTOList(List<Issue> issues) {
        List<IssueDTO> result = new ArrayList<>();
        for (Issue issue : issues) {
            Book book = bookRepository.findById(issue.getBookId()).orElse(null);
            User user = userRepository.findById(issue.getUserId()).orElse(null);
            String bookTitle = (book != null) ? book.getTitle() : "Unknown Book";
            String userName  = (user != null) ? user.getName()  : "Unknown User";
            result.add(new IssueDTO(
                    issue.getId(), issue.getUserId(), userName,
                    issue.getBookId(), bookTitle,
                    issue.getIssueDate(), issue.getDueDate(), issue.getReturnDate(),
                    issue.getStatus(), issue.getFine()));
        }
        return result;
    }
}
