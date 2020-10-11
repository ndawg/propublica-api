package ndawg.publica

import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import ndawg.VOTE_VIEW_INTERCEPTOR
import ndawg.VoteViewAPI
import ndawg.publica.raw.PublicaAPIResponse
import ndawg.publica.raw.PublicaError
import ndawg.publica.raw.RawPublicaAPI
import ndawg.publica.types.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module.module
import org.koin.standalone.StandAloneContext.startKoin
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import kotlin.reflect.KClass

/**
 * The main, developer-friendly entry point to the ProPublica API.
 */
class PublicaAPI(val api: RawPublicaAPI, private val key: String) {
	
	private val pageSize = 20
	private val pipe = PublicaPipeline()
	
	// =============
	// === BILLS ===
	// =============
	
	/**
	 * Searches titles and full texts of legislation by keyword, returning 20 results at a time.
	 * Searches cover House and Senate bills from the 113th Congress through the current Congress.
	 *
	 * @param request the request configuration of the search.
	 * @param page the page to use.
	 */
	suspend fun searchBills(request: SearchRequest, page: Int = 0) = withContext(Dispatchers.IO) {
		require(request.search.isNotEmpty()) { "Empty search query" }
		queryFirstResult {
			api.searchBills(
				key,
				request.search,
				request.sort,
				request.order,
				page * pageSize
			).fetch()
		}.bills.map { pipe.transform(it) }
	}
	
	/**
	 * Searches titles and full texts of legislation by keyword, returning 20 results at a time.
	 * Searches cover House and Senate bills from the 113th Congress through the current Congress.
	 * This will continue to return results until there are no more.
	 *
	 * @param request the request configuration of the search.
	 */
	fun searchBills(request: SearchRequest) = asFlow {
		searchBills(request, it)
	}
	
	/**
	 * Retrieves "recent" bills, as in bills that have had recent official activity on them, not necessarily just votes.
	 * For example, being introduced to congress, or being vetoed.
	 *
	 * @param congress The congressional session ID to get bills for.
	 * @param chamber The chamber to filter by, either `senate`, `house`, or `both`.
	 * @param type The current status of the bill. Supported values: `introduced`, `updated`, `active`, `passed`, `enacted`, and `vetoed`.
	 * @param page The page number to get results for.
	 */
	suspend fun getRecentBills(congress: Long, chamber: String, type: String, page: Int = 0): List<BillDetails> = withContext(Dispatchers.IO) {
		require(chamber in listOf("senate", "house", "both")) { "Unknown chamber, use one of: senate, house, both" }
		queryFirstResult {
			api.getRecentBills(key, congress, chamber, type, page * pageSize).fetch()
		}.bills.map { pipe.transform(it) }
	}
	
	/**
	 * Retrieves "recent" bills, as in bills that have had recent official activity on them, not necessarily just votes.
	 * For example, being introduced to congress, or being vetoed. This method does not require a page number and will continue
	 * producing bills until the API doesn't return a full set of results.
	 *
	 * @param congress The congressional session ID to get bills for.
	 * @param chamber The chamber to filter by, either `senate`, `house`, or `both`.
	 * @param type The current status of the bill. Supported values: `introduced`, `updated`, `active`, `passed`, `enacted`, and `vetoed`.
	 */
	fun getRecentBills(congress: Long, chamber: String, type: String) = asFlow {
		getRecentBills(congress, chamber, type, it)
	}
	
	/**
	 * Retrieves recent bills that are either introduced or updated by a certain member. The returned bills are sorted with
	 * the most recently modified bills first.
	 *
	 * @param type The type of bill to get, either `introduced` or `updated`. Hopefully this will eventually support all types,
	 * see: https://github.com/propublica/congress-api-docs/issues/242
	 */
	suspend fun getBillsByMember(member: String, type: String, page: Int) = withContext(Dispatchers.IO) {
		require(type in listOf("introduced", "updated", "active", "passed", "enacted", "vetoed")) { "Unknown type, use either: introduced, updated, active, passed, enacted, or vetoed" }
		queryFirstResult {
			api.getBillsByMember(key, member, type, page * pageSize).fetch()
		}.bills.map { pipe.transform(it) }
	}
	
	/**
	 * Retrieves all bills that are either introduced or updated by a certain member. The returned bills are sorted with
	 * the most recently modified bills first.
	 *
	 * @param type The type of bill to get, either `introduced` or `updated`. Hopefully this will eventually support all types,
	 * see: https://github.com/propublica/congress-api-docs/issues/242
	 */
	suspend fun getBillsByMember(member: String, type: String) = asFlow {
		getBillsByMember(member, type, it)
	}
	
	/**
	 * Gets recent bills by a congressional legislative subject. Subjects are determined by Congress, and are listed here:
	 * https://www.congress.gov/help/field-values/legislative-subject-terms. This method takes slugged versions of the subject names,
	 * for example "Social Welfare" => `social-welfare`.
	 */
	suspend fun getBillsBySubject(subject: String, page: Int = 0) = withContext(Dispatchers.IO) {
		val res = api.getBillsBySubject(key, subject, page * pageSize).fetch()
		res.results ?: throw IllegalStateException("No results returned")
		res.results.map { pipe.transform(it) }
	}
	
	/**
	 * Gets recent bills by a congressional legislative subject. Subjects are determined by Congress, and are listed here:
	 * https://www.congress.gov/help/field-values/legislative-subject-terms. This method takes slugged versions of the subject names,
	 * for example "Social Welfare" => `social-welfare`.
	 */
	suspend fun getBillsBySubject(subject: String) = asFlow {
		getBillsBySubject(subject, it)
	}
	
	/**
	 * Retrieves bills that are expected to be voted on soon. Only accepts "senate" or "house" for chamber.
	 */
	suspend fun getUpcomingBills(chamber: String): List<UpcomingBill> = withContext(Dispatchers.IO) {
		require(chamber in listOf("senate", "house")) { "Unknown chamber, use either: senate, house" }
		queryFirstResult {
			api.getUpcomingBills(key, chamber).fetch()
		}.bills.map { pipe.transform(it) }
	}
	
	/**
	 * Retrieves an in-depth look at the given bill.
	 *
	 * @param bill the slug of the bill, eg "s765" or "hr1"
	 */
	suspend fun getBillDetails(bill: String, congress: Long): BillDetails = withContext(Dispatchers.IO) {
		pipe.transform(queryFirstResult {
			api.getBillDetails(key, congress, bill).fetch()
		})
	}
	
	/**
	 * Retrieves amendments made to a specific bill.
	 *
	 * @param bill the slug of the bill, eg "s765" or "hr1"
	 * @param congress the congressional session relevant to the bill.
	 */
	suspend fun getAmendments(bill: String, congress: Long, page: Int): List<BillAmendment> = withContext(Dispatchers.IO) {
		queryFirstResult {
			api.getBillAmendments(key, congress, bill, page * pageSize).await()
		}.amendments.map { pipe.transform(it) }
	}
	
	/**
	 * Retrieves amendments made to a specific bill.
	 *
	 * @param bill the slug of the bill, eg "s765" or "hr1"
	 * @param congress the congressional session relevant to the bill.
	 */
	fun getAmendments(bill: String, congress: Long): Flow<BillAmendment> = asFlow {
		getAmendments(bill, congress, it)
	}
	
	/**
	 * Returns the given bills subjects, as determined by the Library of Congress.
	 */
	suspend fun getBillSubjects(congress: Long, bill: String, page: Int = 0) = withContext(Dispatchers.IO) {
		val subjects = queryFirstResult {
			api.getBillSubjects(key, congress, bill, page * pageSize).fetch()
		}.subjects ?: throw IllegalStateException("No bill subjects returned")
		subjects.map { pipe.transform(it) }
	}
	
	/**
	 * Returns related bills, as decided by the Library of Congress.
	 */
	suspend fun getRelatedBills(congress: Long, bill: String, page: Int = 0) = withContext(Dispatchers.IO) {
		val related = queryFirstResult {
			api.getRelatedBills(key, congress, bill, page * pageSize).fetch()
		}.related_bills ?: throw IllegalStateException("No related bills returned")
		related.map { pipe.transform(it) }
	}
	
	/**
	 * Returns related bills, as decided by the Library of Congress.
	 */
	fun getRelatedBills(congress: Long, bill: String) = asFlow {
		getRelatedBills(congress, bill, it)
	}
	
	/**
	 * Searches amongst all bill subjects for subjects that have the given search phrase.
	 */
	suspend fun searchBillSubjects(search: String) = withContext(Dispatchers.IO) {
		queryFirstResult {
			api.searchBillSubjects(key, search).fetch()
		}.subjects.map { pipe.transform(it) }
	}
	
	/**
	 * Gets the entire list of cosponsors for a given bill.
	 */
	suspend fun getBillCosponsors(congress: Long, bill: String) = withContext(Dispatchers.IO) {
		queryFirstResult {
			api.getBillCosponsors(key, congress, bill).fetch()
		}.cosponsors.map { pipe.transform(it) }
	}
	
	// =============
	// === VOTES ===
	// =============
	
	/**
	 * Retrieves recent votes taken within congress. The returned data is a summary of the vote, but does
	 * not contain full data on how each member voted. To retrieve that data, use [getPositions]. In addition,
	 * the returned votes are _usually_ [LegislativeVote]s, but there are specific types for speaker votes
	 * ([SpeakerVote]) and motion votes ([MotionVote]).
	 */
	suspend fun getRecentVotes(chamber: String, page: Int = 0): List<IVote> = withContext(Dispatchers.IO) {
		require(chamber in listOf("senate", "house", "both")) { "Unknown chamber, use either: senate, house, both" }
		val res = api.getRecentVotes(key, chamber, page * pageSize).await().check()
		res.results ?: throw IllegalStateException("No results returned")
		pipe.transform(res.results)
	}
	
	/**
	 * Retrieves recent votes taken within congress. The returned data is a summary of the vote, but does
	 * not contain full data on how each member voted. To retrieve that data, use [getPositions]. In addition,
	 * the returned votes are _usually_ [LegislativeVote]s, but there are specific types for speaker votes
	 * ([SpeakerVote]) and motion votes ([MotionVote]).
	 */
	fun getRecentVotes(chamber: String): Flow<IVote> = asFlow {
		getRecentVotes(chamber, it)
	}
	
	/**
	 * Retrieves member-specific votes for a roll call vote. This also contains the original topic that was voted on,
	 * meaning that this endpoint retrieves the most information possible about any vote: the list of how each person voted,
	 * and all of the details about the original topic.
	 */
	suspend fun getPositions(congress: Long, chamber: String, session: Long, rollCall: Long): IVotePositions<*> = withContext(Dispatchers.IO) {
		require(chamber in listOf("senate", "house")) { "Unknown chamber, use either: senate, house" }
		val res = api.getVotes(key, congress, chamber, session, rollCall).fetch()
		res.results ?: throw IllegalStateException("No results returned")
		pipe.transform(res.results)
	}
	
	/**
	 * Retrieves recent votes on Presidential nominees that took place, which are always within the Senate. This
	 * returns an overview of the vote, but it does not contain data on how each member voted. Use [getPositions]
	 * to retrieve that information.
	 */
	suspend fun getSenateNominationVotes(congress: Long, page: Int = 0): List<LegislativeVote> = withContext(Dispatchers.IO) {
		queryFirstResult {
			api.getSenateNominationVotes(key, congress, page * pageSize).fetch()
		}.votes.map { pipe.toVote(it) as LegislativeVote }
	}
	
	/**
	 * Retrieves recent votes on Presidential nominees that took place, which are always within the Senate. This
	 * returns an overview of the vote, but it does not contain data on how each member voted. Use [getPositions]
	 * to retrieve that information.
	 */
	fun getSenateNominationVotes(congress: Long) = asFlow {
		getSenateNominationVotes(congress, it)
	}
	
	// ===============
	// === MEMBERS ===
	// ===============
	
	/**
	 * Retrieves the profile of a congressional member using their unique congressional identifier.
	 */
	suspend fun getMember(id: String): CongressMember = withContext(Dispatchers.IO) {
		pipe.transform(queryFirstResult {
			api.getMember(key, id).fetch()
		})
	}
	
	// TODO WIP
	suspend fun getMembers(congress: Long, chamber: String) {
		val res = queryFirstResult {
			api.getMemberList(key, congress, chamber).fetch()
		}
//		res.members.map {
//			it.copy(
//				chamber = res.chamber
//			)
//		}.map { pipe.transform(it) }
	}
	
	// TODO WIP
	suspend fun getHouseMembers(state: String) {
		val district: Int = when (state) {
			// States with at-large districts
			"AK", "DE", "MT", "ND", "SD", "VT", "WY" -> 1
			// Territories
			"GU", "AS", "VI", "MP" -> 1
			// DC
			"DC" -> 1
			else -> 0
		}
	}
	
	// ==================
	// === STATEMENTS ===
	// ==================
	
	suspend fun getRecentStatements(page: Int = 0): List<Statement> = withContext(Dispatchers.IO) {
		val res = api.getRecentStatements(key, page * pageSize).fetch()
		res.results ?: throw IllegalStateException("No results returned")
		res.results.map { pipe.transform(it) }
	}
	
	fun getRecentStatements() = asFlow {
		getRecentStatements(it)
	}
	
	suspend fun searchStatements(query: String, page: Int = 0): List<Statement> = withContext(Dispatchers.IO) {
		val res = api.searchStatements(key, query, page * pageSize).fetch()
		res.results ?: throw IllegalStateException("No results returned")
		res.results.map { pipe.transform(it) }
	}
	
	fun searchStatements(query: String) = asFlow {
		searchStatements(query, it)
	}
	
	suspend fun getStatementSubjects(): List<StatementSubject> = withContext(Dispatchers.IO) {
		val res = api.getStatementSubjects(key).fetch()
		res.results ?: throw IllegalStateException("No results returned")
		res.results.map { pipe.transform(it) }
	}
	
	suspend fun getStatementsBySubject(subject: String, page: Int = 0): List<Statement> = withContext(Dispatchers.IO) {
		val res = api.getStatementsBySubject(key, subject, page * pageSize).fetch()
		res.results ?: throw IllegalStateException("No results returned")
		res.results.map { pipe.transform(it) }
	}
	
	fun getStatmentsBySubject(subject: String) = asFlow {
		getStatementsBySubject(subject, it)
	}
	
	suspend fun getStatementsByMember(member: String, congress: Long, page: Int = 0): List<Statement> = withContext(Dispatchers.IO) {
		val res = api.getStatementsByMember(key, member, congress, page * pageSize).fetch()
		res.results ?: throw IllegalStateException("No results returned")
		res.results.map {
			pipe.transform(it.copy(member_id = member))
		}
	}
	
	/**
	 * Retrieves statements made by members about a certain bill. This method retrieves an individual page
	 * of statements. Using [getStatements] retrieves all statements made about a certain bill.
	 */
	suspend fun getStatements(congress: Long, bill: String, page: Int = 0): List<Statement> = withContext(Dispatchers.IO) {
		val res = api.getBillStatements(key, congress, bill, page * pageSize).fetch()
		res.results ?: throw IllegalStateException("No results returned")
		res.results.map { pipe.transform(it) }
	}
	
	/**
	 * Retrieves statements made by members about a certain bill. This method retrieves all statements made
	 * on a certain bill; each member can make multiple statements.
	 */
	fun getStatements(congress: Long, bill: String): Flow<Statement> = asFlow {
		getStatements(congress, bill, it)
	}
	
	// =============
	// === UTILS ===
	// =============
	
	/**
	 * ProPublica's API doesn't set HTTP return codes correctly. The status is almost always 200 even if an
	 * error has occurred (like a record not being found). This method correctly identifies and throws errors
	 * as necessary.
	 */
	private fun <T> PublicaAPIResponse<T>.check(): PublicaAPIResponse<T> {
		if (this.errors != null && this.errors.isNotEmpty())
			throw PublicaError(this.errors)
		if (this.results == null)
			throw IllegalStateException("Null results returned without an error. Response: $this")
		return this
	}
	
	/**
	 * Performs the fetching of the response from the API, as well as doing some error checking along the way.
	 * This method will rethrow errors returned from the ProPublica API.
	 */
	private suspend fun <T> Deferred<PublicaAPIResponse<T>?>?.fetch(): PublicaAPIResponse<T> {
		requireNotNull(this) {
			"The deferred instance is null - this can happen when dependency injection is not setup properly"
		}
		val res = requireNotNull(this.await()) { "Null response returned by Deferred.await()" }
		return res.check()
	}
	
	/**
	 * Queries an individual page of results from a Publica API endpoint. This is specifically for calls where the API
	 * returns a nested list in the `results` field even though there should only be a single one.
	 *
	 * @param call A function that should query a single page listing.
	 * @return the `results` field of the response, unless there was a problem with it.
	 */
	private suspend fun <T> queryFirstResult(call: suspend () -> PublicaAPIResponse<List<T>>): T {
		return withContext(Dispatchers.IO) {
			val res = call().check()
			res.results ?: throw IllegalStateException("Null results returned without an error. Response: $res")
			check(res.results.size <= 1) { "Multiple results returned when only one was expected" }
			res.results[0]
		}
	}
	
	/**
	 * Continues making the given call until the returned list is either empty or of a size less than
	 * the standard ProPublica page size. The function is a page index.
	 */
	internal fun <T> asFlow(call: suspend (Int) -> List<T>) = flow {
		var n = 0
		while (true) {
			val res = call(n)
			if (res.isEmpty()) break
			res.forEach { emit(it) }
			if (res.size < pageSize) break
			n++
		}
	}
	
	// TODO move this?
	companion object {
		fun start() {
			val app = module {
				single("publica-api-key") { System.getenv("PROPUBLICA-KEY") }
				single {
					GsonBuilder().apply {
						parseAdapter(LocalDate::class, LocalDate::parse)
						parseAdapter(LocalTime::class, LocalTime::parse)
						parseAdapter(ZonedDateTime::class, ZonedDateTime::parse)
					}.setPrettyPrinting().create()
				}
				single {
					OkHttpClient.Builder().apply {
						addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
					}.build()
				}
				single {
					Retrofit.Builder()
						.baseUrl("https://api.propublica.org/")
						.addCallAdapterFactory(CoroutineCallAdapterFactory())
						.addConverterFactory(GsonConverterFactory.create(get()))
						.client(OkHttpClient.Builder().apply {
							addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
						}.build())
						.build()
						.create<RawPublicaAPI>()
				}
				single {
					Retrofit.Builder()
						.baseUrl("https://voteview.com/")
						.addCallAdapterFactory(CoroutineCallAdapterFactory())
						.addConverterFactory(GsonConverterFactory.create(get()))
						.client(OkHttpClient.Builder().apply {
							addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
							addInterceptor(VOTE_VIEW_INTERCEPTOR)
						}.build())
						.build()
						.create<VoteViewAPI>()
				}
				single {
					PublicaAPI(get(), get("publica-api-key"))
				}
			}
			
			startKoin(listOf(app))
		}
	}
	
}

fun <T : Any> GsonBuilder.parseAdapter(type: KClass<T>, producer: (String) -> T) {
	this.registerTypeAdapter(type.java, object : TypeAdapter<T>() {
		override fun write(out: JsonWriter, value: T?) {
			if (value == null) out.nullValue() else out.value(value.toString())
		}
		
		override fun read(reader: JsonReader): T? {
			return if (reader.peek() == JsonToken.NULL) {
				reader.nextNull()
				return null
			} else {
				val str = reader.nextString()
				try {
					producer(str)
				} catch (e: Exception) {
					throw IllegalArgumentException("Failed to parse type $type from '$str'", e)
				}
			}
		}
	})
}

data class SearchRequest(
	internal val sort: String = "date",
	internal val order: String = "desc",
	internal val search: String = ""
) {
	
	/**
	 * Searches the given keywords as an OR search.
	 */
	fun keywords(list: List<String>) = this.copy(
		search = list.joinToString(" ")
	)
	
	fun keywords(vararg list: String) = this.copy(
		search = list.joinToString(" ")
	)
	
	/**
	 * Searches using the given phrase.
	 */
	fun phrase(phrase: String) = this.copy(
		search = "\"$phrase\""
	)
	
	/**
	 * Sorts the search results in an ascending manner.
	 */
	fun ascending() = this.copy(order = "asc")
	
	/**
	 * Sorts the search results in a descending manner.
	 */
	fun descending() = this.copy(order = "desc")
	
	/**
	 * Orders the search results by date.
	 */
	fun byDate() = this.copy(sort = "date")
	
	/**
	 * Orders the search results by relevance.
	 */
	fun byRelevance() = this.copy(sort = "_score")
	
}

// ====================
// === CONVENIENCES ===
// ====================

/**
 * Retrieves an in-depth look at the given bill.
 *
 * @param bill the metadata of the bill to lookup.
 */
suspend fun PublicaAPI.getDetails(bill: BillMeta) = getBillDetails(bill.slug, bill.congress)

/**
 * Retrieves amendments made to a specific bill.
 *
 * @param bill The metadata of the bill to get amendments for.
 * @param page The page of results to view.
 */
suspend fun PublicaAPI.getAmendments(bill: BillMeta, page: Int): List<BillAmendment> = getAmendments(bill.id, bill.congress, page)

/**
 * Retrieves amendments made to a specific bill.
 *
 * @param bill The metadata of the bill to get amendments for.
 */
fun PublicaAPI.getAmendments(bill: BillMeta): Flow<BillAmendment> = getAmendments(bill.id, bill.congress)

/**
 * Retrieves "recent" bills, as in bills that have had recent official activity on them, not necessarily just votes.
 * For example, being introduced to congress, or being vetoed.
 *
 * @param congress The congressional session ID to get bills for.
 * @param chamber The chamber to filter by, either `senate`, `house`, or `null` for both.
 * @param type The current status of the bill.
 * @param page The page number to get results for.
 */
suspend fun PublicaAPI.getRecentBills(congress: Long, chamber: Chamber?, type: BillStatus, page: Int = 0) =
	getRecentBills(congress, chamber?.name?.toLowerCase() ?: "both", type.name.toLowerCase(), page)

/**
 * Retrieves "recent" bills, as in bills that have had recent official activity on them, not necessarily just votes.
 * For example, being introduced to congress, or being vetoed. This method does not require a page number and will continue
 * producing bills until the API doesn't return a full set of bills.
 *
 * @param congress The congressional session ID to get bills for.
 * @param chamber The chamber to filter by, either `senate`, `house`, or `null` for both.
 * @param type The current status of the bill. Supported values: `introduced`, `updated`, `active`, `passed`, `enacted`, and `vetoed`.
 */
fun PublicaAPI.getRecentBills(congress: Long, chamber: Chamber?, type: BillStatus) =
	getRecentBills(congress, chamber?.name?.toLowerCase() ?: "both", type.name.toLowerCase())

/**
 * Retrieves bills that are expected to be voted on soon.
 */
suspend fun PublicaAPI.getUpcomingBills(chamber: Chamber) = getUpcomingBills(chamber.name.toLowerCase())

/**
 * Retrieves recent votes taken within congress.
 */
suspend fun PublicaAPI.getRecentVotes(chamber: Chamber?, page: Int = 0): List<IVote> =
	getRecentVotes(chamber?.name?.toLowerCase() ?: "both", page)

/**
 * Retrieves member-specific votes for a roll call vote.
 */
suspend fun PublicaAPI.getPositions(vote: VoteMeta) =
	getPositions(vote.congress, vote.chamber.name.toLowerCase(), vote.session, vote.rollCall)
