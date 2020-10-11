package ndawg.publica

import ndawg.publica.raw.PublicaAPIResponse
import ndawg.publica.raw.RawPublicaAPI
import ndawg.publica.raw.RawStatement
import ndawg.publica.types.Party
import ndawg.publica.types.State
import ndawg.publica.types.StatementSubject
import ndawg.util.from
import com.google.gson.Gson
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.whenever
import io.kotlintest.shouldBe
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.jupiter.api.Tag
import org.koin.standalone.get
import org.koin.test.declareMock
import java.io.File
import java.time.LocalDate

class StatementTests : PublicaAPITest() {
	
	@Test
	@Tag("mocked")
	fun page0() {
		declareMock<RawPublicaAPI> {
			whenever(getBillStatements(any(), eq(116), eq("hr1"), eq(0))).thenReturn(
				CompletableDeferred(get<Gson>().from<PublicaAPIResponse<List<RawStatement>>>(
					testFile("statements/hr1_0.json")))
			)
		}
		
		val api = get<PublicaAPI>()
		
		runBlocking {
			val res = api.getStatements(116, "hr1", 0)
			res.size shouldBe 20
			
			res[0].apply {
				url shouldBe "https://www.speaker.gov/newsroom/32019/"
				date shouldBe LocalDate.of(2019, 3, 20)
				title shouldBe "Pelosi Remarks at Press Event Calling for Passage of H.R. 6, the American Dream and Promise Act of 2019"
				type shouldBe "Press Release"
				subjects shouldBe emptyList<StatementSubject>()
				
				member.apply {
					name shouldBe "Nancy Pelosi"
					state shouldBe State.CALIFORNIA
					party shouldBe Party.D
					id shouldBe "P000197"
				}
			}
		}
	}
	
	@Test
	@Tag("mocked")
	fun hr1All() {
		val gson = get<Gson>()
		
		declareMock<RawPublicaAPI> {
			(0..20).forEach { page ->
				whenever(this.getBillStatements(any(), eq(116L), eq("hr1"), eq(page * 20))).thenReturn(
					testData("statements/hr1_$page.json")
				)
			}
		}
		
		val api = get<PublicaAPI>()
		
		runBlocking {
			val res = api.getStatements(116, "hr1")
			res.count() shouldBe 410
		}
	}
	
	@Test
	@Tag("exhaustive")
	fun live_allStatements() {
		// This test verifies that all Statement objects from the recent statements endpoint parses right
		val api = get<PublicaAPI>()
		
		runBlocking {
			api.getRecentStatements().take(500).toList().vetAll()
		}
	}
	
	@Test
	@Tag("exhaustive")
	fun live_byMember() {
		// TODO currently failing due to a lack of a custom response type
		// ie "name" is in the response object itself, not each result
		
		// This test verifies that all Statement objects from the recent statements endpoint parses right
		val api = get<PublicaAPI>()
		
		runBlocking {
			api.getStatementsByMember("S000033", 116, 0).vetAll()
		}
	}
	
	@Test
	@Tag("mocked")
	fun search() {
		declareMock<RawPublicaAPI> {
			whenever(searchStatements(any(), eq("climate change"), eq(0))).thenReturn(
				testData("statements/statement_search.json")
			)
		}
		
		val api = get<PublicaAPI>()
		
		runBlocking {
			val res = api.searchStatements("climate change", 0)
			res.size shouldBe 20
			
			res.first().apply {
				url shouldBe "https://www.speaker.gov/newsroom/113019"
				date shouldBe LocalDate.of(2019, 11, 30)
				title shouldBe "Pelosi Leads Bicameral Congressional Delegation to Madrid for COP25"
				type shouldBe "Press Release"
				
				member.apply {
					id shouldBe "P000197"
					name shouldBe "Nancy Pelosi"
					state shouldBe State.CALIFORNIA
					party shouldBe Party.D
				}
				
				subjects shouldBe emptyList<StatementSubject>()
			}
		}
	}
	
	@Test
	@Tag("mocked")
	fun subjects() {
		declareMock<RawPublicaAPI> {
			whenever(getStatementSubjects(any())).thenReturn(
				testData("statements/statement_subjects.json")
			)
		}
		
		val api = get<PublicaAPI>()
		
		runBlocking {
			val res = api.getStatementSubjects()
			res.size shouldBe 62
			
			res.first().apply {
				name shouldBe "2016 Election Russia Investigation"
				slug shouldBe "2016-election-russia-investigation"
			}
			
			res.last().apply {
				name shouldBe "Water"
				slug shouldBe "water"
			}
		}
	}
	
	//	@Test
	@Tag("mocked")
	fun byMember() {
		declareMock<RawPublicaAPI> {
			whenever(getStatementsByMember(any(), eq("S000033"), eq(116L), eq(0))).thenReturn(
				testData("statements/statements_sanders.json")
			)
		}
		
		val api = get<PublicaAPI>()
		
		runBlocking {
			val res = api.getStatementsByMember("S000033", 0)
			res.size shouldBe 20
			
			res.first().apply {
				url shouldBe "http://www.sanders.senate.gov/newsroom/video-audio/sanders-and-khanna-partner-to-end-yemen-war"
				date shouldBe LocalDate.of(2019, 11, 27)
				title shouldBe "Sanders and Khanna Partner to End Yemen War"
				type shouldBe "Press Release"
				
				member.apply {
					id shouldBe "S000033"
					state shouldBe State.VERMONT
					party shouldBe Party.I
				}
				
				subjects shouldBe emptyList<StatementSubject>()
			}
		}
	}
	
	//	@Test
	fun captureByMember() {
		val (key, raw, gson) = raw()
		runBlocking {
			val res = raw.getStatementsByMember(key, "S000033", 116, 0).await()
			res.results ?: throw IllegalStateException("No results returned")
			res.results!!.map {
				PublicaPipeline().transform(it.copy(member_id = "S000033"))
			}
//			File("statements_sanders.json").writeText(gson.toJson(res))
		}
	}
	
	//	@Test
	fun captureSubjects() {
		val (key, raw, gson) = raw()
		runBlocking {
			val res = raw.getStatementSubjects(key).await()
			File("statement_subjects.json").writeText(gson.toJson(res))
		}
	}
	
	//	@Test
	fun captureSearch() {
		val (key, raw, gson) = raw()
		runBlocking {
			val res = raw.searchStatements(key, "climate change", 0).await()
			File("statement_search.json").writeText(gson.toJson(res))
		}
	}
	
}