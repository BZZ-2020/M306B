package dev.groupb.m306groupb.fileChange;

import dev.groupb.m306groupb.model.SDATFile.SDATCache;
import dev.groupb.m306groupb.model.meterReading.MeterReadingCache;
import org.springframework.boot.devtools.filewatch.ChangedFile;
import org.springframework.boot.devtools.filewatch.ChangedFiles;
import org.springframework.boot.devtools.filewatch.FileChangeListener;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class SDATFileChangeListener implements FileChangeListener {

    @Override
    public void onChange(Set<ChangedFiles> changeSet) {
        for (ChangedFiles cfiles : changeSet) {
            for (ChangedFile cfile : cfiles.getFiles()) {
                switch (cfile.getType()) {
                    case ADD -> {
                        System.out.println("SDAT File added: " + cfile.getFile().getName());
                        SDATCache.addNewFile(cfile.getFile());
                        MeterReadingCache.fillCacheParallel();
                        System.out.println("Caches updated");
                    }
                    case DELETE -> {
                        System.out.println("SDAT File deleted: " + cfile.getFile().getName());
                        SDATCache.fileRemoved(cfile.getFile().getName());
                        MeterReadingCache.fillCacheParallel();
                        System.out.println("Caches updated");
                    }
                    case MODIFY -> {
                        System.out.println("SDAT File modified: " + cfile.getFile().getName());
                        SDATCache.fileChanged(cfile.getFile().getName(), cfile.getFile());
                        MeterReadingCache.fillCacheParallel();
                        System.out.println("Caches updated");
                    }
                }
            }
        }
    }
}
