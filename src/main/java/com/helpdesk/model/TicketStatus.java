package com.helpdesk.model;

/**
 * Enum representing the lifecycle status of a support ticket.
 * Tickets flow through: OPEN -> IN_PROGRESS -> RESOLVED -> CLOSED.
 */
public enum TicketStatus {
    OPEN,
    IN_PROGRESS,
    RESOLVED,
    CLOSED
}
