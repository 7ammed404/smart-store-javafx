package smart.store;

import java.util.List;

public interface IRepository {
    boolean saveData(String filePath, List<String> data);
    List<String> loadData(String filePath);
}