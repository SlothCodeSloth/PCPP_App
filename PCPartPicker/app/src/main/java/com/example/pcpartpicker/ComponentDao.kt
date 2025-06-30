package com.example.pcpartpicker

import androidx.room.*

@Dao
interface ComponentDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertComponent(componentEntity: ComponentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(list: ListEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrossRef(crossRef: ListComponentCrossRef)

    @Transaction
    @Query("SELECT * FROM lists")
    suspend fun getAllListsWithComponents(): List<ListWithComponents>

    @Transaction
    @Query("SELECT * FROM lists WHERE id = :listId")
    suspend fun getListWithComponents(listId: Int): ListWithComponents
}