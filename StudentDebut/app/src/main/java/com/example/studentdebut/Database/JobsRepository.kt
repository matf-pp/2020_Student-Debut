package com.example.studentdebut.Database

import com.example.studentdebut.MyApp.Companion.filtersJob
import com.example.studentdebut.MyApp.Companion.filtersPosition

// repozitorijum koji sluzi za upravljanje vise razlicitih izvora podataka
// koliko sam skontala nije neophodan, ali preporuceno je jer obezbedjuje cistiji kod
// i pojam komunikacije izmedju baze i ViewModel-a
class JobsRepository(private val jobsDao: DataAccessObject) {



    suspend fun getAllJobs(): MutableList<jobItem>? {
        return jobsDao.getAllJobs(filtersJob, filtersPosition)
    }
     suspend fun filterThroughLanguages() : List<jobItem>{
         return jobsDao.filterThroughLanguages()
     }
}
