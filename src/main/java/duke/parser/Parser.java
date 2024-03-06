package duke.parser;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import duke.DukeException;
import duke.storage.Storage;
import duke.task.Deadline;
import duke.task.Event;
import duke.task.Task;
import duke.task.TaskList;
import duke.task.Todo;
import duke.ui.Ui;

/**
 * The Parser class is responsible for parsing user input and executing corresponding commands
 * in the Duke application. It handles the creation of tasks, marking tasks as done,
 * deleting tasks, listing tasks, and searching for tasks based on user input.
 */
public class Parser {
    private static Storage storage;
    public Parser(Storage storage) {
        this.storage = storage;
    }
    /**
     * Parses the user input and executes the corresponding command.
     *
     * @param userInput The user input to be parsed.
     * @param tasks     The TaskList object containing the list of tasks.
     * @param ui        The Ui object responsible for user interface interactions.
     * @throws DukeException If an error occurs during parsing or execution of the command.
     */
    public static void parse(String userInput, TaskList tasks, Ui ui) throws DukeException {
        String[] parts = userInput.trim().split(" ", 2);
        String command = parts[0].toLowerCase();

        switch (command) {
        case "bye":
            ui.showGoodbye();
            System.exit(0);
            break;
        case "list":
            ui.showMessage(tasks.listTasks());

            break;
        case "mark":
            if (parts.length < 2) {
                throw new DukeException("Mark command needs a task number.");
            }
            int markIndex = Integer.parseInt(parts[1]) - 1;
            tasks.markTask(markIndex + 1);
            ui.showMessage("Marked as done: " + tasks.getTasks().get(markIndex));
            break;
        case "unmark":
            if (parts.length < 2) {
                throw new DukeException("Unmark command needs a task number.");
            }
            int unmarkIndex = Integer.parseInt(parts[1]) - 1;
            tasks.unmarkTask(unmarkIndex + 1);
            ui.showMessage("Unmarked as done: " + tasks.getTasks().get(unmarkIndex));
            break;
        case "delete":
            if (parts.length < 2) {
                throw new DukeException("Delete command needs a task number.");
            }
            int deleteIndex = Integer.parseInt(parts[1]) - 1;
            tasks.deleteTask(deleteIndex + 1);
            ui.showMessage("Deleted: " + tasks.getTasks().get(deleteIndex)
                + "\nNow you have " + tasks.getTasks().size() + " tasks in the list.");
            break;
        case "todo":
        case "deadline":
        case "event":
            handleTaskCreation(command, parts, tasks, ui);
            storage.save(tasks.getTasks());
            break;
        case "find":
            if (parts.length < 2 || parts[1].isEmpty()) {
                throw new DukeException("The keyword for find cannot be empty.");
            }
            tasks.findTask(parts[1]);
            ui.showMessage(tasks.findTask(parts[1]));
            break;
        default:
            throw new DukeException("I'm sorry, but I don't know what that means :-(");
        }
    }

    /**
     * Handles the creation of tasks based on the given command and input parts.
     *
     * @param command The command indicating the type of task to create.
     * @param parts   The input parts containing task details.
     * @param tasks   The TaskList object to add the created task to.
     * @param ui      The Ui object responsible for user interface interactions.
     * @throws DukeException If an error occurs during task creation.
     */
    private static void handleTaskCreation(String command, String[] parts, TaskList tasks, Ui ui) throws DukeException {
        if (parts.length < 2 || parts[1].isEmpty()) {
            throw new DukeException("The description of a " + command + " cannot be empty.");
        }

        Task newTask = null;
        switch (command) {
        case "todo":
            newTask = new Todo(parts[1], false);
            break;
        case "deadline":
            String[] deadlineParts = parts[1].split(" /by ", 2);
            if (deadlineParts.length < 2) {
                throw new DukeException("duke.task.Deadline format incorrect. "
                    + "Please use the format: deadline description /by yyyy-MM-dd");
            }
            LocalDate deadlineDate;
            try {
                deadlineDate = LocalDate.parse(deadlineParts[1], DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (DateTimeParseException e) {
                throw new DukeException("Date format incorrect. Please use the format: yyyy-MM-dd");
            }
            newTask = new Deadline(deadlineParts[0], deadlineDate, false);
            break;
        case "event":
            String[] eventParts = parts[1].split(" /at ", 2);
            if (eventParts.length < 2) {
                newTask = new Event(eventParts[0], "", "", false);
            } else {
                String[] timeParts = eventParts[1].split(" to ", 2);
                if (timeParts.length < 2) {
                    throw new DukeException("duke.task.Event time format incorrect. "
                        + "Please include both start and end times.");
                }
                newTask = new Event(eventParts[0], timeParts[0], timeParts[1], false);
            }
            break;
        default:
            throw new DukeException("I'm sorry, but I don't know what that means :-(");
        }

        if (newTask != null) {
            tasks.addTask(newTask);
            ui.showMessage("Got it. I've added this task:\n  " + newTask
                + "\nNow you have " + tasks.getTasks().size() + " tasks in the list.");
        }
    }
}

