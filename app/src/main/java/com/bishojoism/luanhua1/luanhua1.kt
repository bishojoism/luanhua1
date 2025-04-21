package com.bishojoism.luanhua1

import kotlin.random.Random

private const val N = 120

fun encrypt(seed: String): String {
    return generate(shuffle(seed))
}

fun decrypt(seed: String): String {
    val array = shuffle(seed)
    return generate(IntArray(N).also { for (i in 0 until N) it[array[i]] = i })
}

private fun generate(map: IntArray): String {
    val filter = StringBuilder()
    for (i in 1..N) filter.append("[0]crop=iw/${N}:ih:iw/${N}*${map[i - 1]}:0[${i}];")
    for (i in 1..N) filter.append("[${i}]")
    filter.append("hstack=inputs=${N}")
    return filter.toString()
}

private fun shuffle(seed: String): IntArray {
    return IntArray(N) { it }.apply { shuffle(Random(seed.hashCode())) }
}