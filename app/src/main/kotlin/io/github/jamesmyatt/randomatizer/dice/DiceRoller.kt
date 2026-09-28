package io.github.jamesmyatt.randomatizer.dice

/** Rolls dice using [random]. All roll randomness must go through this class. */
class DiceRoller(private val random: RandomSource) {
    fun roll(die: StandardDie): DieResult = DieResult(die, random.nextInt(die.sides) + 1)

    /** Rolls [dice] together, keeping their order. */
    fun roll(dice: List<StandardDie>): Roll {
        require(dice.isNotEmpty()) { "A roll needs at least one die" }
        return Roll(dice.map(::roll))
    }
}
