package ndawg.publica

import ndawg.publica.raw.*
import ndawg.publica.types.*

/**
 * Transforms raw API results into their more friendly types.
 */
class PublicaPipeline {
	
	/**
	 * Transforms the given list of recent votes into their respective vote types. This
	 * includes special support for a [SpeakerVote], [MotionVote], and [LegislativeVote].
	 * These votes do not have per position position information attached.
	 */
	fun transform(raw: RawRecentVotes): List<IVote> {
		return raw.votes.map(::toVote)
	}
	
	/**
	 * Transform the given raw vote into the respective vote positions, which also includes
	 * the vote itself. This method handles special types of votes, like the House's election
	 * of a speaker.
	 */
	fun transform(raw: RawVote): IVotePositions<*> = pipe(raw) {
		require(raw.positions != null) {
			// A RawVote with positions should always be passed to `toVote`
			"RawVote with null positions passed to the incorrect transform method."
		}
		
		when (raw.question) {
			"Election of the Speaker" -> SpeakerVotePositions.from(raw)
			else -> LegislativeVotePositions.from(raw)
		}
	}
	
	/**
	 * A convenience method to convert the given result objects underlying [RawVote]
	 * object to its respective [IVotePositions] object via `transform(RawVote)`.
	 */
	fun transform(raw: RawVoteResults): IVotePositions<*> = pipe(raw) {
		transform(it.votes.vote)
	}
	
	/**
	 * Transforms [RawBillDetails] to [BillDetails] using [BillDetails.Companion.from].
	 */
	fun transform(raw: RawBillDetails) = pipe(raw, BillDetails.Companion::from)
	
	/**
	 * Transforms [RawBillAmendment] to [BillAmendment] using [BillAmendment.from]
	 */
	fun transform(raw: RawBillAmendment) = pipe(raw, BillAmendment.Companion::from)
	
	/**
	 * Transforms [RawMember] to [CongressMember] using [CongressMember.Companion.from].
	 */
	fun transform(raw: RawMember) = pipe(raw, CongressMember.Companion::from)
	
	/**
	 * Transforms [RawUpcomingBill] to [UpcomingBill] using [UpcomingBill.Companion.from].
	 */
	fun transform(raw: RawUpcomingBill) = pipe(raw, UpcomingBill.Companion::from)
	
	/**
	 * Transforms [RawStatement] to [Statement] using [Statement.Companion.from].
	 */
	fun transform(raw: RawStatement) = pipe(raw, Statement.Companion::from)
	
	/**
	 * Transforms [RawStatement] to [Statement] using [Statement.Companion.from].
	 */
	fun transform(raw: RawStatementSubject) = pipe(raw, StatementSubject.Companion::from)
	
	/**
	 * Transforms [RawBillWithCosponsors.RawCosponsor] to [SponsorMeta] using [SponsorMeta.Companion.from].
	 */
	fun transform(raw: RawBillWithCosponsors.RawCosponsor) = pipe(raw, SponsorMeta.Companion::from)
	
	/**
	 * Transforms [RawBillDetails.RawBillSubject] to [BillSubject] using [BillSubject.Companion.from].
	 */
	fun transform(raw: RawBillDetails.RawBillSubject) = pipe(raw, BillSubject.Companion::from)
	
	/**
	 * Utility method for converting a RawVote to the most appropriate [IVote] implementation
	 * that is relevant. These votes include no position info, unlike [IVotePositions].
	 */
	internal fun toVote(raw: RawVote): IVote = pipe(raw) {
		when {
			raw.question == "Election of the Speaker" -> SpeakerVote.from(raw)
			MotionVote.isMotion(raw) -> MotionVote.from(raw)
			else -> LegislativeVote.from(raw)
		}
	}
	
	/**
	 * A convenience method that wraps a transformation attempt and throws a
	 * [TransformException] in the event of a failure.
	 */
	private fun <T : Any, R> pipe(arg: T, block: (T) -> R): R {
		try {
			return block(arg)
		} catch (e: Exception) {
			throw TransformException(arg, e)
		}
	}
	
	/**
	 * An exception thrown when a raw object fails to be properly transformed.
	 *
	 * @param raw The object that was supposed to be transformed.
	 * @param cause The cause of the failure to transform.
	 */
	class TransformException(val raw: Any, cause: Throwable) : RuntimeException("Failed to transform object in pipeline. Object: $raw", cause)
	
}