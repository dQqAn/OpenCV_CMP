@file:Suppress("PARCELABLE_PRIMARY_CONSTRUCTOR_IS_EMPTY")

package com.example.common.navigation_native

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
internal actual class MainPager actual constructor() : Page, Parcelable

@Parcelize
internal actual class CameraPager actual constructor() : Page, Parcelable
