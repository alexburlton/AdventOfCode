package burlton.adventofcode2018.code

import java.io.File

fun main(args: Array<String>)
{
    calculateTotalFrequency()
    calculateFirstRepeatedFrequency()

    calculateBoxesCheckSum()
}

private fun calculateBoxesCheckSum()
{
    val boxList = readBoxes()

    val boxesWithTwoCharsRepeated = boxList.filter {it -> hasRepeatedLetters(it, 2)}
    val boxesWithThreeCharsRepeated = boxList.filter{it -> hasRepeatedLetters(it, 3)}

    val checksum = boxesWithThreeCharsRepeated.size * boxesWithTwoCharsRepeated.size
    println("2A: $checksum")
}

private fun hasRepeatedLetters(s : String, repeatsRequired : Int) : Boolean
{
    val charMap = mutableMapOf<Char, Int>()
    s.forEach { incrementValue(charMap, it) }

    return charMap.values.contains(repeatsRequired)
}
private fun <K> incrementValue(map : MutableMap<K, Int>, key : K)
{
    var current = map.getOrPut(key, {0})
    current++
    map[key] = current
}

private fun calculateFirstRepeatedFrequency()
{
    val frequencies = readFrequencies()

    var foundDuplicate = false
    var initialTotal = 0
    val totals = mutableSetOf<Int>()
    while (!foundDuplicate)
    {
        for (freq in frequencies)
        {
            initialTotal += freq
            if (totals.contains(initialTotal))
            {
                foundDuplicate = true
                break

            }

            totals.add(initialTotal)
        }
    }

    println("1B: $initialTotal")
}

private fun calculateTotalFrequency()
{
    val frequencyList = readFrequencies()

    var frequencySum = 0
    frequencyList.forEach{frequencySum += it}

    println("1A: $frequencySum")
}

private fun readFrequencies() : MutableList<Int>
{
    val frequencyList = mutableListOf<Int>()

    File("Frequencies.txt").useLines { lines -> lines.forEach{frequencyList.add(Integer.parseInt(it))}}

    return frequencyList
}

private fun readBoxes() : MutableList<String>
{
    val boxList = mutableListOf<String>()

    File("2. Boxes").useLines{ lines -> lines.forEach{boxList.add(it)}}

    return boxList
}