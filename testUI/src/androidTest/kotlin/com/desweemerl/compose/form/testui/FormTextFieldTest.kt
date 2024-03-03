package com.desweemerl.compose.form.testui

import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.desweemerl.compose.form.controls.textControl
import com.desweemerl.compose.form.ui.asTextField
import org.junit.Rule
import org.junit.Test

class FormTextFieldTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun valueTest() {
        val control = textControl("hello")
        composeTestRule.setContent {
            control.asTextField(testTag = "field1")
        }
        val node = composeTestRule.onNodeWithTag("field1")
        node.assertTextContains("hello")
        node.performClick()
        assert(control.state.touched)
        assert(!control.state.dirty)

        node.performTextClearance()
        node.performTextInput("changed")
        assert(control.state.value == "changed")
        assert(control.state.touched)
        assert(control.state.dirty)
    }
}