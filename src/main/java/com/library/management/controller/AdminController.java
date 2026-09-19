package com.library.management.controller;

import com.library.management.dto.IssueDTO;
import com.library.management.dto.UserResponse;
import com.library.management.model.Book;
import com.library.management.model.Issue;
import com.library.management.model.User;
import com.library.management.repository.BookRepository;
import com.library.management.repository.UserRepository;
import com.library.management.service.IssueService;
import com.library.management.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final IssueService issueService;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public AdminController(UserService userService,
                           IssueService issueService,
                           BookRepository bookRepository,
                           UserRepository userRepository) {
        this.userService = userService;
        this.issueService = issueService;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    // -------------------------------------------------------
    // Users
    // -------------------------------------------------------

    @GetMapping("/users")
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers().stream()
                .map(u -> new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getRole()))
                .toList();
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        if (userService.getUserById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully");
    }

    // -------------------------------------------------------
    // Overdue issues
    // -------------------------------------------------------

    @GetMapping("/issues/overdue")
    public List<IssueDTO> getOverdueIssues() {
        List<Issue> issues = issueService.getOverdueIssues();
        return toIssueDTOList(issues);
    }

    // -------------------------------------------------------
    // Admin issues a book to a specific user
    // -------------------------------------------------------

    @PostMapping("/issues/issue")
    public ResponseEntity<?> issueBookToUser(
            @RequestParam Long userId,
            @RequestParam Long bookId) {

        Book book = bookRepository.findById(bookId).orElse(null);
        if (book == null) {
            return ResponseEntity.badRequest().body("Book not found.");
        }
        if (book.getQuantity() <= 0) {
            return ResponseEntity.badRequest()
                    .body("Book '" + book.getTitle() + "' is not available.");
        }
        if (!userRepository.existsById(userId)) {
            return ResponseEntity.badRequest().body("User not found.");
        }

        try {
            Issue issued = issueService.issueBook(userId, bookId);
            if (issued == null) {
                return ResponseEntity.internalServerError().body("Failed to issue book.");
            }
            return ResponseEntity.ok(issued);
        } catch (IllegalStateException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
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
