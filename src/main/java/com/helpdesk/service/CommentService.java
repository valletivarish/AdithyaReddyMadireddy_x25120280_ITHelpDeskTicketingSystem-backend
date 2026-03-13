package com.helpdesk.service;

import com.helpdesk.dto.CommentDTO;
import com.helpdesk.dto.CommentResponseDTO;
import com.helpdesk.exception.ResourceNotFoundException;
import com.helpdesk.model.Comment;
import com.helpdesk.model.Ticket;
import com.helpdesk.model.User;
import com.helpdesk.repository.CommentRepository;
import com.helpdesk.repository.TicketRepository;
import com.helpdesk.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for comment management on tickets.
 * Handles creating, reading, updating, and deleting comments
 * that track communication between users and agents on tickets.
 */
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    public CommentService(CommentRepository commentRepository,
                          TicketRepository ticketRepository,
                          UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
    }

    /**
     * Creates a new comment on a ticket by the specified user.
     * Validates that both the ticket and user exist.
     */
    @Transactional
    public CommentResponseDTO createComment(CommentDTO dto, Long userId) {
        Ticket ticket = ticketRepository.findById(dto.getTicketId())
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", dto.getTicketId()));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Comment comment = Comment.builder()
                .content(dto.getContent())
                .ticket(ticket)
                .user(user)
                .build();

        Comment saved = commentRepository.save(comment);
        return mapToResponseDTO(saved);
    }

    /** Retrieves all comments for a specific ticket, ordered by creation time. */
    @Transactional(readOnly = true)
    public List<CommentResponseDTO> getCommentsByTicket(Long ticketId) {
        return commentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /** Retrieves a single comment by ID. */
    @Transactional(readOnly = true)
    public CommentResponseDTO getCommentById(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", id));
        return mapToResponseDTO(comment);
    }

    /** Updates an existing comment's content. */
    @Transactional
    public CommentResponseDTO updateComment(Long id, CommentDTO dto) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", id));
        comment.setContent(dto.getContent());
        Comment updated = commentRepository.save(comment);
        return mapToResponseDTO(updated);
    }

    /** Deletes a comment by ID. */
    @Transactional
    public void deleteComment(Long id) {
        if (!commentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Comment", id);
        }
        commentRepository.deleteById(id);
    }

    /** Maps Comment entity to CommentResponseDTO. */
    private CommentResponseDTO mapToResponseDTO(Comment comment) {
        return CommentResponseDTO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .ticketId(comment.getTicket().getId())
                .userId(comment.getUser().getId())
                .userName(comment.getUser().getFullName())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
