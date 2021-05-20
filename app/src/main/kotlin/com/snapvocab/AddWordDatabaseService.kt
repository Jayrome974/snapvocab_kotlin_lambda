package com.snapvocab

import arrow.core.Option
import arrow.core.Some
import arrow.core.none
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient
import software.amazon.awssdk.enhanced.dynamodb.Expression
import software.amazon.awssdk.enhanced.dynamodb.TableSchema
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

@DynamoDbBean
data class Word(
    @get:DynamoDbPartitionKey var id: String = "",
    var value: String = "",
    var nbOccurrences: Int = 0
)

val wordsTable: DynamoDbAsyncTable<Word> =
    DynamoDbEnhancedAsyncClient
        .create()
        .table(
            System.getenv("WORDS_TABLE_NAME"),
            TableSchema.fromBean(Word::class.java)
        )

fun findWordInDatabase(wordValue: String): Option<Word> {
    // TODO handle exception
    var foundWord: Option<Word> = none()

    wordsTable
        .scan { it.filterExpression(expressionValueEquals(wordValue)) }
        .items()
        .limit(1)
        .subscribe {
            foundWord = Some(it)
        }.join()

    return foundWord
}

private fun expressionValueEquals(wordValue: String): Expression =
    Expression
        .builder()
        .expression("#a = :b")
        .putExpressionName("#a", "value")
        .putExpressionValue(":b", AttributeValue.builder().s(wordValue).build())
        .build()

fun saveWordInDatabase(word: Word) {
    // TODO handle exception
    wordsTable.putItem(word).join()
}
