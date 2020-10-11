package ndawg.util

import ndawg.publica.raw.RawBillDetails
import ndawg.publica.types.*
import org.koin.standalone.KoinComponent
import java.time.LocalDate

// TODO rename PublicaUtil?
object CitizenUtil : KoinComponent {
    
    /**
     * Creates a timeline of important events, such as introduction and passage, from the details of a bill.
     * This timeline is basically the major events, and doesn't include committee referrals, debate time, etc.
     */
    // TODO unit tests
    fun createTimeline(bill: BillDetails): List<BillStatusModel> {
        val list = mutableListOf<BillStatusModel>()
        list.add(
            BillStatusModel(
                "Introduced",
                bill.dates.introduced,
                null,
                VoteResult.PASS
            )
        )

        val latestHouse = bill.history.find { it.chamber == Chamber.House }
        val latestSenate = bill.history.find { it.chamber == Chamber.Senate }

        // Single resolutions don't require passage from other Chambers
        if (bill.meta.type == "hres") {
            addChamber(list, latestHouse, Chamber.House)
            return list
        }

        if (bill.meta.type == "sres") {
            addChamber(list, latestSenate, Chamber.Senate)
            return list
        }

        // In the event that Senate passage is before House passage, make sure the order matches
        // TODO could probably just use if the bill starts with "h" or "s"
        val houseFailed = latestHouse == null || latestHouse.vote.result == VoteResult.FAIL
        val houseAfter = latestSenate != null && latestHouse != null && latestHouse.date.isAfter(latestSenate.date)
        if (latestSenate != null && (houseFailed || houseAfter)) {
            addChamber(list, latestSenate, Chamber.Senate)
            // Only show the House is the Senate has not failed
            if (latestSenate.vote.result == VoteResult.PASS) addChamber(list, latestHouse, Chamber.House)
        } else {
            addChamber(list, latestHouse, Chamber.House)
            if ((latestHouse != null && latestHouse.vote.result == VoteResult.PASS) || latestHouse == null)
                addChamber(list, latestSenate, Chamber.Senate)
        }

        // If it's passed in both chambers
        if (list.size == 3 && bill.actions.any { it.action_type == "ResolvingDifferences" }) {
            list.add(
                BillStatusModel(
                    "Resolving Differences",
                    bill.actions.find { it.action_type == "ResolvingDifferences" }?.datetime,
                    null, null
                )
            )
        }

        // Concurrent resolutions don't require passage by the President
        if (bill.meta.type == "hconres" || bill.meta.type == "jconres") return list

        if (list.last().result != VoteResult.FAIL) {
            list.add(
                BillStatusModel(
                    "Sent to President",
                    bill.actions.find { it.description.contains("Presented to President") }?.datetime,
                    null, null
                )
            )

            if (bill.dates.vetoed != null) {
                list.add(
                    BillStatusModel(
                        "Vetoed",
                        bill.actions.find { it.description == "Vetoed by President." }?.datetime,
                        null, VoteResult.FAIL
                    )
                )
            } else {
                list.add(
                    BillStatusModel(
                        "Signed Into Law",
                        bill.actions.find { it.description.contains("Became Public Law") }?.datetime,
                        null, null
                    )
                )
            }
        }

        return list
    }

    /**
     * Adds the BillStatusModel to represent a decision that a chamber has come to.
     */
    private fun addChamber(list: MutableList<BillStatusModel>, decision: BillDecision?, chamber: Chamber) {
        if (decision == null) {
            list.add(
                BillStatusModel(
                    "Voted on in the ${chamber.name}",
                    null,
                    null,
                    null
                )
            )
            return
        }

        // TODO support a null decision to indicate that the bill has not made it to this chamber yet
        var model = if (decision.vote.result == VoteResult.FAIL)
            BillStatusModel(
                "Failed in the ${chamber.name}",
                decision.date,
                decision.vote.overview,
                VoteResult.FAIL
            )
        else
            BillStatusModel(
                "Passed in the ${chamber.name}",
                decision.date,
                decision.vote.overview,
                VoteResult.PASS
            )

        if (decision.vote.overview != null) {
            val f = decision.vote.overview!!
            model =
                model.copy(status = model.status + " (${f.votes[VotePosition.Yes]} yea - ${f.votes[VotePosition.No]} nay)")
        } else model = model.copy(status = model.status + " (voice vote)")

        list.add(model)
    }

    /**
     * Attempts to fit the given vote question and description into a VoteGoal.
     */
    fun categorize(question: String, description: String = ""): VoteGoal? {
        return when {
            question == "On Passage" -> VoteGoal.PASS
            question == "On Passage of the Bill" -> VoteGoal.PASS
            question == "On the Resolution" -> VoteGoal.AGREE
            question == "On Motion to Adjourn" -> VoteGoal.ADJOURN
            question == "On the Motion to Adjourn" -> VoteGoal.ADJOURN
            question == "On Approving the Journal" -> VoteGoal.JOURNAL
            question == "On Motion to Fix the Convening Time" -> VoteGoal.FIX_TIME
            question == "On Motion to Reconsider" -> VoteGoal.RECONSIDER
            question == "On the Motion to Reconsider" -> VoteGoal.RECONSIDER
            question == "On Motion to go to Conference" -> VoteGoal.GO_TO_CONFERENCE
            question == "On Motion to Instruct Conferees" -> VoteGoal.INSTRUCT_CONFEREES
            question == "On Closing Portions of the Conference" -> VoteGoal.INSTRUCT_CONFEREES
            question == "On the Nomination" -> VoteGoal.ELECTION
            question == "On the Amendment" -> VoteGoal.AMEND
            question == "On the Joint Resolution" -> VoteGoal.PASS
            question == "On the Conference Report" -> VoteGoal.CONFERENCE_REPORT
            question == "On Agreeing to the Conference Report" -> VoteGoal.CONFERENCE_REPORT
            question == "On the Motion to Discharge" -> VoteGoal.DISCHARGE
            question == "On the Decision of the Chair" -> VoteGoal.APPEAL
            question == "On the Motion to Postpone" -> VoteGoal.DELAY
            question == "On the Resolution of Ratification" -> VoteGoal.RATIFY
            question == "On Consideration of the Resolution" -> VoteGoal.CONSIDER
            question == "On Agreeing to the Amendment" -> VoteGoal.AMEND
            question == "Motion to Commit" -> VoteGoal.GO_TO_CONFERENCE
            question == "Election of the Speaker" -> VoteGoal.ELECTION
            question == "Call by States" -> VoteGoal.CALL_BY_STATES
            question == "Call of the House" -> VoteGoal.ATTENDANCE
            question.contains("and Pass") -> VoteGoal.PASS
            question.contains("Agree") -> VoteGoal.AGREE
            question.contains("Concur") -> VoteGoal.CONCUR
            question.contains("Recommit") -> VoteGoal.RECOMMIT
            question.contains("Previous Question") -> VoteGoal.END_DEBATE
            question.contains("Proceed") -> VoteGoal.INTRODUCE
            question.contains("Cloture Motion") -> VoteGoal.CLOTURE
            question.contains("Attendance") -> VoteGoal.ATTENDANCE
            question.contains("Call of the House") -> VoteGoal.ATTENDANCE
            question.contains("Commit with Instructions") -> VoteGoal.AMEND
            question.contains("On the Point of Order") -> VoteGoal.POINT_OF_ORDER
            question.startsWith("Table") -> VoteGoal.TABLE
            question.matches(Regex("On (the )?Motion to Table( .*)?")) -> VoteGoal.TABLE
            question == "On the Motion" && description.startsWith("To amend") -> VoteGoal.AMEND
            question == "On the Motion" -> VoteGoal.PASS
            else -> null
        }
    }

    private fun isSuspendedRules(question: String) = question.contains("to Suspend the Rules")

    fun interpret(vote: LegislativeVote): String? {
        val suspended = isSuspendedRules(vote.question)
        val category = categorize(vote.question, vote.description) ?: return "Unknown vote type."
        val relevant = vote.description.replace(Regex(".*((H\\.J\\. Res\\.|H\\.R\\.) ?\\d+).*"), "$1")

        return buildString {
            if (vote.bill == null) {
                log().info { "Attempted to interpret vote without bill" }
                return@buildString
            }

            when (category) {
                VoteGoal.PASS -> {
                    // TODO not binding depending on billType
                    when (vote.bill.meta.type) {
                        // Simple Resolution
                        // Doesn't require passage from other chambers
                        "hres", "sres" -> {

                        }
                        // Concurrent Resolution
                        // Requires passage from other chambers but not president
                        "hconres", "sconres" -> {

                        }
                        // Joint Resolutions and Bills
                        // Requires both chambers and the President
                        "hjres", "sjres", "hr", "s" -> {
                            append("This was a vote to pass ${vote.bill.meta.name}, which is a piece of legislation.")
                            append(
                                " It requires passage from both the House and the Senate as well as approval from the" +
                                        " President before it becomes law."
                            )
                        }
                    }
                }
                VoteGoal.CLOTURE -> {
                    append(
                        "This was a vote on cloture, which ends debate on a topic and moves towards a vote of passage." +
                                " This vote can be used to overcome a filibuster."
                    )
                }
                VoteGoal.TABLE -> {
                    append("This was a vote to table a matter, which permanently kills the matter and any further debate.")
                }
                VoteGoal.CONFERENCE_REPORT -> {
                    append(
                        "This was a vote to agree to a final version of legislation which has been passed by both the" +
                                " House and the Senate. In other words, it resolves the differences between the chambers" +
                                " while approving the legislation."
                    )
                }
                VoteGoal.AGREE -> {
                    if (vote.description.startsWith("Providing for consideration")) {
                        append(
                            "This was a vote to set the rules for debate for ${vote.bill.meta.name}, such as restricting amendments " +
                                    "and floor debate time."
                        )
                    }
                }
                VoteGoal.END_DEBATE -> {
                    append("This was a vote to bring an end to the debate of legislation $relevant")
                }
                VoteGoal.RECOMMIT -> {
                    append(
                        "This was a vote to recommit the bill, which is the final opportunity to amend and debate a bill. " +
                                "This tactic is usually used by the minority party to present and vote on an alternate bill."
                    )
                }
            }

            // TODO movement method for linking to certain topics
            if (suspended) {
                appendln()
                appendln()
                append(
                    "For this vote, the House chose to suspend the rules, which is a common practice for bills " +
                            "that are not contentious."
                )
            }
        }.ifEmpty { null }
    }

    fun toDecision(bill: BillDetails?, action: RawBillDetails.RawAction): BillDecision? {
        val vote = when {
            // With bills that have a direct roll call, it can be used to supply voting information.
            action.description.contains(Regex("Roll no. \\d+")) -> {
                val roll = action.description.replace(Regex(".*Roll no. (\\d+).*"), "$1").toLong()
                val vote = bill?.let {
                    it.votes.find {
                        it.rollCall == roll
                    } // TODO votes can still be loaded with an API call
                }
                object : BillVote {
                    override val voice = false
                    override val result = when {
                        vote != null -> when {
                            vote.result == "Failed" -> VoteResult.FAIL
                            vote.result.contains("Rejected") -> VoteResult.FAIL
                            else -> VoteResult.PASS
                        }
                        action.description.contains("Failed") -> VoteResult.FAIL
                        else -> VoteResult.PASS
                    }
                    override val overview = vote
                }
            }
            action.description.contains(Regex("Record Vote Number: \\d+.")) -> {
                val roll = action.description.replace(Regex(".*Record Vote Number: (\\d+).*"), "$1").toLong()
                val vote = bill?.let {
                    it.votes.find {
                        it.rollCall == roll
                    } // TODO votes can still be loaded with an API call
                }
                object : BillVote {
                    override val voice = false
                    override val result = when {
                        vote != null -> when {
                            vote.result == "Failed" -> VoteResult.FAIL
                            vote.result.contains("Rejected") -> VoteResult.FAIL
                            else -> VoteResult.PASS
                        }
                        action.description.contains("Failed") -> VoteResult.FAIL
                        else -> VoteResult.PASS
                    }
                    override val overview = vote
                }
            }
            action.description.contains("On motion to suspend the rules and pass the bill, as amended Agreed to by voice vote.") ||
                    action.description.contains(Regex("Passed Senate .* by .*\\.")) ||
                    action.description.contains(Regex("(Senate|House) agreed to the (Senate|House) amendment to .* by .*\\.")) ||
                    action.description.contains(Regex("Senate concurred .* by .*\\.")) -> {
                object : BillVote {
                    override val voice = true
                    override val result = VoteResult.PASS
                    override val overview = null
                }
            }
            else -> null
        } ?: return null

        return BillDecision(
            chamber = action.chamber,
            date = action.datetime,
            description = action.description,
            type = action.action_type,
            vote = vote
        )
    }
}

enum class VoteGoal {
    /**
     * Used to agree to resolutions.
     */
    AGREE,
    PASS,
    /**
     * Used to ratify resolutions between the US and another country.
     */
    RATIFY,
    AMEND, // or recommit
    /**
     * Senate rules permit one motion to reconsider any question decided by vote, if offered by a senator who voted on
     * the winning side. Normally a supporter of the outcome immediately moves to reconsider the vote, and the same
     * senator or another immediately moves to table this motion, thus securing the outcome of the vote.
     */
    RECONSIDER,
    RECOMMIT,
    /**
     * When one chamber makes changes to a bill, the other must agree to the changes by concurring.
     */
    CONCUR,
    /**
     * A conference report is the final version of a bill agreed to by both chambers. It has to pass each chamber.
     * This is different from CONCUR and PASS.
     */
    CONFERENCE_REPORT,
    // TODO
    GO_TO_CONFERENCE,
    /**
     * After establishing a conference committee, each house may instruct the conferees to take a certain position in
     * the conference. These instructions to the conferees are not binding.
     * https://www.senate.gov/CRSpubs/d94b1431-8a98-465e-98c1-3dfa2cd4ccf2.pdf
     */
    INSTRUCT_CONFEREES,
    /**
     * Also referred to as "previous question", it immediately ends debate and starts a vote to pass.
     */
    END_DEBATE,
    /**
     * Used in both the Senate and House, if adopted a motion to table permanently kills a pending
     * matter and ends any further debate on the matter.
     **/
    TABLE,
    /**
     * Used to delay consideration of a bill.
     */
    DELAY,
    /**
     * A motion, usually offered by the majority leader to bring a bill or other measure up for consideration. The usual
     * way of bringing a measure to the floor when unanimous consent to do so cannot be obtained. For legislative
     * business, the motion is debatable under most circumstances, and therefore may be subject to filibuster.
     */
    INTRODUCE,
    /**
     * To "call up" or "lay down" a bill or other measure on the Senate floor is to
     * place it before the full Senate for consideration, including debate, amendment, and voting. Measures normally
     * come before the Senate for consideration by the majority leader requesting unanimous consent that the Senate
     * take it up.
     */
    CONSIDER,
    /** End the day's session. */
    ADJOURN,
    /**
     * The House is required to approve its previous day’s journal of activities each day,
     * but the journal is never the true subject of these votes. Because the journal is routinely approved,
     * representatives use these votes to appear to be voting for or against their party, whichever is more politically
     * useful, in aggregate vote statistics, knowing that their vote here does not truly matter. These votes may also be
     * used by the majority party to get representatives to the floor or to conduct an informal tally about their
     * position on another matter. That other matter is not made known to the general public.
     */
    JOURNAL,
    /**
     * The only procedure by which the Senate can vote to place a time limit on consideration of a bill or other matter,
     * and thereby overcome a filibuster. Under the cloture rule (Rule XXII), the Senate may limit consideration of a
     * pending matter to 30 additional hours, but only by vote of three-fifths of the full Senate, normally 60 votes.
     */
    CLOTURE,
    /**
     * Used to establish a quorum.
     */
    ATTENDANCE,
    /**
     * A claim made by a senator from the floor that a rule of the Senate is being violated. If the chair sustains the
     * point of order, the action in violation of the rule is not permitted.
     */
    POINT_OF_ORDER,
    /** Used for elections of the speaker and other congressional nominations. **/
    ELECTION,
    /**
     * A motion to discharge a committee from further consideration of a bill or resolution operates, when agreed to,
     * upon the bill or resolution as originally referred to the committee rather than as it may have been amended in
     * the committee before the committee acted upon it adversely
     */
    DISCHARGE,
    /**
     * On the Decision of the Chair
     * https://en.wikipedia.org/wiki/Appeal_(motion)
     */
    APPEAL,
    /** A formal roll call at the start of a new congress. **/
    CALL_BY_STATES,
    /** A basically irrelevant vote. */
    FIX_TIME;
}

/**
 * Data class that will be passed to the timeline to display.
 */
data class BillStatusModel(val status: String, val date: LocalDate?, val action: BillVoteOverview?, val result: VoteResult?)
