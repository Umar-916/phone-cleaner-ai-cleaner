package com.phonecleaner.aicleaner.domain.model

import android.net.Uri
import androidx.annotation.StringRes
import com.phonecleaner.aicleaner.R

enum class FileCategory(@StringRes val label: Int) {
    IMAGES(R.string.files_images),
    VIDEO(R.string.files_video),
    AUDIO(R.string.files_audio),
    DOCUMENTS(R.string.files_documents),
    APK(R.string.files_apk),
    DOWNLOAD(R.string.files_download)
}

enum class DocumentType(@StringRes val label: Int, val extensions: Set<String>) {
    ALL(R.string.files_doc_all, emptySet()),
    PDF(R.string.files_doc_pdf, setOf("pdf")),
    WORD(R.string.files_doc_word, setOf("doc", "docx", "odt", "rtf", "txt")),
    EXCEL(R.string.files_doc_excel, setOf("xls", "xlsx", "csv", "ods")),
    PPT(R.string.files_doc_ppt, setOf("ppt", "pptx", "odp"))
}

data class FileEntry(
    val uri: Uri,
    val id: Long,
    val path: String,
    val name: String,
    val sizeBytes: Long,
    val modifiedMs: Long,
    val mime: String,
    val category: FileCategory
) {
    val extension: String get() = name.substringAfterLast('.', "").lowercase()
    val folderName: String
        get() {
            val parent = path.substringBeforeLast('/', "")
            return if (parent.endsWith("/emulated/0")) "" else parent.substringAfterLast('/')
        }
    val isMedia: Boolean get() = category == FileCategory.IMAGES || category == FileCategory.VIDEO
}

data class CategorySummary(val count: Int = 0, val bytes: Long = 0L)

data class BrowserItem(
    val path: String,
    val name: String,
    val isDirectory: Boolean,
    val sizeBytes: Long,
    val modifiedMs: Long,
    val childCount: Int
)
