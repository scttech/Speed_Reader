package com.scttech.android.kotlin.speedreader.data

import android.content.Context
import com.scttech.android.kotlin.speedreader.model.Passage
import com.scttech.android.kotlin.speedreader.model.ScanTask
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

interface PassageRepository {
    /** Passage for the n-th baseline attempt; rotates through the library so re-tests differ. */
    fun baselinePassage(attemptIndex: Int): Passage

    /** Excerpt for the n-th practice session; the asset order alternates fiction / non-fiction. */
    fun practicePassage(attemptIndex: Int): Passage

    /** Excerpt, one of its fact-hunt questions and the sentence holding the answer, for the n-th skim-and-scan round. */
    fun scanTask(attemptIndex: Int): ScanTask
}

/** Wraps around the list so any (even negative) index maps to an element. */
fun <T> rotate(items: List<T>, index: Int): T = items[Math.floorMod(index, items.size)]

@Singleton
class LibraryPassageRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : PassageRepository {

    private val excerpts: List<Passage> by lazy {
        val dir = "excerpts"
        context.assets.list(dir).orEmpty().filter { it.endsWith(".json") }.sorted().map { name ->
            context.assets.open("$dir/$name").bufferedReader().use { ExcerptParser.parse(it.readText()) }
        }
    }

    override fun baselinePassage(attemptIndex: Int): Passage = rotate(PassageLibrary.baseline, attemptIndex)

    override fun practicePassage(attemptIndex: Int): Passage = rotate(excerpts, attemptIndex)

    override fun scanTask(attemptIndex: Int): ScanTask {
        // Stride 7 walks every excerpt before repeating and keeps scan out of step with practice; each time
        // an excerpt comes round again it serves its next scan question.
        val passage = rotate(excerpts, Math.floorMod(attemptIndex, excerpts.size) * 7 + 3)
        val scan = rotate(passage.scanQuestions, attemptIndex / excerpts.size)
        return ScanTask(passage, scan.question, scan.evidence)
    }
}
