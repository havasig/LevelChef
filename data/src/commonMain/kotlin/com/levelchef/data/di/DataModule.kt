package com.levelchef.data.di

import com.levelchef.data.repository.UserProfileRepositoryImpl
import com.levelchef.domain.repository.UserProfileRepository
import org.koin.dsl.module

/** Platform-agnostic repository bindings. Combine with `databaseModule` (androidMain) for the DB-backed repos. */
val dataModule = module {
    single<UserProfileRepository> { UserProfileRepositoryImpl(get(), get(), get()) }
}
