package dev.jordond.connectivity

import dev.drewhamilton.poko.Poko
import io.ktor.client.statement.HttpResponse

/**
 * Represents the result of a poll operation.
 */
public sealed interface PollResult {

    /**
     * Represents a successful poll result.
     *
     * @property response The [HttpResponse] received from the poll operation.
     */
    @Poko
    public class Response(public val response: HttpResponse) : PollResult

    /**
     * Represents an error that occurred during the poll operation.
     *
     * @property throwable The [Throwable] that represents the error.
     */
    @Poko
    public class Error(public val throwable: Throwable) : PollResult
}