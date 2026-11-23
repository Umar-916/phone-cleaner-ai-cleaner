package com.phonecleaner.aicleaner.data.media

import android.content.ContentResolver
import android.net.Uri
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.security.MessageDigest

/**
 * Content hash used to decide whether two same-size files are identical. Because a wrong "identical" verdict leads to deleting
 * a file the user wanted, small and medium files are hashed completely; only very large files are sampled, and then at many spread points.
 */
object FileHasher {
    private const val CHUNK = 1024 * 1024
    private const val FULL_HASH_LIMIT = 256L * 1024 * 1024
    private const val SAMPLES = 16

    fun sha256(resolver: ContentResolver, uri: Uri, sizeBytes: Long): String? = runCatching {
        val digest = MessageDigest.getInstance("SHA-256")
        resolver.openFileDescriptor(uri, "r")?.use { pfd ->
            FileInputStream(pfd.fileDescriptor).use { stream ->
                val channel = stream.channel
                val buffer = ByteBuffer.allocate(CHUNK)
                if (sizeBytes <= FULL_HASH_LIMIT) {
                    channel.position(0)
                    while (true) {
                        buffer.clear()
                        val read = readFully(channel, buffer)
                        if (read <= 0) break
                        digest.update(buffer.array(), 0, read)
                    }
                } else {
                    val span = sizeBytes - CHUNK
                    for (i in 0 until SAMPLES) {
                        buffer.clear()
                        channel.position(span * i / (SAMPLES - 1))
                        val read = readFully(channel, buffer)
                        if (read > 0) digest.update(buffer.array(), 0, read)
                    }
                }
            }
        } ?: return null
        digest.digest().joinToString("") { "%02x".format(it) }
    }.getOrNull()

    /** FileChannel.read may return fewer bytes than requested; keep reading until the buffer is full or EOF. */
    private fun readFully(channel: FileChannel, buffer: ByteBuffer): Int {
        var total = 0
        while (buffer.hasRemaining()) {
            val n = channel.read(buffer)
            if (n <= 0) break
            total += n
        }
        return total
    }
}
