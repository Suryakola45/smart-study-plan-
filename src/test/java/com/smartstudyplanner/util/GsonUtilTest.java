package com.smartstudyplanner.util;

import com.google.gson.Gson;
import com.smartstudyplanner.service.ProgressService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Gson Serialization Tests for Java 17 Date/Time")
class GsonUtilTest {

    @Test
    @DisplayName("Serializing SubjectProgressItem with LocalDate should not fail with InaccessibleObjectException")
    void testSubjectProgressItemSerialization() {
        Gson gson = GsonUtil.getGson();

        ProgressService.SubjectProgressItem item = new ProgressService.SubjectProgressItem(
                1L, "Data Structures", "CS201", "#4361ee", LocalDate.now().plusDays(10),
                10, 5, 2, 40.0, 300, 120
        );

        List<ProgressService.SubjectProgressItem> list = Collections.singletonList(item);
        String json = gson.toJson(list);

        assertNotNull(json);
        assertTrue(json.contains("Data Structures"));
        assertTrue(json.contains("CS201"));
        assertTrue(json.contains("#4361ee"));
    }
}

