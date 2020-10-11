package ndawg.publica

import ndawg.publica.raw.RawPublicaAPI
import ndawg.publica.raw.RawVote
import ndawg.publica.types.*
import ndawg.util.VoteGoal
import ndawg.util.from
import com.google.gson.Gson
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.whenever
import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import ndawg.publica.types.*
import org.junit.Test
import org.junit.jupiter.api.Tag
import org.koin.standalone.get
import org.koin.test.declareMock
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.time.LocalDate
import java.time.LocalTime

class VoteTests : PublicaAPITest() {
	
	// A live (non-mocked) response test for a specific roll call
	@Test
	@Tag("live")
	fun loadVote118() {
		val api = get<PublicaAPI>()
		runBlocking {
			val vote = api.getPositions(116, "house", 1, 118)
			vote as LegislativeVotePositions
			
			// Should match hr1 above
			vote.topic.apply {
				meta.apply {
					congress shouldBe 116L
					chamber shouldBe Chamber.House
					session shouldBe 1L
					rollCall shouldBe 118L
					source shouldBe "http://clerk.house.gov/evs/2019/roll118.xml"
				}
				
				bill should { it != null }
				bill!!.apply {
					this.meta.apply {
						slug shouldBe "hr1"
						congress shouldBe 116L
						name shouldBe "H.R.1"
						type shouldBe "hr"
						id shouldBe "hr1-116"
					}
				}
				
				amendment shouldBe null
				nomination shouldBe null
				
				question shouldBe "On Passage"
				description shouldBe "For the People Act"
				
				goal shouldBe VoteGoal.PASS
				type shouldBe VoteType.YEA_AND_NAY
				date shouldBe LocalDate.of(2019, 3, 8)
				time shouldBe LocalTime.of(11, 21, 0)
				
				result shouldBe "Passed"
				
				votes[Party.D] should { it != null }
				votes[Party.D]?.let {
					it[VotePosition.Yes] shouldBe 234L
					it[VotePosition.No] shouldBe 0L
					it[VotePosition.Abstain] shouldBe 1L
					it.majority shouldBe MajorityPosition.Yes
				}
				
				votes[Party.R] should { it != null }
				votes[Party.R]?.let {
					it[VotePosition.Yes] shouldBe 0L
					it[VotePosition.No] shouldBe 193L
					it[VotePosition.Abstain] shouldBe 4L
					it.majority shouldBe MajorityPosition.No
				}
				
				votes[Party.I] should { it != null }
				votes[Party.I]?.let {
					it[VotePosition.Yes] shouldBe 0L
					it[VotePosition.No] shouldBe 0L
					it[VotePosition.Abstain] shouldBe 0L
				}
				
				votes[null] should { it != null }
				votes[null]?.let {
					it[VotePosition.Yes] shouldBe 234L
					it[VotePosition.No] shouldBe 193L
					it[VotePosition.Abstain] shouldBe 5L
				}
			}
			
			vote.topic.totalVotes shouldBe (234 + 193 + 5).toLong()
			
			vote.positions[VotePosition.Yes]!!.size shouldBe 234
			vote.positions[VotePosition.No]!!.size shouldBe 193
			// TODO
//            vote.positions[VotePosition.Abstain]!!.size shouldBe 5
		}
	}
	
	@Test
	@Tag("live")
	fun loadVotesSpeaker() {
		val api = get<PublicaAPI>()
		runBlocking {
			val vote = api.getPositions(115, "house", 1, 2)
			vote as SpeakerVotePositions
			
			// First test the topic
			vote.topic.apply {
				// Winner of the nomination
				result shouldBe "Ryan (WI)"
				
				votes[Party.D]!!["Pelosi"] shouldBe 189L
				votes[Party.R]!!["Ryan (WI)"] shouldBe 239L
				
				votes[null]!!["Ryan (WI)"] shouldBe 239L
				votes[null]!!["Pelosi"] shouldBe 189L
			}
			
			// Now test votes
			val pos = vote.positions.toList()
			pos.size shouldBe 6
			
			val ryan = vote.positions.keys.filterIsInstance<ICongressMember>().find { it.id == "R000570" } as ICongressMember
			val pelosi = vote.positions.keys.filterIsInstance<ICongressMember>().find { it.id == "P000197" } as ICongressMember
			
			pos[0].apply {
				(first as ICongressMember).id shouldBe ryan.id
				
				// This member voted for them
				second.find { it.id == "A000374" } should { it != null }
			}
		}
	}
	
	@Test
	@Tag("live")
	fun loadVotesSpeaker2() {
		val api = get<PublicaAPI>()
		runBlocking {
			val vote = api.getPositions(116, "house", 1, 2)
			vote as SpeakerVotePositions
			
			// First test the topic
			vote.topic.apply {
				// Winner of the nomination
				result shouldBe "Pelosi"
				
				votes[Party.D]!!["Pelosi"] shouldBe 220L
				votes[Party.D]!!["Present"] shouldBe 3L
				votes[Party.D]!!["Biden"] shouldBe 1L
				
				votes[Party.R]!!["McCarthy"] shouldBe 192L
				
				votes[null]!!["Pelosi"] shouldBe 220L
			}
			
			// Now test votes
			val pos = vote.positions.toList()
			pos.size shouldBe 13
			
			val pelosi = vote.positions.keys.filterIsInstance<ICongressMember>().find { it.id == "P000197" } as ICongressMember
			val present = vote.positions.keys.find { it.name == "Present" }
			val biden = vote.positions.keys.find { it.name == "Biden" }
			val mccarthy = vote.positions.keys.find { it.name == "Kevin McCarthy" }
			
			vote.positions.apply {
				get(pelosi)!!.size shouldBe 220
				get(present)!!.size shouldBe 3
				get(biden)!!.size shouldBe 1
				
				get(mccarthy)!!.size shouldBe 192
			}
			// TODO test votes
		}
	}
	
	fun loadVotesTieBreak() {
		// TODO
	}
	
	@Test
	@Tag("mocked")
	fun abstains() {
		// TODO use mocking
		val raw = get<Gson>().from<RawVote>(testFile("senate_vote_52.json"))
		val vote = PublicaPipeline().transform(raw) as LegislativeVotePositions
		vote.topic.get(Party.D, VotePosition.Present) shouldBe 42L
	}
	
	// TODO what is this
//	@Test
//	@Tag("mocked")
//	fun rollCall333() {
//		val raw = get<RawPublicaAPI>()
//		runBlocking {
//			val res = raw.getVotes(get<Credentials>().publicaKey, 116, "senate", 1, 333).await()
//			println(res.results!!.votes.vote.positions!!)
//		}
//
//		val api = get<PublicaAPI>()
//		runBlocking {
//			val res = api.getPositions(116, "senate", 1, 33)
//			println(res)
//		}
//	}
	
	@Test
	@Tag("mocked")
	fun allVotesJson() {
		// All votes as of 2019-10-26
		val pipe = PublicaPipeline()
		val votes = get<Gson>().from<List<RawVote>>(testFile("all_votes.json"))
		testAllVotes(votes)
	}
	
	//	@Test
	@Tag("exhaustive")
	fun allVotesLive() {
		val (key, raw) = raw()
		
		runBlocking {
			testAllVotes(asFlow {
				raw.getRecentVotes(key, "both", it).await().results!!.votes
			}.toList())
		}
	}
	
	@Test
	@Tag("raw")
	fun vetSenateNominationVotes() {
		val (key, raw) = raw()
		runBlocking {
			val res = raw.getSenateNominationVotes(key, 116, 0).await()
			res.results!![0].votes.vetAll()
		}
	}
	
	@Test
	@Tag("mocked")
	fun parseSenateNominationVotes() {
		// TODO endpoint is currently bad
		// see: https://github.com/propublica/congress-api-docs/issues/248
		declareMock<RawPublicaAPI> {
			whenever(getSenateNominationVotes(any(), eq(116), eq(0))).thenReturn(
				testData("senate_nomination_votes_0.json")
			)
			whenever(getSenateNominationVotes(any(), eq(116), eq(1))).thenReturn(
				testData("senate_nomination_votes_0.json")
			)
		}
		
		val api = get<PublicaAPI>()
		
		runBlocking {
			val res1 = api.getSenateNominationVotes(116, 0).toList()
			res1.size shouldBe 20
			
			res1.first().apply {
				meta.apply {
					congress shouldBe 116
					chamber shouldBe Chamber.Senate
					session shouldBe 1
					rollCall shouldBe 426
				}
				
				bill shouldBe null
				amendment shouldBe null
				nomination shouldNotBe null
				
				nomination.apply {
					// TODO
				}
				
				question shouldBe "On the Nomination"
				description shouldBe "Stephen E. Biegun, of Michigan, to be Deputy Secretary of State"
				type shouldBe VoteType.ONE_HALF
				date shouldBe LocalDate.of(2019, 12, 19)
				time shouldBe LocalTime.of(16, 8)
				result shouldBe "Nomination Confirmed"
				
				votes[Party.D]!!.apply {
					this[VotePosition.Yes] shouldBe 38L
					this[VotePosition.No] shouldBe 3L
					this[VotePosition.Abstain] shouldBe 4L
					this.majority shouldBe VotePosition.Yes
				}
				
				votes[Party.R]!!.apply {
					this[VotePosition.Yes] shouldBe 51L
					this[VotePosition.No] shouldBe 0L
					this[VotePosition.Abstain] shouldBe 2L
					this.majority shouldBe VotePosition.Yes
				}
				
				votes[Party.I]!!.apply {
					this[VotePosition.Yes] shouldBe 1L
					this[VotePosition.No] shouldBe 0L
					this[VotePosition.Abstain] shouldBe 1L
				}
				
				votes[null]!!.apply {
					this[VotePosition.Yes] shouldBe 90L
					this[VotePosition.No] shouldBe 3L
					this[VotePosition.Abstain] shouldBe 7L
				}
			}
		}
	}
	
//	@Test
	fun captureSenateNominationVotes() {
		val (key, raw, gson) = raw()
		
		runBlocking {
			File("senate_nomination_votes_0.json").writeText(gson.toJson(
				raw.getSenateNominationVotes(key, 116, 0).await()))
			File("senate_nomination_votes_1.json").writeText(gson.toJson(
				raw.getSenateNominationVotes(key, 116, 20).await()))
		}
	}
	
	fun testAllVotes(votes: List<RawVote>) {
		val pipe = PublicaPipeline()
		val failed = mutableMapOf<RawVote, Exception>()
		votes.forEach {
			try {
				pipe.toVote(it)
			} catch (e: Exception) {
				if (failed.size == 5)
					throw IllegalStateException("Too many errors encountered.")
				failed[it] = e
			}
		}
		val gson = get<Gson>()
		
		println("--- FAILURES ---")
		failed.forEach { (t, u) ->
			println("Object: $t")
			println("JSON: ${gson.toJson(t).replace("\n", "")}")
			val str = StringWriter()
			val writer = PrintWriter(str)
			u.printStackTrace(writer)
			println("Exception: $str")
			
			println("")
			println("---------------")
			println("")
		}
	}
	
	//	@Test
	fun saveAll() {
		val (key, raw) = raw()
		
		val f = File("all_votes.json")
		val loaded = mutableListOf<RawVote>()
		
		runBlocking {
			exhaust({
				raw.getRecentVotes(key, "both", it).await().results!!.votes
			}, { it.size }, {
				loaded.addAll(it)
			})
		}
		
		f.writeText(get<Gson>().toJson(loaded))
	}
	
}