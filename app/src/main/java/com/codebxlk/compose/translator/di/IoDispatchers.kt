package com.codebxlk.compose.translator.di

import javax.inject.Qualifier
import kotlin.annotation.AnnotationRetention.RUNTIME

@Qualifier
@Retention(RUNTIME)
annotation class Dispatcher(val ioDispatchers: IoDispatchers)

enum class IoDispatchers { IO }
