package com.snapvocab

import arrow.core.Option
import arrow.core.Some
import arrow.core.none
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.nio.file.Files
import java.nio.file.Path

const val ADD_WORD_AMAZING_REQUEST_FILENAME = "add_word_amazing_request.json"
const val WORD_AMAZING = "amazing"

class AddWordEventLambdaUnitTest {
    @Test
    fun givenAddNewWordRequestThenWordIsCreatedAndSuccessfulResponseIsReturned() {
        AddWordEventLambda(::findNewWordAmazingSpy, ::saveNewWordAmazingSpy).handler(
            addWordRequestFrom(ADD_WORD_AMAZING_REQUEST_FILENAME)
        ).apply {
            Assertions.assertEquals(successfulResponse(), this)
        }
    }

    private fun addWordRequestFrom(requestFilename: String): ApiGatewayRequest {
        return resolve(requestFilename).let {
            Files.readString(it)
        }.let {
            parse(it)
        }
    }

    private fun parse(requestAsJson: String): ApiGatewayRequest {
        val objectMapper = jacksonObjectMapper().configure(
            DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
            false
        )
        return objectMapper.readValue(requestAsJson)
    }

    private fun resolve(requestBodyFilename: String): Path =
        Path.of("src/test/resources").resolve(requestBodyFilename)

    private fun successfulResponse(): ApiGatewayResponse = ApiGatewayResponse(200)

    private fun findNewWordAmazingSpy(wordValue: String): Option<Word> {
        Assertions.assertEquals(WORD_AMAZING, wordValue)
        return none()
    }

    private fun saveNewWordAmazingSpy(word: Word) {
        Assertions.assertFalse(word.id.isBlank())
        Assertions.assertEquals(WORD_AMAZING, word.value)
        Assertions.assertEquals(1, word.nbOccurrences)
    }

    @ParameterizedTest
    @ValueSource(strings = ["empty_add_word_request.json", "add_no_word_request.json"])
    fun givenInvalidAddWordRequestThenInvalidRequestResponseIsReturned(invalidRequestFilename: String) {
        AddWordEventLambda().handler(
            addWordRequestFrom(invalidRequestFilename)
        ).apply {
            Assertions.assertEquals(invalidRequestResponse(), this)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["add_empty_word_request_1.json", "add_empty_word_request_2.json"])
    fun givenAddEmptyWordRequestThenNoWordFoundResponseIsReturned(emptyWordRequestFilename: String) {
        AddWordEventLambda().handler(
            addWordRequestFrom(emptyWordRequestFilename)
        ).apply {
            Assertions.assertEquals(noWordFoundResponse(), this)
        }
    }

    private fun invalidRequestResponse(): ApiGatewayResponse = ApiGatewayResponse(400, "Invalid request")

    private fun noWordFoundResponse(): ApiGatewayResponse = ApiGatewayResponse(400, "No word found")

    @Test
    fun givenAddExistingWordRequestThenWordNbOccurrencesIsIncrementedAndSuccessfulResponseIsReturned() {
        AddWordEventLambda(::findExistingWordAmazingSpy, ::saveExistingWordAmazingSpy).handler(
            addWordRequestFrom(ADD_WORD_AMAZING_REQUEST_FILENAME)
        ).apply {
            Assertions.assertEquals(successfulResponse(), this)
        }
    }

    private fun findExistingWordAmazingSpy(wordValue: String): Option<Word> {
        Assertions.assertEquals(WORD_AMAZING, wordValue)
        return Some(Word("id", wordValue, 2))
    }

    private fun saveExistingWordAmazingSpy(word: Word) {
        Assertions.assertEquals(WORD_AMAZING, word.value)
        Assertions.assertEquals(3, word.nbOccurrences)
    }
}