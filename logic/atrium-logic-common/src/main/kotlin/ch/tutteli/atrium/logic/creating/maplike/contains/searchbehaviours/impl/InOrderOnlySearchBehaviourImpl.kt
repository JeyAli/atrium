package ch.tutteli.atrium.logic.creating.maplike.contains.searchbehaviours.impl

import ch.tutteli.atrium.logic.creating.maplike.contains.searchbehaviours.InOrderOnlySearchBehaviour
import ch.tutteli.atrium.reporting.translating.Translatable
import ch.tutteli.atrium.reporting.translating.TranslatableWithArgs
import ch.tutteli.atrium.translations.DescriptionMapLikeAssertion

class InOrderOnlySearchBehaviourImpl : InOrderOnlySearchBehaviour {
    override fun decorateDescription(description: Translatable): Translatable =
        TranslatableWithArgs(DescriptionMapLikeAssertion.IN_ORDER_ONLY, description)
}
