package com.workpilot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workpilot.entity.CalendarificResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value; // ✅ Correct

@Service
public class PublicHolidayService {

    @Value("${calendarific.api.key}") // ✅ Correct : syntaxe Spring correcte
    private String apiKey;


    private final RestTemplate restTemplate = new RestTemplate();

    public List<CalendarificResponse.Holiday> getPublicHolidays(int year) {
        String url = "https://calendarific.com/api/v2/holidays?api_key=" + apiKey +
                "&country=TN&year=" + year;

        ResponseEntity<CalendarificResponse> response =
                restTemplate.getForEntity(url, CalendarificResponse.class);

        return response.getBody().extractHolidays();
    }

    public List<String> getAllHolidaysIsoByYear(int year) {
        return getPublicHolidays(year)
                .stream()
                .map(CalendarificResponse.Holiday::getDate) // car getDate() retourne déjà un String ISO
                .collect(Collectors.toList());
    }
    public Set<LocalDate> getIslamicHolidaysForYear(int year) {
        Set<LocalDate> religiousHolidays = new HashSet<>();

        for (int month = 1; month <= 12; month++) {
            for (int day = 1; day <= YearMonth.of(year, month).lengthOfMonth(); day++) {
                LocalDate date = LocalDate.of(year, month, day);
                String hijriDate = getHijriDateFromApi(date);

                if (hijriDate != null) {
                    // Ex: "1-10-1446" → 1 Shawwal → Aïd el-Fitr
                    if (hijriDate.startsWith("1-10")) {
                        religiousHolidays.add(date); // Aïd el-Fitr
                    }
                    if (hijriDate.startsWith("10-12")) {
                        religiousHolidays.add(date); // Aïd el-Adha
                    }
                    if (hijriDate.startsWith("12-3")) {
                        religiousHolidays.add(date); // Mouled
                    }
                }
            }
        }

        return religiousHolidays;
    }

    private String getHijriDateFromApi(LocalDate date) {
        try {
            String apiUrl = String.format("https://hijri-api.herokuapp.com/gregorian/%d/%d/%d",
                    date.getYear(), date.getMonthValue(), date.getDayOfMonth());

            HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
            conn.setRequestMethod("GET");

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String response = reader.lines().collect(Collectors.joining());

                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response);

                return root.path("data").path("hijri").path("date").asText(); // Ex: "1-10-1446"
            }
        } catch (Exception e) {
            System.err.println("Erreur API Hijri : " + e.getMessage());
            return null;
        }
    }
    public Set<LocalDate> getAllCombinedHolidays(int year) {
        Set<LocalDate> allHolidays = new HashSet<>();

        // 1. Ajouter les jours fériés officiels (ISO string → LocalDate)
        List<String> officialIsoDates = getAllHolidaysIsoByYear(year);
        for (String isoDate : officialIsoDates) {
            allHolidays.add(LocalDate.parse(isoDate));
        }

        // 2. Ajouter les jours fériés religieux
        Set<LocalDate> islamicHolidays = getIslamicHolidaysForYear(year);
        allHolidays.addAll(islamicHolidays);

        return allHolidays;
    }

}
