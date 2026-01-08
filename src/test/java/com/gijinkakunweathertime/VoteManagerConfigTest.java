package com.gijinkakunweathertime;

import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class VoteManagerConfigTest {

    @Test
    public void disabledWorldsUseConfiguredLabelsOrFallbacks() {
        List<Object> entries = List.of(
            "world_nether",
            Map.of("id", "world_the_end"),
            Map.of("id", "custom_world", "name", "Mystic Realm")
        );

        Map<String, String> parsed = VoteManager.parseDisabledWorlds(entries);

        assertEquals("world_nether", parsed.get("world_nether"));
        assertEquals("world_the_end", parsed.get("world_the_end"));
        assertEquals("Mystic Realm", parsed.get("custom_world"));
    }
}
