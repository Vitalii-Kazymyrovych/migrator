package com.vezha.migrator.facemigration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FaceImageOrganizer {

    private static final Logger log = LoggerFactory.getLogger(FaceImageOrganizer.class);

    public void organize(JdbcTemplate sourceJdbcTemplate, String imageFolderPath) {
        List<Map<String, Object>> itemRows = sourceJdbcTemplate.queryForList(
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
        );
        List<Map<String, Object>> listRows = sourceJdbcTemplate.queryForList("SELECT id, name FROM face_lists");

        Map<Integer, String> itemIdToFirstImage = new LinkedHashMap<>();
        Map<Integer, String> itemIdToPersonName = new LinkedHashMap<>();
        Map<Integer, Integer> itemIdToListId = new LinkedHashMap<>();
        for (Map<String, Object> row : itemRows) {
            Integer itemId = asInt(row.get("item_id"));
            itemIdToFirstImage.putIfAbsent(itemId, fileName(String.valueOf(row.get("image_path"))));
            itemIdToPersonName.put(itemId, Objects.toString(row.get("person_name"), ""));
            itemIdToListId.put(itemId, asInt(row.get("list_id")));
        }

        Map<Integer, String> listIdToName = new HashMap<>();
        for (Map<String, Object> row : listRows) {
            listIdToName.put(asInt(row.get("id")), sanitize(Objects.toString(row.get("name"), "")));
        }

        Map<Integer, String> finalNames = resolveSanitizedNames(itemIdToPersonName, itemIdToListId);
        Path imageFolder = Path.of(imageFolderPath);
        Path listsRoot = Path.of(System.getProperty("user.dir")).toAbsolutePath().resolve("Lists");

        Map<Integer, String> renamedFiles = renameToPersonNames(itemIdToFirstImage, finalNames, imageFolder);
        createListDirectories(itemIdToListId, listIdToName, listsRoot);
        moveToListDirectories(renamedFiles, itemIdToListId, listIdToName, imageFolder, listsRoot);
    }

    private Map<Integer, String> resolveSanitizedNames(Map<Integer, String> personNames, Map<Integer, Integer> itemIdToListId) {
        Map<Integer, String> sanitizedByItem = new LinkedHashMap<>();
        Map<Integer, Map<String, List<Integer>>> grouped = new HashMap<>();

        for (Map.Entry<Integer, String> entry : personNames.entrySet()) {
            Integer itemId = entry.getKey();
            String sanitized = sanitize(entry.getValue());
            sanitizedByItem.put(itemId, sanitized);

            Integer listId = itemIdToListId.get(itemId);
            grouped.computeIfAbsent(listId, ignored -> new HashMap<>())
                    .computeIfAbsent(sanitized, ignored -> new java.util.ArrayList<>())
                    .add(itemId);
        }

        for (Map<String, List<Integer>> sameList : grouped.values()) {
            for (Map.Entry<String, List<Integer>> collision : sameList.entrySet()) {
                if (collision.getValue().size() > 1) {
                    for (Integer itemId : collision.getValue()) {
                        sanitizedByItem.put(itemId, collision.getKey() + "_" + itemId);
                    }
                }
            }
        }

        return sanitizedByItem;
    }

    private Map<Integer, String> renameToPersonNames(Map<Integer, String> itemIdToFirstImage,
                                                      Map<Integer, String> finalNames,
                                                      Path imageFolder) {
        Map<Integer, String> renamedFiles = new LinkedHashMap<>();
        for (Map.Entry<Integer, String> entry : itemIdToFirstImage.entrySet()) {
            Integer itemId = entry.getKey();
            String originalName = entry.getValue();
            String renamed = finalNames.get(itemId) + extension(originalName);

            Path source = imageFolder.resolve(originalName);
            Path target = imageFolder.resolve(renamed);
            if (!Files.exists(source)) {
                log.warn("Face image source does not exist for item {}: {}", itemId, source);
                continue;
            }
            if (Files.exists(target)) {
                log.warn("Face image target already exists for item {}: {}", itemId, target);
                continue;
            }
            try {
                Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
                renamedFiles.put(itemId, renamed);
            } catch (IOException e) {
                log.warn("Failed to rename face image for item {}", itemId, e);
            }
        }
        return renamedFiles;
    }

    private void createListDirectories(Map<Integer, Integer> itemIdToListId,
                                       Map<Integer, String> listIdToName,
                                       Path listsRoot) {
        for (Integer listId : itemIdToListId.values()) {
            String listName = listIdToName.getOrDefault(listId, String.valueOf(listId));
            try {
                Files.createDirectories(listsRoot.resolve(listName));
            } catch (IOException e) {
                throw new IllegalStateException("Failed to create list directory: " + listName, e);
            }
        }
    }

    private void moveToListDirectories(Map<Integer, String> renamedFiles,
                                       Map<Integer, Integer> itemIdToListId,
                                       Map<Integer, String> listIdToName,
                                       Path imageFolder,
                                       Path listsRoot) {
        for (Map.Entry<Integer, String> entry : renamedFiles.entrySet()) {
            Integer itemId = entry.getKey();
            String renamedFile = entry.getValue();
            Integer listId = itemIdToListId.get(itemId);
            String listName = listIdToName.getOrDefault(listId, String.valueOf(listId));

            Path source = imageFolder.resolve(renamedFile);
            if (!Files.exists(source)) {
                log.warn("Renamed file missing before list move for item {}: {}", itemId, source);
                continue;
            }

            Path target = listsRoot.resolve(listName).resolve(renamedFile);
            try {
                Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException e) {
                log.warn("Failed to move image for item {} into list {}", itemId, listName, e);
            }
        }
    }

    private static String fileName(String path) {
        int slash = path.lastIndexOf('/');
        if (slash >= 0) {
            return path.substring(slash + 1);
        }
        return path;
    }

    private static String extension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot) : "";
    }

    private static Integer asInt(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }

    private static String sanitize(String value) {
        return value.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}
