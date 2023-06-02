package dev.groupb.m306groupb.controller;

import dev.groupb.m306groupb.model.FileDate;
import dev.groupb.m306groupb.model.SDATFile.SDATCache;
import dev.groupb.m306groupb.model.SDATFile.SDATFile;
import dev.groupb.m306groupb.utils.GlobalStuff;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/export")
public class CsvExportService {
    private final SDATCache cacheData = SDATCache.getInstance();

    /**
     * Exports data as a CSV file within a specific time range.
     *
     * @param from     Start date of the time range (in yyyy-MM-dd format)
     * @param to       End date of the time range (in yyyy-MM-dd format)
     * @param response HttpServletResponse to set the CSV file as the response
     */
    @GetMapping(
            value = "/csv/{from}/{to}",
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    public void exportDataCSV(
            @PathVariable("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date from,
            @PathVariable("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date to,
            HttpServletResponse response
    ) throws IOException {
        // Retrieve the relevant SDATFiles from the SDATCache within the specified time range
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(from);
        cal2.setTime(to);
        boolean sameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);

        Map<FileDate, SDATFile[]> filteredMap;
        if (sameDay) {
            filteredMap = cacheData.getSdatFileHashMap().entrySet().stream().parallel()
                    .filter(entry -> {
                        Calendar cal3 = Calendar.getInstance();
                        cal3.setTime(entry.getKey().getStartDate());
                        return cal1.get(Calendar.YEAR) == cal3.get(Calendar.YEAR) &&
                                cal1.get(Calendar.DAY_OF_YEAR) == cal3.get(Calendar.DAY_OF_YEAR);
                    })
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        } else {
            Calendar cal4 = Calendar.getInstance();
            cal4.setTime(to);
            cal4.add(Calendar.DATE, 1); // add one day to make the range inclusive
            Date inclusiveLatestDate = cal4.getTime();
            filteredMap = cacheData.getSdatFileHashMap().entrySet().stream().parallel()
                    .filter(entry ->
                            !entry.getKey().getStartDate().before(from) && !entry.getKey().getStartDate().after(inclusiveLatestDate)
                    )
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }

        response.setContentType("text/csv");
        SimpleDateFormat dateFormat = new SimpleDateFormat(GlobalStuff.FILENAME_DATE_FORMAT);
        String currentDateTime = dateFormat.format(new Date());

        String headerKey = "Content-Disposition";

        String fromDate = dateFormat.format(from);
        String toDate = dateFormat.format(to);
        String fileName = "data_" + currentDateTime + "_from_" + fromDate + "_to_" + toDate + ".csv";
        String headerValue = "attachment; filename=" + fileName;
        response.setHeader(headerKey, headerValue);

        // Generate CSV content
        String[] csvHeader = {"Start", "End", "EconomicActivity", "ResolutionType", "ResolutionAmount", "MeasureUnit", "Observations"};
        String[] nameMapping = {"startDate", "endDate", "economicActivity", "resolutionType", "resolutionAmount", "measureUnit", "observations"};

        ICsvMapWriter csvWriter = new CsvMapWriter(response.getWriter(), CsvPreference.EXCEL_PREFERENCE);
        csvWriter.writeHeader(csvHeader);

        for (Map.Entry<FileDate, SDATFile[]> entry : filteredMap.entrySet()) {
            FileDate fileDate = entry.getKey();
            SDATFile[] sdatFiles = entry.getValue();
            for (SDATFile sdatFile : sdatFiles) {
                Map<String, Object> csvContent = new HashMap<>();
                csvContent.put("startDate", fileDate.getStartDate());
                csvContent.put("endDate", fileDate.getEndDate());
                csvContent.put("economicActivity", sdatFile.getEconomicActivity());
                csvContent.put("resolutionType", sdatFile.getResolution().getTimeUnit());
                csvContent.put("resolutionAmount", sdatFile.getResolution().getResolution());
                csvContent.put("measureUnit", sdatFile.getMeasureUnit());
                csvContent.put("observations", sdatFile.getObservations());
                csvWriter.write(csvContent, nameMapping);
            }
        }

        csvWriter.close();
    }

    @GetMapping(
            value = "/json/{from}/{to}",
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    public void exportDataJSON(
            @PathVariable("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date from,
            @PathVariable("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date to,
            HttpServletResponse response
    ) throws IOException {
        // Retrieve the relevant SDATFiles from the SDATCache within the specified time range
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(from);
        cal2.setTime(to);
        boolean sameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);

        Map<FileDate, SDATFile[]> filteredMap;
        if (sameDay) {
            filteredMap = cacheData.getSdatFileHashMap().entrySet().stream().parallel()
                    .filter(entry -> {
                        Calendar cal3 = Calendar.getInstance();
                        cal3.setTime(entry.getKey().getStartDate());
                        return cal1.get(Calendar.YEAR) == cal3.get(Calendar.YEAR) &&
                                cal1.get(Calendar.DAY_OF_YEAR) == cal3.get(Calendar.DAY_OF_YEAR);
                    })
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        } else {
            Calendar cal4 = Calendar.getInstance();
            cal4.setTime(to);
            cal4.add(Calendar.DATE, 1); // add one day to make the range inclusive
            Date inclusiveLatestDate = cal4.getTime();
            filteredMap = cacheData.getSdatFileHashMap().entrySet().stream().parallel()
                    .filter(entry ->
                            !entry.getKey().getStartDate().before(from) && !entry.getKey().getStartDate().after(inclusiveLatestDate)
                    )
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }

        // json
        response.setContentType("application/json");
        SimpleDateFormat dateFormat = new SimpleDateFormat(GlobalStuff.FILENAME_DATE_FORMAT);
        String currentDateTime = dateFormat.format(new Date());

        String headerKey = "Content-Disposition";

        String fromDate = dateFormat.format(from);
        String toDate = dateFormat.format(to);
        String fileName = "data_" + currentDateTime + "_from_" + fromDate + "_to_" + toDate + ".json";

        // todo
    }
}
