package blogify.backend.database.optimizer

import blogify.reflect.entity.database.optimizer.makeJoinForClass
import blogify.backend.resources.Comment

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

import com.andreapivetta.kolor.lightRed

class SingleRefOptimizerTest {

    @Test fun `should create proper join for Comment`() {
        val join = makeJoinForClass(Comment::class)

        println(join.columns.map { "${it.table.tableName}[${it.name.lightRed()}]" })

        listOf (
            "Comments[uuid]", "Comments[commenter]", "Comments[article]", "Comments[content]",
            "Comments[parent_comment]", "Comments[created_at]",
            "Comment->commenter[uuid]", "Comment->commenter[username]",
            "Comment->commenter[password]", "Comment->commenter[email]",
            "Comment->commenter[name]", "Comment->commenter[settings]",
            "Comment->commenter[profile_picture]", "Comment->commenter[cover_picture]",
            "Comment->commenter[is_admin]", "Comment->commenter[biography]",
            "Comment->article[uuid]", "Comment->article[title]",
            "Comment->article[created_at]", "Comment->article[created_by]",
            "Comment->article[content]", "Comment->article[summary]",
            "Comment->article[is_draft]", "Comment->article[is_pinned]",
            "Comment->parentComment[uuid]", "Comment->parentComment[commenter]",
            "Comment->parentComment[article]", "Comment->parentComment[content]",
            "Comment->parentComment[parent_comment]", "Comment->parentComment[created_at]"
        ).forEach {
            assertTrue (
                join.columns.map { c -> "${c.table.tableName}[${c.name}]" }
                    .contains(it), "join did not contain $it"
            )
        }
    }

}
