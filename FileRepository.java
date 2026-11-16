package smart.store;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileRepository implements IRepository {

    @Override
    public boolean saveData(String filePath, List<String> data) {
        File file = new File(filePath);
        File parent = file.getParentFile();

        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (String line : data) {
                writer.write(line);
                writer.newLine();
            }
            return true;
        } catch (IOException e) {
            System.out.println("Could not save file: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<String> loadData(String filePath) {
        List<String> data = new ArrayList<>();
        File file = new File(filePath);

        if (!file.exists()) {
            return data;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    data.add(line);
                }
            }
        } catch (IOException e) {
            System.out.println("Could not read file: " + e.getMessage());
        }

        return data;
    }
}