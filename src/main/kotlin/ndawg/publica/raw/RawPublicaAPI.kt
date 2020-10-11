package ndawg.publica.raw

import kotlinx.coroutines.Deferred
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

@Suppress("DeferredIsResult")
interface RawPublicaAPI {
	
	// =============
	// === BILLS ===
	// =============
	
	@GET("congress/v1/bills/search.json")
	fun searchBills(@Header("X-API-Key") key: String,
	                @Query("query") query: String,
	                @Query("sort") sort: String,
	                @Query("dir") dir: String,
	                @Query("offset") offset: Int):
		Deferred<PublicaAPIResponse<List<RecentBillResults>>>
	
	@GET("congress/v1/{congress}/{chamber}/bills/{type}.json")
	fun getRecentBills(@Header("X-API-Key") key: String,
	                   @Path("congress") congressId: Long,
	                   @Path("chamber") chamber: String,
	                   @Path("type") type: String,
	                   @Query("offset") offset: Int):
		Deferred<PublicaAPIResponse<List<RecentBillResults>>>
	
	@GET("/congress/v1/members/{member-id}/bills/{type}.json")
	fun getBillsByMember(@Header("X-API-Key") key: String,
	                     @Path("member-id") memberId: String,
	                     @Path("type") type: String,
	                     @Query("offset") offset: Int):
		Deferred<PublicaAPIResponse<List<RawRecentMemberBills>>>
	
	@GET("/congress/v1/bills/subjects/{subject}.json")
	fun getBillsBySubject(@Header("X-API-Key") key: String,
	                      @Path("subject") subject: String,
	                      @Query("offset") offset: Int):
		Deferred<PublicaAPIResponse<List<RawBillDetails>>>
	
	@GET("/congress/v1/bills/upcoming/{chamber}.json")
	fun getUpcomingBills(@Header("X-API-Key") key: String,
	                     @Path("chamber") chamber: String):
		Deferred<PublicaAPIResponse<List<RawUpcomingBillResult>>>
	
	@GET("congress/v1/{congress}/bills/{bill-id}.json")
	fun getBillDetails(@Header("X-API-Key") key: String,
	                   @Path("congress") congressId: Long,
	                   @Path("bill-id") billId: String):
		Deferred<PublicaAPIResponse<List<RawBillDetails>>>
	
	@GET("congress/v1/{congress}/bills/{bill-id}/amendments.json")
	fun getBillAmendments(@Header("X-API-Key") key: String,
	                      @Path("congress") congressId: Long,
	                      @Path("bill-id") billId: String,
	                      @Query("offset") offset: Int):
		Deferred<PublicaAPIResponse<List<RawBillAmendmentsResult>>>
	
	@GET("congress/v1/{congress}/bills/{bill-id}/subjects.json")
	fun getBillSubjects(@Header("X-API-Key") key: String,
	                    @Path("congress") congressId: Long,
	                    @Path("bill-id") billId: String,
	                    @Query("offset") offset: Int):
		Deferred<PublicaAPIResponse<List<RawBillDetails>>>
	
	@GET("congress/v1/{congress}/bills/{bill-id}/related.json")
	fun getRelatedBills(@Header("X-API-Key") key: String,
	                    @Path("congress") congressId: Long,
	                    @Path("bill-id") billId: String,
	                    @Query("offset") offset: Int):
		Deferred<PublicaAPIResponse<List<RawBillDetails>>>
	
	@GET("congress/v1/bills/subjects/search.json")
	fun searchBillSubjects(@Header("X-API-Key") key: String,
	                       @Query("query") query: String):
		Deferred<PublicaAPIResponse<List<RawBillSubjectSearchResults>>>
	
	@GET("congress/v1/{congress}/bills/{bill-id}/cosponsors.json")
	fun getBillCosponsors(@Header("X-API-Key") key: String,
	                      @Path("congress") congressId: Long,
	                      @Path("bill-id") billId: String):
		Deferred<PublicaAPIResponse<List<RawBillWithCosponsors>>>
	
	// =============
	// === VOTES ===
	// =============
	
	@GET("congress/v1/{chamber}/votes/recent.json")
	fun getRecentVotes(@Header("X-API-Key") key: String,
	                   @Path("chamber") chamber: String,
	                   @Query("offset") offset: Int):
		Deferred<PublicaAPIResponse<RawRecentVotes>>
	
	@GET("/congress/v1/{congress}/{chamber}/sessions/{session-number}/votes/{roll-call-number}.json")
	fun getVotes(@Header("X-API-Key") key: String,
	             @Path("congress") congressId: Long,
	             @Path("chamber") chamber: String,
	             @Path("session-number") sessionNumber: Long,
	             @Path("roll-call-number") rollCall: Long):
		Deferred<PublicaAPIResponse<RawVoteResults>>
	
	@GET("/congress/v1/{congress}/nominations.json")
	fun getSenateNominationVotes(@Header("X-API-Key") key: String,
	                             @Path("congress") congressId: Long,
	                             @Query("offset") offset: Int):
		Deferred<PublicaAPIResponse<List<RawRecentVotes>>>
	
	// ===============
	// === MEMBERS ===
	// ===============
	
	@GET("/congress/v1/{congress}/{chamber}/members.json")
	fun getMemberList(@Header("X-API-Key") key: String,
	                  @Path("congress") congressId: Long,
	                  @Path("chamber") chamber: String):
		Deferred<PublicaAPIResponse<List<ListMemberResult>>>
	
	@GET("/congress/v1/members/senate/{state}/current.json")
	fun getCurrentSenateMembers(@Header("X-API-Key") key: String,
	                            @Path("state") state: String):
		Deferred<PublicaAPIResponse<List<RawBarebonesMember>>>
	
	@GET("/congress/v1/members/house/{state}/{district}/current.json")
	fun getCurrentHouseMembers(@Header("X-API-Key") key: String,
	                           @Path("state") state: String,
	                           @Path("district") district: String):
		Deferred<PublicaAPIResponse<List<RawBarebonesMember>>>
	
	@GET("/congress/v1/members/{member}.json")
	fun getMember(@Header("X-API-Key") key: String,
	              @Path("member") member: String):
		Deferred<PublicaAPIResponse<List<RawMember>>>
	
	// ==================
	// === STATEMENTS ===
	// ==================
	
	@GET("/congress/v1/statements/latest.json")
	fun getRecentStatements(@Header("X-API-Key") key: String,
	                        @Query("offset") page: Int):
		Deferred<PublicaAPIResponse<List<RawStatement>>>
	
	@GET("/congress/v1/{congress}/bills/{bill-id}/statements.json")
	fun getBillStatements(@Header("X-API-Key") key: String,
	                      @Path("congress") congressId: Long,
	                      @Path("bill-id") billId: String,
	                      @Query("offset") page: Int):
		Deferred<PublicaAPIResponse<List<RawStatement>>>
	
	@GET("/congress/v1/statements/search.json")
	fun searchStatements(@Header("X-API-Key") key: String,
	                     @Query("query") query: String,
	                     @Query("offset") page: Int):
		Deferred<PublicaAPIResponse<List<RawStatement>>>
	
	@GET("/congress/v1/statements/subjects.json")
	fun getStatementSubjects(@Header("X-API-Key") key: String):
		Deferred<PublicaAPIResponse<List<RawStatementSubject>>>
	
	// TODO custom response type wrapper
	@GET("/congress/v1/members/{member-id}/statements/{congress}.json")
	fun getStatementsByMember(@Header("X-API-Key") key: String,
	                          @Path("member-id") member: String,
	                          @Path("congress") congressId: Long,
	                          @Query("offset") page: Int):
		Deferred<PublicaAPIResponse<List<RawStatement>>>
	
	@GET("/congress/v1/statements/subject/{subject}.json")
	fun getStatementsBySubject(@Header("X-API-Key") key: String,
	                           @Path("subject") subject: String,
	                           @Query("offset") page: Int):
		Deferred<PublicaAPIResponse<List<RawStatement>>>
	
	// ==================
	// === COMMITTEES ===
	// ==================
	
	@GET("/congress/v1/{congress}/{chamber}/committees.json")
	fun getCommittees(@Header("X-API-Key") key: String,
	                  @Path("congress") congressId: Long,
	                  @Path("chamber") chamber: String):
		Deferred<PublicaAPIResponse<List<RawCommitteeResponse>>>
	
	@GET("/congress/v1/{congress}/{chamber}/committees/{committee-id}.json")
	fun getCommittee(@Header("X-API-Key") key: String,
	                 @Path("congress") congressId: Long,
	                 @Path("chamber") chamber: String,
	                 @Path("committee-id") commiteeId: String):
		Deferred<PublicaAPIResponse<List<RawCommittee>>>
	
	@GET("/congress/v1/{congress}/{chamber}/committees/{committee-id}/subcommittees/{subcommittee-id}.json")
	fun getSubcommittee(@Header("X-API-Key") key: String,
	                    @Path("congress") congressId: Long,
	                    @Path("chamber") chamber: String,
	                    @Path("committee-id") commiteeId: String,
	                    @Path("subcommittee-id") subcommitteeId: String):
		Deferred<PublicaAPIResponse<List<RawSubcommittee>>>
	
}

/**
 * An interface that represents data which should be present in every single API response.
 */
interface IPublicaAPIResponse<T> {
	val status: String
	val copyright: String
	val results: T?
	val errors: String?
}

/**
 * Generic type that wraps almost all of ProPublica's API responses. Ideally this would be able to wrap
 * every single response, but that might not be possible until v2.
 */
data class PublicaAPIResponse<T>(
	override val status: String,
	override val copyright: String,
	override val results: T?,
	override val errors: String?
) : IPublicaAPIResponse<T>

/**
 * Thrown when ProPublica's API returns an error, but the request was completed successfully.
 */
class PublicaError(error: String) : Exception(error)