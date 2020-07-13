package com.app.sourcing.entity

final case class GitHubUserSearchResult(
    total_count: Long,
    incomplete_results: Boolean,
    items: List[GitHubUser])