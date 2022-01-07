package io.nozemi.discordnews.helpers

import java.util.*

fun <T> List<T>.toLinkedList() = LinkedList<T>(this)