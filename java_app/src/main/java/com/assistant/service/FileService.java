package com.assistant.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Iterator;

public class FileService {
    private final ObjectMapper objectMapper;

    public FileService() {
        this.objectMapper = new ObjectMapper();
    }

    public String parseFile(File file) throws IOException {
        String fileName = file.getName().toLowerCase();
        if (fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) {
            return parseExcel(file);
        } else if (fileName.endsWith(".txt") || fileName.endsWith(".log") || fileName.endsWith(".csv")) {
            return parseText(file);
        } else {
            throw new IllegalArgumentException("Unsupported file format: " + fileName);
        }
    }

    private String parseText(File file) throws IOException {
        String content = Files.readString(file.toPath());
        // Limit to first 2000 chars as sample
        if (content.length() > 2000) {
            return content.substring(0, 2000);
        }
        return content;
    }

    private String parseExcel(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            ArrayNode jsonArray = objectMapper.createArrayNode();
            
            Iterator<Row> rowIterator = sheet.iterator();
            if (!rowIterator.hasNext()) return "[]";

            // Get Headers
            Row headerRow = rowIterator.next();
            String[] headers = new String[headerRow.getLastCellNum()];
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                headers[i] = cell != null ? cell.toString() : "col_" + i;
            }

            // Get Data (Limit to 20 rows for sample)
            int count = 0;
            while (rowIterator.hasNext() && count < 20) {
                Row row = rowIterator.next();
                ObjectNode jsonRow = jsonArray.addObject();
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    if (cell != null) {
                        jsonRow.put(headers[i], cell.toString());
                    } else {
                        jsonRow.put(headers[i], "");
                    }
                }
                count++;
            }
            
            return objectMapper.writeValueAsString(jsonArray);
        }
    }
}
