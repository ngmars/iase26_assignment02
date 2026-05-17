package de.seuhd.worldcup

import kotlin.io.path.createTempFile
import kotlin.test.Test
import kotlin.test.assertEquals

/** Tests for [FileBettingService]. */
class FileBettingServiceTest {

    @Test
    fun `test file betting with threads`() {
        val file = createTempFile("bets", ".txt").toFile()
        val service = FileBettingService(file)

        val thread1 = Thread {
            repeat(50) { i -> service.placeBet(Bet(i, Prediction.HOME_WIN)) }
        }
        val thread2 = Thread {
            repeat(50) { i -> service.placeBet(Bet(i + 50, Prediction.AWAY_WIN)) }
        }

        thread1.start()
        thread2.start()
        thread1.join()
        thread2.join()

        // Each thread placed 50 unique bets, so 100 total are expected.
        assertEquals(100, service.getBets().size)

        file.delete()
    }

    @Test
    fun `save bets to the shared file`() {
        val file = createTempFile("bets", ".txt").toFile()
        val service = FileBettingService(file)
        service.placeBet(Bet(1, Prediction.HOME_WIN))
        service.placeBet(Bet(2, Prediction.DRAW))
        service.placeBet(Bet(3, Prediction.AWAY_WIN))
        val bets = service.getBets()
        assertEquals(3, bets.size)

        file.delete()
    }

    @Test
    fun `fresh service has no bets`() {
        val file = createTempFile("bets", ".txt").toFile()
        file.delete()
        val service = FileBettingService(file)
        assertEquals(0, service.getBets().size)
    }
}
