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

    @Query("DELETE FROM ListComponentCrossRef WHERE listId = :listId AND componentUrl = :componentUrl")
    suspend fun deleteCrossRef(listId: Int, componentUrl: String)

    @Query("SELECT COUNT(*) FROM ListComponentCrossRef WHERE componentUrl = :url")
    suspend fun getListCountForComponent(url: String): Int

    @Query("DELETE FROM components WHERE url = :url")
    suspend fun deleteComponent(url: String)
}