package com.workpilot.entity;

import java.util.List;
import java.util.stream.Collectors;

public class CalendarificResponse {

    private Response response;

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public List<Holiday> extractHolidays() {
        return response.getHolidays().stream()
                .map(h -> new Holiday(h.getName(), h.getDate().getIso()))
                .collect(Collectors.toList());
    }

    public static class Response {
        private List<HolidayWrapper> holidays;

        public List<HolidayWrapper> getHolidays() {
            return holidays;
        }

        public void setHolidays(List<HolidayWrapper> holidays) {
            this.holidays = holidays;
        }
    }

    public static class HolidayWrapper {
        private String name;
        private DateWrapper date;

        public String getName() {
            return name;
        }

        public DateWrapper getDate() {
            return date;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setDate(DateWrapper date) {
            this.date = date;
        }
    }

    public static class DateWrapper {
        private String iso;

        public String getIso() {
            return iso;
        }

        public void setIso(String iso) {
            this.iso = iso;
        }
    }

    public static class Holiday {
        private String name;
        private String date;

        public Holiday(String name, String date) {
            this.name = name;
            this.date = date;
        }

        public String getName() {
            return name;
        }

        public String getDate() {
            return date;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setDate(String date) {
            this.date = date;
        }
    }
}
