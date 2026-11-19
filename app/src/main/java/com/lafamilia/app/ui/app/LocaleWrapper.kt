package com.lafamilia.app.ui.app

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.lafamilia.app.data.settings.AppLanguage
import java.util.Locale

/**
 * A Context that serves strings from a chosen language. It still wraps the Activity,
 * so anything that needs the Activity (like starting a share intent) keeps working.
 */
private class LocaleContext(base: Context, locale: Locale) : ContextWrapper(base) {
    private val localizedResources: Resources = run {
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        base.createConfigurationContext(config).resources
    }

    override fun getResources(): Resources = localizedResources
}

/**
 * Re-draws [content] in the chosen language. For Arabic the layout direction becomes
 * right-to-left automatically, so rows, icons and text mirror correctly.
 */
@Composable
fun LocaleWrapper(language: AppLanguage, content: @Composable () -> Unit) {
    val tag = language.localeTag
    if (tag == null) {
        content()
        return
    }

    val base = LocalContext.current
    val locale = remember(tag) { Locale.forLanguageTag(tag) }
    val localizedContext = remember(base, locale) { LocaleContext(base, locale) }
    val configuration = remember(localizedContext) { localizedContext.resources.configuration }
    val direction = if (TextUtilsRtl.isRtl(locale)) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalConfiguration provides configuration,
        LocalLayoutDirection provides direction
    ) {
        content()
    }
}

/** Tiny helper so the direction rule is in one place. */
internal object TextUtilsRtl {
    fun isRtl(locale: Locale): Boolean =
        android.text.TextUtils.getLayoutDirectionFromLocale(locale) == android.view.View.LAYOUT_DIRECTION_RTL
}
