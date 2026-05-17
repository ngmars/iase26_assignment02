# Flaky Test Report

**Name:** Gopinath, Nitish

## Flaky Test 1

**Test name:** `de.seuhd.worldcup.WorldCupTest#evaluate returns zero when no bets are placed`

**Root cause:**
`BettingService` is shared by all tests. The tests removed the saved bets before each test, but they did not remove the saved result in `cachedResult`. So if another test ran first and created a result, this test could accidentally reuse that old result even though there were no bets anymore.

**Fix:**
I removed the cached result from `BettingService.evaluate`. Now it calculates the result from the current bets every time, so old results from earlier tests cannot affect this test.

## Flaky Test 2

**Test name:** `de.seuhd.worldcup.WorldCupTest#standings are stable when multiple teams tie on all criteria`

**Root cause:**
The standings used an `IdentityHashMap` to store teams. This map does not keep teams in a order. The sorting only compared points, goal difference, and goals scored. If two teams were equal in all of these values, there was no final rule to decide their order. Because of that, the tied teams could appear in different orders on different runs.

**Fix:**
I changed the map to a `LinkedHashMap`, which keeps the teams in the order they were added. I also added the original group order as the last tie-breaker. Now teams that are fully tied always appear in the same order.

## Flaky Test 3

**Test name:** `de.seuhd.worldcup.WorldCupTest#load json from network`

**Root cause:**
The test loaded JSON from real URLs. `JsonLoader.loadJsonFromNetwork` shuffled the URL list, and one of the URLs was not reachable. Sometimes the test tried the bad URL first and timed out or failed. Other times it tried a working URL first and passed. So the result depended on random URL order and network timing.

**Fix:**
I removed the random shuffle and made the loader try the provided URLs in a fixed order. If one URL fails, it catches the error and tries the next URL. The test still uses `JsonLoader.loadJsonFromNetwork()`, but it no longer depends on random URL order.

## Flaky Test 4

**Test name:** `de.seuhd.worldcup.FileBettingServiceTest#test file betting with threads`

**Root cause:**
Two threads wrote bets to the same file at the same time. `placeBet` first read the file, then changed the bets, then wrote the whole file again. Without locking, one thread could overwrite changes from the other thread. That meant some bets could disappear depending on the exact timing of the threads.

**Fix:**
I made `placeBet` and `getBets` synchronized. Now only one thread can read or write through the same service at a time, so the full read-write step finishes before another thread starts.

## Flaky Test 5

**Test name:** `de.seuhd.worldcup.FileBettingServiceTest#fresh service has no bets`

**Root cause:**
This test used the same temporary file as another test. If `save bets to the shared file` ran first, it wrote bets into that file. Then `fresh service has no bets` opened the same file and found those old bets, so it failed. If the fresh test ran first, it passed. This made the result depend on test order.

**Fix:**
I gave each test its own temporary file instead of sharing one file. The fresh-service test now starts with a file path that has no old bets, so another test cannot affect it.
