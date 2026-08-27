package amadeus.task;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Task#descriptionContains(String)}, which backs the
 * "find" command.
 */
public class TaskTest {

    @Test
    void descriptionContains_keywordInDescription_returnsTrue() {
        assertTrue(new Todo("read book").descriptionContains("book"));
    }

    /** Case is ignored, so the user does not have to match how they typed it originally. */
    @Test
    void descriptionContains_differentCase_returnsTrue() {
        assertTrue(new Todo("Read Book").descriptionContains("book"));
    }

    @Test
    void descriptionContains_keywordAbsent_returnsFalse() {
        assertFalse(new Todo("read book").descriptionContains("magazine"));
    }
}
