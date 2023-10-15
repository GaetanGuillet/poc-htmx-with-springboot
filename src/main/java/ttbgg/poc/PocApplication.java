package ttbgg.poc;

import jakarta.websocket.server.PathParam;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@SpringBootApplication
public class PocApplication {
    public static void main(String[] args) {
        SpringApplication.run(PocApplication.class, args);
    }
}

record Todo(String id, String title) {
}

class TodoResetEvent extends ApplicationEvent {
    TodoResetEvent() {
        super(Instant.now());
    }
}

@Controller
@RequestMapping("todos")
class TodosController {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final Map<String, Todo> todos = new HashMap<>();

    TodosController(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @EventListener({ApplicationReadyEvent.class, TodoResetEvent.class})
    public void init() {
        todos.clear();
        Stream.of("learn html and css", "try events", "try html", "try webjars", "thing functional")
                .forEach(this::addTodo);
    }

    @GetMapping
    String getTodos(Model model) {
        model.addAttribute("todos", todos.values());
        return "todos";
    }

    @PostMapping
    String addTodo(Model model, @RequestParam("add-todo") String todo) {
        addTodo(todo);
        model.addAttribute("todos", todos.values());
        return "todos :: todos-list";
    }

    @PostMapping("/reset")
    String reset(Model model) {
        applicationEventPublisher.publishEvent(new TodoResetEvent());
        model.addAttribute("todos", todos.values());
        return "todos :: todos-list";
    }

    @DeleteMapping(value = "/{id}", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    String deleteTodo(@PathParam("id") String id) {
        todos.remove(id);
        return "";
    }

    Todo addTodo(String title) {
        var todo = new Todo(String.valueOf(title.hashCode()), title);
        todos.put(String.valueOf(title.hashCode()), todo);
        return todo;
    }

}