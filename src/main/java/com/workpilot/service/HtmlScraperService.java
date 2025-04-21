package com.workpilot.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
public class HtmlScraperService {

    public void scrapeHtmlFile() {
        // Charger le fichier depuis le classpath
        try (InputStream in = getClass().getResourceAsStream("/app.txt")) {
            if (in == null) {
                System.err.println("Le fichier app.txt n'a pas été trouvé dans le dossier resources.");
                return;
            }

            // Parser le contenu HTML
            Document doc = Jsoup.parse(in, "UTF-8", "");

            // EXEMPLE 1 : Extraire les titres (balises <h1>, <h2>, <h3>)
            Elements titles = doc.select("h1, h2, h3");
            System.out.println("=== Titres trouvés ===");
            for (Element title : titles) {
                System.out.println(title.tagName() + " : " + title.text());
            }

            // EXEMPLE 2 : Extraire les liens (balises <a>)
            Elements links = doc.select("a");
            System.out.println("=== Liens trouvés ===");
            for (Element link : links) {
                String href = link.attr("href");
                String linkText = link.text();
                System.out.println("Lien : " + linkText + " -> " + href);
            }

            // EXEMPLE 3 : Extraire une section spécifique (par exemple avec l'id "content")
            Element contentSection = doc.getElementById("content");
            if (contentSection != null) {
                System.out.println("=== Contenu de la section 'content' ===");
                System.out.println(contentSection.text());
            } else {
                System.out.println("Aucune section avec l'id 'content' n'a été trouvée.");
            }

            // EXEMPLE 4 : Extraire tous les tableaux et leurs données
            Elements tables = doc.select("table");
            System.out.println("=== Tableaux trouvés ===");
            int tableCount = 0;
            for (Element table : tables) {
                tableCount++;
                System.out.println("Tableau " + tableCount + ":");
                // Récupérer toutes les lignes du tableau
                Elements rows = table.select("tr");
                for (Element row : rows) {
                    // Extraire les cellules d'en-tête (<th>)
                    Elements headerCells = row.select("th");
                    for (Element header : headerCells) {
                        System.out.print(header.text() + "\t");
                    }
                    // Extraire les cellules de données (<td>)
                    Elements dataCells = row.select("td");
                    for (Element cell : dataCells) {
                        System.out.print(cell.text() + "\t");
                    }
                    // Sauter à la ligne après chaque ligne du tableau
                    System.out.println();
                }
                // Séparation entre les tableaux
                System.out.println();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
