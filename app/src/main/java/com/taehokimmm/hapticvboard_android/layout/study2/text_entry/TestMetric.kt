package com.taehokimmm.hapticvboard_android.layout.study2.text_entry


fun calculateTouchDuration(startTime: Long, endTime: Long): Long {
    return endTime - startTime
}


fun calculateWPM(startTime: Long, endTime: Long, sentence: String): Double {
    val wordCount = (sentence.length - 1) / 5
    val duration = (endTime - startTime) / 1000.0
    return (wordCount / duration) * 60
}

fun calculateAccuracy(typedText: String, referenceText: String): Double {
    val typedWords = typedText.split("\\s+".toRegex())
    val referenceWords = referenceText.split("\\s+".toRegex())
    val correctWords = typedWords.zip(referenceWords).count { it.first == it.second }
    return (correctWords.toDouble() / referenceWords.size) * 100
}

fun calculateIKI(timestamps: List<Long>): Double {
    val ikiList = mutableListOf<Long>()

    for (i in 1 until timestamps.size) {
        val iki = timestamps[i] - timestamps[i - 1]
        ikiList.add(iki)
    }

    return ikiList.average()
}

fun damerauLevenshteinDistance(str1: String, str2: String): Int {
    val lenStr1 = str1.length
    val lenStr2 = str2.length
    val dp = Array(lenStr1 + 1) { IntArray(lenStr2 + 1) }

    for (i in 0..lenStr1) {
        dp[i][0] = i
    }
    for (j in 0..lenStr2) {
        dp[0][j] = j
    }

    for (i in 1..lenStr1) {
        for (j in 1..lenStr2) {
            val cost = if (str1[i - 1] == str2[j - 1]) 0 else 1

            dp[i][j] = minOf(
                dp[i - 1][j] + 1,     // Deletion
                dp[i][j - 1] + 1,     // Insertion
                dp[i - 1][j - 1] + cost // Substitution
            )

            if (i > 1 && j > 1 && str1[i - 1] == str2[j - 2] && str1[i - 2] == str2[j - 1]) {
                dp[i][j] = minOf(
                    dp[i][j],
                    dp[i - 2][j - 2] + cost // Transposition
                )
            }
        }
    }
    return dp[lenStr1][lenStr2]
}

fun calculateUER(stimulus: String, enteredText: String): Double {
    val editDistance = damerauLevenshteinDistance(stimulus, enteredText)
    val maxLength = maxOf(stimulus.length, enteredText.length)
    return (editDistance.toDouble() / maxLength) * 100
}

fun keyboardEfficiency(inputText: String, keyStrokeNum: Int): Double {
    if (keyStrokeNum == 0) return (-1).toDouble()
    val charNum = inputText.length
    return charNum.toDouble() / keyStrokeNum.toDouble()
}

fun calculatePressDuration(pressDurations: List<Long>): Double {
    return pressDurations.average()
}