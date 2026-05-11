package org.example.notifier.infrastructure.external

data class EmailTemplate(
    val to: String,
    val subject: String,
    val textContent: String,
    val htmlContent: String? = null,
    val from: String,
    val cc: List<String> = emptyList(),
    val bcc: List<String> = emptyList(),
    val attachments: List<EmailAttachment> = emptyList()
) {
    fun hasHtmlContent(): Boolean = !htmlContent.isNullOrBlank()

    fun getEffectiveContent(): String = htmlContent ?: textContent

    fun hasAttachments(): Boolean = attachments.isNotEmpty()
}

data class EmailAttachment(
    val filename: String,
    val content: ByteArray,
    val contentType: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EmailAttachment

        if (filename != other.filename) return false
        if (!content.contentEquals(other.content)) return false
        if (contentType != other.contentType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = filename.hashCode()
        result = 31 * result + content.contentHashCode()
        result = 31 * result + contentType.hashCode()
        return result
    }
}
