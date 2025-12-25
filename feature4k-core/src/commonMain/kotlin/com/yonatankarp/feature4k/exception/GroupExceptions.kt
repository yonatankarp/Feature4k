package com.yonatankarp.feature4k.exception

import com.yonatankarp.feature4k.exception.Feature4kException.GroupException

/**
 * Thrown when a requested feature group cannot be found.
 *
 * @property groupName The name of the group that was not found
 */
class GroupNotFoundException(
    val groupName: String,
    cause: Throwable? = null,
) : GroupException("Group not found: $groupName", cause)
