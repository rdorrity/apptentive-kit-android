package apptentive.com.android.feedback.platform

import apptentive.com.android.feedback.model.Person
import apptentive.com.android.util.Factory

internal class DefaultPersonFactory : Factory<Person> {
    override fun create(): Person = Person()
}
