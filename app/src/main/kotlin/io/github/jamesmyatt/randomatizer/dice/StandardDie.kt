package io.github.jamesmyatt.randomatizer.dice

/** A standard polyhedral die numbered `1..sides`. */
enum class StandardDie(val sides: Int) {
    D4(4),
    D6(6),
    D8(8),
    D10(10),
    D12(12),
    D20(20),
    D100(100),
    ;

    /** Conventional short name, e.g. "d20". */
    val label: String get() = "d$sides"
}
