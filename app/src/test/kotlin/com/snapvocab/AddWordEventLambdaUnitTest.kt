package com.snapvocab

import arrow.core.Option
import arrow.core.Some
import arrow.core.none
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.nio.file.Files
import java.nio.file.Path

const val REQUEST_BODY_WITH_WORD_AMAZING_FILENAME = "request_body_with_word_amazing.json"
const val WORD_AMAZING = "amazing"

class AddWordEventLambdaUnitTest {
    @Test
    fun givenAddNewWordRequestThenWordIsCreatedAndSuccessfulResponseIsReturned() {
        AddWordEventLambda(::findNewWordAmazingSpy, ::saveNewWordAmazingSpy).handler(
            addWordRequestWith(REQUEST_BODY_WITH_WORD_AMAZING_FILENAME)
        ).apply {
            Assertions.assertEquals(successfulResponse(), this)
        }
    }

    private fun addWordRequestWith(requestBodyFilename: String): ApiGatewayRequest {
        return resolve(requestBodyFilename).let {
            Files.readString(it)
        }.let {
            ApiGatewayRequest(it, emptyMap())
        }
    }

    private fun resolve(requestBodyFilename: String): Path =
        Path.of("src/test/resources").resolve(requestBodyFilename)

    private fun successfulResponse(): ApiGatewayResponse = ApiGatewayResponse(200)

    private fun findNewWordAmazingSpy(wordValue: String): Option<Word> {
        Assertions.assertEquals(WORD_AMAZING, wordValue)
        return none()
    }

    private fun saveNewWordAmazingSpy(word: Word): Word {
        Assertions.assertFalse(word.id.isBlank())
        Assertions.assertEquals(WORD_AMAZING, word.value)
        Assertions.assertEquals(1, word.nbOccurrences)
        return word
    }

    @ParameterizedTest
    @ValueSource(strings = ["request_body_empty_1.json", "request_body_empty_2.json", "request_body_with_no_word.json"])
    fun givenInvalidAddWordRequestThenInvalidRequestResponseIsReturned(invalidRequestBodyFilename: String) {
        AddWordEventLambda().handler(
            addWordRequestWith(invalidRequestBodyFilename)
        ).apply {
            Assertions.assertEquals(invalidRequestResponse(), this)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["request_body_with_empty_word_1.json", "request_body_with_empty_word_2.json"])
    fun givenAddEmptyWordRequestThenNoWordFoundResponseIsReturned(invalidRequestBodyFilename: String) {
        AddWordEventLambda().handler(
            addWordRequestWith(invalidRequestBodyFilename)
        ).apply {
            Assertions.assertEquals(noWordFoundResponse(), this)
        }
    }

    private fun invalidRequestResponse(): ApiGatewayResponse = ApiGatewayResponse(400, "Invalid request")

    private fun noWordFoundResponse(): ApiGatewayResponse = ApiGatewayResponse(400, "No word found")

    @Test
    fun givenAddExistingWordRequestThenWordNbOccurrencesIsIncrementedAndSuccessfulResponseIsReturned() {
        AddWordEventLambda(::findExistingWordAmazingSpy, ::saveExistingWordAmazingSpy).handler(
            addWordRequestWith(REQUEST_BODY_WITH_WORD_AMAZING_FILENAME)
        ).apply {
            Assertions.assertEquals(successfulResponse(), this)
        }
    }

    private fun findExistingWordAmazingSpy(wordValue: String): Option<Word> {
        Assertions.assertEquals(WORD_AMAZING, wordValue)
        return Some(Word("id", wordValue, 2))
    }

    private fun saveExistingWordAmazingSpy(word: Word): Word {
        Assertions.assertEquals(WORD_AMAZING, word.value)
        Assertions.assertEquals(3, word.nbOccurrences)
        return word
    }
}