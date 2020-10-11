package ndawg

import ndawg.publica.types.Chamber
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Deferred
import okhttp3.Interceptor
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path

// https://voteview.com/static/data/out/members/S116_members.json
interface VoteViewAPI {
	@GET("static/data/out/members/{chamber}{session}_members.json")
	fun getMembers(@Path("chamber") chamber: Char,
	               @Path("session") session: Long):
		Deferred<List<MemberScore>>
}

fun VoteViewAPI.getMembers(chamber: Chamber, session: Long) = getMembers(chamber.name[0], session)

data class MemberScore(
	val congress: Long,
	val chamber: Chamber,
	val icpsr: Long,
	@SerializedName("state_icpsr")
	val stateIcpsr: Long,
	@SerializedName("district_code")
	val districtCode: Long,
	@SerializedName("state_abbrev")
	val stateAbbrev: String,
	@SerializedName("party_code")
	val partyCode: Long,
	val occupancy: String,
	@SerializedName("last_means")
	val lastMeans: String,
	val bioname: String,
	@SerializedName("bioguide_id")
	val bioguideID: String,
	val born: Long,
	val died: Long? = null,
	/**
	 * The first scale of the dw_nominate, which is focuses on economic matters
	 * "redistributive" (liberal) vs "economic" (conservative)
	 * -1 is most liberal, 0 is center, 1 is most conservative
	 */
	@SerializedName("nominate_dim1")
	val nominateDim1: Double? = null,
	/**
	 * The second scale of the dw_nominate, which focuses on social matters
	 * "social" (liberal) vs "racial" (conservative)
	 * -1 is most liberal, 0 is center, 1 is most conservative
	 */
	@SerializedName("nominate_dim2")
	val nominateDim2: Double? = null,
	@SerializedName("nominate_log_likelihood")
	val nominateLogLikelihood: Double? = null,
	@SerializedName("nominate_geo_mean_probability")
	val nominateGeoMeanProbability: Double? = null,
	@SerializedName("nominate_number_of_votes")
	val nominateNumberOfVotes: String,
	@SerializedName("nominate_number_of_errors")
	val nominateNumberOfErrors: String,
	val conditional: Any? = null,
	@SerializedName("nokken_poole_dim1")
	val nokkenPooleDim1: Double,
	@SerializedName("nokken_poole_dim2")
	val nokkenPooleDim2: Double
)

/**
 * Because VoteView's JSON page returns a malformed string, fix it ourselves.
 */
val VOTE_VIEW_INTERCEPTOR = Interceptor { chain ->
	val res = chain.proceed(chain.request())
	
	res.body()?.let { body ->
		res.newBuilder().body(
			ResponseBody.create(body.contentType(),
				"[${body.string().replace("\n", ",\n")}]"))
			.build()
	} ?: res
}