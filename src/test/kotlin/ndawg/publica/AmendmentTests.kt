package ndawg.publica

import ndawg.publica.raw.RawPublicaAPI
import ndawg.publica.types.Party
import ndawg.publica.types.State
import com.google.gson.Gson
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.whenever
import io.kotlintest.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.jupiter.api.Tag
import org.koin.standalone.get
import org.koin.test.declareMock
import java.io.File
import java.time.LocalDate

class AmendmentTests : PublicaAPITest() {
	
	@Test
	@Tag("mocked")
	fun `Mocked - HR1 Amendments`() {
		declareMock<RawPublicaAPI> {
			(0..2).forEach {
				whenever(getBillAmendments(any(), eq(116), eq("hr1"), eq(it * 20))).thenReturn(
					testData("amendments/hr1_amendments_$it.json")
				)
			}
		}
		
		val api = get<PublicaAPI>()
		
		runBlocking {
			val page1 = api.getAmendments("hr1", 116, 0)
			page1.size shouldBe 20
			page1.first().apply {
				this.number shouldBe "H.AMDT.60"
				this.slug shouldBe "hamdt60"
				this.sponsor.apply {
					this.title shouldBe "Rep."
					this.name shouldBe "Mary Gay Scanlon"
					this.id shouldBe "S001205"
					this.party shouldBe Party.D
					this.state shouldBe State.PENNSYLVANIA
				}
				this.introduced shouldBe LocalDate.of(2019, 3, 6)
				this.title shouldBe "An amendment numbered 7 printed in Part B of House Report 116-16 to request a study by the Federal Election Commission to specifically assess whether the small donor match cap and the six-to-one ratio in H.R.1 is appropriately scaled for both House and Senate elections."
				this.link shouldBe "https://www.congress.gov/amendment/116th-congress/house-amendment/60/text"
				this.lastActionDate shouldBe LocalDate.of(2019, 3, 6)
				this.lastAction shouldBe "On agreeing to the Scanlon amendment (A007) Agreed to by voice vote."
			}
			
			page1.last().apply {
				this.number shouldBe "H.AMDT.87"
				this.slug shouldBe "hamdt87"
				this.sponsor.apply {
					this.title shouldBe "Rep."
					this.name shouldBe "Mark Pocan"
					this.id shouldBe "P000607"
					this.party shouldBe Party.D
					this.state shouldBe State.WISCONSIN
				}
				this.introduced shouldBe LocalDate.of(2019, 3, 7)
				this.title shouldBe "An amendment numbered 37 printed in Part B of House Report 116-16 to end the practice of prison gerrymandering whereby incarcerated persons are counted in Census population counts as residents of correctional facilities and not their most recent residence prior to imprisonment."
				this.link shouldBe "https://www.congress.gov/amendment/116th-congress/house-amendment/87/text"
				this.lastActionDate shouldBe LocalDate.of(2019, 3, 7)
				this.lastAction shouldBe "On agreeing to the Pocan amendment (A034) Agreed to by voice vote."
			}
			
			val page2 = api.getAmendments("hr1", 116, 1)
			page2.size shouldBe 20
			page2.first().apply {
				this.number shouldBe "H.AMDT.86"
			}
			page2.last().apply {
				this.number shouldBe "H.AMDT.101"
			}
			
			val page3 = api.getAmendments("hr1", 116, 2)
			page3.size shouldBe 14
			page3.first().apply {
				this.number shouldBe "H.AMDT.100"
			}
			page3.last().apply {
				this.number shouldBe "H.AMDT.61"
			}
		}
	}
	
	@Test
	@Tag("raw")
	fun `Raw - HR1 Amendments`() {
		val (key, raw) = raw()
		runBlocking {
			raw.getBillAmendments(key, 116, "hr1", 0).await().results!![0].amendments.vetAll()
		}
	}
	
	//	@Test
	fun captureHr1Amendments() {
		val (key, raw) = raw()
		for (i in 0 until 3) {
			runBlocking {
				File("hr1_amendments_$i.json").writeText(get<Gson>().toJson(raw.getBillAmendments(key, 116, "hr1", i * 20).await()))
			}
		}
	}
	
}