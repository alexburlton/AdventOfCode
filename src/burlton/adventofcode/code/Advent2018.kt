package burlton.adventofcode.code

import java.awt.Point
import java.io.File
import java.sql.Timestamp
import kotlin.streams.toList

fun main(args: Array<String>)
{
    calculateTotalFrequency()
    calculateFirstRepeatedFrequency()

    calculateBoxesCheckSum()
    findMatchingBoxes()

    findOverlappingTiles()
    findNonOverlappingClaim()

    chooseSleepiestGuard()

    chainReaction()
    findImprovedPolymer()

    findLargestFiniteArea()
    findPointsWithinTotalDistance()
}

private fun readLocations() : List<Point>
{
    val lines = readFile("6. Coordinates")

    return lines.stream()
                .map{ s -> Point(s.split(", ")[0].toInt(), s.split(", ")[1].toInt())}
                .toList()
}

private fun findPointsWithinTotalDistance()
{
    val locList = readLocations()

    //Work out a "worst case" range to go through and check
    val sumX = locList.stream().mapToInt{pt -> pt.x}.sum().toDouble()
    val sumY = locList.stream().mapToInt{pt -> pt.y}.sum().toDouble()

    //The *biggest* X value we need to check is one where
    val maxX = Math.floor((10000 + sumX)/50).toInt()
    val minX = Math.ceil((sumX - 10000)/50).toInt()

    val maxY = Math.floor((10000 + sumY)/50).toInt()
    val minY = Math.ceil((sumY - 10000)/50).toInt()

    var area = 0
    for (x in minX until maxX)
    {
        for (y in minY until maxY)
        {
            if (isWithinArea(Point(x, y), locList))
            {
                area++
            }
        }
    }

    println("6B: Total area of points within 10,000 = $area")
}
private fun isWithinArea(pt : Point, locList: List<Point>) : Boolean
{
    val sum = locList.stream().mapToInt{it -> computeManhattenDistance(pt, it)}.sum()
    return sum < 10000
}

private fun findLargestFiniteArea()
{
    val locList = readLocations()

    //Points with infinite areas will be those at the extreme(s). Find out what those are.
    val minX = locList.sortedBy{it.x}.first().x
    val maxX = locList.sortedBy{it.x}.last().x

    val minY = locList.sortedBy{it.y}.first().y
    val maxY = locList.sortedBy{it.y}.last().y

    var locToArea = mutableMapOf<Point, Int>()
    for (x in minX until maxX)
    {
        for (y in minY until maxY)
        {
            val loc = computeNearestLoc(Point(x, y), locList)
            if (loc != null)
            {
                val previousArea = locToArea.getOrDefault(loc, 0)
                locToArea[loc] = previousArea + 1
            }
        }
    }

    locToArea = locToArea.filter{it.key.x != minX
                    && it.key.x != maxX
                    && it.key.y != minY
                    && it.key.y != maxY}.toMutableMap()

    val entries = locToArea.entries.sortedBy{it.value}
    val biggestEntry = entries.last()
    println("6A: Largest area is ${biggestEntry.value} for location ${biggestEntry.key}")

}
private fun computeNearestLoc(pt : Point, locList : List<Point>) : Point?
{
    var minDist = Int.MAX_VALUE
    var minDistLoc : Point? = null
    for (loc in locList)
    {
        val dist = computeManhattenDistance(pt, loc)
        if (dist < minDist)
        {
            minDist = dist
            minDistLoc = loc
        }
        else if (dist == minDist)
        {
            minDistLoc = null
        }
    }

    return minDistLoc
}
private fun computeManhattenDistance(p1 : Point, p2 : Point) : Int
{
    return Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y)
}


private fun findImprovedPolymer()
{
    val polymer = readFile("5. Polymer").first()

    var bestLength = Integer.MAX_VALUE
    var bestLetterToRemove = ""
    val letterArray = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray()
    for (l in letterArray)
    {
        var newPolymer = polymer.replace("$l", "", true)
        newPolymer = doChainReaction(newPolymer)

        if (newPolymer.length < bestLength) {
            bestLength = newPolymer.length
            bestLetterToRemove = l.toString()
        }
    }

    println("5B: By removing $bestLetterToRemove, size becomes $bestLength")

}

private fun chainReaction()
{
    var polymer = readFile("5. Polymer").first()

    polymer = doChainReaction(polymer)

    println("5A: Final size = ${polymer.length} [$polymer]")
}
private fun doChainReaction(polymer : String) : String
{
    var newPolymer = polymer

    var lastSize : Int
    var currentSize = polymer.length
    do
    {
        lastSize = currentSize
        newPolymer = doChainReactionStep(newPolymer)
        currentSize = newPolymer.length
    }
    while (currentSize < lastSize)

    return newPolymer
}
private fun doChainReactionStep(polymer: String) : String
{
    var newPolymer = polymer
    val letterArray = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray()
    for (l in letterArray)
    {
        val lowerCaseL = l.toLowerCase()

        newPolymer = newPolymer.replace("$l$lowerCaseL", "")
        newPolymer = newPolymer.replace("$lowerCaseL$l", "")
    }

    return newPolymer
}

private fun chooseSleepiestGuard()
{
    val lines = readFile("4. Guard data")

    //Wrap them up as GuardObservation objects, and sort them by date
    val observations = lines.stream().map { s -> GuardObservation(s) }.toList()
    val observationsSorted = observations.sortedBy { it.dt }

    //Get map of {Guard Id -> Total time slept}. Whilst doing this, fill in GuardId on every GuardObservation for later
    val mapOfGuardSleepTimes = getMapOfGuardIdToNanosAsleep(observationsSorted)

    //Get the last element as the "sleepiest guard"
    val guardEntries = mapOfGuardSleepTimes.entries.sortedBy{ it.value }
    val sleepyGuardId = guardEntries.last().key

    //Now need to figure out which minute he's asleep for most often.

    val sleepGuardObservations = observationsSorted.filter{ it -> it.guardId == sleepyGuardId}
    val hmMinuteToCount = getMostFrequentlyAsleepGuardAndMinute(sleepGuardObservations)

    var guardAndMinute = hmMinuteToCount.entries.sortedBy{it.value}.last().key.split("_")

    println("4A: Guard #${guardAndMinute[0]}, minute ${guardAndMinute[1]} = ${guardAndMinute[0].toInt() * guardAndMinute[1].toInt()}")

    //Method 2 - same as above, but don't filter to the single guard
    val hmGuardAndMinuteToCount = getMostFrequentlyAsleepGuardAndMinute(observationsSorted)
    guardAndMinute = hmGuardAndMinuteToCount.entries.sortedBy{it.value}.last().key.split("_")

    println("4B: Guard #${guardAndMinute[0]}, minute ${guardAndMinute[1]} = ${guardAndMinute[0].toInt() * guardAndMinute[1].toInt()}")
}

private fun getMostFrequentlyAsleepGuardAndMinute(observations : List<GuardObservation>) : Map<String, Int>
{
    val hmGuardAndMinuteToCount = mutableMapOf<String, Int>()

    var sleepTime : Timestamp? = null

    observations.forEach{
        if (it.action == "falls asleep")
        {
            sleepTime = it.dt
        }
        else if (it.action == "wakes up")
        {
            val startMin = getMinutes(sleepTime!!)
            val endMin = getMinutes(it.dt)

            for (x in startMin until endMin)
            {
                val key = "${it.guardId}_$x"
                val current = hmGuardAndMinuteToCount.getOrDefault(key, 0)
                hmGuardAndMinuteToCount[key] = current+1
            }
        }
    }

    return hmGuardAndMinuteToCount
}


private fun getMinutes(dt : Timestamp) : Int
{
    val dtParts = dt.toString().split(" ")

    return dtParts[1].split(":")[1].toInt()
}


private fun getMapOfGuardIdToNanosAsleep(observationsSorted : List<GuardObservation>) : Map<Int, Long>
{
    val guardToSleepTimeNanos = mutableMapOf<Int, Long>()
    var guardId = -1
    var asleepTime = Timestamp(System.currentTimeMillis())

    observationsSorted.forEach{
        if (it.guardId > -1)
        {
            guardId = it.guardId
        }
        else
        {
            it.guardId = guardId
        }

        if (it.action == "falls asleep")
        {
            asleepTime = it.dt
        }
        else if (it.action == "wakes up")
        {
            val nanosAsleep = it.dt.time - asleepTime.time

            val currentNanos = guardToSleepTimeNanos.getOrDefault(guardId, 0)
            guardToSleepTimeNanos[guardId] = currentNanos + nanosAsleep
        }
    }

    return guardToSleepTimeNanos
}


private fun findOverlappingTiles()
{
    val hmSquareToClaims = getClaimsMap()

    val filteredMap = hmSquareToClaims.filterKeys {pt -> hmSquareToClaims[pt]!! > 1}
    println("3A: Number of overlapping square inches = ${filteredMap.size}")
}

private fun findNonOverlappingClaim()
{
    val hmSquareToClaims = getClaimsMap()

    val claimList = readFile("3. Fabric Claims")
    for (claimStr in claimList)
    {
        val claim = Claim(claimStr)

        if (isNonOverlappingClaim(claim, hmSquareToClaims))
        {
            println("3B: Non-overlapping claim is #${claim.claimId}")
        }
    }
}

private fun isNonOverlappingClaim(claim : Claim, hmPointToClaimCount : Map<Point, Int>) : Boolean
{
    val claimPoints = claim.getClaimPoints()
    for (pt in claimPoints)
    {
        if (hmPointToClaimCount[pt]!! > 1)
        {
            return false
        }
    }

    return true
}

private fun getClaimsMap() : Map<Point, Int>
{
    val claimList = readFile("3. Fabric Claims")

    val hmSquareToClaims = mutableMapOf<Point, Int>()
    for (claimStr in claimList)
    {
        val claim = Claim(claimStr)

        val claimPoints = claim.getClaimPoints()
        for (pt in claimPoints)
        {
            val current = hmSquareToClaims.getOrDefault(pt, 0)
            hmSquareToClaims[pt] = current+1
        }
    }

    return hmSquareToClaims
}

private fun findMatchingBoxes()
{
    val boxList = readFile("2. Boxes")

    for ((ix1, box) in boxList.withIndex())
    {
        for ((ix2, box2) in boxList.withIndex())
        {
            if (ix1 > ix2) {
                checkMatchingBoxes(box, box2)
            }
        }
    }
}

private fun checkMatchingBoxes(box1 : String, box2 : String)
{
    var countMatching = 0
    var mismatchIx = 0
    box1.forEachIndexed{index, letter ->

        if (box2[index].equals(letter)) {
            countMatching++
        }
        else
        {
            mismatchIx = index
        }
    }

    if (countMatching == box1.length - 1)
    {
        val finalbox1 = box1.removeRange(mismatchIx, mismatchIx+1)
        val finalbox2 = box2.removeRange(mismatchIx, mismatchIx+1)

        println("2B: $finalbox1 = $finalbox2 : ${finalbox1 == finalbox2}")
    }
}

private fun calculateBoxesCheckSum()
{
    val boxList = readFile("2. Boxes")

    val boxesWithTwoCharsRepeated = boxList.filter {it -> hasRepeatedLetters(it, 2) }
    val boxesWithThreeCharsRepeated = boxList.filter{it -> hasRepeatedLetters(it, 3) }

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

private fun readFile(filename : String) : MutableList<String>
{
    val boxList = mutableListOf<String>()

    File(filename).useLines{ lines -> lines.forEach{boxList.add(it)}}

    return boxList
}

private class GuardObservation(observationStr : String)
{
    val dt : Timestamp
    var guardId = -1
    var action = ""

    init
    {
        val endOfTimeStamp = observationStr.indexOf("]")
        val dtStr = observationStr.substring(1, endOfTimeStamp) + ":00"

        dt = Timestamp.valueOf(dtStr)
        val info = observationStr.substring(endOfTimeStamp+2)

        val idIndex = info.indexOf("#")
        if (idIndex > -1)
        {
            val nextSpaceIndex = info.indexOf(" ", idIndex)
            guardId = info.substring(idIndex+1, nextSpaceIndex).toInt()
            action = "Change of guard"
        }
        else
        {
            action = info
        }



    }

}

private class Claim(claimStr : String)
{
    var startPt : Point
    var width : Int
    var height : Int
    var claimId : Int

    init
    {
        val parts = claimStr.split(" ")

        val startCoords = parts[2].removeSuffix(":")
        val coordParts = startCoords.split(",")

        startPt = Point(coordParts[0].toInt(), coordParts[1].toInt())

        val dimensions = parts[3].split("x")
        width = dimensions[0].toInt()
        height = dimensions[1].toInt()

        claimId = parts[0].removePrefix("#").toInt()
    }

    fun getClaimPoints() : List<Point>
    {
        val list = mutableListOf<Point>()
        for (x in 0 until width)
        {
            for (y in 0 until height)
            {
                val pt = Point(startPt.x + x, startPt.y + y)
                list.add(pt)
            }
        }

        return list
    }
}
