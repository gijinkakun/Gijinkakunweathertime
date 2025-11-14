package com.gijinkakunweathertime;

import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PraiseTypeTest {

    @Test
    public void resolvesCommandArgumentCaseInsensitive() {
        Optional<PraiseType> resolved = PraiseType.fromCommandArgument("SuN");
        assertEquals(PraiseType.SUN, resolved.orElseThrow());
    }

    @Test
    public void rejectsUnknownCommandArgument() {
        assertTrue(PraiseType.fromCommandArgument("unknown").isEmpty());
    }
}
