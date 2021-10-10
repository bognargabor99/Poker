package hu.bme.aut.onlab.poker.testutils

import kotlin.test.*

fun <T> assertNotContains(container: Iterable<T>, element: T) {
    assertFailsWith<AssertionError> {
        assertContains(container, element)
    }
}

fun <T> assertContentNotEquals(expected: Iterable<T>?, actual: Iterable<T>?) {
    assertFailsWith<AssertionError> {
        assertContentEquals(expected, actual)
    }
}