package ndawg.publica

import ndawg.publica.raw.RawPublicaAPI
import ndawg.publica.types.*
import ndawg.util.VoteGoal
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.whenever
import io.kotlintest.should
import io.kotlintest.shouldBe
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Tag
import org.koin.standalone.StandAloneContext.stopKoin
import org.koin.standalone.get
import org.koin.test.declareMock
import java.time.LocalDate
import java.time.LocalTime

class RecentVoteTests : PublicaAPITest() {
	
	@Before
	override fun before() {
		super.before()
		
		declareMock<RawPublicaAPI> {
			whenever(getRecentVotes(any(), eq("both"), eq(0))).thenReturn(
				testData("recent_votes.json")
			)
		}
	}
	
	// Specifically tests a recent occurrence of a bill vote
	@Test
	@Tag("mocked")
	fun recentBillVote() {
		val api = get<PublicaAPI>()
		
		runBlocking {
			val res = api.getRecentVotes("both", 0)
			res.size shouldBe 20
			res.first().apply {
				this as LegislativeVote
				
				meta.apply {
					congress shouldBe 116L
					chamber shouldBe Chamber.Senate
					session shouldBe 1L
					rollCall shouldBe 49L
					source shouldBe "https://www.senate.gov/legislative/LIS/roll_call_votes/vote1161/vote_116_1_00049.xml"
				}
				
				bill should { it != null }
				bill!!.apply {
					this.meta.apply {
						slug shouldBe "hjres46"
						congress shouldBe 116L
						name shouldBe "H.J.RES.46"
						type shouldBe "hjres"
						id shouldBe "hjres46-116"
					}
				}
				
				amendment shouldBe null
				nomination shouldBe null
				
				question shouldBe "On the Joint Resolution"
				description shouldBe "A joint resolution relating to a national emergency declared by the President " +
					"on February 15, 2019."
				goal shouldBe VoteGoal.PASS
				type shouldBe VoteType.ONE_HALF
				date shouldBe LocalDate.of(2019, 3, 14)
				time shouldBe LocalTime.of(14, 24, 0)
				
				result shouldBe "Joint Resolution Passed"
				passed shouldBe true
				
				votes[Party.D] should { it != null }
				votes[Party.D]?.let {
					it[VotePosition.Yes] shouldBe 45L
					it[VotePosition.No] shouldBe 0L
					it[VotePosition.Abstain] shouldBe 0L
					it.majority shouldBe MajorityPosition.Yes
				}
				
				votes[Party.R] should { it != null }
				votes[Party.R]?.let {
					it[VotePosition.Yes] shouldBe 12L
					it[VotePosition.No] shouldBe 41L
					it[VotePosition.Abstain] shouldBe 0L
					it.majority shouldBe MajorityPosition.No
				}
				
				votes[Party.I] should { it != null }
				votes[Party.I]?.let {
					it[VotePosition.Yes] shouldBe 2L
					it[VotePosition.No] shouldBe 0L
					it[VotePosition.Abstain] shouldBe 0L
				}
				
				votes[null] should { it != null }
				votes[null]?.let {
					it[VotePosition.Yes] shouldBe 59L
					it[VotePosition.No] shouldBe 41L
					it[VotePosition.Abstain] shouldBe 0L
				}
			}
		}
	}
	
	// Specifically tests a recent occurrence of an amendment vote
	@Test
	@Tag("mocked")
	fun recentAmendmentVote() {
		val api = get<PublicaAPI>()
		
		runBlocking {
			val res = api.getRecentVotes("both", 0)
			res[3].apply {
				this as LegislativeVote
				
				meta.apply {
					congress shouldBe 116L
					chamber shouldBe Chamber.Senate
					session shouldBe 1L
					rollCall shouldBe 47L
					source shouldBe "https://www.senate.gov/legislative/LIS/roll_call_votes/vote1161/vote_116_1_00047.xml"
				}
				
				bill should { it != null }
				bill!!.apply {
					this.meta.apply {
						slug shouldBe "sjres7"
						congress shouldBe 116L
						name shouldBe "S.J.RES.7"
						type shouldBe "sjres"
						id shouldBe "sjres7-116"
					}
				}
				
				amendment should { it != null }
				amendment!!.apply {
					number shouldBe "S.Amdt. 194"
					sponsor.apply {
						id shouldBe "I000024"
						name shouldBe "James M. Inhofe"
						party shouldBe Party.R
						state shouldBe State.OKLAHOMA
					}
				}
				
				nomination shouldBe null
				
				question shouldBe "On the Motion to Table"
				description shouldBe "To provide an exception for supporting efforts to defend against ballistic missile," +
					" cruise missile, and unmanned aerial vehicle threats to civilian population centers in coalition" +
					" countries, including locations where citizens and nationals of the United States reside."
				goal shouldBe VoteGoal.TABLE
				type shouldBe VoteType.ONE_HALF
				date shouldBe LocalDate.of(2019, 3, 13)
				time shouldBe LocalTime.of(17, 30, 0)
				
				result shouldBe "Motion to Table Agreed to"
				
				votes[Party.D] should { it != null }
				votes[Party.D]?.let {
					it[VotePosition.Yes] shouldBe 45L
					it[VotePosition.No] shouldBe 0L
					it[VotePosition.Abstain] shouldBe 0L
					it.majority shouldBe MajorityPosition.Yes
				}
				
				votes[Party.R] should { it != null }
				votes[Party.R]?.let {
					it[VotePosition.Yes] shouldBe 5L
					it[VotePosition.No] shouldBe 48L
					it[VotePosition.Abstain] shouldBe 0L
					it.majority shouldBe MajorityPosition.No
				}
				
				votes[Party.I] should { it != null }
				votes[Party.I]?.let {
					it[VotePosition.Yes] shouldBe 2L
					it[VotePosition.No] shouldBe 0L
					it[VotePosition.Abstain] shouldBe 0L
				}
				
				votes[null] should { it != null }
				votes[null]?.let {
					it[VotePosition.Yes] shouldBe 52L
					it[VotePosition.No] shouldBe 48L
					it[VotePosition.Abstain] shouldBe 0L
				}
			}
		}
	}
	
	// Specifically tests a recent occurrence of a nomination vote
	@Test
	@Tag("mocked")
	fun recentNominationVote() {
		val api = get<PublicaAPI>()
		
		runBlocking {
			val res = api.getRecentVotes("both", 0)
			res[5].apply {
				this as LegislativeVote
				
				meta.apply {
					congress shouldBe 116L
					chamber shouldBe Chamber.Senate
					session shouldBe 1L
					rollCall shouldBe 46L
					source shouldBe "https://www.senate.gov/legislative/LIS/roll_call_votes/vote1161/vote_116_1_00046.xml"
				}
				
				bill shouldBe null
				amendment shouldBe null
				
				nomination should { it != null }
				nomination!!.apply {
					id shouldBe "PN171-116"
					number shouldBe "PN171"
					name shouldBe "William Beach"
					agency shouldBe "Department of Labor"
				}
				
				question shouldBe "On the Nomination"
				description shouldBe "William Beach, of Kansas, to be Commissioner of Labor Statistics, Department of " +
					"Labor, for a term of four years"
				goal shouldBe VoteGoal.ELECTION
				type shouldBe VoteType.ONE_HALF
				date shouldBe LocalDate.of(2019, 3, 13)
				time shouldBe LocalTime.of(13, 47, 0)
				
				result shouldBe "Nomination Confirmed"
				
				votes[Party.D] should { it != null }
				votes[Party.D]?.let {
					it[VotePosition.Yes] shouldBe 2L
					it[VotePosition.No] shouldBe 42L
					it[VotePosition.Abstain] shouldBe 1L
					it.majority shouldBe MajorityPosition.No
				}
				
				votes[Party.R] should { it != null }
				votes[Party.R]?.let {
					it[VotePosition.Yes] shouldBe 53L
					it[VotePosition.No] shouldBe 0L
					it[VotePosition.Abstain] shouldBe 0L
					it.majority shouldBe MajorityPosition.Yes
				}
				
				votes[Party.I] should { it != null }
				votes[Party.I]?.let {
					it[VotePosition.Yes] shouldBe 0L
					it[VotePosition.No] shouldBe 2L
					it[VotePosition.Abstain] shouldBe 0L
				}
				
				votes[null] should { it != null }
				votes[null]?.let {
					it[VotePosition.Yes] shouldBe 55L
					it[VotePosition.No] shouldBe 44L
					it[VotePosition.Abstain] shouldBe 1L
				}
			}
		}
	}
	
	/**
	 * Tests all votes to see if any pipeline errors occur. If this method finishes normally, that
	 * means that all currently live votes are handled without error.
	 */
	@Test
	@Tag("exhaustive")
	fun allRecentVotes() {
		// Restart Koin to avoid mock interference
		stopKoin()
		PublicaAPI.start()
		
		val api = get<PublicaAPI>()
		
		runBlocking {
			api.getRecentVotes("both").toList().vetAll()
		}
	}
	
	// TODO move
	@Test
	fun rollCall698() {
		// Restart Koin to avoid mock interference
		stopKoin()
		PublicaAPI.start()
		
		val api = get<PublicaAPI>()
		
		runBlocking {
			api.getPositions(116, "house", 1, 698).vet()
		}
	}
	
}