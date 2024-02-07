package duke;

import duke.ui.Ui;
import duke.storage.Storage;
import duke.task.TaskList;
import duke.parser.Parser;

public class Duke {
    private Storage storage;
    private TaskList tasks;
    private Ui ui;


    /**
     * Constructs a Duke object with the specified file path.
     *
     * @param filePath The file path where tasks will be stored.
     */
    public Duke(String filePath) {
        ui = new Ui();
        storage = new Storage(filePath);

        try {
            tasks = new TaskList(storage.load());
        } catch (DukeException e) {
            ui.showLoadingError();
            tasks = new TaskList();
        }
    }


    /**
     * Runs the Duke application.
     */
    public void run() {
        ui.showWelcome();
        boolean isExit = false;

        while (!isExit) {
            try {
                String userInput = ui.readCommand();
                Parser.parse(userInput, tasks, ui);
                storage.save(tasks.getTasks());
            } catch (DukeException e) {
                ui.showError(e.getMessage());
            } catch (Exception e) {
                ui.showError("Oops! Something went wrong: " + e.getMessage());
            }
        }
    }

    /**
     * Main method to start the Duke application.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        new Duke("./data/duke.txt").run();
    }
}
