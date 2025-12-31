package com.yonatankarp.feature4k.audit

/**
 * Common audit metadata fixtures used across test modules.
 *
 * @author Yonatan Karp-Rudin
 */
object AuditFixtures {
    /**
     * Admin user identifier for audit events.
     */
    const val ADMIN_USER = "admin"

    /**
     * Regular user identifier for audit events.
     */
    const val REGULAR_USER = "user1"

    /**
     * Web API source identifier for audit events.
     */
    const val WEB_API_SOURCE = "WEB_API"

    /**
     * Localhost host identifier for audit events.
     */
    const val LOCALHOST = "localhost"
}
