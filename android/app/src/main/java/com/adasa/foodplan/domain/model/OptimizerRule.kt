package com.adasa.foodplan.domain.model

data class OptimizerRule(
    val id: String = "",
    val type: RuleType,
    val target: String,        // diet category name or ingredientId
    val targetName: String,    // human-readable display name
    val constraint: RuleConstraint,
    val value: Int             // the X in "min/max X per week"
)

enum class RuleType { DIET_CATEGORY, INGREDIENT }
enum class RuleConstraint { MIN_PER_WEEK, MAX_PER_WEEK }