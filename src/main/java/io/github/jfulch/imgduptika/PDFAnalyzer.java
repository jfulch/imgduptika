package io.github.jfulch.imgduptika;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.*;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.Map.Entry;

public class PDFAnalyzer {
    public static void main(String[] args) {
        String pdfPath = "PrevalenceInappropriate.pdf";
        String outputPath = "analysis.md";

        try {
            analyzePDF(pdfPath, outputPath);
            System.out.println("Analysis complete. Results written to " + outputPath);
        } catch (IOException e) {
            System.err.println("Error reading or writing file: " + e.getMessage());
            e.printStackTrace();
        } catch (TikaException e) {
            System.err.println("Error parsing PDF: " + e.getMessage());
            e.printStackTrace();
        } catch (SAXException e) {
            System.err.println("Error in XML parsing: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void analyzePDF(String pdfPath, String outputPath) throws IOException, TikaException, SAXException {
        try (InputStream input = new FileInputStream(pdfPath);
             FileWriter writer = new FileWriter(outputPath)) {

            // Extract metadata and content
            Metadata metadata = new Metadata();
            BodyContentHandler handler = new BodyContentHandler(-1);
            ParseContext context = new ParseContext();
            PDFParser parser = new PDFParser();
            parser.parse(input, handler, metadata, context);

            String content = handler.toString();
            
            // Clean up the content
            String cleanContent = content
                .replaceAll("\\d+\\s*$", "")
                .replaceAll("^\\s*\\d+\\s*", "")
                .replaceAll("\\s{2,}", " ")
                .replaceAll("(?<=\\d)\\s+", " ")
                .replaceAll("\\s*\\n+\\s*", " ")
                .replaceAll("\\s+,", ",")
                .replaceAll("\\s+\\.", ".")
                .replaceAll("\\s*\\*\\s*", "")
                .replaceAll("\\s*#\\s*", "")
                .trim();

            // Write in Markdown format
            writer.write("# PDF Analysis Results\n\n");
            
            writer.write("## Document Properties\n");
            writer.write("- **PDF Version:** " + metadata.get("pdf:version") + "\n");
            writer.write("- **Page Count:** " + metadata.get("xmpTPg:NPages") + "\n");
            writer.write("- **PDF Encrypted:** " + metadata.get("pdf:encrypted") + "\n");
            writer.write("- **Has XMP?:** " + metadata.get("xmp:CreatorTool") + "\n\n");

            writer.write("## Content Statistics\n");
            String[] words = cleanContent.split("\\s+");
            writer.write("- **Word Count:** " + words.length + "\n");
            writer.write("- **Character Count:** " + cleanContent.length() + "\n");
            writer.write("- **Paragraph Count:** " + cleanContent.split("\n\n").length + "\n\n");

            writer.write("## Reference Analysis\n");
            Pattern citationPattern = Pattern.compile("\\([12][0-9]{3}\\)");
            Matcher citationMatcher = citationPattern.matcher(cleanContent);
            Set<String> uniqueCitations = new HashSet<>();
            while (citationMatcher.find()) {
                uniqueCitations.add(citationMatcher.group());
            }
            writer.write("- **Citation Count:** " + uniqueCitations.size() + "\n");
            writer.write("- **Citations Found:** " + String.join(", ", uniqueCitations) + "\n\n");

            writer.write("## Figure Analysis\n");
            Pattern figurePattern = Pattern.compile("(?i)(fig\\.|figure|table)\\s*\\d+");
            Matcher figureMatcher = figurePattern.matcher(cleanContent);
            Set<String> uniqueFigures = new HashSet<>();
            while (figureMatcher.find()) {
                uniqueFigures.add(figureMatcher.group().toLowerCase());
            }
            writer.write("- **Figure/Table Count:** " + uniqueFigures.size() + "\n");
            writer.write("- **References Found:** " + String.join(", ", uniqueFigures) + "\n\n");

            writer.write("## Technical Details\n");
            writer.write("- **Producer:** " + metadata.get("pdf:producer") + "\n");
            writer.write("- **Creator Tool:** " + metadata.get("xmp:CreatorTool") + "\n");
            writer.write("- **Creation Date:** " + metadata.get(TikaCoreProperties.CREATED) + "\n");
            writer.write("- **Modified Date:** " + metadata.get(TikaCoreProperties.MODIFIED) + "\n\n");

            writer.write("## Key Findings\n");
            String summary = generateSummary(cleanContent);
            String[] summaryPoints = summary.split("\n\n");
            for (int i = 0; i < summaryPoints.length; i++) {
                writer.write(String.format("%d. %s\n\n", i + 1, summaryPoints[i].trim()));
            }
        }
    }

    private static String generateSummary(String content) {
        String[] sentences = content.split("(?<=[.!?])\\s+");
        Map<String, Integer> wordFrequencies = new HashMap<>();
        Pattern wordPattern = Pattern.compile("\\b\\w+\\b");
        Matcher matcher = wordPattern.matcher(content.toLowerCase());
        
        while (matcher.find()) {
            String word = matcher.group();
            if (word.length() > 3) {
                wordFrequencies.merge(word, 1, Integer::sum);
            }
        }

        Map<String, Double> sentenceScores = new HashMap<>();
        for (String sentence : sentences) {
            if (sentence.length() < 20) continue;
            
            double score = 0;
            Matcher wordMatcher = wordPattern.matcher(sentence.toLowerCase());
            while (wordMatcher.find()) {
                String word = wordMatcher.group();
                if (word.length() > 3) {
                    score += wordFrequencies.getOrDefault(word, 0);
                }
            }
            sentenceScores.put(sentence, score / Math.sqrt(sentence.length()));
        }

        return sentenceScores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(3)
                .map(Entry::getKey)
                .map(String::trim)
                .collect(Collectors.joining("\n\n"));
    }
}