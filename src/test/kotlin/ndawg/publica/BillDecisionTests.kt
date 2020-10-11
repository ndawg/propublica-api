package ndawg.publica

import ndawg.publica.raw.RawBillDetails
import ndawg.publica.types.BillVoteOverview
import ndawg.publica.types.Chamber
import ndawg.publica.types.VotePosition
import ndawg.publica.types.VoteResult
import ndawg.util.CitizenUtil
import ndawg.util.from
import com.google.gson.Gson
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.junit.Test
import org.koin.standalone.get
import java.time.LocalDate
import java.time.LocalTime

class BillDecisionTests : PublicaAPITest() {
	
	@Test
	fun case1() {
		val gson = get<Gson>()
		val bill = gson.from<RawBillDetails>(testFile("decisions/sample_decisions_1.json"))
		
		PublicaPipeline().transform(bill).apply {
			CitizenUtil.toDecision(this, action(21L) ?: throw IllegalStateException("Coulnd't find action"))!!.let {
				it.vote.result shouldBe VoteResult.FAIL
				it.vote.overview shouldNotBe null
			}
			
			CitizenUtil.toDecision(this, action(9L) ?: throw IllegalStateException("Coulnd't find action"))!!.let {
				it.vote.result shouldBe VoteResult.PASS
				it.vote.overview shouldBe null
				it.vote.voice shouldBe true
			}
			
			CitizenUtil.toDecision(this, action(10L) ?: throw IllegalStateException("Coulnd't find action"))!!.let {
				it.vote.result shouldBe VoteResult.PASS
				it.vote.overview shouldBe null
				it.vote.voice shouldBe true
			}
			
			CitizenUtil.toDecision(this, action(11L) ?: throw IllegalStateException("Coulnd't find action"))!!.let {
				it.vote.result shouldBe VoteResult.PASS
				it.vote.overview shouldBe null
				it.vote.voice shouldBe true
			}
		}
	}
	
	@Test
	fun case2() {
		val gson = get<Gson>()
		val bill = gson.from<RawBillDetails>(testFile("decisions/sample_decisions_2.json"))
		
		PublicaPipeline().transform(bill).apply {
			CitizenUtil.toDecision(this, action(20L) ?: throw IllegalStateException("Coulnd't find action"))!!.let {
				it.vote.result shouldBe VoteResult.PASS
				it.vote.overview shouldBe null
				it.vote.voice shouldBe true
			}
			
			CitizenUtil.toDecision(this, action(22L) ?: throw IllegalStateException("Coulnd't find action"))!!.let {
				it.vote.result shouldBe VoteResult.PASS
				it.vote.overview shouldBe null
				it.vote.voice shouldBe true
			}
			
			CitizenUtil.toDecision(this, action(23L) ?: throw IllegalStateException("Coulnd't find action"))!!.let {
				it.vote.result shouldBe VoteResult.PASS
				it.vote.overview shouldBe null
				it.vote.voice shouldBe true
			}
		}
	}
	
	@Test
	fun case3() {
		val gson = get<Gson>()
		val bill = gson.from<RawBillDetails>(testFile("decisions/sample_decisions_3.json"))
		
		PublicaPipeline().transform(bill).apply {
			CitizenUtil.toDecision(this, action(12L) ?: throw IllegalStateException("Coulnd't find action"))!!.let {
				it.vote.result shouldBe VoteResult.PASS
				it.vote.overview shouldBe null
				it.vote.voice shouldBe true
			}
			
			CitizenUtil.toDecision(this, action(15L) ?: throw IllegalStateException("Coulnd't find action"))!!.let {
				it.vote.result shouldBe VoteResult.PASS
				it.vote.overview shouldBe null
				it.vote.voice shouldBe true
			}
			
			CitizenUtil.toDecision(this, action(19L) ?: throw IllegalStateException("Coulnd't find action"))!!.let {
				it.vote.result shouldBe VoteResult.PASS
				it.vote.overview shouldBe null
				it.vote.voice shouldBe true
			}
			
			CitizenUtil.toDecision(this, action(25L) ?: throw IllegalStateException("Coulnd't find action"))!!.let {
				it.vote.result shouldBe VoteResult.PASS
				it.vote.overview shouldNotBe null
			}
		}
	}
	
	@Test
	fun case4() {
		val gson = get<Gson>()
		val bill = gson.from<RawBillDetails>(testFile("decisions/sample_decisions_4.json"))
		
		PublicaPipeline().transform(bill).apply {
			CitizenUtil.toDecision(this, action(10L) ?: throw IllegalStateException("Coulnd't find action"))!!.let {
				it.vote.result shouldBe VoteResult.PASS
				it.vote.overview shouldBe null
				it.vote.voice shouldBe true
			}
			
			CitizenUtil.toDecision(this, action(15L) ?: throw IllegalStateException("Coulnd't find action"))!!.let {
				it.vote.result shouldBe VoteResult.PASS
				it.vote.overview shouldBe null
				it.vote.voice shouldBe true
			}
			
			CitizenUtil.toDecision(this, action(18L) ?: throw IllegalStateException("Coulnd't find action"))!!.let {
				it.vote.result shouldBe VoteResult.PASS
				it.vote.overview shouldNotBe null
			}
			
			CitizenUtil.toDecision(this, action(23L) ?: throw IllegalStateException("Coulnd't find action"))!!.let {
				it.vote.result shouldBe VoteResult.PASS
				it.vote.overview shouldNotBe null
			}
			
			CitizenUtil.toDecision(this, action(40L) ?: throw IllegalStateException("Coulnd't find action"))!!.let {
				it.vote.result shouldBe VoteResult.PASS
				it.vote.overview shouldBe null
				it.vote.voice shouldBe true
			}
			
			CitizenUtil.toDecision(this, action(46L) ?: throw IllegalStateException("Coulnd't find action"))!!.let {
				it.vote.result shouldBe VoteResult.PASS
				it.vote.overview shouldBe null
				it.vote.voice shouldBe true
			}
			
			CitizenUtil.toDecision(this, action(51L) ?: throw IllegalStateException("Coulnd't find action"))!!.let {
				it.vote.result shouldBe VoteResult.PASS
				it.vote.overview shouldNotBe null
			}
			
			CitizenUtil.toDecision(this, action(54L) ?: throw IllegalStateException("Coulnd't find action"))!!.let {
				it.vote.result shouldBe VoteResult.PASS
				it.vote.overview shouldNotBe null
			}
			
			CitizenUtil.toDecision(this, action(56L) ?: throw IllegalStateException("Coulnd't find action"))!!.let {
				it.vote.result shouldBe VoteResult.PASS
				it.vote.overview shouldNotBe null
			}
		}
	}
	
	@Test
	fun case5() {
		val gson = get<Gson>()
		val bill = gson.from<RawBillDetails>(testFile("decisions/sample_decisions_5.json"))
		
		PublicaPipeline().transform(bill).apply {
			CitizenUtil.toDecision(this, action(21L) ?: throw IllegalStateException("Coulnd't find action"))!!.let {
				it.vote.result shouldBe VoteResult.FAIL
				it.vote.overview shouldNotBe null
			}
		}
	}
	
	// TODO more timeline tests
	@Test
	fun timeline() {
		val gson = get<Gson>()
		val bill = gson.from<RawBillDetails>(testFile("timeline_test.json"))
		
		CitizenUtil.createTimeline(PublicaPipeline().transform(bill)).apply {
			this[0].apply {
				this.status shouldBe "Introduced"
				this.date shouldBe LocalDate.of(2019, 2, 22)
				this.result shouldBe VoteResult.PASS
				this.action shouldBe null
			}
			
			this[1].apply {
				this.status shouldBe "Passed in the House (245 yea - 182 nay)"
				this.date shouldBe LocalDate.of(2019, 2, 26)
				this.result shouldBe VoteResult.PASS
				this.action shouldBe BillVoteOverview(
					Chamber.House,
					LocalDate.of(2019, 2, 26),
					LocalTime.of(18, 32, 0),
					94,
					"On Passage",
					"Passed",
					mapOf(
						VotePosition.Yes to 245L,
						VotePosition.No to 182L,
						VotePosition.Abstain to 5L
					)
				)
			}
		}
	}
	
}