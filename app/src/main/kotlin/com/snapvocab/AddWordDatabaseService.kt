package com.snapvocab

import arrow.core.Option
import arrow.core.Some

data class Word(val id: String, val value: String, val nbOccurrences: Int)

fun findWordInDatabase(wordValue: String): Option<Word> = Some(Word("1344", wordValue, 2))

fun saveWordInDatabase(word: Word): Word = word
