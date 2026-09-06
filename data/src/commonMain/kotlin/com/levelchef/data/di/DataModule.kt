package com.levelchef.data.di

import com.levelchef.data.repository.RecipeRepositoryImpl
import com.levelchef.data.repository.UserProfileRepositoryImpl
import com.levelchef.domain.repository.RecipeRepository
import com.levelchef.domain.repository.UserProfileRepository
import org.koin.dsl.module

/** Platform-agnostic repository bindings. Combine with `databaseModule` (androidMain) for the DB-backed repository. */
val dataModule = module {
    single<RecipeRepository> { RecipeRepositoryImpl() }
    single<UserProfileRepository> { UserProfileRepositoryImpl(get(), get(), get()) }
}
