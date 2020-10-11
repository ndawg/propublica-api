package ndawg.publica.types

import ndawg.publica.PublicaAPI
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * A manager for data for members of congress. Due to ProPublica's varied responses on member types, it is
 * necessary to perform some extra heavy lifting for managing members efficiently. This class serves
 * as a cache for members.
 *
 * The following properties are only available when listing members, and not when retrieving members specifically:
 * - dw_nominate
 * - ideal_point
 * - total_votes
 * - missed_votes
 * - total_present
 * - senate_class
 * - state_rank
 * - lis_id
 * - next_election
 */
class MemberManager(val api: PublicaAPI) {
	
	private val members = mutableMapOf<String, CongressMember>()
	
	/**
	 * Loads all members at once in a single query to allow subsequent members to be retrieved.
	 */
	private suspend fun loadAll(ids: List<String>): Flow<CongressMember> = coroutineScope {
		ids.asFlow().map { id ->
			async(Dispatchers.IO) {
				getMember(id)
			}
		}.buffer(10).map { it.await() }
	}
	
	/**
	 * Loads an individual member.
	 */
	suspend fun getMember(id: String): CongressMember {
		if (id !in members)
			members[id] = api.getMember(id)
		return members[id]!!
	}
	
	/**
	 * Properties only available when listing members.
	 */
	data class ListExlusiveData(
		val dw_nominate: Float,
		val total_votes: Int,
		val missed_votes: Int,
		val total_present: Int,
		val senate_class: String,
		val state_rank: String,
		val lis_id: String,
		val next_election: String
	)
	
}
