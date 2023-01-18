package com.desweemerl.compose.form.testui

import android.util.Log
import android.util.Log.INFO
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.desweemerl.compose.form.textControl
import com.desweemerl.compose.form.ui.FormTextField
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class FormTextFieldTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun valueTest() {
        val control = textControl("hello")
        composeTestRule.setContent {
            FormTextField(flowState = control, testTag = "field1")
        }
        val node = composeTestRule.onNodeWithTag("field1")
        node.assertTextContains("hello")
        node.performClick()
        assert(control.state.value.touched)
        assert(!control.state.value.dirty)

        node.performTextClearance()
        node.performTextInput("changed")
        assert(control.state.value.value == "changed")
        assert(control.state.value.touched)
        assert(control.state.value.dirty)
    }
}