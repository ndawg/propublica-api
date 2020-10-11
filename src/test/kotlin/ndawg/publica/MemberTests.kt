package ndawg.publica

import ndawg.publica.raw.RawPublicaAPI
import ndawg.publica.types.Chamber
import ndawg.publica.types.Party
import ndawg.publica.types.State
import com.google.gson.Gson
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.whenever
import io.kotlintest.should
import io.kotlintest.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Tag
import org.koin.standalone.get
import org.koin.test.declareMock
import java.io.File
import java.time.LocalDate

class MemberTests : PublicaAPITest() {
	
	@Before
	fun setup() {
		declareMock<RawPublicaAPI> {
			whenever(getMember(any(), eq("S000033"))).thenReturn(
				testData("members/member_sanders.json")
			)
			whenever(getMemberList(any(), eq(116), eq("house"))).thenReturn(
				testData("members/house_members.json")
			)
			whenever(getMemberList(any(), eq(116), eq("senate"))).thenReturn(
				testData("members/house_members.json")
			)
		}
	}
	
	@Test
	@Tag("mocked")
	fun loadSanders() {
		declareMock<RawPublicaAPI> {
			whenever(getMember(any(), eq("S000033"))).thenReturn(
				testData("members/member_sanders.json")
			)
		}
		
		val api = get<PublicaAPI>()
		
		runBlocking {
			val res = api.getMember("S000033")
			res.apply {
				id shouldBe "S000033"
				
				firstName shouldBe "Bernard"
				middleName shouldBe null
				lastName shouldBe "Sanders"
				name shouldBe "Bernard Sanders"
				
				dob shouldBe LocalDate.of(1941, 9, 8)
				gender shouldBe "M"
				website shouldBe "https://www.sanders.senate.gov"
				
				ids.apply {
					govtrack shouldBe "400357"
					cspan shouldBe "994"
					votesmart shouldBe "27110"
					icpsr shouldBe "29147"
					crp shouldBe "N00000528"
					google shouldBe "/m/01_gbv"
				}
				
				accounts.apply {
					twitter shouldBe "SenSanders"
					facebook shouldBe "senatorsanders"
					youtube shouldBe "senatorsanders"
				}
				
				inOffice shouldBe true
				
				roles.size shouldBe 15
				roles.first().apply {
					congress shouldBe 116L
					chamber shouldBe Chamber.Senate
					state shouldBe State.VERMONT
					party shouldBe Party.I
					district shouldBe null
					
					title shouldBe "Senator, 1st Class"
					shortTitle shouldBe "Sen."
					role shouldBe null
					seniority shouldBe 13L // TODO verify never null
					senateClass shouldBe 1
					stateRank shouldBe "junior" // TODO check all values
					start shouldBe LocalDate.of(2019, 1, 3)
					end shouldBe LocalDate.of(2021, 1, 3)
					
					ids.apply {
						fec shouldBe "H8VT01016"
						lis shouldBe "S313"
						ocd shouldBe "ocd-division/country:us/state:vt"
					}
					
					contact.apply {
						office shouldBe "332 Dirksen Senate Office Building"
						phone shouldBe "202-224-5141"
						fax shouldBe null
						contact shouldBe "http://www.sanders.senate.gov/contact/"
					}
					
					stats.apply {
						billsSponsored shouldBe 10L
						billsCosposnored shouldBe 92L
						missedVotes shouldBe 12.24F
						votesWithParty shouldBe 81.4F
					}
					
					committees.keys.map { it.code } shouldBe listOf("SSEG", "SSVA", "SSEV", "SSHR", "SSBU")
					
					committees.entries.first().apply {
						key.apply {
							name shouldBe "Committee on Energy and Natural Resources"
							code shouldBe "SSEG"
							side shouldBe "minority"
							title shouldBe "Member"
							partyRank shouldBe 4L
							start shouldBe LocalDate.of(2019, 1, 9)
							end shouldBe LocalDate.of(2021, 1, 3)
						}
						
						value.map { it.code } shouldBe listOf("SSEG01", "SSEG04", "SSEG07")
						value.first().apply {
							name shouldBe "Energy"
							code shouldBe "SSEG01"
							side shouldBe "minority"
							title shouldBe "Member"
							partyRank shouldBe 4L
							start shouldBe LocalDate.of(2019, 2, 5)
							end shouldBe LocalDate.of(2021, 1, 3)
						}
					}
				}
				
				currentRole should { it != null }
				currentRole shouldBe roles.first()
				currentRole!!.start shouldBe LocalDate.of(2019, 1, 3)
			}
		}
	}
	
	@Test
	@Tag("live")
	fun live_getMember() {
		val api = get<PublicaAPI>()
		runBlocking {
			api.getMember("S000033").vet()
		}
	}
	
	@Test
	fun live_listMembers() {
//		val (key, raw) = raw()
//		runBlocking {
//			raw.getMemberList(key, 116, "house").await().results!![0].members.vetAll()
//		}
		
		val api = get<PublicaAPI>()
		runBlocking {
			api.getMembers(116, "house").vet()
		}
	}
	
	//	@Test
	fun captureMembersList() {
		val (key, raw) = raw()
		
		runBlocking {
			File("house_members.json").writeText(get<Gson>().toJson(raw.getMemberList(key, 116, "house").await()))
			File("senate_members.json").writeText(get<Gson>().toJson(raw.getMemberList(key, 116, "senate").await()))
		}
	}
	
}