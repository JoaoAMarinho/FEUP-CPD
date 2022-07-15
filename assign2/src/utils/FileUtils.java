package utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class FileUtils {
    public static void CreateFile(String relativePath, String content) throws IOException {
        Path currentRelativePath = Paths.get("");
        var fileName = currentRelativePath.toAbsolutePath().toString() + relativePath;

        File file = new File(fileName);
        file.createNewFile();

        FileWriter writer = new FileWriter(file);
        writer.write(content);
        writer.close();
    }

    public static synchronized void UpdateFile(String relativePath, String content) throws IOException {
        Path currentRelativePath = Paths.get("");
        var fileName = currentRelativePath.toAbsolutePath().toString() + relativePath;

        File file = new File(fileName);
        FileWriter writer = new FileWriter(file);
        writer.write(content);
        writer.close();
    }

    public static String ReadFromFile(String relativePath, String separator) throws FileNotFoundException {
        Path currentRelativePath = Paths.get("");
        var fileName = currentRelativePath.toAbsolutePath().toString() + relativePath;

        File membershipCounterFile = new File(fileName);
        Scanner fileReader = new Scanner(membershipCounterFile);

        StringBuilder content = new StringBuilder();
        while (fileReader.hasNextLine()) {
            content.append(fileReader.nextLine()).append(separator);
        }
        fileReader.close();
        if (content.isEmpty()) return "";

        content.setLength(content.length()-1);
        return content.toString();
    }

    public static void CreateDir(String relativePath) {
        Path currentRelativePath = Paths.get("");
        var dirPath = currentRelativePath.toAbsolutePath().toString() + relativePath;

        File newDir = new File(dirPath);
        newDir.mkdir();
    }

    public static void ChangeFileName(String filePath, String newFilePath) throws IOException {
        Path currentRelativePath = Paths.get("");
        Path actualPath = Paths.get(currentRelativePath.toAbsolutePath().toString() + filePath);
        Path newPath = Paths.get(currentRelativePath.toAbsolutePath().toString() + newFilePath);

        Files.move(actualPath, newPath, StandardCopyOption.REPLACE_EXISTING);
    }

    public static List<String> GetFolderSortedFiles(String relativePath) throws IOException {
        Path currentRelativePath = Paths.get("");
        var folderPath = currentRelativePath.toAbsolutePath().toString() + relativePath;

        return Files.list(Path.of(folderPath))
                .sorted()
                .map(filePath -> {
                    if (filePath.toFile().isDirectory()) return null;
                    var list = filePath.toString().split("\\\\");
                    var fileName = list[list.length-1];
                    return fileName.substring(0, fileName.length()-4);
                })
                .collect(Collectors.toList());
    }

    public static void DeleteDirectory(String path) {
        Path currentRelativePath = Paths.get("");
        var deletePath = currentRelativePath.toAbsolutePath().toString() + path;

        File directoryToBeDeleted = new File(deletePath);
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                DeleteDirectory(file.getPath());
            }
        }
        directoryToBeDeleted.delete();
    }
}
