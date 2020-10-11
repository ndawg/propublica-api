package ndawg.publica

import ndawg.publica.raw.PublicaAPIResponse
import ndawg.publica.raw.RawCommitteeResponse
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.jupiter.api.Tag

class CommitteeTests : PublicaAPITest() {
	
	@Test
	@Tag("live")
	fun live_allCommittees() {
		// This test verifies that all responses can be parsed, and all nullable types
		// are correctly identified. If nulls are in non-nullable fields, `copy` will error
		val (key, raw) = raw()
		runBlocking {
			raw.getCommittees(key, 116, "house").vet()
			raw.getCommittees(key, 116, "senate").vet()
			raw.getCommittees(key, 116, "joint").vet()
			
			raw.getCommittees(key, 115, "house").vet()
			raw.getCommittees(key, 115, "senate").vet()
			raw.getCommittees(key, 115, "joint").vet()
		}
	}
	
	@Test
	@Tag("live")
	fun live_specificCommittees() {
		val (key, raw) = raw()
		runBlocking {
			suspend fun list(res: Deferred<PublicaAPIResponse<List<RawCommitteeResponse>>>) =
				res.await().results!![0].committees
			
			list(raw.getCommittees(key, 116, "house")).forEach {
				raw.getCommittee(key, 116, "house", it.id).await().results!![0].vet()
			}
			list(raw.getCommittees(key, 116, "senate")).forEach {
				raw.getCommittee(key, 116, "senate", it.id).await().results!![0].vet()
			}
			list(raw.getCommittees(key, 116, "joint")).forEach {
				raw.getCommittee(key, 116, "joint", it.id).await().results!![0].vet()
			}
		}
	}
	
	@Test
	@Tag("live")
	fun live_specificSubcommittees() {
		val (key, raw) = raw()
		runBlocking {
			suspend fun list(res: Deferred<PublicaAPIResponse<List<RawCommitteeResponse>>>) =
				res.await().results!![0].committees
			
			list(raw.getCommittees(key, 116, "house")).forEach {
				it.subcommittees.forEach { sub ->
					raw.getSubcommittee(key, 116, "house", it.id, sub.id).await().results!![0].vet()
				}
			}
			list(raw.getCommittees(key, 116, "senate")).forEach {
				it.subcommittees.forEach { sub ->
					raw.getSubcommittee(key, 116, "senate", it.id, sub.id).await().results!![0].vet()
				}
			}
			list(raw.getCommittees(key, 116, "joint")).forEach {
				it.subcommittees.forEach { sub ->
					raw.getSubcommittee(key, 116, "joint", it.id, sub.id).await().results!![0].vet()
				}
			}
		}
	}
	
}