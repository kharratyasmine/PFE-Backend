package com.workpilot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workpilot.entity.CalendarificResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PublicHolidayService {

    @Value("${calendarific.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    // ✅ Cache mémoire pour éviter les appels multiples
    private final Map<Integer, Set<LocalDate>> holidayCache = new HashMap<>();

    public List<CalendarificResponse.HolidayWrapper> getPublicHolidays(int year) {
        try {
            String url = "https://calendarific.com/api/v2/holidays?api_key=" + apiKey +
                    "&country=TN&year=" + year;

            ResponseEntity<CalendarificResponse> response =
                    restTemplate.getForEntity(url, CalendarificResponse.class);

            if (response.getBody() != null && response.getBody().getResponse() != null) {
                return response.getBody().getResponse().getHolidays();
            } else {
                System.err.println("⚠️ Réponse vide ou malformée de Calendarific pour l’année " + year);
                return Collections.emptyList();
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'appel à Calendarific : " + e.getMessage());
            return Collections.emptyList();
        }
    }


    public List<String> getAllOfficialHolidayIsoDates(int year) {
        return getPublicHolidays(year).stream()
                .filter(h -> "Public Holiday".equals(h.getPrimary_type()))
                .map(h -> h.getDate().getIso())
                .collect(Collectors.toList());
    }

    public Set<LocalDate> getAllCombinedHolidays(int year) {
        // ✅ Utilise le cache s’il existe déjà
        if (holidayCache.containsKey(year)) {
            return holidayCache.get(year);
        }

        Set<LocalDate> allHolidays = new HashSet<>();
        try {
            // 1. Appel réel à Calendarific
            List<String> officialIsoDates = getAllOfficialHolidayIsoDates(year);
            for (String isoDate : officialIsoDates) {
                allHolidays.add(LocalDate.parse(isoDate));
            }

            // ✅ Mettre en cache le résultat
            holidayCache.put(year, allHolidays);

        } catch (Exception e) {
            System.err.println("⚠️ Erreur lors de l'appel à Calendarific pour l'année " + year + " : " + e.getMessage());

            // ✅ Fallback de secours local si l'API échoue
            allHolidays = getFallbackHolidays(year);
        }

        return allHolidays;
    }

    public Set<LocalDate> getAllCombinedHolidaysBetween(LocalDate start, LocalDate end) {
        Set<LocalDate> holidays = new HashSet<>();
        for (int year = start.getYear(); year <= end.getYear(); year++) {
            holidays.addAll(getAllCombinedHolidays(year));
        }
        return holidays;
    }

    public List<CalendarificResponse.Holiday> getSimplifiedHolidays(int year) {
        return getPublicHolidays(year).stream()
                .filter(h -> "Public Holiday".equals(h.getPrimary_type()))
                .map(h -> new CalendarificResponse.Holiday(h.getName(), h.getDate().getIso()))
                .collect(Collectors.toList());
    }

    public Set<LocalDate> getPublicHolidaysForYear(int year) {
        return getAllCombinedHolidays(year);
    }

    // ✅ Fallback local si l'API échoue ou limite dépassée
    private Set<LocalDate> getFallbackHolidays(int year) {
        if (year == 2025) {
            return Set.of(
                    LocalDate.of(2025, 1, 1),   // Jour de l'an
                    LocalDate.of(2025, 3, 20),  // Fête de l’indépendance
                    LocalDate.of(2025, 5, 1),   // Fête du travail
                    LocalDate.of(2025, 7, 25),  // Fête de la République
                    LocalDate.of(2025, 9, 15)   // Exemple personnalisé
            );
        }
        return Collections.emptySet();
    }
}
