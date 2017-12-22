package ch.tutteli.atrium.spec.assertions

import ch.tutteli.atrium.api.cc.en_UK.containsDefaultTranslationOf
import ch.tutteli.atrium.api.cc.en_UK.message
import ch.tutteli.atrium.api.cc.en_UK.toThrow
import ch.tutteli.atrium.assertions.DescriptionCharSequenceAssertion.AT_MOST
import ch.tutteli.atrium.creating.AssertionPlant
import ch.tutteli.atrium.reporting.translating.Translatable
import ch.tutteli.atrium.reporting.translating.Untranslatable
import ch.tutteli.atrium.spec.AssertionVerb
import ch.tutteli.atrium.spec.AssertionVerbFactory
import ch.tutteli.atrium.spec.describeFun
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.SpecBody
import org.jetbrains.spek.api.dsl.context
import org.jetbrains.spek.api.include

abstract class CharSequenceContainsDefaultTranslationAssertionSpec(
    verbs: AssertionVerbFactory,
    containsDefaultTranslationOf: String,
    containsAtLeastTriple: Triple<String, (String, String) -> String, AssertionPlant<CharSequence>.(Int, Translatable, Array<out Translatable>) -> AssertionPlant<CharSequence>>,
    containsAtMostTriple: Triple<String, (String, String) -> String, AssertionPlant<CharSequence>.(Int, Translatable, Array<out Translatable>) -> AssertionPlant<CharSequence>>,
    containsAtMostIgnoringCaseTriple: Triple<String, (String, String) -> String, AssertionPlant<CharSequence>.(Int, Translatable, Array<out Translatable>) -> AssertionPlant<CharSequence>>,
    describePrefix: String = "[Atrium] "
) : Spek({

    include(object : ch.tutteli.atrium.spec.assertions.SubjectLessAssertionSpec<CharSequence>(describePrefix,
        containsAtLeastTriple.first to mapToCreateAssertion { containsAtLeastTriple.third(this, 2, AssertionVerb.ASSERT, arrayOf()) },
        containsAtMostTriple.first to mapToCreateAssertion { containsAtMostTriple.third(this, 2, AssertionVerb.ASSERT, arrayOf()) },
        containsAtMostIgnoringCaseTriple.first to mapToCreateAssertion { containsAtMostIgnoringCaseTriple.third(this, 2, AssertionVerb.ASSERT, arrayOf()) }
    ) {})

    include(object : ch.tutteli.atrium.spec.assertions.CheckingAssertionSpec<String>(verbs, describePrefix,
        checkingTriple(containsAtLeastTriple.first, { containsAtLeastTriple.third(this, 2, AssertionVerb.ASSERT, arrayOf()) }, "assert a, assert b", "a"),
        checkingTriple(containsAtMostTriple.first, { containsAtMostTriple.third(this, 2, AssertionVerb.ASSERT, arrayOf()) }, "assert", "assert, assert and assert"),
        checkingTriple(containsAtMostIgnoringCaseTriple.first, { containsAtMostIgnoringCaseTriple.third(this, 2, AssertionVerb.ASSERT, arrayOf()) }, "Assert aSSert", "assert Assert AsSert")
    ) {})

    fun describeFun(vararg funName: String, body: SpecBody.() -> Unit)
        = describeFun(describePrefix, funName, body = body)

    val assert: (CharSequence) -> AssertionPlant<CharSequence> = verbs::checkImmediately
    val expect = verbs::checkException

    val text = "Assert - assert, assert, assert - ASSERT; expect the thrown exception"
    val fluent = assert(text)

    val (_, containsAtLeastTest, containsAtLeastFunArr) = containsAtLeastTriple
    fun AssertionPlant<CharSequence>.containsAtLeastFun(atLeast: Int, a: Translatable, vararg aX: Translatable)
        = containsAtLeastFunArr(atLeast, a, aX)

    val (_, containsAtMostTest, containsAtMostFunArr) = containsAtMostTriple
    fun AssertionPlant<CharSequence>.containsAtMostFun(atLeast: Int, a: Translatable, vararg aX: Translatable)
        = containsAtMostFunArr(atLeast, a, aX)

    val (_, containsAtMostIgnoringCase, containsAtMostIgnoringCaseFunArr) = containsAtMostIgnoringCaseTriple
    fun AssertionPlant<CharSequence>.containsAtMostIgnoringCaseFun(atLeast: Int, a: Translatable, vararg aX: Translatable)
        = containsAtMostIgnoringCaseFunArr(atLeast, a, aX)

    describeFun(containsDefaultTranslationOf) {

        context("text $text") {
            test("${containsAtLeastTest("${AssertionVerb.ASSERT}", "once")} does not throw") {
                fluent.containsAtLeastFun(1, AssertionVerb.ASSERT)
            }
            test("${containsAtLeastTest("${AssertionVerb.ASSERT}, ${AssertionVerb.ASSERT} and ${AssertionVerb.ASSERT}", "once")} does not throw") {
                fluent.containsAtLeastFun(1, AssertionVerb.ASSERT, AssertionVerb.ASSERT, AssertionVerb.ASSERT)
            }

            test("${containsAtLeastTest("'${AssertionVerb.ASSERT}' and ${AssertionVerb.EXPECT_THROWN}", "once")} does not throw") {
                fluent.containsAtLeastFun(1, AssertionVerb.ASSERT, AssertionVerb.EXPECT_THROWN)
            }

            test("${containsAtMostTest(AssertionVerb.ASSERT.toString(), "3 times")} does not throw") {
                fluent.containsAtMostFun(3, AssertionVerb.ASSERT)
            }
            test("${containsAtMostIgnoringCase(AssertionVerb.ASSERT.toString(), "5 times")} does not throw") {
                fluent.containsAtMostIgnoringCaseFun(5, AssertionVerb.ASSERT)
            }
            test("${containsAtMostIgnoringCase("${AssertionVerb.ASSERT} and ${Untranslatable("Assert")}", "4 times")} does not throw") {
                fluent.containsAtMostIgnoringCaseFun(5, AssertionVerb.ASSERT, Untranslatable("Assert"))
            }

            test("${containsAtMostTest(AssertionVerb.ASSERT.toString(), "twice")} throws AssertionError") {
                expect {
                    fluent.containsAtMostFun(2, AssertionVerb.ASSERT)
                }.toThrow<AssertionError> { message { containsDefaultTranslationOf(AT_MOST) } }
            }
            test("${containsAtMostIgnoringCase(AssertionVerb.ASSERT.toString(), "4 times")} throws AssertionError") {
                expect {
                    fluent.containsAtMostIgnoringCaseFun(4, AssertionVerb.ASSERT)
                }.toThrow<AssertionError> { message { containsDefaultTranslationOf(AT_MOST) } }
            }
        }
    }
})