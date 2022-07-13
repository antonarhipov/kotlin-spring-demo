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
class MessageController(val service: MessageService) {
    @GetMapping
    fun index(): List<Message> = service.findMessages()

    @GetMapping("/{id}")
    fun index(@PathVariable id: String): List<Message> = service.findMessageById(id)

    @GetMapping("/firstAndLast")
    fun firstAndLast(): List<Message> {
        val messages = service.findMessages()
        return listOf(messages.first(), messages.last())
    }

    @GetMapping("/firstMessageLongerThan10")
    fun firstMessageLongerThan10(): Message {
        val messages = service.findMessages()
        return messages.first { it.text.length > 10 }
    }

    @GetMapping("/firstMessageLongerThan10OrNull")
    fun firstMessageOrNull(): Message {
        val messages = service.findMessages()
        return messages.firstOrNull { it.text.length > 10 }
            ?: Message(null, "Default message")
    }

    @GetMapping("/filterMessagesLongerThan10")
    fun filterMessagesLongerThan10(): List<Message> {
        val messages = service.findMessages()
        return messages.filter { it.text.length > 10 }
    }

    @GetMapping("/sortByLastLetter")
    fun sortByLastLetter(): List<Message> {
        val messages = service.findMessages()
        return messages.sortedBy { it.text.last() }
    }

    @GetMapping("/groups")
    fun groups(): Map<String, List<Message>> {
        val messages = service.findMessages()
        val groups = listOf("hello", "bye")

        val map = messages.groupBy { message ->
            groups.firstOrNull {
                message.text.contains(it, ignoreCase = true)
            } ?: "other"
        }

        return map
    }

    @GetMapping("/transformMessagesToListOfStrings")
    fun transformMessagesToListOfStrings(): List<String> {
        val messages = service.findMessages()
        return messages.map { "${it.id} ${it.text}" }
    }

    @GetMapping("/averageMessageLength")
    fun averageMessageLength(): Double {
        val messages = service.findMessages()
        return messages.map { it.text.length }.average()
    }

    @GetMapping("findTheLongestMessage")
    fun findTheLongestMessage(): Message {
        val messages = service.findMessages()
        return messages.reduce { first, second ->
            if (first.text.length > second.text.length) first else second
        }
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