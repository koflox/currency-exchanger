package com.koflox.currency_exchanger.domain.usecase

import com.koflox.currency_exchanger.data.repo.DataSourcePriority
import com.koflox.currency_exchanger.data.repo.RatesRepository
import com.koflox.currency_exchanger.domain.entity.Currency
import com.koflox.currency_exchanger.domain.entity.generateCurrency
import com.koflox.currency_exchanger.domain.entity.generateExchangeRates
import com.koflox.currency_exchanger.util.database.DatabaseException
import io.mockk.called
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalCoroutinesApi::class)
class GetRatesUseCaseImplTest {

    private lateinit var useCase: GetRatesUseCaseImpl
    private lateinit var ratesRepository: RatesRepository
    private lateinit var getCurrentTimeUseCase: GetCurrentTimeUseCase
    private val testDispatcher = UnconfinedTestDispatcher()

    private val usdCurrency = generateCurrency("USD")

    @Before
    fun setup() {
        ratesRepository = mockk()
        getCurrentTimeUseCase = mockk()
        useCase = GetRatesUseCaseImpl(
            ratesRepository = ratesRepository,
            getCurrentTimeUseCase = getCurrentTimeUseCase,
            dispatcherIo = testDispatcher,
        )
    }

    @After
    fun cleanup() {
        clearAllMocks()
        unmockkAll()
    }

    @Test
    fun `getBaseCurrency() always returns USD currency`() {
        val result = useCase.getBaseCurrency()

        assertEquals(Currency("USD"), result)
    }

    @Test
    fun `initDataUpdates requests updates every 30 minutes`() = runTest {
        coEvery { ratesRepository.getExchangeRates(DataSourcePriority.LOCAL) } throws DatabaseException.NoData()
        coEvery { ratesRepository.getExchangeRates(DataSourcePriority.REMOTE) } returns generateExchangeRates()

        val resultJob = launch {
            useCase.initDataUpdates()
        }
        advanceTimeBy(91.minutes)

        resultJob.cancel()
        coVerify(exactly = 3) {
            ratesRepository.getExchangeRates(DataSourcePriority.REMOTE)
        }
    }

    @Test
    fun `getExchangeRates() uses local priority when data is still up to date`() = runTest {
        val lastFetchTimeMillis = 0L
        coEvery { ratesRepository.getExchangeRates(any()) } returns generateExchangeRates(
            lastFetchTimeMillis = lastFetchTimeMillis,
        )
        val lastTimeBeforeUpdate = lastFetchTimeMillis + 30.minutes.inWholeMilliseconds - 1
        coEvery { getCurrentTimeUseCase.getCurrentTimeMillis() } returns lastTimeBeforeUpdate

        useCase.getExchangeRates()

        coVerify {
            ratesRepository.getExchangeRates(DataSourcePriority.LOCAL)
            getCurrentTimeUseCase.getCurrentTimeMillis()
        }
        coVerify(exactly = 0) {
            ratesRepository.getExchangeRates(DataSourcePriority.REMOTE)
        }
    }

    @Test
    fun `getExchangeRates() uses remote priority when data is missing`() = runTest {
        coEvery { ratesRepository.getExchangeRates(DataSourcePriority.LOCAL) } throws DatabaseException.NoData()
        coEvery { ratesRepository.getExchangeRates(DataSourcePriority.REMOTE) } returns generateExchangeRates()

        useCase.getExchangeRates()

        coVerify {
            ratesRepository.getExchangeRates(DataSourcePriority.LOCAL)
            ratesRepository.getExchangeRates(DataSourcePriority.REMOTE)
            getCurrentTimeUseCase wasNot called
        }
    }

    @Test
    fun `getExchangeRates() uses remote priority source when data is stale`() = runTest {
        val lastFetchTimeMillis = 0L
        coEvery { ratesRepository.getExchangeRates(any()) } returns generateExchangeRates(
            lastFetchTimeMillis = lastFetchTimeMillis,
        )
        val lastTimeBeforeUpdate = lastFetchTimeMillis + 30.minutes.inWholeMilliseconds + 1
        coEvery { getCurrentTimeUseCase.getCurrentTimeMillis() } returns lastTimeBeforeUpdate

        useCase.getExchangeRates()

        coVerify {
            ratesRepository.getExchangeRates(DataSourcePriority.LOCAL)
            ratesRepository.getExchangeRates(DataSourcePriority.REMOTE)
            getCurrentTimeUseCase.getCurrentTimeMillis()
        }
    }

    @Test
    fun `exchangeValue() fails when passed value is not a decimal`() = runTest {
        val stringValue = ""

        val result = useCase.exchangeValue(
            requestedCurrency = usdCurrency,
            value = stringValue,
        )

        assertTrue(result.isFailure)
        assert(result.exceptionOrNull()!! is ExchangeValueException.InvalidValue)
    }

    @Test
    fun `exchangeValue() always uses local priority`() = runTest {
        val usdRates = generateExchangeRates(
            base = usdCurrency,
            lastFetchTimeMillis = 0L,
            rates = mapOf()
        )
        coEvery { ratesRepository.getExchangeRates(any()) } returns usdRates

        val result = useCase.exchangeValue(
            requestedCurrency = usdCurrency,
            value = "100",
        )

        assertTrue(result.isSuccess)
        coVerify {
            ratesRepository.getExchangeRates(DataSourcePriority.LOCAL)
        }
    }

    @Test
    fun `exchangeValue() maps to the same amount when rates currency equals to requested`() = runTest {
        val value = "100"
        val usdRates = generateExchangeRates(
            base = usdCurrency,
            lastFetchTimeMillis = 0L,
            rates = mapOf(
                usdCurrency to BigDecimal.ONE,
            )
        )
        coEvery { ratesRepository.getExchangeRates(any()) } returns usdRates

        val result = useCase.exchangeValue(
            requestedCurrency = usdCurrency,
            value = value,
        )

        assertTrue(result.isSuccess)
        assertEquals("$value.00", result.getOrThrow().rates[usdCurrency].toString())
    }

    @Test
    fun `exchangeValue() maps amount correctly when base currency equals to requested`() = runTest {
        val value = 100
        val currencyEur = Currency("eur")
        val eurRate = 0.8.toBigDecimal()
        val currencyJpy = Currency("jpy")
        val jpyRate = 150.toBigDecimal()
        val usdRates = generateExchangeRates(
            base = usdCurrency,
            lastFetchTimeMillis = 0L,
            rates = mapOf(
                usdCurrency to BigDecimal.ONE,
                currencyEur to eurRate,
                currencyJpy to jpyRate,
            )
        )
        coEvery { ratesRepository.getExchangeRates(any()) } returns usdRates

        val result = useCase.exchangeValue(
            requestedCurrency = usdCurrency,
            value = value.toString(),
        )

        assertTrue(result.isSuccess)
        assertEquals("$value.00", result.getOrThrow().rates[usdCurrency].toString())
        assertEquals("80.00", result.getOrThrow().rates[currencyEur].toString())
        assertEquals("15000.00", result.getOrThrow().rates[currencyJpy].toString())
    }

    @Test
    fun `exchangeValue() maps amount correctly when base currency does not equals to requested`() = runTest {
        val value = 100
        val currencyEur = Currency("eur")
        val eurRate = 0.8.toBigDecimal()
        val currencyJpy = Currency("jpy")
        val jpyRate = 150.toBigDecimal()
        val usdRates = generateExchangeRates(
            base = usdCurrency,
            lastFetchTimeMillis = 0L,
            rates = mapOf(
                usdCurrency to BigDecimal.ONE,
                currencyEur to eurRate,
                currencyJpy to jpyRate,
            )
        )
        coEvery { ratesRepository.getExchangeRates(any()) } returns usdRates

        val result = useCase.exchangeValue(
            requestedCurrency = currencyEur,
            value = value.toString(),
        )

        assertTrue(result.isSuccess)
        assertEquals("125.00", result.getOrThrow().rates[usdCurrency].toString())
        assertEquals("$value.00", result.getOrThrow().rates[currencyEur].toString())
        assertEquals("18750.00", result.getOrThrow().rates[currencyJpy].toString())
    }

    @Test
    fun `exchangeValue() fails when requested currency is not present in rates pool and not equals to base`() = runTest {
        val currencyJpy = Currency("jpy")
        val currencyEur = Currency("eur")
        val usdRates = generateExchangeRates(
            base = usdCurrency,
            lastFetchTimeMillis = 0L,
            rates = mapOf(
                currencyJpy to 150.toBigDecimal(),
            ),
        )
        coEvery { ratesRepository.getExchangeRates(any()) } returns usdRates

        val result = useCase.exchangeValue(
            requestedCurrency = currencyEur,
            value = "100",
        )

        assertTrue(result.isFailure)
        assert(result.exceptionOrNull()!! is ExchangeValueException.MissingCurrency)
    }

    @Test
    fun `exchangeValue() does not fail when requested currency rates is zero`() = runTest {
        val value = 100
        val currencyEur = Currency("eur")
        val eurRate = BigDecimal.ZERO
        val currencyJpy = Currency("jpy")
        val jpyRate = 150.toBigDecimal()
        val usdRates = generateExchangeRates(
            base = usdCurrency,
            lastFetchTimeMillis = 0L,
            rates = mapOf(
                usdCurrency to BigDecimal.ONE,
                currencyEur to eurRate,
                currencyJpy to jpyRate,
            )
        )
        coEvery { ratesRepository.getExchangeRates(any()) } returns usdRates

        val result = useCase.exchangeValue(
            requestedCurrency = currencyEur,
            value = value.toString(),
        )

        assertTrue(result.isSuccess)
        assertEquals("0.00", result.getOrThrow().rates[usdCurrency].toString())
        assertEquals("$value.00", result.getOrThrow().rates[currencyEur].toString())
        assertEquals("0.00", result.getOrThrow().rates[currencyJpy].toString())
    }

}
