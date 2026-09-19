package com.library.management.controller;

import com.library.management.model.Book;
import com.library.management.service.BookService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    // ADMIN only through SecurityConfig
    @PostMapping
    public ResponseEntity<Book> addBook(@RequestBody Book book) {
        return ResponseEntity.ok(bookService.addBook(book));
    }

    // Any authenticated user
    @GetMapping
    public List<Book> getAllBooks() {
        return bookService.getAllBooks();
    }

    // Any authenticated user
    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        return bookService.getBookById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ADMIN only through SecurityConfig
    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(
            @PathVariable Long id,
            @RequestBody Book book) {

        return bookService.getBookById(id)
                .map(existing -> {
                    existing.setTitle(book.getTitle());
                    existing.setAuthor(book.getAuthor());
                    existing.setIsbn(book.getIsbn());
                    existing.setQuantity(book.getQuantity());

                    return ResponseEntity.ok(
                            bookService.addBook(existing)
                    );
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ADMIN only through SecurityConfig
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBook(@PathVariable Long id) {

        if (bookService.getBookById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        bookService.deleteBook(id);

        return ResponseEntity.ok("Book deleted successfully");
    }
}