package com.withlambda.ktor2.server.bind.config.auth

/**
 * Returns an [AuthorizationPredicate] that evaluates to `true` only when both this predicate
 * and [other] return `true` for the given roles and call.
 *
 * @param other The second predicate that must also pass.
 * @return A combined predicate requiring both conditions to hold.
 */
infix fun AuthorizationPredicate.and(other: AuthorizationPredicate): AuthorizationPredicate =
    { roles, call -> this(roles, call) && other(roles, call) }

/**
 * Returns an [AuthorizationPredicate] that evaluates to `true` when at least one of this predicate
 * or [other] returns `true` for the given roles and call.
 *
 * @param other The alternative predicate that may pass instead.
 * @return A combined predicate requiring at least one condition to hold.
 */
infix fun AuthorizationPredicate.or(other: AuthorizationPredicate): AuthorizationPredicate =
    { roles, call -> this(roles, call) || other(roles, call) }

/**
 * Returns an [AuthorizationPredicate] that evaluates to the logical negation of this predicate.
 *
 * @return A predicate that returns `true` when this predicate returns `false`, and vice versa.
 */
fun AuthorizationPredicate.not(): AuthorizationPredicate =
    { roles, call -> !this(roles, call) }
