package com.adasa.foodplan.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.adasa.foodplan.domain.model.OptimizerRule
import com.adasa.foodplan.domain.model.RuleConstraint
import com.adasa.foodplan.domain.model.RuleType

@Entity(tableName = "optimizer_rules")
data class OptimizerRuleEntity(
    @PrimaryKey val id: String,
    val type: String,           // RuleType.name
    val target: String,
    val targetName: String,
    val constraint: String,     // RuleConstraint.name
    val value: Int
) {
    fun toDomain() = OptimizerRule(
        id = id,
        type = RuleType.valueOf(type),
        target = target,
        targetName = targetName,
        constraint = RuleConstraint.valueOf(constraint),
        value = value
    )
}

fun OptimizerRule.toEntity() = OptimizerRuleEntity(
    id = id,
    type = type.name,
    target = target,
    targetName = targetName,
    constraint = constraint.name,
    value = value
)