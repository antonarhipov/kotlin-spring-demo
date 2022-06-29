package demo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*
import java.util.*

@SpringBootApplication
class DemoApplication

fun main(args: Array<String>) {
    runApplication<DemoApplication>(*args)
}

@RestController
class MessageResource(val service: MessageService) {
    @GetMapping
    fun index(): List<Message> = service.findMessages()

    @GetMapping("/{id}")
    fun index(@PathVariable id: String): List<Message> = service.findMessageById(id)

    @GetMapping("/groups")
    fun groups(): Map<String, List<Message>> {
        val messages = service.findMessages()
        val groups = listOf("hello", "bye")

        val map = messages.groupByTo(mutableMapOf()) { message ->
            groups.firstOrNull {
                message.text.contains(it, ignoreCase = true)
            } ?: "other"
        }

        return map
    }

    @PostMapping
    fun post(@RequestBody message: Message) {
        service.save(message)
    }

}

@Table("MESSAGES")
data class Message(@Id var id: String?, val text: String)

@Service
class MessageService(val db: MessageRepository) {

    fun findMessages(): List<Message> = db.findAll().toList()

    fun findMessageById(id: String): List<Message> = db.findById(id).toList()

    fun save(message: Message) {
        db.save(message)
    }

    fun <T : Any> Optional<out T>.toList(): List<T> =
        if (isPresent) listOf(get()) else emptyList()
}

interface MessageRepository : CrudRepository<Message, String>