// "Specify type explicitly" "true"
package a

public fun <T> emptyList(): List<T> = null!!

public val <caret>l = emptyList<Int>()
/* IGNORE_K2 */

// FUS_QUICKFIX_NAME: org.jetbrains.kotlin.idea.intentions.SpecifyTypeExplicitlyIntention