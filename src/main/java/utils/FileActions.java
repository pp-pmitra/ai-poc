package utils;

import com.opencsv.CSVReader;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class FileActions {

    public static List<String[]> readAllDataAtOnce(String file) {
        List<String[]> allData = null;
        try {
            FileReader filereader = new FileReader(file);
            CSVReader csvReader = new CSVReader(filereader);
            allData = csvReader.readAll();
            /*for (String[] row : allData) {
                System.out.println(String.join(", ", row));
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
        return allData;
    }

    public static int fetchColumnCountFromCSV(Path filePath, String columnName) throws IOException {
        int rowCount = 0;
        try (BufferedReader br = Files.newBufferedReader(filePath)) {
            String header = br.readLine();
            if (header == null) return 0;
            String[] headers = header.split(",");
            int index = -1;
            for (int i = 0; i < headers.length; i++) {
                if (headers[i].trim().equalsIgnoreCase(columnName)) {
                    index = i;
                    break;
                }
            }
            if (index == -1) {
                throw new IllegalStateException("Column '" + columnName + "' not found in " + filePath.getFileName());
            }
            String line;
            while ((line = br.readLine()) != null) {
                String[] columns = line.split(",");
                if (columns.length > index && !columns[index].trim().isEmpty()) {
                    rowCount++;
                }
            }
        }
        return rowCount;
    }

    public static int fetchColumnCountFromExcel(Path filePath, String columnName) throws IOException {
        try (Workbook wb = new XSSFWorkbook(Files.newInputStream(filePath))) {
            Sheet sheet = wb.getSheetAt(0);
            if (sheet == null) return 0;

            Row header = sheet.getRow(0);
            if (header == null) return 0;

            int colIndex = -1;
            for (Cell c : header)
                if (c.getStringCellValue().trim().equalsIgnoreCase(columnName)) colIndex = c.getColumnIndex();

            if (colIndex == -1)
                throw new IllegalStateException("Column '" + columnName + "' not found in " + filePath.getFileName());

            int count = 0;
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Cell cell = sheet.getRow(i) != null ? sheet.getRow(i).getCell(colIndex) : null;
                if (cell != null && !cell.toString().trim().isEmpty()) count++;
            }
            return count;
        }
    }

    private static Path resolvePath(String fileName) throws IOException {
        Path[] paths = {
            Paths.get("src/main/resources", fileName),
            Paths.get("src/main/resources/uploadfiles", fileName),
            Paths.get(System.getProperty("user.home"), "Downloads", fileName)
        };

        for (Path path : paths) {
            if (Files.exists(path)) {
                return path;
            }
        }
        throw new FileNotFoundException("File not found: " + fileName);
    }

    public static int countRecordsFromTextFile(String fileName) throws IOException {
        Path basePath = resolvePath(fileName);
        List<String> lines = Files.readAllLines(basePath);
        return Math.toIntExact(
                lines.stream().filter(line -> !line.trim().isEmpty()).count());
    }

    public static int fetchRowCountFromCSV(Path filePath) throws IOException {
        return fetchRowCount(filePath, true);
    }

    public static int fetchRowCountExcludeHeaderFromCSVAndTxt(String fileName) throws IOException {
        return fetchRowCount(resolvePath(fileName), true);
    }

    private static int fetchRowCount(Path filePath, boolean excludeHeader) throws IOException {
        try (Stream<String> lines = Files.lines(filePath)) {
            return (int) lines
                    .skip(excludeHeader ? 1 : 0) // Skip header if true
                    .filter(line -> !line.trim().isEmpty()) // Ignore empty lines
                    .distinct() // Remove duplicates
                    .count();
        }
    }

    public static int fetchRowCountFromExcel(String fileName) throws IOException {
        Path basePath = resolvePath(fileName);
        try (InputStream is = Files.newInputStream(basePath);
                Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            return sheet.getLastRowNum();
        }
    }
}
