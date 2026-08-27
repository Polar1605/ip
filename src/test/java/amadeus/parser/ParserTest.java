package amadeus.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import amadeus.AmadeusException;

/**
 * Unit tests for {@link Parser#parseTaskIndex(String, int)}.
 * <p>
 * That method is worth testing because it does the 1-based to 0-based conversion
 * for "mark" and "unmark", and has to reject everything the user might type that
 * is not a valid task number.
 */
public class ParserTest {

    /**
     * The user counts from 1 and arrays count from 0, so the result must be one
     * less than the number typed. This is where an off-by-one error would show up.
     * <p>
     * parseTaskIndex declares AmadeusException (a checked exception), so the test
     * method has to declare it too, otherwise the code would not compile.
     */
    @Test
    void parseTaskIndex_firstTask_returnsZero() throws AmadeusException {
        assertEquals(0, Parser.parseTaskIndex("mark 1", 3));
    }

    /** One past the end is the closest invalid number, so it is the one worth checking. */
    @Test
    void parseTaskIndex_numberAboveTaskCount_throwsAmadeusException() {
        assertThrows(AmadeusException.class, () -> Parser.parseTaskIndex("mark 4", 3));
    }

    /**
     * Integer.parseInt reports bad input by throwing NumberFormatException, which
     * the parser must convert into an AmadeusException so the app can print a
     * friendly message instead of crashing.
     */
    @Test
    void parseTaskIndex_notANumber_throwsAmadeusException() {
        assertThrows(AmadeusException.class, () -> Parser.parseTaskIndex("mark two", 3));
    }
}
