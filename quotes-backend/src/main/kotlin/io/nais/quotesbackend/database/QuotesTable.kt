package io.nais.quotesbackend.database

import org.jetbrains.exposed.sql.Table

object QuotesTable : Table("quotes") {
  val id = integer("id").autoIncrement()
  val text = varchar("text", 1000)
  val author = varchar("author", 255)

  override val primaryKey = PrimaryKey(id)
}
