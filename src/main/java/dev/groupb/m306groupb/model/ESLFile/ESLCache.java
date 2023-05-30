package dev.groupb.m306groupb.model.ESLFile;

import dev.groupb.m306groupb.model.FileDate;
import dev.groupb.m306groupb.model.SDATFile.SDATFile;
import dev.groupb.m306groupb.utils.ESLFileReader;
import dev.groupb.m306groupb.utils.FileReader;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;

@Getter
public class ESLCache {
    // Lombok ignore setter and getter
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private static ESLCache instance;

    private final HashMap<FileDate, ESLFile> eslFileMap = new HashMap<>();

    private ESLCache() {

    }

    public static void fillCacheParallel(String filesPath) {
        ESLCache sdatCache = ESLCache.getInstance();
        sdatCache.getEslFileMap().clear();

        ESLFileReader eslFileReader = new ESLFileReader();
        File[] files = FileReader.getFiles(filesPath);

        Arrays.stream(files).parallel().forEach(file -> {
            int amountOfESLFilesToExpect = eslFileReader.amountOfEslFiles(file);
            for (int i = 0; i < amountOfESLFilesToExpect; i++) {
                ESLFile eslFile = eslFileReader.parseFile(file, i);
                if (eslFile == null) {
                    continue;
                }
                FileDate fileDate = eslFileReader.getFileDate(file, i);
                sdatCache.addESLFile(fileDate, eslFile);
            }
        });
    }

    public static ESLCache getInstance() {
        if (instance == null) {
            instance = new ESLCache();
        }
        return instance;
    }

    public void addESLFile(FileDate fileDate, ESLFile eslFile) {
        eslFileMap.put(fileDate, eslFile);

        ESLFile existing = eslFileMap.get(fileDate);
        if (existing != null) {
            // if the existing has something null, and the new one has in that field a value, replace it with the new one (so its not null)

        } else {
            sdatFileHashMap.put(fileDate, new SDATFile[]{sdatFile});
        }
    }
}
