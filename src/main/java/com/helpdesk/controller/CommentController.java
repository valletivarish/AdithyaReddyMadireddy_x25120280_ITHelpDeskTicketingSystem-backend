package com.helpdesk.controller;

import com.helpdesk.dto.CommentDTO;
import com.helpdesk.dto.CommentResponseDTO;
import com.helpdesk.model.User;
import com.helpdesk.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for comment management endpoints.
 * Provides CRUD operations for comments on tickets.
 * The authenticated user is automatically set as the comment author.
 */
@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * POST /api/comments - Create a new comment on a ticket.
     * The authenticated user is set as the comment author.
     */
    @PostMapping
    public ResponseEntity<CommentResponseDTO> createComment(
            @Valid @RequestBody CommentDTO dto,
            @AuthenticationPrincipal User user) {
        CommentResponseDTO response = commentService.createComment(dto, user.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /** GET /api/comments/ticket/{ticketId} - Get all comments for a ticket. */
    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<List<CommentResponseDTO>> getCommentsByTicket(@PathVariable Long ticketId) {
        return ResponseEntity.ok(commentService.getCommentsByTicket(ticketId));
    }

    /** GET /api/comments/{id} - Get a specific comment by ID. */
    @GetMapping("/{id}")
    public ResponseEntity<CommentResponseDTO> getCommentById(@PathVariable Long id) {
        return ResponseEntity.ok(commentService.getCommentById(id));
    }

    /** PUT /api/comments/{id} - Update an existing comment. */
    @PutMapping("/{id}")
    public ResponseEntity<CommentResponseDTO> updateComment(
            @PathVariable Long id,
            @Valid @RequestBody CommentDTO dto) {
        return ResponseEntity.ok(commentService.updateComment(id, dto));
    }

    /** DELETE /api/comments/{id} - Delete a comment. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }
}
