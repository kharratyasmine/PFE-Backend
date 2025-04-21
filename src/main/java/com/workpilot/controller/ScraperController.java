package com.workpilot.controller;

import com.workpilot.service.HtmlScraperService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ScraperController {

    private final HtmlScraperService htmlScraperService;

    public ScraperController(HtmlScraperService htmlScraperService) {
        this.htmlScraperService = htmlScraperService;
    }

    @GetMapping("/scrape")
    public String scrape() {
        htmlScraperService.scrapeHtmlFile();
        return "Scraping terminé, consultez la console pour voir le résultat.";
    }
}
