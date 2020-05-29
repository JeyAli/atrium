@file:Suppress("DEPRECATION" /* TODO remove with 1.0.0 */)
package ch.tutteli.atrium.assertions.iterable.contains.creators

import ch.tutteli.atrium.assertions.Assertion
import ch.tutteli.atrium.assertions.AssertionGroup
import ch.tutteli.atrium.assertions.builders.invisibleGroup
import ch.tutteli.atrium.assertions.iterable.contains.IterableContains
import ch.tutteli.atrium.assertions.iterable.contains.searchbehaviours.IterableContainsInAnyOrderOnlySearchBehaviour
import ch.tutteli.atrium.creating.AssertionPlant
import ch.tutteli.atrium.creating.SubjectProvider
import ch.tutteli.atrium.domain.builders.AssertImpl
import ch.tutteli.atrium.domain.robstoll.lib.assertions.LazyThreadUnsafeAssertionGroup
import ch.tutteli.atrium.reporting.Text
import ch.tutteli.atrium.reporting.translating.Translatable
import ch.tutteli.atrium.translations.DescriptionBasic.TO_BE
import ch.tutteli.atrium.translations.DescriptionIterableAssertion
import ch.tutteli.atrium.translations.DescriptionIterableAssertion.*

/**
 * Represents the base class for `in any order only` assertion creators and provides a corresponding template to fulfill
 * its responsibility.
 *
 * @param T The type of the [AssertionPlant.subject][SubjectProvider.subject] for which the `contains` assertion is be build.
 * @param SC The type of the search criteria.
 *
 * @property searchBehaviour The search behaviour -- in this case representing `in any order only` which is used to
 *   decorate the description (a [Translatable]) which is used for the [AssertionGroup].
 *
 * @constructor Represents the base class for `in any order only` assertion creators and provides a corresponding
 *   template to fulfill its responsibility.
 * @param searchBehaviour The search behaviour -- in this case representing `in any order only` which is used to
 *   decorate the description (a [Translatable]) which is used for the [AssertionGroup].
 */
@Deprecated("Please open an issue if you used this class; will be removed with 1.0.0")
abstract class IterableContainsInAnyOrderOnlyAssertionCreator<E, T : Iterable<E?>, SC>(
    private val searchBehaviour: IterableContainsInAnyOrderOnlySearchBehaviour
) : IterableContains.Creator<T, SC> {

    fun createAssertionGroup(plant: AssertionPlant<T>, searchCriterion: SC, otherSearchCriteria: Array<out SC>): AssertionGroup
        = createAssertionGroup(plant, listOf(searchCriterion, *otherSearchCriteria))

    final override fun createAssertionGroup(subjectProvider: SubjectProvider<T>, searchCriteria: List<SC>): AssertionGroup {
        return LazyThreadUnsafeAssertionGroup {
            val list = subjectProvider.subject.toMutableList()
            val actualSize = list.size
            val assertions = mutableListOf<Assertion>()

            val mismatches = createAssertionsForAllSearchCriteria(searchCriteria, list, assertions)
            val featureAssertions = createSizeFeatureAssertion(searchCriteria, actualSize)
            if (mismatches == 0 && list.isNotEmpty()) {
                featureAssertions.add(LazyThreadUnsafeAssertionGroup {
                    createExplanatoryGroupForMismatchesEtc(list, WARNING_ADDITIONAL_ENTRIES)
                })
            }
            assertions.add(AssertImpl.builder.feature
                .withDescriptionAndRepresentation(list::size.name, Text(actualSize.toString()))
                .withAssertions(featureAssertions)
                .build()
            )

            val description = searchBehaviour.decorateDescription(CONTAINS)
            val summary = AssertImpl.builder.summary
                .withDescription(description)
                .withAssertions(assertions)
                .build()

            if (mismatches != 0 && list.isNotEmpty()) {
                val warningDescription = when (list.size) {
                    mismatches -> WARNING_MISMATCHES
                    else -> WARNING_MISMATCHES_ADDITIONAL_ENTRIES
                }
                AssertImpl.builder.invisibleGroup
                    .withAssertions(
                        summary,
                        createExplanatoryGroupForMismatchesEtc(list, warningDescription)
                    )
                    .build()
            } else {
                summary
            }
        }
    }

    private fun createAssertionsForAllSearchCriteria(allSearchCriteria: List<SC>, list: MutableList<E?>, assertions: MutableList<Assertion>): Int {
        var mismatches = 0
        allSearchCriteria.forEach {
            val (found, assertion) = createAssertionForSearchCriterionAndRemoveMatchFromList(it, list)
            if (!found) ++mismatches
            assertions.add(assertion)
        }
        return mismatches
    }

    protected abstract fun createAssertionForSearchCriterionAndRemoveMatchFromList(searchCriterion: SC, list: MutableList<E?>): Pair<Boolean, Assertion>

    private fun createSizeFeatureAssertion(allSearchCriteria: List<SC>, actualSize: Int): MutableList<Assertion>
        = mutableListOf(AssertImpl.builder.descriptive
            .withTest { actualSize == allSearchCriteria.size }
            .withDescriptionAndRepresentation(
                TO_BE,
                Text(allSearchCriteria.size.toString())
            )
            .build()
        )

    private fun createExplanatoryGroupForMismatchesEtc(list: MutableList<E?>, warning: DescriptionIterableAssertion): AssertionGroup {
        val assertions = list.map { AssertImpl.builder.explanatory.withExplanation(it).build() }
        val additionalEntries = AssertImpl.builder.list
            .withDescriptionAndEmptyRepresentation(warning)
            .withAssertions(assertions)
            .build()
        return AssertImpl.builder.explanatoryGroup
            .withWarningType
            .withAssertion(additionalEntries)
            .build()
    }
}
