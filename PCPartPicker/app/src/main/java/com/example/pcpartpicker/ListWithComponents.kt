package com.example.pcpartpicker

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class ListWithComponents(
    @Embedded val list: ListEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "url",
        associateBy = Junction(ListComponentCrossRef::class)
    )

    val component: List<ComponentEntity>
)
