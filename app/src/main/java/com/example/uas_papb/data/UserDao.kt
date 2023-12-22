package com.example.uas_papb.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import java.util.concurrent.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user")
    fun getAll(): List<User>

    @Query("SELECT * FROM user WHERE id IN (:userIds)")
    fun loadAllByIds(userIds: ArrayList<String>): List<User>

    @Query("SELECT * FROM user WHERE id IN (:email)")
    fun findbyEmail(email: String): User

    @Query("SELECT * FROM user WHERE id IN (:userId)")
    fun selectById(userId: String): User

    @Insert
    fun insertAll(vararg user : User)

    @Delete
    fun delete(user : User)

    @Query("DELETE FROM user WHERE id LIKE :userId")
    fun deleteByID(userId: String)

    @Query("SELECT * FROM user ORDER BY name ASC")
    fun getAlphabetized(): kotlinx.coroutines.flow.Flow<List<User>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert (user: User)

    @Update
    fun update (user: User)

    @get:Query("SELECT * FROM user ORDER BY id ASC")
    val allNotes: LiveData<List<User>>
}