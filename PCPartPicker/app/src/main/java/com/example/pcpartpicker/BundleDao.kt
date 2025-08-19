package com.example.pcpartpicker

import androidx.room.*

@Dao
interface BundleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bundle: BundleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(bundles: List<BundleEntity>)

    @Update
    suspend fun update(bundle: BundleEntity)

    @Delete
    suspend fun delete(bundle: BundleEntity)

    @Query("SELECT * FROM bundles WHERE bundleId = :bundleId")
    suspend fun getBundle(bundleId: Int): BundleEntity?

    @Query("SELECT * FROM components")
    suspend fun getAllComponents(): List<ComponentEntity>

    @Query("SELECT * FROM bundles WHERE listId = :listId")
    suspend fun getBundlesForList(listId: Int): List<BundleEntity>

    @Query("SELECT * FROM bundles")
    suspend fun getAllBundles(): List<BundleEntity>

    @Transaction
    @Query("SELECT * FROM bundles WHERE bundleId = :bundleId")
    suspend fun getBundleWithComponents(bundleId: Int): BundleWithComponents?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRef(crossRef: BundleComponentCrossRef)

    @Query("SELECT COUNT(*) FROM BundleComponentCrossRef WHERE url = :url")
    suspend fun getBundleCountForComponent(url: String): Int
}