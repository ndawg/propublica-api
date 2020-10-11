package ndawg.publica

import ndawg.util.from
import com.google.gson.Gson
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.whenever
import io.kotlintest.shouldBe
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import ndawg.publica.raw.*
import ndawg.publica.types.*
import org.junit.Test
import org.junit.jupiter.api.Tag
import org.koin.standalone.get
import org.koin.test.declareMock
import java.io.File
import java.time.LocalDate
import java.time.LocalTime

class BillTests : PublicaAPITest() {
	
	@Test
	@Tag("mocked")
	fun hr1Details() {
		declareMock<RawPublicaAPI> {
			whenever(getBillDetails(any(), eq(116), eq("hr1"))).thenReturn(
				testData("hr1.json")
			)
		}
		
		val api = get<PublicaAPI>()
		
		runBlocking {
			val hr1 = api.getBillDetails("hr1", 116)
			
			hr1.meta.apply {
				slug shouldBe "hr1"
				congress shouldBe 116L
				name shouldBe "H.R.1"
				id shouldBe "hr1-116"
			}
			
			hr1.title shouldBe "To expand Americans' access to the ballot box, reduce the influence of big money in" +
				" politics, and strengthen ethics rules for public servants, and for other purposes."
			hr1.shortTitle shouldBe "For the People Act of 2019"
			
			hr1.sponsor.apply {
				title shouldBe "Rep."
				name shouldBe "John Sarbanes"
				id shouldBe "S001168"
				party shouldBe Party.D
				state shouldBe State.MARYLAND
			}
			
			hr1.links.apply {
				gpo shouldBe "https://www.govinfo.gov/content/pkg/BILLS-116hr1eh/pdf/BILLS-116hr1eh.pdf"
				congress shouldBe "https://www.congress.gov/bill/116th-congress/house-bill/1"
				govtrack shouldBe "https://www.govtrack.us/congress/bills/116/hr1"
			}
			
			hr1.dates.apply {
				introduced shouldBe LocalDate.of(2019, 1, 3)
				lastVote shouldBe LocalDate.of(2019, 3, 8)
				housePassage shouldBe LocalDate.of(2019, 3, 8)
				senatePassage shouldBe null
				enacted shouldBe null
				vetoed shouldBe null
			}
			
			hr1.cosponsors.apply {
				this[Party.D] shouldBe 236L
				this[Party.R] shouldBe 0L
				this[Party.I] shouldBe 0L
			}
			hr1.withdrawnCosponors shouldBe 0L
			hr1.totalCosponors shouldBe 236L
			
			hr1.subject shouldBe "Government Operations and Politics"
			hr1.committee shouldBe "House Homeland Security Committee"
			hr1.committees shouldBe listOf(
				"HSJU",
				"HSHM",
				"HSSO",
				"HSBA",
				"HSWM",
				"HSED",
				"HSSY",
				"HSGO",
				"HLIG",
				"HSHA"
			)
			hr1.subcommittees shouldBe emptyList<String>()
			
			hr1.summary shouldBe "For the People Act of 2019 This bill addresses voter access, election integrity, " +
				"election security, political spending, and ethics for the three branches of government.  " +
				"Specifically, the bill expands voter registration and voting access, makes Election Day a federal " +
				"holiday, and limits removing voters from voter rolls. The bill provides for states to establish" +
				" independent, nonpartisan redistricting commissions. The bill also sets forth provisions related" +
				" to election security, including sharing intelligence information with state election officials," +
				" protecting the security of the voter rolls, supporting states in securing their election systems," +
				" developing a national strategy to protect the security and integrity of U.S. democratic institutions," +
				" establishing in the legislative branch the National Commission to Protect United States Democratic" +
				" Institutions, and other provisions to improve the cybersecurity of election systems. This bill " +
				"addresses campaign spending, including by expanding the ban on foreign nationals contributing to" +
				" or spending on elections; expanding disclosure rules pertaining to organizations spending money" +
				" during elections, campaign advertisements, and online platforms; and revising disclaimer" +
				" requirements for political advertising.  This bill establishes an alternative campaign funding" +
				" system for certain federal offices. The system involves federal matching of small contributions" +
				" for qualified candidates.  This bill sets forth provisions related to ethics in all three branches" +
				" of government. Specifically, the bill requires a code of ethics for federal judges and justices," +
				" prohibits Members of the House from serving on the board of a for-profit entity, expands " +
				"enforcement of regulations governing foreign agents, and establishes additional conflict-of-interest" +
				" and ethics provisions for federal employees and the White House.  The bill also requires candidates" +
				" for President and Vice President to submit 10 years of tax returns."
			
			hr1.shortSummary shouldBe "For the People Act of 2019 This bill addresses voter access, election integrity, " +
				"election security, political spending, and ethics for the three branches of government.  " +
				"Specifically, the bill expands voter registration and voting access, makes Election Day a federal " +
				"holiday, and limits removing voters from voter rolls. The bill provides for states to establish " +
				"independent, nonpartisan redistricting commissions. The bill also sets forth provisions related to " +
				"election security, including shari..."
			
			hr1.versions shouldBe listOf(
				RawBillDetails.RawVersion(
					status = "Engrossed as Agreed to or Passed by House",
					title = "For the People Act of 2019",
					congressdotgov_url = "https://www.govinfo.gov/content/pkg/BILLS-116hr1eh/html/BILLS-116hr1eh.htm"
				),
				RawBillDetails.RawVersion(
					status = "Reported in House",
					title = "For the People Act of 2019",
					congressdotgov_url = "https://www.govinfo.gov/content/pkg/BILLS-116hr1rh/html/BILLS-116hr1rh.htm"
				),
				RawBillDetails.RawVersion(
					status = "Introduced in House",
					title = "For the People Act of 2019",
					congressdotgov_url = "https://www.govinfo.gov/content/pkg/BILLS-116hr1ih/html/BILLS-116hr1ih.htm"
				)
			)
			
			hr1.actions.size shouldBe 110
			hr1.actions.find { it.id == 108L }!!.apply {
				id shouldBe 108L
				chamber shouldBe Chamber.House
				action_type shouldBe "Floor"
				datetime shouldBe LocalDate.of(2019, 3, 8)
				description shouldBe "Motion to reconsider laid on the table Agreed to without objection."
			}
			
			hr1.votes.size shouldBe 10
			hr1.votes.first().apply {
				chamber shouldBe Chamber.House
				date shouldBe LocalDate.of(2019, 3, 8)
				time shouldBe LocalTime.of(11, 21, 0)
				rollCall shouldBe 118L
				question shouldBe "On Passage"
				result shouldBe "Passed"
				
				votes[VotePosition.Yes] shouldBe 234L
				votes[VotePosition.No] shouldBe 193L
				votes[VotePosition.Abstain] shouldBe 5L
			}
			
			hr1.votes.forEach {
				println("${it.question} -> ${it.goal!!}")
			}
		}
	}
	
	@Test
	@Tag("mocked")
	fun byMemberMocked() {
		declareMock<RawPublicaAPI> {
			(0..19).forEach {
				whenever(getBillsByMember(any(), eq("S000033"), eq("introduced"), eq(it * 20))).thenReturn(
					testData("introduced/sanders_$it.json")
				)
			}
		}
		
		val api = get<PublicaAPI>()
		runBlocking {
			api.getBillsByMember("S000033", "introduced", 0).apply {
				size shouldBe 20
				
				first().apply {
					meta.apply {
						slug shouldBe "s677"
						congress shouldBe 116L
						name shouldBe "S.677"
						type shouldBe "s"
						id shouldBe "s677-116"
					}
					
					title shouldBe "A bill to amend the Food and Nutrition Act of 2008 to provide for the participation" +
						" of Puerto Rico, American Samoa, and the Commonwealth of the Northern Mariana Islands in the" +
						" supplemental nutrition assistance program, and for other purposes."
					shortTitle shouldBe "Equitable Nutrition Assistance for the Territories Act of 2019"
					
					sponsor.apply {
						id shouldBe "S000033"
						name shouldBe "Bernard Sanders"
						party shouldBe Party.I
						state shouldBe State.VERMONT
						title shouldBe "Sen."
					}
					
					links.apply {
						gpo shouldBe "https://www.govinfo.gov/content/pkg/BILLS-116s677is/pdf/BILLS-116s677is.pdf"
						congress shouldBe "https://www.congress.gov/bill/116th-congress/senate-bill/677"
						govtrack shouldBe "https://www.govtrack.us/congress/bills/116/s677"
					}
					
					dates.introduced shouldBe LocalDate.of(2019, 3, 6)
					active shouldBe false
					
					cosponsors[Party.R] shouldBe 0L
					cosponsors[Party.D] shouldBe 11L
					cosponsors[Party.I] shouldBe 0L
					withdrawnCosponors shouldBe 0L
					
					committee shouldBe "Senate Agriculture, Nutrition, and Forestry Committee"
				}
			}
			
			// TODO runs into unsupported bills
//			api.getBills("S000033", "introduced").size shouldBe 389
		}
	}
	
	@Test
	fun byMemberAll() {
		val api = get<PublicaAPI>()
		
		runBlocking {
			// TODO all bills of a member
		}
	}
	
	@Test
	@Tag("mocked")
	fun recentlyIntroduced() {
		declareMock<RawPublicaAPI> {
			(0 until 3).forEach {
				whenever(getRecentBills(any(), eq(116), eq("both"), eq("introduced"), eq(it * 20))).thenReturn(
					testData("recent_bills/recent_bills_introduced_$it.json")
				)
			}
		}
		
		val api = get<PublicaAPI>()
		
		runBlocking {
			val page1 = api.getRecentBills(116, "both", "introduced", 0)
			page1.size shouldBe 20
			
			page1.first().apply {
				meta.apply {
					id shouldBe "sres414-116"
					slug shouldBe "sres414"
					type shouldBe "sres"
					name shouldBe "S.RES.414"
				}
				title shouldBe "A resolution recognizing National Native American Heritage Month and celebrating the heritages and cultures of Native Americans and the contributions of Native Americans to the United States."
				shortTitle shouldBe "A resolution recognizing National Native American Heritage Month and celebrating the heritages and cultures of Native Americans and the contributions of Native Americans to the United States."
				sponsor.apply {
					title shouldBe "Sen."
					name shouldBe "John Hoeven"
					id shouldBe "H001061"
					party shouldBe Party.R
					state shouldBe State.NORTH_DAKOTA
				}
				links.apply {
					congress shouldBe "https://www.congress.gov/bill/116th-congress/senate-resolution/414"
					govtrack shouldBe "https://www.govtrack.us/congress/bills/116/sres414"
				}
				dates.apply {
					introduced shouldBe LocalDate.of(2019, 11, 7)
				}
				// TODO the JSON reports 31 but the sum is 30
				totalCosponors shouldBe 30L
				cosponsors[Party.R] shouldBe 10L
				cosponsors[Party.D] shouldBe 20L
				cosponsors[Party.I] shouldBe 0L
				withdrawnCosponors shouldBe 0L
				subject shouldBe ""
				committee shouldBe ""
				committees shouldBe emptyList<String>()
				summary shouldBe ""
				shortSummary shouldBe ""
				active shouldBe true
			}
		}
	}
	
	@Test
	@Tag("mocked")
	fun recentlyIntroducedEnacted() {
		declareMock<RawPublicaAPI> {
			(0 until 3).forEach {
				whenever(getRecentBills(any(), eq(116), eq("both"), eq("introduced"), eq(it * 20))).thenReturn(
					testData("recent_bills/recent_bills_introduced_$it.json")
				)
			}
			
			(0 until 2).forEach {
				whenever(getRecentBills(any(), eq(116), eq("both"), eq("enacted"), eq(it * 20))).thenReturn(
					testData("recent_bills/recent_bills_enacted_$it.json")
				)
			}
		}
		
		val api = get<PublicaAPI>()
		
		runBlocking {
			val page1 = api.getRecentBills(116, "both", "enacted", 0)
			page1.size shouldBe 20
			
			page1.first().apply {
				meta.apply {
					id shouldBe "s693-116"
					slug shouldBe "s693"
					type shouldBe "s"
					name shouldBe "S.693"
				}
				title shouldBe "A bill to amend title 36, United States Code, to require that the POW/MIA flag be displayed on all days that the flag of the United States is displayed on certain Federal property."
				shortTitle shouldBe "National POW/MIA Flag Act"
				sponsor.apply {
					title shouldBe "Sen."
					name shouldBe "Elizabeth Warren"
					id shouldBe "W000817"
					party shouldBe Party.D
					state shouldBe State.MASSACHUSETTS
				}
				links.apply {
					congress shouldBe "https://www.congress.gov/bill/116th-congress/senate-bill/693"
					govtrack shouldBe "https://www.govtrack.us/congress/bills/116/s693"
				}
				dates.apply {
					introduced shouldBe LocalDate.of(2019, 3, 7)
					housePassage shouldBe LocalDate.of(2019, 10, 22)
					senatePassage shouldBe LocalDate.of(2019, 5, 2)
					enacted shouldBe LocalDate.of(2019, 11, 7)
				}
				active shouldBe true
				totalCosponors shouldBe 7L
				cosponsors[Party.R] shouldBe 4L
				cosponsors[Party.D] shouldBe 3L
				cosponsors[Party.I] shouldBe 0L
				withdrawnCosponors shouldBe 0L
				subject shouldBe "Government Operations and Politics"
				committee shouldBe "House Judiciary Committee"
				committees shouldBe listOf("HSJU", "SSJU")
				summary shouldBe "National POW/MIA Flag Act The bill changes the days on which the POW/MIA flag is required to be displayed at specified locations to all days on which the U.S. flag is displayed. (Current law requires the POW/MIA flag to be displayed only on Armed Forces Day, Memorial Day, Flag Day, Independence Day, National POW/MIA Recognition Day, and Veterans Day.) "
				shortSummary shouldBe summary
			}
		}
	}
	
	@Test
	@Tag("mocked")
	fun upcoming() {
		declareMock<RawPublicaAPI> {
			whenever(getUpcomingBills(any(), eq("house"))).thenReturn(
				testData("upcoming_bills.json")
			)
		}
		
		val api = get<PublicaAPI>()
		
		runBlocking {
			val res = api.getUpcomingBills("house")
			res.size shouldBe 14
			
			res.first().apply {
				meta.apply {
					congress shouldBe 116L
					chamber shouldBe Chamber.House
					id shouldBe "hconres24-116"
					slug shouldBe "hconres24"
					type shouldBe "hconres"
					name shouldBe "H.CON.RES.24"
				}
				
				day shouldBe LocalDate.of(2019, 3, 14)
				range shouldBe "week"
			}
		}
	}
	
	@Test
	@Tag("live")
	fun supportAllBillTypes() {
		val api = get<PublicaAPI>()
		runBlocking {
			api.getBillsByMember("S000033", "introduced", 0)
			api.getBillsByMember("S000033", "updated", 0)
			api.getBillsByMember("S000033", "active", 0)
			api.getBillsByMember("S000033", "passed", 0)
			api.getBillsByMember("S000033", "enacted", 0)
			api.getBillsByMember("S000033", "vetoed", 0)
		}
	}
	
	//	@Test
	@Tag("exhaustive")
	fun allBillsLive() {
		val api = get<PublicaAPI>()
		runBlocking {
			api.getRecentBills(116, null, BillStatus.INTRODUCED).toList()
		}
	}
	
	//	@Test
	fun captureSearches() {
		val (key, raw, gson) = raw()
		runBlocking {
			// Search 1 config: phrase "climate change", sort "date", dir "desc"
			// Search 2 config: keywords "climate change", sort "date", dir "desc"
			val res1 = raw.searchBills(key, "\"climate change\"", "date", "desc", 0).await()
			val res2 = raw.searchBills(key, "climate change", "date", "desc", 0).await()
			File("search_01.json").writeText(gson.toJson(res1))
			File("search_02.json").writeText(gson.toJson(res2))
		}
	}
	
	@Test
	@Tag("mocked")
	fun searchPhrase() {
		declareMock<RawPublicaAPI> {
			whenever(searchBills(any(), eq("\"climate change\""), any(), any(), eq(0))).thenReturn(
				CompletableDeferred(get<Gson>().from<PublicaAPIResponse<List<RecentBillResults>>>(
					testFile("searches/search_01.json"))
				)
			)
		}
		
		val api = get<PublicaAPI>()
		
		runBlocking {
			// This section verifies the default properties of the search
			val res1 = api.searchBills(SearchRequest().phrase("climate change").byDate().descending(), 0)
			val res2 = api.searchBills(SearchRequest().phrase("climate change"), 0)
			res1 shouldBe res2
		}
		
		runBlocking {
			val res1 = api.searchBills(SearchRequest().phrase("climate change").byDate().descending(), 0)
			
			// Check the first result to make sure it is being transformed correctly
			res1.first().apply {
				meta.apply {
					id shouldBe "hr3115-116"
					slug shouldBe "hr3115"
					type shouldBe "hr"
					name shouldBe "H.R.3115"
				}
				
				title shouldBe "To direct the Administrator of the National Oceanic and Atmospheric Administration to make grants to State and local governments and nongovernmental organizations for purposes of carrying out climate-resilient living shoreline projects that protect coastal communities by supporting ecosystem functions and habitats with the use of natural materials and systems, and for other purposes."
				shortTitle shouldBe "Living Shorelines Act of 2019"
				
				sponsor.apply {
					title shouldBe "Rep."
					name shouldBe "Frank Pallone"
					id shouldBe "P000034"
					party shouldBe Party.D
					state shouldBe State.NEW_JERSEY
				}
				
				links.apply {
					congress shouldBe "https://www.congress.gov/bill/116th-congress/house-bill/3115"
					govtrack shouldBe "https://www.govtrack.us/congress/bills/116/hr3115"
				}
				
				dates.apply {
					introduced shouldBe LocalDate.of(2019, 6, 5)
				}
				
				active shouldBe true
				totalCosponors shouldBe 43L
				cosponsors[Party.R] shouldBe 6L
				cosponsors[Party.D] shouldBe 37L
				subject shouldBe "Public Lands and Natural Resources"
				committee shouldBe "House Natural Resources Committee"
				committees shouldBe listOf("HSII")
				summary shouldBe "Living Shorelines Act of 2019  This bill directs the National Oceanic and Atmospheric Administration to award grants to state or local governments, Indian tribes, or nonprofit organizations to (1) implement climate-resilient living shoreline projects; and (2) encourage innovation in the use of natural materials to protect coastal communities, habitats, and natural system functions."
				shortSummary shouldBe "Living Shorelines Act of 2019  This bill directs the National Oceanic and Atmospheric Administration to award grants to state or local governments, Indian " +
					"tribes, or nonprofit organizations to (1) implement climate-resilient living shoreline projects; and (2) encourage innovation in the use of natural materials to protect coastal communities, habitats, and natural system functions."
			}
			
			// Check all the IDs
			res1.map { it.meta.id } shouldBe listOf(
				"hr3115-116", "s2955-116", "hr1132-116", "hr5194-116", "s2903-116",
				"s2418-116", "s2893-116", "hr3541-116", "hr4732-116", "hr5102-116",
				"hr2247-116", "hconres74-116", "hr4874-116", "hr4723-116", "hr4986-116",
				"hres676-116", "sres404-116", "s2718-116", "hr4891-116", "s2704-116"
			)
		}
	}
	
	@Test
	@Tag("mocked")
	fun searchKeywords() {
		declareMock<RawPublicaAPI> {
			whenever(searchBills(any(), eq("climate change"), any(), any(), eq(0))).thenReturn(
				CompletableDeferred(get<Gson>().from<PublicaAPIResponse<List<RecentBillResults>>>(
					testFile("searches/search_02.json"))
				)
			)
		}
		
		val api = get<PublicaAPI>()
		
		runBlocking {
			// This section verifies the default properties of the search
			val res1 = api.searchBills(SearchRequest().keywords("climate", "change").byDate().descending(), 0)
			val res2 = api.searchBills(SearchRequest().keywords("climate", "change"), 0)
			res1 shouldBe res2
		}
		
		runBlocking {
			val res1 = api.searchBills(SearchRequest().keywords("climate", "change").byDate().descending(), 0)
			
			// Check the first result to make sure it is being transformed correctly
			res1.first().apply {
				meta.apply {
					id shouldBe "hr2189-116"
					slug shouldBe "hr2189"
					type shouldBe "hr"
					name shouldBe "H.R.2189"
				}
				
				title shouldBe "To require the Secretary of Commerce, acting through the Administrator of the National Oceanic and Atmospheric Administration, to establish a constituent-driven program to provide a digital information platform capable of efficiently integrating coastal data with decision-support tools, training, and best practices and to support collection of priority coastal geospatial data to inform and improve local, State, regional, and Federal capacities to manage the coastal region, and for other purposes."
				shortTitle shouldBe "Digital Coast Act"
				
				sponsor.apply {
					title shouldBe "Rep."
					name shouldBe "C.A. Dutch Ruppersberger"
					id shouldBe "R000576"
					party shouldBe Party.D
					state shouldBe State.MARYLAND
				}
				
				links.apply {
					congress shouldBe "https://www.congress.gov/bill/116th-congress/house-bill/2189"
					govtrack shouldBe "https://www.govtrack.us/congress/bills/116/hr2189"
				}
				
				dates.apply {
					introduced shouldBe LocalDate.of(2019, 4, 9)
				}
				
				active shouldBe true
				totalCosponors shouldBe 4L
				cosponsors[Party.R] shouldBe 2L
				cosponsors[Party.D] shouldBe 2L
				subject shouldBe "Public Lands and Natural Resources"
				committee shouldBe "House Natural Resources Committee"
				committees shouldBe listOf("HSII")
				summary shouldBe "Digital Coast Act This bill provides statutory authority for and revises the National Oceanic and Atmospheric Administration\u0027s (NOAA\u0027s) Digital Coast program. (The program currently exists under NOAA to provide data, tools, and training that communities use to manage their coastal resources.) NOAA must focus on filling data needs and gaps for critical coastal management issues and support continued improvement in existing efforts to coordinate the acquisition and integration of key data sets needed for coastal management. NOAA may enter into financial agreements and collect fees to carry out the program."
				shortSummary shouldBe "Digital Coast Act This bill provides statutory authority for and revises the National Oceanic and Atmospheric Administration\u0026#39;s (NOAA\u0026#39;s) Digital Coast program. (The program currently exists under NOAA to provide data, tools, and training that communities use to manage their coastal resources.) NOAA must focus on filling data needs and gaps for critical coastal management issues and support continued improvement in existing efforts to coordinate the acquisition and integration of key data sets ..."
			}
			
			// Check all the IDs
			res1.map { it.meta.id } shouldBe listOf(
				"hr2189", "hr4895", "hr3115", "hr5264", "hr5258",
				"hr4727", "s2955", "hr5041", "hr3991", "hr5133",
				"s1838", "hr5038", "hr1309", "hr1132", "hr2250",
				"hr5194", "hr2699", "hconres76", "hr2546", "hr3432"
			).map { "$it-116" }
		}
	}
	
	//	@Test
	fun captureRecentBySubject() {
		val (key, raw, gson) = raw()
		runBlocking {
			val res = raw.getBillsBySubject(key, "science-technology-communications", 0).await()
			File("recent_bills_by_subject_0.json").writeText(gson.toJson(res))
		}
	}
	
	@Test
	@Tag("mocked")
	fun recentBySubject() {
		declareMock<RawPublicaAPI> {
			whenever(getBillsBySubject(any(), eq("science-technology-communications"), any())).thenReturn(
				CompletableDeferred(get<Gson>().from<PublicaAPIResponse<List<RawBillDetails>>>(
					testFile("recent_bills/recent_bills_by_subject_0.json"))
				)
			)
		}
		
		val api = get<PublicaAPI>()
		
		runBlocking {
			val res = api.getBillsBySubject("science-technology-communications", 0)
			
			// Test the first entry to make sure data is being piped correctly
			res.first().apply {
				meta.apply {
					id shouldBe "hr4229-116"
					slug shouldBe "hr4229"
					type shouldBe "hr"
					name shouldBe "H.R.4229"
				}
				
				title shouldBe "To require the Federal Communications Commission to issue rules relating to the collection of data with respect to the availability of broadband services, and for other purposes."
				shortTitle shouldBe "Broadband Deployment Accuracy and Technological Availability Act"
				
				sponsor.apply {
					name shouldBe "Dave Loebsack"
					id shouldBe "L000565"
					party shouldBe Party.D
					state shouldBe State.IOWA
				}
				
				links.apply {
					congress shouldBe "https://www.congress.gov/bill/116th-congress/house-bill/4229"
					govtrack shouldBe "https://www.govtrack.us/congress/bills/116/hr4229"
				}
				
				dates.apply {
					introduced shouldBe LocalDate.of(2019, 9, 6)
				}
				
				active shouldBe true
				totalCosponors shouldBe 18L
				cosponsors[Party.R] shouldBe 7L
				cosponsors[Party.D] shouldBe 11L
				
				subject shouldBe "Science, Technology, Communications"
				committee shouldBe "House Energy and Commerce Committee"
				committees shouldBe listOf("HSIF")
			}
		}
	}
	
	@Test
	@Tag("mocked")
	fun getBillCosponsors() {
		declareMock<RawPublicaAPI> {
			whenever(getBillCosponsors(any(), eq(116L), eq("hr1"))).thenReturn(
				CompletableDeferred(get<Gson>().from<PublicaAPIResponse<List<RawBillWithCosponsors>>>(
					testFile("hr1_cosponsors.json"))
				)
			)
		}
		
		val api = get<PublicaAPI>()
		
		runBlocking {
			val res = api.getBillCosponsors(116L, "hr1")
			res.size shouldBe 236
			
			res.first().apply {
				id shouldBe "P000197"
				name shouldBe "Nancy Pelosi"
				title shouldBe "Rep."
				state shouldBe State.CALIFORNIA
				party shouldBe Party.D
			}
		}
	}
	
	@Test
	@Tag("mocked")
	fun getRelatedBills() {
		declareMock<RawPublicaAPI> {
			whenever(getRelatedBills(any(), eq(116), eq("hr1"), eq(0))).thenReturn(
				testData("hr1_related.json")
			)
		}
		
		val api = get<PublicaAPI>()
		
		runBlocking {
			val res = api.getRelatedBills(116, "hr1", 0)
			res.size shouldBe 20
			
			res.first().apply {
				meta.apply {
					id shouldBe "hr294-116"
					slug shouldBe "hr294"
					type shouldBe "hr"
					name shouldBe "H.R.294"
				}
				
				title shouldBe "To treat the Tuesday next after the first Monday in November in the same manner as any legal public holiday for purposes of Federal employment, and for other " +
					"purposes."
				shortTitle shouldBe "Election Day Holiday Act of 2019"
				
				sponsor.apply {
					title shouldBe "Rep."
					name shouldBe "Anna G. Eshoo"
					id shouldBe "E000215"
					party shouldBe Party.D
					state shouldBe State.CALIFORNIA
				}
				
				links.apply {
					congress shouldBe "https://www.congress.gov/bill/116th-congress/house-bill/294"
					govtrack shouldBe "https://www.govtrack.us/congress/bills/116/hr294"
				}
				
				dates.apply {
					introduced shouldBe LocalDate.of(2019, 1, 8)
				}
				
				active shouldBe false
				totalCosponors shouldBe 29L
				cosponsors[Party.D] shouldBe 29L
				subject shouldBe "Government Operations and Politics"
				committees shouldBe listOf("HSGO")
				summary shouldBe "Election Day Holiday Act of 2019 This bill requires a federal election day to be treated as a holiday for federal employees."
				shortSummary shouldBe "Election Day Holiday Act of 2019 This bill requires a federal election day to be treated as a holiday for federal employees."
			}
			
			res.map { it.meta.id } shouldBe listOf(
				"hr294", "hr209", "hr44", "hr93", "hr273",
				"s195", "hr736", "hr745", "hr391", "s232",
				"hr868", "hr783", "hr842", "hr599", "hr812",
				"hr964", "hr1057", "s338", "hr1176", "hr137"
			).map { "$it-116" }
		}
	}
	
	@Test
	@Tag("mocked")
	fun getBillSubjects() {
		declareMock<RawPublicaAPI> {
			whenever(getBillSubjects(any(), eq(116), eq("hr1"), eq(0))).thenReturn(
				testData("hr1_subjects.json")
			)
		}
		
		val api = get<PublicaAPI>()
		
		runBlocking {
			val res = api.getBillSubjects(116, "hr1", 0)
			res.size shouldBe 20
			
			res.first().apply {
				name shouldBe "Federal officials"
				slug shouldBe "federal-officials"
			}
			
			res.last().apply {
				name shouldBe "Business ethics"
				slug shouldBe "business-ethics"
			}
		}
	}
	
	@Test
	@Tag("mocked")
	fun searchBills() {
		declareMock<RawPublicaAPI> {
			whenever(searchBillSubjects(any(), eq("climate"))).thenReturn(
				CompletableDeferred(get<Gson>().from<PublicaAPIResponse<List<RawBillSubjectSearchResults>>>(
					testFile("searches/bill_subject_search.json"))
				)
			)
		}
		
		val api = get<PublicaAPI>()
		
		runBlocking {
			val res = api.searchBillSubjects("climate")
			res.size shouldBe 4
			
			res.first().name shouldBe "Climate"
			res.first().slug shouldBe "climate"
			
			res.last().name shouldBe "Global climate change"
			res.last().slug shouldBe "global-climate-change"
		}
	}
	
	//	@Test
	fun captureSubjectSearch() {
		val (key, raw, gson) = raw()
		
		runBlocking {
			val res = raw.searchBillSubjects(key, "climate").await()
			File("bill_subject_search.json").writeText(gson.toJson(res))
		}
	}
	
	//		@Test
	fun captureBillSubjects() {
		val (key, raw, gson) = raw()
		
		runBlocking {
			val res = raw.getBillSubjects(key, 116, "hr1", 0).await()
			File("hr1_subjects.json").writeText(gson.toJson(res))
		}
	}
	
	//	@Test
	fun captureRelatedBills() {
		val (key, raw, gson) = raw()
		
		runBlocking {
			val res = raw.getRelatedBills(key, 116, "hr1", 0).await()
			File("hr1_related.json").writeText(gson.toJson(res))
		}
	}
	
	//	@Test
	fun captureBillCosponsors() {
		val (key, raw, gson) = raw()
		runBlocking {
			val res = raw.getBillCosponsors(key, 116, "hr1").await()
			File("hr1_cosponsors.json").writeText(gson.toJson(res))
		}
	}
	
	//	@Test
	fun captureRecent() {
		val (key, raw) = raw()
		runBlocking {
			for (i in 0 until 3)
				File("recent_bills_introduced_$i.json").writeText(get<Gson>().toJson(raw.getRecentBills(key, 116, "senate", "introduced", i * 20).await()))
			for (i in 0 until 2)
				File("recent_bills_enacted_$i.json").writeText(get<Gson>().toJson(raw.getRecentBills(key, 116, "senate", "enacted", i * 20).await()))
		}
	}
	
	@Test
	@Tag("raw")
	fun raw_billsByMember_introduced() {
		val member = "S000033"
		val (key, raw) = raw()
		runBlocking {
			raw.getBillsByMember(key, member, "introduced", 0).await()
		}
	}
	
	@Test
	@Tag("raw")
	fun raw_billsByMember_updated() {
		val member = "S000033"
		val (key, raw) = raw()
		runBlocking {
			raw.getBillsByMember(key, member, "updated", 0).await()
		}
	}
	
	@Test
	@Tag("raw")
	fun raw_billsByMember_active() {
		val member = "S000033"
		val (key, raw) = raw()
		runBlocking {
			raw.getBillsByMember(key, member, "active", 0).await()
		}
	}
	
	@Test
	@Tag("raw")
	fun raw_billsByMember_passed() {
		val member = "S000033"
		val (key, raw) = raw()
		runBlocking {
			raw.getBillsByMember(key, member, "passed", 0).await()
		}
	}
	
	@Test
	@Tag("raw")
	fun raw_billsByMember_enacted() {
		val member = "S000033"
		val (key, raw) = raw()
		runBlocking {
			raw.getBillsByMember(key, member, "enacted", 0).await()
		}
	}
	
	@Test
	@Tag("raw")
	fun raw_billsByMember_vetoed() {
		val member = "S000033"
		val (key, raw) = raw()
		runBlocking {
			raw.getBillsByMember(key, member, "vetoed", 0).await()
		}
	}
	
	fun captureBillsByMember() {
//		val (key, raw, gson) = raw()
//		runBlocking {
//			asFlow {
//				raw.getBillsByMember(key, "S000033", "")
//			}
//		}
	}
	
}