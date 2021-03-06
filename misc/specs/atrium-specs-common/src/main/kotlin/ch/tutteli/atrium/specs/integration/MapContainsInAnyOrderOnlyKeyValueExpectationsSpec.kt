package ch.tutteli.atrium.specs.integration

import ch.tutteli.atrium.api.fluent.en_GB.*
import ch.tutteli.atrium.api.verbs.internal.expect
import ch.tutteli.atrium.creating.Expect
import ch.tutteli.atrium.specs.*
import org.spekframework.spek2.style.specification.Suite

abstract class MapContainsInAnyOrderOnlyKeyValueExpectationsSpec(
    keyWithValueAssertions: MFun2<String, Int, Expect<Int>.() -> Unit>,
    keyWithNullableValueAssertions: MFun2<String?, Int?, (Expect<Int>.() -> Unit)?>,
    rootBulletPoint: String,
    successfulBulletPoint: String,
    failingBulletPoint: String,
    warningBulletPoint: String,
    listBulletPoint: String,
    explanatoryBulletPoint: String,
    featureArrow: String,
    featureBulletPoint: String,
    describePrefix: String = "[Atrium] "
) : MapLikeContainsFormatSpecBase(
    rootBulletPoint,
    successfulBulletPoint,
    failingBulletPoint,
    warningBulletPoint,
    listBulletPoint,
    explanatoryBulletPoint,
    featureArrow,
    featureBulletPoint,
    {

        include(object : SubjectLessSpec<Map<out String, Int>>(
            describePrefix,
            keyWithValueAssertions.forSubjectLess(
                keyValue("a") { toBe(1) },
                arrayOf(keyValue("a") { isLessThanOrEqual(2) })
            )
        ) {})

        include(object : SubjectLessSpec<Map<out String?, Int?>>(
            "$describePrefix[nullable Key] ",
            keyWithNullableValueAssertions.forSubjectLess(
                keyNullableValue(null) { toBe(1) },
                arrayOf(keyNullableValue("a", null))
            )
        ) {})

        include(object : AssertionCreatorSpec<Map<out String, Int>>(
            describePrefix, map,
            assertionCreatorSpecTriple(keyWithValueAssertions.name, "$lessThanDescr: 2",
                {
                    keyWithValueAssertions(
                        this,
                        keyValue("a") { isLessThan(2) },
                        arrayOf(keyValue("b") { isLessThan(3) })
                    )
                },
                { keyWithValueAssertions(this, keyValue("a") { }, arrayOf(keyValue("a") { })) }
            )
        ) {})

        include(object : AssertionCreatorSpec<Map<out String?, Int?>>(
            "$describePrefix[nullable] ", mapOf("a" to 1, "b" to null),
            assertionCreatorSpecTriple(keyWithNullableValueAssertions.name, "$lessThanDescr: 2",
                {
                    keyWithNullableValueAssertions(
                        this,
                        keyNullableValue("a") { isLessThan(2) },
                        arrayOf(keyNullableValue("b", null))
                    )
                },
                { keyWithNullableValueAssertions(this, keyNullableValue("a") { }, arrayOf()) }
            )
        ) {})

        fun describeFun(vararg pairs: SpecPair<*>, body: Suite.() -> Unit) =
            describeFunTemplate(describePrefix, pairs.map { it.name }.toTypedArray(), body = body)


        describeFun(keyWithValueAssertions, keyWithValueAssertions) {
            val containsKeyWithValueAssertionsFunctions =
                uncheckedToNonNullable(keyWithValueAssertions, keyWithNullableValueAssertions)
            val emptyMap: Map<out String, Int> = mapOf()

            context("empty map") {
                containsKeyWithValueAssertionsFunctions.forEach { (name, containsFun) ->
                    it("$name - a to { toBe(1) } throws AssertionError, reports a") {
                        expect {
                            expect(emptyMap).containsFun(keyValue("a") { toBe(1) }, arrayOf())
                        }.toThrow<AssertionError> {
                            message {
                                containsInAnyOrderOnlyDescr()
                                containsSize(0, 1)
                                entryNonExisting("a", "$toBeDescr: 1")
                                containsNot(additionalEntriesDescr)
                            }
                        }
                    }

                    it("$name - a to { isLessThan(1) }, b to { toBe(3) }, c to { isLessThan(4) } } throws AssertionError, reports a, b and c") {
                        expect {
                            expect(emptyMap).containsFun(
                                keyValue("a") { isLessThan(1) },
                                arrayOf(
                                    keyValue("b") { toBe(3) },
                                    keyValue("c") { isLessThan(4) }
                                ))
                        }.toThrow<AssertionError> {
                            message {
                                containsInAnyOrderOnlyDescr()
                                containsSize(0, 3)
                                entryNonExisting("a", "$lessThanDescr: 1")
                                entryNonExisting("b", "$toBeDescr: 3")
                                entryNonExisting("c", "$lessThanDescr: 4")
                                containsNot(additionalEntriesDescr)
                            }
                        }
                    }
                }
            }

            context("map $map") {
                containsKeyWithValueAssertionsFunctions.forEach { (name, containsFun) ->
                    listOf(
                        "a to { toBe(1) }, b to { toBe(2) }" to listOf(
                            keyValue("a") { toBe(1) },
                            keyValue("b") { toBe(2) }),
                        "b to { toBe(2) }, a to { toBe(1) }" to listOf(
                            keyValue("b") { toBe(2) },
                            keyValue("a") { toBe(1) }),
                        "b to { isGreaterThan(1) }, a to { isLessThan(2) }" to listOf(
                            keyValue("b") { isGreaterThan(1) },
                            keyValue("a") { isLessThan(2) }),
                        "a to { isGreaterThan(0) }, b to { isLessThan(3) }" to listOf(
                            keyValue("a") { isGreaterThan(0) },
                            keyValue("b") { isLessThan(3) })
                    ).forEach { (description, list) ->
                        it("$name - $description does not throw") {
                            expect(map).containsFun(list.first(), list.drop(1).toTypedArray())
                        }
                    }

                    it("$name - a to { isLessThan(2) } throws AssertionError, reports second a and missing b") {
                        expect {
                            expect(map).containsFun(keyValue("a") { isLessThan(2) },arrayOf())
                        }.toThrow<AssertionError> {
                            message {
                                containsInAnyOrderOnlyDescr()
                                containsSize(2, 1)
                                entrySuccess("a", 1, "$lessThanDescr: 2")
                                additionalEntries("b" to 2)
                            }
                        }
                    }

                    it("$name - a to { isLessThan(2) }, a to { toBe(1) } throws AssertionError, reports second a and missing b") {
                        expect {
                            expect(map).containsFun(keyValue("a") { isLessThan(2) }, arrayOf(keyValue("a") { toBe(1) }))
                        }.toThrow<AssertionError> {
                            message {
                                containsInAnyOrderOnlyDescr()
                                entrySuccess("a", 1, "$lessThanDescr: 2")
                                entryNonExisting("a", "$toBeDescr: 1")
                                additionalEntries("b" to 2)

                                containsNot(sizeDescr)
                            }
                        }
                    }

                    it("$name - a to { isLessThan(3), b to { isLessThan(1), c to { toBe(4) } throws AssertionError, reports b and c") {
                        expect {
                            expect(map).containsFun(
                                keyValue("a") { isLessThan(3) },
                                arrayOf(
                                    keyValue("b") { isLessThan(1) },
                                    keyValue("c") { toBe(4) }
                                )
                            )
                        }.toThrow<AssertionError> {
                            message {
                                containsInAnyOrderOnlyDescr()
                                containsSize(2, 3)
                                entrySuccess("a", 1, "$lessThanDescr: 3")
                                entryFailing("b", 2, "$lessThanDescr: 1")
                                entryNonExisting("c", "$toBeDescr: 4")

                                containsNot(additionalEntriesDescr)
                            }
                        }
                    }
                }
            }
        }

        describeFun(keyWithNullableValueAssertions) {
            val containsKeyWithNullableValueAssertionsFun = keyWithNullableValueAssertions.lambda
            context("map: $nullableMap") {
                listOf(
                    "null { toBe(1) }, (a, null), b{ toBe(2) }" to
                        listOf(
                            keyNullableValue(null) { toBe(1) },
                            keyNullableValue("a", null),
                            keyNullableValue("b") { toBe(2) }),
                    "b { toBe(2) }, null{ toBe(1) }, (a, null)" to
                        listOf(
                            keyNullableValue("b") { toBe(2) },
                            keyNullableValue(null) { toBe(1) },
                            keyNullableValue("a", null)
                        )
                ).forEach { (description, keyValues) ->
                    it("$description does not throw") {
                        expect(nullableMap).containsKeyWithNullableValueAssertionsFun(
                            keyValues.first(),
                            keyValues.drop(1).toTypedArray()
                        )
                    }
                }
                it("a to { toBe(1) } throws AssertionError, reports failure and missing null and b") {
                    expect {
                        expect(nullableMap).containsKeyWithNullableValueAssertionsFun(
                            keyNullableValue("a") { toBe(1) },
                            arrayOf()
                        )
                    }.toThrow<AssertionError> {
                        message {
                            containsInAnyOrderOnlyDescr()
                            containsSize(3, 1)
                            entryFailingExplaining("a", null, "$toBeDescr: 1")
                            additionalEntries(null to 1, "b" to 2)
                        }
                    }
                }

                it("a to { toBe(1) }, c to { isLessThan(3) }, null to null, b to { isLessThan(3) } throws AssertionError, reports all but b") {
                    expect {
                        expect(nullableMap).containsKeyWithNullableValueAssertionsFun(
                            keyNullableValue("a") { toBe(1) },
                            arrayOf(
                                keyNullableValue("c") { isLessThan(3) },
                                keyNullableValue(null, null),
                                keyNullableValue("b") { isLessThan(3) }
                            )
                        )
                    }.toThrow<AssertionError> {
                        message {
                            containsInAnyOrderOnlyDescr()
                            containsSize(3, 4)
                            entryFailingExplaining("a", null, "$toBeDescr: 1")
                            entryNonExisting("c", "$lessThanDescr: 3")
                            entryFailing(null, "1", "$toBeDescr: null")
                            entrySuccess("b", "2", "$lessThanDescr: 3")

                            containsNot(additionalEntriesDescr)
                        }
                    }
                }
            }
        }
    })
