# NSeqVerify 0.0.1

A Java-based desktop application for the **integrated analysis of Next-Generation Sequencing (NGS) data**, from raw FASTQ reads to assembled and annotated genomes.

<p align="center">
  <img src="[https://raw.githubusercontent.com/roberto117343/NSeqVerify/main/logo.png](https://github.com/roberto117343/NSeqVerify/blob/main/NSeqVerify/logo%20NSeqVerify.png)" 
       alt="NSeqVerify Logo" width="200"/>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/License-GPLv3-blue.svg" alt="License: GPL v3"/>
  <img src="https://img.shields.io/badge/Java-11+-orange.svg" alt="Java Version"/>
  <img src="https://img.shields.io/badge/Platform-Cross--Platform-lightgrey.svg" alt="Platform"/>
</p>

    
<p align="center">
  <a href="https://raw.githubusercontent.com/roberto117343/NSeqVerify/main/NSeqVerify/target/NSeqVerify-0.0.1.jar" style="text-decoration:none;">
    <img src="https://img.shields.io/badge/Download-NSeqVerify--0.0.1.jar-brightgreen?style=for-the-badge&logo=github" alt="Download .jar"/>
  </a>
</p>
<!-- 
NOTA: Para que este enlace de descarga funcione, debes ir a la sección "Releases" en tu repositorio de GitHub, 
crear un nuevo release con la etiqueta 'v0.0.1' y adjuntar tu archivo NSeqVerify-0.0.1.jar. 
-->

---

## 💡 What is NSeqVerify?

**NSeqVerify** is a user-friendly, all-in-one desktop application designed to streamline the complex workflow of genomic analysis. It empowers researchers without specialized bioinformatics training to process their own NGS data, from the raw output of a sequencer to biologically meaningful results.

The software was developed and validated using both reference viral genomes and real-world metagenomic samples, demonstrating its capability to assemble known genomes with high fidelity and to **discover novel viral sequences** in complex datasets.

---

## 🎯 The Problem

The analysis of NGS data is a multi-step process that typically requires proficiency with multiple command-line tools, creating a significant barrier for many wet-lab scientists. Key challenges include:
-   **Data Quality Control**: Filtering and cleaning raw FASTQ files.
-   ***De Novo* Assembly**: Reconstructing genomes from millions of short reads, a computationally intensive task.
-   **Taxonomic Annotation**: Identifying the assembled sequences by comparing them against massive public databases.

There is a need for an integrated tool that simplifies this entire pipeline, making genomic analysis more accessible and accelerating the pace of scientific discovery.

---

## 🔬 Our Solution: An Integrated Three-Module Workflow

NSeqVerify encapsulates the entire analysis pipeline within a single, intuitive graphical user interface (GUI), structured into three core modules.

### **Module 1: FASTQ Preprocessing (`Preprocess FASTQ`)**
The first stage ensures that only high-quality data is used for assembly. It allows users to:
1.  **Filter Reads**: By average Phred quality score.
2.  **Trim Ends**: Remove low-quality bases or adapter remnants from the ends of reads.
3.  **Subsample Data**: Process a subset of reads for quick tests.
4.  **Generate Reverse Complements**: Enhance the connectivity for the assembly graph.

### **Module 2: *De Novo* Assembly (`Assemble`)**
This is the core of NSeqVerify. It reconstructs genomes using a powerful assembler with advanced features:
1.  **Multi-K-mer Strategy**: Iteratively assembles the data using a range of k-mer sizes. Contigs from one round are used as "super-reads" in the next, dramatically improving contiguity.
2.  **Advanced Graph Simplification**: Implements robust algorithms to "pop bubbles" and "clip tips" in the De Bruijn graph, correcting for sequencing errors and genomic variants to produce cleaner, longer contigs.

### **Module 3: Taxonomic Assignment (`Classify nt` / `Classify aa`)**
The final stage provides a biological identity to your assembled contigs. The module:
1.  **Automates BLAST Searches**: Submits each contig to the NCBI web BLAST service (BLASTn or BLASTp).
2.  **Parses Results**: Automatically extracts key metrics (E-value, percent identity, query cover, etc.) from the most significant hit.
3.  **Generates Reports**: Creates a clean, tab-separated (TSV) file with the results and isolates any unclassified contigs into a separate FASTA file for further investigation.

---

## ✨ Key Features of the GUI

<p align="center">
  <img src="https://raw.githubusercontent.com/roberto117343/NSeqVerify/main/gui_screenshot.png" alt="NSeqVerify GUI Screenshot" width="450"/>
</p>
<!-- 
NOTA: Para que la captura de pantalla funcione, sube una imagen llamada 'gui_screenshot.png' a la raíz de tu repositorio. 
-->

1.  **Unified Workflow**
    -   Select the desired task (`Preprocess`, `Assemble`, `Classify`) from a single dropdown menu.
    -   The interface dynamically enables/disables parameter fields relevant to the selected task.

2.  **Intuitive Parameter Control**
    -   Easily set parameters for quality control, k-mer strategies, and graph cleaning heuristics.
    -   Select input and output files using a simple file browser.

3.  **Real-time Process Management**
    -   A `Calculate` button to start the analysis and a `Cancel` button to safely interrupt long-running processes.

---

## 📦 Installation & Usage

### Requirements
*   **Java Runtime Environment (JRE)** version 11 or higher.

### Using the App (NSeqVerify-0.0.1.jar)

No complex installation is needed. Simply download the `.jar` file from the link at the top and run it from your terminal:

```bash
java -jar NSeqVerify-0.0.1.jar
```

### General Workflow:
1.  **Load Input File**: Click `Input` to load your FASTQ file (for preprocessing) or a plain text/FASTA file of sequences (for assembly/classification).
2.  **Select Calculation Type**: Choose the desired module from the dropdown.
3.  **Set Parameters**: Adjust the settings for your specific analysis. For metagenomic discovery, a good starting point is a wide `K-MERS` range (e.g., `21;33;45;55;65`) and a low `Min. k-mer Freq.` (e.g., `3`).
4.  **Specify Output File**: Click `Output` to choose a location and name for your results.
5.  **Run**: Click `Calculate` to start the process.

---

## 📜 How to Cite

If you use NSeqVerify in your research, please cite the accompanying preprint:

> coming soon.

---

## 📄 License

This project is licensed under the **GNU General Public License v3.0**. See the `LICENSE` file for details.

---

## 📬 Contact

**Roberto Reinosa Fernández**
-   📧 roberto117343@gmail.com
-   💻 [GitHub Profile](https://github.com/roberto117343)
