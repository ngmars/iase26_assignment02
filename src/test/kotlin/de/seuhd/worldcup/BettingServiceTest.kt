package de.seuhd.worldcup

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BettingServiceTest {

    private fun match(id: Int, home: String, away: String, hs: Int?, aws: Int?) =
        Match(
            matchId = id,
            round = "Matchday 1",
            date = "2026-06-01",
            homeTeam = home,
            awayTeam = away,
            homeScore = hs,
            awayScore = aws,
            ground = "Test Stadium"
        )

    @BeforeTest
    fun resetBets() {
        BettingService.clear()
    }

    // ── evaluateBonus ──────────────────────────────────────────────────────────

    @Test
    fun `evaluateBonus awards 3 points for an exact score prediction`() {
        BettingService.placeBet(Bet(1, Prediction.HOME_WIN, predictedHomeScore = 2, predictedAwayScore = 0))

        val bonus = BettingService.evaluateBonus(listOf(match(1, "NITISH", "GOPINATH", 2, 0)))

        assertEquals(3, bonus)
    }

    @Test
    fun `evaluateBonus awards 1 point for correct outcome without exact score`() {
        BettingService.placeBet(Bet(1, Prediction.HOME_WIN, predictedHomeScore = 3, predictedAwayScore = 1))

        val bonus = BettingService.evaluateBonus(listOf(match(1, "NITISH", "GOPINATH", 2, 0)))

        assertEquals(1, bonus)
    }

    @Test
    fun `evaluateBonus awards 0 points for a wrong prediction`() {
        BettingService.placeBet(Bet(1, Prediction.AWAY_WIN, predictedHomeScore = 0, predictedAwayScore = 1))

        val bonus = BettingService.evaluateBonus(listOf(match(1, "NITISH", "GOPINATH", 2, 0)))

        assertEquals(0, bonus)
    }

    @Test
    fun `evaluateBonus ignores unplayed matches`() {
        BettingService.placeBet(Bet(1, Prediction.HOME_WIN, predictedHomeScore = 2, predictedAwayScore = 0))

        val bonus = BettingService.evaluateBonus(listOf(match(1, "NITISH", "GOPINATH", null, null)))

        assertEquals(0, bonus)
    }

    // ── removeBet ─────────────────────────────────────────────────────────────

    @Test
    fun `removeBet removes an existing bet so it no longer affects evaluation`() {
        BettingService.placeBet(Bet(1, Prediction.HOME_WIN))
        BettingService.removeBet(1)

        val result = BettingService.evaluate(listOf(match(1, "NITISH", "GOPINATH", 2, 0)))

        assertEquals(0, result.evaluated)
    }

    @Test
    fun `removeBet does nothing when no bet exists for that matchId`() {
        BettingService.placeBet(Bet(1, Prediction.HOME_WIN))
        BettingService.removeBet(2)

        val result = BettingService.evaluate(listOf(match(1, "NITISH", "GOPINATH", 2, 0)))

        assertEquals(1, result.correct)
    }

    // ── changeBet ─────────────────────────────────────────────────────────────

    @Test
    fun `changeBet updates the prediction for an existing bet`() {
        BettingService.placeBet(Bet(1, Prediction.HOME_WIN))
        BettingService.changeBet(Bet(1, Prediction.AWAY_WIN))

        val result = BettingService.evaluate(listOf(match(1, "NITISH", "GOPINATH", 0, 2)))

        assertEquals(1, result.correct)
    }

    @Test
    fun `changeBet throws when no bet exists for that matchId`() {
        assertFailsWith<IllegalArgumentException> {
            BettingService.changeBet(Bet(1, Prediction.HOME_WIN))
        }
    }
}