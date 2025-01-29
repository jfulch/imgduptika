# PDF Analyzer

A Java application that uses Apache Tika to analyze PDF documents and generate detailed reports in Markdown format.

## Resutls

If you are only here looking for the results of the pdf anayasis you can find the sample article that was used here: [The Prevalence of Inappropriate Image Duplication in Biomedical
Research Publications](./PrevalenceInappropriate.pdf) and you can find the results of the tika anaysys here: [analysis.md](./analysis.md)

## Features

- Extracts document metadata and properties
- Provides content statistics (word count, character count, etc.)
- Identifies and counts citations
- Tracks figure and table references
- Generates key findings using text analysis
- Outputs results in clean Markdown format

## Prerequisites

- Java 11 or higher
- Maven
- A PDF file to analyze

## Setup

1. Clone the repository:
```bash
git clone <repository-url>
cd imgduptika
```

2. Compile the source code:
```bash
mvn compile
```

## Usage

To run the PDF analyzer, use the following command:

```bash
mvn exec:java -Dexec.mainClass="pdf.PdfAnalyzer" -Dexec.args="path/to/pdf/file.pdf"
```

Replace `path/to/pdf/file.pdf` with the path to the PDF file you want to analyze.

The output will be saved to a file named `analysis.md` in the root directory.