package com.listadecompras.app

import android.app.Application
import com.listadecompras.app.data.AppDatabase
import com.listadecompras.app.data.ShoppingRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ListaDeComprasApp : Application() {

    lateinit var repository: ShoppingRepository
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        repository = ShoppingRepository(AppDatabase.build(this))

        val prefs = getSharedPreferences("app", MODE_PRIVATE)
        if (!prefs.getBoolean("seeded", false)) {
            appScope.launch {
                repository.seedIfEmpty()
                prefs.edit().putBoolean("seeded", true).apply()
            }
        }
    }
}
