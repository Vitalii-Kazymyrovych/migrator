package com.vezha.migrator.facemigration;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FaceImageOrganizerTest {

    @Test
    void renamesSanitizesHandlesCollisionsAndMovesImagesIntoListFolders() throws IOException {
        Path tempRoot = Files.createTempDirectory("face-org-test");
        Path imageFolder = Files.createDirectories(tempRoot.resolve("images"));

        Files.writeString(imageFolder.resolve("1725440323601.jpg"), "one");
        Files.writeString(imageFolder.resolve("second.jpg"), "two");
        Files.writeString(imageFolder.resolve("third.jpg"), "three");
        Files.writeString(imageFolder.resolve("Alice_Bob.jpg"), "pre-existing");

        JdbcTemplate source = mock(JdbcTemplate.class);
        when(source.queryForList(
                """
                        SELECT fli.id        AS item_id,
                               fli.name      AS person_name,
                               fli.list_id,
                               flii.id       AS image_id,
                               flii.path     AS image_path
                        FROM face_list_items fli
                        JOIN face_list_items_images flii ON flii.list_item_id = fli.id
                        ORDER BY flii.id ASC
                        """
        )).thenReturn(List.of(
                itemImageRow(1, "Alice/Bob", 10, 2, "face_lists/0/0/1725440323601.jpg"),
                itemImageRow(1, "Alice/Bob", 10, 3, "face_lists/0/0/ignored.jpg"),
                itemImageRow(2, "John:Doe", 10, 4, "face_lists/0/0/second.jpg"),
                itemImageRow(3, "John?Doe", 10, 5, "face_lists/0/0/third.jpg"),
                itemImageRow(4, "Missing", 20, 6, "face_lists/0/0/missing.jpg")
        ));
        when(source.queryForList("SELECT id, name FROM face_lists")).thenReturn(List.of(
                listRow(10, "Main/List"),
                listRow(20, "Secondary")
        ));

        String previousUserDir = System.getProperty("user.dir");
        System.setProperty("user.dir", tempRoot.toString());
        try {
            new FaceImageOrganizer().organize(source, imageFolder.toString());
        } finally {
            System.setProperty("user.dir", previousUserDir);
        }

        Path listsRoot = tempRoot.resolve("Lists");
        assertTrue(Files.exists(listsRoot.resolve("Main_List")));
        assertTrue(Files.exists(listsRoot.resolve("Secondary")));

        assertTrue(Files.exists(listsRoot.resolve("Main_List").resolve("John_Doe_2.jpg")));
        assertTrue(Files.exists(listsRoot.resolve("Main_List").resolve("John_Doe_3.jpg")));

        assertTrue(Files.exists(imageFolder.resolve("Alice_Bob.jpg")));
        assertFalse(Files.exists(listsRoot.resolve("Main_List").resolve("Alice_Bob.jpg")));
        assertFalse(Files.exists(imageFolder.resolve("missing.jpg")));
    }

    private Map<String, Object> itemImageRow(int itemId, String personName, int listId, int imageId, String imagePath) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("item_id", itemId);
        row.put("person_name", personName);
        row.put("list_id", listId);
        row.put("image_id", imageId);
        row.put("image_path", imagePath);
        return row;
    }

    private Map<String, Object> listRow(int id, String name) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", id);
        row.put("name", name);
        return row;
    }
}
