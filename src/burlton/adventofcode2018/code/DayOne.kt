package burlton.adventofcode2018.code

import java.io.File

fun main(args: Array<String>)
{
    calculateTotalFrequency()
    calculateFirstRepeatedFrequency()
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