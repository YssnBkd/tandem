package org.epoque.tandem

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform