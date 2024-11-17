package com.example.texttool;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_PDF_FILE = 1;
    private static final int PICK_TEXT_FILE = 2;
    private EditText wordCount;
    private EditText paragraphTemperature;
    private TextView displayText;
    private TextView viewRandomParagraph;
    private TextStatistics textStatistics;
    private RandomParagraphGenerator paragraphGenerator;
    private String randomParagraph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Link variables with filed ids.
        wordCount = findViewById(R.id.word_count);
        paragraphTemperature = findViewById(R.id.input_temperature);
        Button uploadPdfButton = findViewById(R.id.button_load_pdf_file);
        Button uploadTextButton = findViewById(R.id.button_load_text_file);
        displayText = findViewById(R.id.text_view_statistics);
        viewRandomParagraph = findViewById(R.id.text_view_random_paragraph);
        Button saveToPdf = findViewById(R.id.button_save_as_pdf);
        Button generateRandomParagraph = findViewById(R.id.button_generate_paragraph);

        // Check if the user wanna upload pdf file or text file.
        uploadPdfButton.setOnClickListener(v -> pickFile(PICK_PDF_FILE));
        uploadTextButton.setOnClickListener(v -> pickFile(PICK_TEXT_FILE));

        // Click the button to generate the random paragraph.
        generateRandomParagraph.setOnClickListener(v -> generateRandomParagraph());

        // Click the button to save the output in a pdf file.
        saveToPdf.setOnClickListener(v -> {
            saveToPdf(getExternalFilesDir(null) + "/output.pdf", displayText.getText().toString(), randomParagraph);
        });

    }

    // Check if the user choose to upload text file or pdf file.
    private void pickFile(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(requestCode == PICK_PDF_FILE ? "application/pdf" : "text/plain");
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String content;
        if (resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            try {
                // User choose to upload the pdf file.
                if (requestCode == PICK_PDF_FILE) {
                    content = processPdfFile(uri);
                // User choose to upload the text file.
                } else {
                    content = processTextFile(uri);
                }
                // Initialize a new textStatistics for further operations.
                textStatistics = new TextStatistics(content);
                // Update the Statistics field to show related statistics (e.g., word count...)
                updateStatisticsDisplay();
                // Prepare for random paragraph generation.
                initializeParagraphGenerator(content);
            } catch (Exception e) {
                displayText.setText("Error processing file: " + e.getMessage());
            }
        }
    }

    private String processPdfFile(Uri uri) throws Exception {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        PdfReader reader = new PdfReader(inputStream);
        PdfDocument pdfDocument = new PdfDocument(reader);

        // Read pdf files.
        StringBuilder text = new StringBuilder();
        for (int i = 1; i <= pdfDocument.getNumberOfPages(); i++) {
            text.append(PdfTextExtractor.getTextFromPage(pdfDocument.getPage(i)));
        }
        pdfDocument.close();

        // Return the content of the pdf file in a string.
        return text.toString();
    }

    private String processTextFile(Uri uri) throws Exception {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        // Read text files.
        StringBuilder text = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            text.append(line).append("\n");
        }
        reader.close();

        // Return the content of the text file in a string.
        return text.toString();
    }

    private void initializeParagraphGenerator(String content) {
        List<String> words = Arrays.asList(content.split("\\W+"));
        paragraphGenerator = new RandomParagraphGenerator(words);
    }

    private void generateRandomParagraph() {
        if (paragraphGenerator != null) {
            // Get word count and temperature.
            String temperatureString = paragraphTemperature.getText().toString();
            String wordCountString = wordCount.getText().toString();
            // Check if these two values are empty.
            if (temperatureString.isEmpty() || wordCountString.isEmpty()) {
                Toast.makeText(this, "Please enter values for word count and temperature", Toast.LENGTH_SHORT).show();
            }
            else {
                // Generate the random paragraph.
                double temperature = Double.valueOf(temperatureString);
                int wordNum = Integer.valueOf(wordCountString);
                randomParagraph = paragraphGenerator.generateParagraph(wordNum, temperature);
                viewRandomParagraph.setText(randomParagraph);
            }
        }
    }

    private void updateStatisticsDisplay() {
        if (textStatistics != null) {
            StringBuilder stats = new StringBuilder();
            // Use textStatistics to get all related statistics.
            stats.append("Word Count: ").append(textStatistics.getWordCount()).append("\n");
            stats.append("Sentence Count: ").append(textStatistics.getSentenceCount()).append("\n");
            stats.append("Unique Words: ").append(textStatistics.getUniqueWordFrequency().size()).append("\n");

            List<Map.Entry<String, Integer>> topWords = textStatistics.getTopFiveWords();
            stats.append("Top 5 Words:\n");
            for (Map.Entry<String, Integer> entry : topWords) {
                stats.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }

            // Update the textview field.
            displayText.setText(stats.toString());
        }
    }

    private void saveToPdf(String filePath, String stats, String paragraph) {
        try {
            // Define the directory
            File pdfDir = new File(getExternalFilesDir(null), "PDFs");
            if (!pdfDir.exists()) {
                pdfDir.mkdir(); // Create directory if it doesn't exist
            }

            // A new file is created.
            File pdfFile = new File(pdfDir, filePath);

            // Create the PDF writer
            PdfWriter writer = new PdfWriter(filePath);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            document.add(new Paragraph("Text Statistics"));
            document.add(new Paragraph(stats));
            document.add(new Paragraph("\nGenerated Paragraph"));
            document.add(new Paragraph(paragraph));

            document.close();

            // Show success message.
            Toast.makeText(this, "PDF file saved successfully!\nLocation: " + pdfFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            // Show error message.
            Toast.makeText(this, "Error saving PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

}
