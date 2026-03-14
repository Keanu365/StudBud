package io.github.keanu365.studbud

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

val supabase = createSupabaseClient(
    supabaseUrl = "https://dyikkrnyteudomofjrdz.supabase.co",
    supabaseKey = "sb_publishable_JqT90Z2mK6aOa7sFsz9PpQ_7OMuWspd"
) {
    install(Postgrest)
    install(Auth){
        alwaysAutoRefresh = true
    }
    install(Storage)
    install(ComposeAuth)
}