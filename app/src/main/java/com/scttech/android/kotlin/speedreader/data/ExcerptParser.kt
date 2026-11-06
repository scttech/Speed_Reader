package com.scttech.android.kotlin.speedreader.data

import com.scttech.android.kotlin.speedreader.model.Passage
import com.scttech.android.kotlin.speedreader.model.PassageKind
import com.scttech.android.kotlin.speedreader.model.Question
import com.scttech.android.kotlin.speedreader.model.ScanQuestion
import org.json.JSONObject

/** Parses one excerpt JSON file from the assets excerpts folder (produced by tools/build_excerpts.py). */
object ExcerptParser {
    fun parse(json: String): Passage {
        val o = JSONObject(json)
        val qs = o.getJSONArray("questions")
        val questions = (0 until qs.length()).map { i ->
            val q = qs.getJSONObject(i)
            val opts = q.getJSONArray("options")
            Question(
                text = q.getString("text"),
                options = (0 until opts.length()).map { opts.getString(it) },
                correctIndex = q.getInt("correctIndex"),
            )
        }
        val scan = o.optJSONArray("scan")
        val scanQuestions = (0 until (scan?.length() ?: 0)).map { i ->
            val q = scan!!.getJSONObject(i)
            val opts = q.getJSONArray("options")
            ScanQuestion(
                question = Question(
                    text = q.getString("text"),
                    options = (0 until opts.length()).map { opts.getString(it) },
                    correctIndex = q.getInt("correctIndex"),
                ),
                evidence = q.getString("evidence"),
            )
        }
        return Passage(
            id = o.getString("id"),
            title = o.getString("title"),
            text = o.getString("text"),
            questions = questions,
            bookTitle = o.optString("bookTitle").ifBlank { null },
            author = o.optString("author").ifBlank { null },
            kind = when (o.optString("kind")) {
                "fiction" -> PassageKind.FICTION
                "nonfiction" -> PassageKind.NONFICTION
                else -> null
            },
            scanQuestions = scanQuestions,
        )
    }
}
