package apptentive.com.android.feedback.survey.viewmodel

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import apptentive.com.android.feedback.survey.R
import apptentive.com.android.feedback.survey.model.MultiChoiceQuestion
import apptentive.com.android.feedback.survey.view.SurveyQuestionContainerView
import apptentive.com.android.ui.ListViewItem
import apptentive.com.android.ui.setInvalid
import com.google.android.material.textfield.TextInputLayout

/**
 * Class which represents range question list item state
 * @param id question id
 * @param title question title
 * @param instructions optional instructions text (for example, "Required")
 * @param validationError contains validation error message in case if the question has an invalid
 *                        answer or <code>null</code> if the answer is valid.
 */
class MultiChoiceQuestionListItem(
    id: String,
    title: String,
    val answerChoices: List<Answer>,
    val allowMultipleAnswers: Boolean,
    instructions: String? = null,
    validationError: String? = null
) : SurveyQuestionListItem(
    id = id,
    type = Type.MultiChoiceQuestion,
    title = title,
    instructions = instructions,
    validationError = validationError
) {
    data class Answer(
        val type: MultiChoiceQuestion.ChoiceType,
        val id: String,
        val title: String,
        val isChecked: Boolean = false,
        val text: String? = null,
        val hint: String? = null
    ) {
        val isTextInputVisible get() = type == MultiChoiceQuestion.ChoiceType.select_other && isChecked
    }

    override fun getChangePayloadMask(oldItem: ListViewItem): Int {
        oldItem as MultiChoiceQuestionListItem

        var mask = super.getChangePayloadMask(oldItem)

        // check if any of the answer choices had changed
        if (oldItem.answerChoices != answerChoices) {
            mask = mask or MASK_SELECTED_ITEMS
        }
        return mask
    }

    //region Companion

    private companion object {
        const val MASK_SELECTED_ITEMS = MASK_CUSTOM shl 1
    }

    //endregion

    //region Equality

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MultiChoiceQuestionListItem) return false
        if (!super.equals(other)) return false

        if (answerChoices != other.answerChoices) return false
        if (allowMultipleAnswers != other.allowMultipleAnswers) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + answerChoices.hashCode()
        result = 31 * result + allowMultipleAnswers.hashCode()
        return result
    }

    //endregion

    //region View Holder

    class ViewHolder(
        itemView: SurveyQuestionContainerView,
        private val onSelectionChanged: (questionId: String, choiceId: String, selected: Boolean, text: String?) -> Unit
    ) : SurveyQuestionListItem.ViewHolder<MultiChoiceQuestionListItem>(itemView) {
        private val choiceContainer: ViewGroup = itemView.findViewById(R.id.choice_container)
        private lateinit var cachedViews: List<CachedViews>

        override fun bindView(
            item: MultiChoiceQuestionListItem,
            position: Int
        ) {
            super.bindView(item, position)

            choiceContainer.removeAllViews() // TODO: recycle views

            val layoutInflater = LayoutInflater.from(itemView.context)
            cachedViews = item.answerChoices.map { choice ->
                val choiceLayoutRes = if (item.allowMultipleAnswers)
                    R.layout.apptentive_survey_question_multiselect_choice else
                    R.layout.apptentive_survey_question_multichoice_choice

                val choiceView = layoutInflater.inflate(choiceLayoutRes, choiceContainer, false)

                // set title
                val compoundButton = choiceView.findViewById<CompoundButton>(R.id.checkbox)
                compoundButton.text = choice.title
                compoundButton.isChecked = choice.isChecked

                // show/hide text input layout
                val textInputLayout = choiceView.findViewById<TextInputLayout>(R.id.other_text_input_layout)
                textInputLayout.isVisible = choice.isTextInputVisible

                // update hint
                textInputLayout.editText?.setText(choice.text)
                textInputLayout.placeholderText = choice.hint
                textInputLayout.editText?.doAfterTextChanged {
                    onSelectionChanged.invoke(questionId, choice.id, true, it.toString())
                }

                compoundButton.setOnCheckedChangeListener { button, isChecked ->
                    // only handle user-generated events
                    // see: https://stackoverflow.com/a/41574200
                    if (button.isPressed) {
                        button.requestFocus()
                        onSelectionChanged.invoke(questionId, choice.id, isChecked, null) // text wasn't changed
                    }
                }

                choiceContainer.addView(choiceView)

                CachedViews(compoundButton, textInputLayout)
            }
        }

        override fun updateView(item: MultiChoiceQuestionListItem, position: Int, changeMask: Int) {
            super.updateView(item, position, changeMask)

            if ((changeMask and MASK_SELECTED_ITEMS) != 0) {
                updateSelection(item)
            }
        }

        override fun updateValidationError(errorMessage: String?) {
            super.updateValidationError(errorMessage)
            if (this::cachedViews.isInitialized) {
                cachedViews.forEach { view ->
                    view.textInputLayout.setInvalid(errorMessage != null)
                }
            }
        }

        private fun updateSelection(item: MultiChoiceQuestionListItem) {
            item.answerChoices.forEachIndexed { index, choice->
                cachedViews[index].compoundButton.isChecked = choice.isChecked
                cachedViews[index].textInputLayout.isVisible = choice.isTextInputVisible
            }
        }
    }

    //endregion

    private data class CachedViews(val compoundButton: CompoundButton, val textInputLayout: TextInputLayout)
}