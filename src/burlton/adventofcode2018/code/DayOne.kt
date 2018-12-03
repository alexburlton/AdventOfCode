package burlton.adventofcode2018.code

import java.io.File

fun main(args: Array<String>)
{
    val lineList = mutableListOf<String>()


    File("Frequencies.txt").useLines { lines -> lines.forEach{lineList.add(it)}}

    var frequencySum = 0
    lineList.forEach{frequencySum += Integer.parseInt(it)}

    println(frequencySum)
}