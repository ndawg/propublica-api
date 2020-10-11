package ndawg.publica

import ndawg.publica.raw.PublicaAPIResponse
import ndawg.publica.raw.PublicaError
import ndawg.publica.raw.RawPublicaAPI
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.whenever
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.jupiter.api.Tag
import org.koin.standalone.get
import org.koin.test.declareMock

class ErrorTests : PublicaAPITest() {
	
	@Test
	@Tag("mocked")
	fun error() {
		declareMock<RawPublicaAPI> {
			whenever(this.getBillDetails(any(), eq(1L), eq("h1"))).thenReturn(
				CompletableDeferred(PublicaAPIResponse("ERROR", "---", emptyList(), "Record not found"))
			)
		}
		
		// Ensure that a PublicaError is directly thrown
		runBlocking {
			shouldThrow<PublicaError> {
				get<PublicaAPI>().getBillDetails("h1", 1)
			}.message shouldBe "Record not found"
		}
	}
	
}