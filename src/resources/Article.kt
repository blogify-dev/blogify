package me.benjozork.resources

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonProperty.Access.*

import me.benjozork.resources.models.Resource

import java.util.*

/**
 * A constant for now.
 */
const val ARTICLE_SUMMARY_LENGTH = 100

/**
 * Represents an Article [Resource].
 *
 * @property title     The title of the [Article].
 * @property createdAt The time of creation of the [Article], in `UNIX` timestamp format.
 * @property content   The [Content][Article.Content] of the article. Not included in the JSON serialization.

 */
data class Article (
    val title: String,
    val createdAt: Long = Date().time,
    @JsonProperty(access = WRITE_ONLY) val content: Content
) : Resource() {

    /**
     * Represents the content of an [Article].
     *
     * @property text    The text content of the article.
     * @property summary The summary of the content, obtained by taking the first [ARTICLE_SUMMARY_LENGTH] characters of [text]
     */
    data class Content(val text: String) {
        val summary = text.take(ARTICLE_SUMMARY_LENGTH)
    }

}