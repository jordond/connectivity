package dev.jordond.connectivity

/**
 * Marks the annotated element as internal to the connectivity module.
 *
 * This API is not intended to be used outside of the connectivity module.
 */
@Target(
    allowedTargets = [
        AnnotationTarget.CLASS,
        AnnotationTarget.PROPERTY,
        AnnotationTarget.FUNCTION,
        AnnotationTarget.TYPEALIAS,
    ],
)
@RequiresOptIn(level = RequiresOptIn.Level.ERROR)
public annotation class InternalConnectivityApi
