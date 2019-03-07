package apptentive.com.android.feedback.model

import apptentive.com.android.feedback.utils.randomUUID
import org.rekotlin.StateType

data class ApptentiveState(
    val activeConversation: Conversation
) : StateType {
    companion object {
        internal fun initialState(): ApptentiveState = ApptentiveState(
            Conversation(
                localIdentifier = randomUUID(),
                person = Person(randomUUID())
            )
        )
    }
}