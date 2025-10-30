/**
 * «Copyright 2025 Roberto Reinosa Fernández»
 * 
 * This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.RRF.nseqverify;

import static com.RRF.nseqverify.Interfaz.jButton3;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author Roberto Reinosa Fernández
 */

public class Ensamblador {
    
    // --- PARÁMETROS DE LA ESTRATEGIA DE BÚSQUEDA ---
    private static List<Integer> K_MER_SIZES = new ArrayList<>(); 
    private static int MIN_KMER_FREQUENCY;
    private static int MIN_CONTIG_LENGTH;
    
    // Parámetros para la simplificación
    private static int TIP_LENGTH_THRESHOLD_FACTOR; // Eliminar tips más cortos que K * factor
    private static int MAX_BUBBLE_PATH_LENGTH;
    private static double BUBBLE_COVERAGE_RATIO;

    public static void cargarEnsamblador(String inputFile, String outputFile, String K_MER_SIZES_i, int MIN_KMER_FREQUENCY_i, 
            int MIN_CONTIG_LENGTH_i, int TIP_LENGTH_THRESHOLD_FACTOR_i, int MAX_BUBBLE_PATH_LENGTH_i, double BUBBLE_COVERAGE_RATIO_i) {
       
                try {
                    
                    MIN_KMER_FREQUENCY = MIN_KMER_FREQUENCY_i;
                    MIN_CONTIG_LENGTH = MIN_CONTIG_LENGTH_i;
                    TIP_LENGTH_THRESHOLD_FACTOR = TIP_LENGTH_THRESHOLD_FACTOR_i;
                    MAX_BUBBLE_PATH_LENGTH = MAX_BUBBLE_PATH_LENGTH_i;
                    BUBBLE_COVERAGE_RATIO = BUBBLE_COVERAGE_RATIO_i;
                    
                    K_MER_SIZES.clear();
                    
                    String cadenaKMERSCortada[] = K_MER_SIZES_i.split(";");
                                        
                    for(int i = 0; i<cadenaKMERSCortada.length; i++){
                        
                        if (Thread.currentThread().isInterrupted()) {

                            break;

                        }
                        
                        K_MER_SIZES.add(Integer.parseInt(cadenaKMERSCortada[i]));
                        
                    }
                    
            System.out.println("--- NSeqVerify v0.0.1 (Multi-K-mer + Bubble Popping) ---");
            long startTime = System.currentTimeMillis();

            List<String> initialReads = loadReadsFromFile(inputFile);
            List<String> contigs = assembleWithMultipleKmers(initialReads);

            saveContigsToFasta(contigs, outputFile);
            printAssemblyStats(contigs);
            
            long endTime = System.currentTimeMillis();
            System.out.println("\nProceso de ensamblaje completado en " + (endTime - startTime) / 1000.0 + " segundos.");

        } catch (NumberFormatException e) {
            
            Logger.getLogger(Ensamblador.class.getName()).log(Level.SEVERE, null, e);
            jButton3.setEnabled(true);
            CancelarHilos.cancelarHilos();
            
        }
    }

    public static List<String> assembleWithMultipleKmers(List<String> initialReads) {
        
        try{
            
            List<String> currentAssemblyInput = new ArrayList<>(initialReads);
            List<String> finalContigs = new ArrayList<>();

            for (int kmerSize : K_MER_SIZES) {

                if (Thread.currentThread().isInterrupted()) {

                    break;

                }

                System.out.println("\n=========================================================");
                System.out.println("--- INICIANDO ENSAMBLAJE PARA K = " + kmerSize + " ---");
                System.out.println("Usando " + currentAssemblyInput.size() + " secuencias como entrada.");

                Map<String, Integer> kmerCounts = KmerCounter.countKmers(currentAssemblyInput, kmerSize);
                DeBruijnGraph graph = GraphBuilder.buildGraph(currentAssemblyInput, kmerCounts, kmerSize);
                GraphSimplifier.simplify(graph, kmerCounts, kmerSize);
                finalContigs = ContigGenerator.generateContigs(graph);

                System.out.println("Ensamblaje para K=" + kmerSize + " finalizado. Se generaron " + finalContigs.size() + " contigs.");

                currentAssemblyInput = new ArrayList<>(initialReads);
                currentAssemblyInput.addAll(finalContigs);
            }

            System.out.println("\n=========================================================");
            System.out.println("Ensamblaje multi-k-mer finalizado.");
            
            return finalContigs;
        
        }catch(Exception e){
            
            Logger.getLogger(Ensamblador.class.getName()).log(Level.SEVERE, null, e);
            jButton3.setEnabled(true);
            CancelarHilos.cancelarHilos();
            
        }
        
        return null;
        
    }

    private static List<String> loadReadsFromFile(String path) {
        
        try{
        
            System.out.println("\n[Fase 1] Cargando reads desde " + path + "...");
            List<String> reads = new ArrayList<>();
            
            try (BufferedReader br = new BufferedReader(new FileReader(path))) {
                
                String line;
                
                while ((line = br.readLine()) != null) {

                    if (Thread.currentThread().isInterrupted()) {

                        break;

                    }

                    String trimmedLine = line.trim();
                    
                    if (!trimmedLine.isEmpty() && trimmedLine.matches("[ACGTNacgtn]+")) {
                        
                        reads.add(trimmedLine.toUpperCase());
                        
                    }
                }
            }
            
            return reads;
        
        }catch(IOException e){
            
            Logger.getLogger(Ensamblador.class.getName()).log(Level.SEVERE, null, e);
            jButton3.setEnabled(true);
            CancelarHilos.cancelarHilos();
            
        }
        
        return null;
        
    }

    private static void saveContigsToFasta(List<String> contigs, String path) {
        
        try{
        
            System.out.println("\nGuardando contigs en " + path + "...");
            
            contigs.sort(Comparator.comparingInt(String::length).reversed());
            
            int contigsSaved = 0;
            
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
                
                final int LINE_WIDTH = 80;
                
                for (String contig : contigs) {

                    if (Thread.currentThread().isInterrupted()) {

                        break;

                    }

                    if (contig.length() < MIN_CONTIG_LENGTH) continue;
                    
                    contigsSaved++;
                    
                    writer.write(">Contig_" + contigsSaved + "_length_" + contig.length());
                    writer.newLine();
                    
                    for (int j = 0; j < contig.length(); j += LINE_WIDTH) {

                        if (Thread.currentThread().isInterrupted()) {

                            break;

                        }

                        writer.write(contig.substring(j, Math.min(j + LINE_WIDTH, contig.length())));
                        writer.newLine();
                        
                    }
                    
                }
                
            }

            System.out.println(contigsSaved + " contigs (longitud >= " + MIN_CONTIG_LENGTH + " bp) guardados.");
        
        }catch(IOException e){
            
            Logger.getLogger(Ensamblador.class.getName()).log(Level.SEVERE, null, e);
            jButton3.setEnabled(true);
            CancelarHilos.cancelarHilos();
            
        }
        
    }
    
    private static void printAssemblyStats(List<String> contigs) {
        
        try{
        
            List<Integer> lengths = contigs.stream()
                .map(String::length)
                .filter(len -> len >= MIN_CONTIG_LENGTH)
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());

            if (lengths.isEmpty()) {
                
                System.out.println("\n--- Estadísticas del Ensamblaje ---");
                System.out.println("No se generaron contigs que cumplieran el criterio de longitud mínima.");
                return;
                
            }

            long totalLength = lengths.stream().mapToLong(Integer::intValue).sum();
            long halfLength = totalLength / 2;
            long cumulativeLength = 0;
            int n50 = 0;
            
            for (int len : lengths) {

                if (Thread.currentThread().isInterrupted()) {

                    break;

                }

                cumulativeLength += len;
                
                if (cumulativeLength >= halfLength) {
                    
                    n50 = len;
                    break;
                    
                }
                
            }

            System.out.println("\n--- Estadísticas del Ensamblaje ---");
            System.out.println("Número de contigs (>=" + MIN_CONTIG_LENGTH + " bp): " + lengths.size());
            System.out.println("Longitud total del ensamblaje: " + totalLength + " bp");
            System.out.println("Contig más largo: " + lengths.get(0) + " bp");
            System.out.println("N50: " + n50 + " bp");
        
        }catch(Exception e){
            
            Logger.getLogger(Ensamblador.class.getName()).log(Level.SEVERE, null, e);
            jButton3.setEnabled(true);
            CancelarHilos.cancelarHilos();
            
        }
        
    }

    static class KmerCounter {
        
        public static Map<String, Integer> countKmers(List<String> sequences, int k) {
            
            Map<String, Integer> kmerCounts = new ConcurrentHashMap<>();
            
            sequences.parallelStream().forEach(seq -> {
                
                if (seq.length() >= k) {
                    
                    for (int i = 0; i <= seq.length() - k; i++) {
                        
                        if (Thread.currentThread().isInterrupted()) {

                            break;

                        }
                        
                        String kmer = seq.substring(i, i + k);
                        kmerCounts.merge(kmer, 1, Integer::sum);
                        
                    }
                    
                }
                
            });
            
            return kmerCounts;
            
        }
    }

    static class GraphBuilder {
        
        public static DeBruijnGraph buildGraph(List<String> sequences, Map<String, Integer> kmerCounts, int k) {
           
            DeBruijnGraph graph = new DeBruijnGraph();
            
            sequences.parallelStream().forEach(seq -> {
                
                if (seq.length() > k) {
                    
                    for (int i = 0; i < seq.length() - k; i++) {
                        
                        if (Thread.currentThread().isInterrupted()) {

                            break;

                        }
                        
                        String kmer1 = seq.substring(i, i + k);
                        String kmer2 = seq.substring(i + 1, i + 1 + k);
                        
                        if (kmerCounts.getOrDefault(kmer1, 0) >= MIN_KMER_FREQUENCY &&
                            kmerCounts.getOrDefault(kmer2, 0) >= MIN_KMER_FREQUENCY) {
                            
                            graph.addEdge(kmer1, kmer2);
                            
                        }
                        
                    }
                    
                }
                
            });
            
            return graph;
            
        }
        
    }

    static class GraphSimplifier {
        
        public static void simplify(DeBruijnGraph graph, Map<String, Integer> kmerCounts, int kmerSize) {
            
            if (graph.nodes.isEmpty()) return;
            
            System.out.println("Simplificando grafo (" + graph.nodes.size() + " nodos)...");
            
            int pass = 0;
            boolean changed;
            
            do {
                
                if (Thread.currentThread().isInterrupted()) {

                    break;

                }
                
                pass++;
                System.out.println("  Pase de simplificación #" + pass + "...");
                boolean compacted = compactPaths(graph, kmerSize);
                boolean bubblesPopped = popBubbles(graph, kmerCounts, kmerSize);
                boolean tipsRemoved = removeTips(graph, kmerSize);
                changed = compacted || bubblesPopped || tipsRemoved;
                
            } while (changed && pass < 10); // Limitador para evitar bucles infinitos
        }

        private static boolean popBubbles(DeBruijnGraph graph, Map<String, Integer> kmerCounts, int kmerSize) {
            
            AtomicBoolean changed = new AtomicBoolean(false);
            List<Node> potentialStarts = new ArrayList<>(graph.nodes.values());
            potentialStarts.removeIf(n -> n.outDegree() < 2);

            for (Node startNode : potentialStarts) {
                
                if (Thread.currentThread().isInterrupted()) {

                    break;

                }
                
                if (findAndResolveBubble(startNode, graph, kmerCounts, kmerSize)) {
                    
                    changed.set(true);
                    
                }
            }
            if (changed.get()) System.out.println("    - Burbujas eliminadas.");
            
            return changed.get();
            
        }
        
        private static boolean findAndResolveBubble(Node startNode, DeBruijnGraph graph, Map<String, Integer> kmerCounts, int kmerSize) {
           
            if (!graph.nodes.containsKey(startNode.getFirstKmer(kmerSize)) || startNode.outDegree() < 2) return false;

            List<List<Node>> paths = new ArrayList<>();
            
            for (Node pathStart : startNode.getOutNodes()) {
                
                if (Thread.currentThread().isInterrupted()) {

                    break;

                }
                
                List<Node> currentPath = new ArrayList<>();
                Node currentNode = pathStart;
                int currentLength = 0;
                
                while (currentNode.inDegree() == 1 && currentLength < MAX_BUBBLE_PATH_LENGTH) {
                    
                    if (Thread.currentThread().isInterrupted()) {

                        break;

                    }
                    
                    currentPath.add(currentNode);
                    currentLength += currentNode.sequence.length() - kmerSize + 1;
                    if (currentNode.outDegree() != 1) break;
                    currentNode = currentNode.getOutNodes().get(0);
                    
                }
                
                if (currentNode.inDegree() > 1 && !currentPath.isEmpty()) {
                    currentPath.add(currentNode);
                    paths.add(currentPath);
                    
                }
                
            }

            Map<Node, List<List<Node>>> bubbles = paths.stream().filter(p -> !p.isEmpty()).collect(Collectors.groupingBy(path -> path.get(path.size() - 1)));

            boolean poppedSomething = false;
            
            for (List<List<Node>> bubblePaths : bubbles.values()) {
                
                if (Thread.currentThread().isInterrupted()) {

                    break;

                }
                
                if (bubblePaths.size() > 1) {
                    
                    bubblePaths.sort(Comparator.comparingDouble((List<Node> p) -> calculatePathCoverage(p, kmerCounts, kmerSize)).reversed());
                    
                    double bestCoverage = calculatePathCoverage(bubblePaths.get(0), kmerCounts, kmerSize);
                    
                    for (int i = 1; i < bubblePaths.size(); i++) {
                        
                        if (Thread.currentThread().isInterrupted()) {

                            break;

                        }
                        
                        List<Node> loserPath = bubblePaths.get(i);
                        double loserCoverage = calculatePathCoverage(loserPath, kmerCounts, kmerSize);
                        
                        if (bestCoverage > loserCoverage * BUBBLE_COVERAGE_RATIO && loserCoverage > 0) {
                            
                            for (int j = 0; j < loserPath.size() - 1; j++) {
                                
                                if (Thread.currentThread().isInterrupted()) {

                                    break;

                                }
                                
                                graph.removeNode(loserPath.get(j), kmerSize);
                            }
                            
                            poppedSomething = true;
                        }
                        
                    }
                    
                }
                
            }
            
            return poppedSomething;
            
        }

        private static double calculatePathCoverage(List<Node> path, Map<String, Integer> kmerCounts, int kmerSize) {
            
            if (path.isEmpty() || path.size() <= 1) return 0.0;
            
            double totalCoverage = 0;
            int nodeCount = 0;
            
            for (int i = 0; i < path.size() - 1; i++) {
                
                if (Thread.currentThread().isInterrupted()) {

                    break;

                }
                
                totalCoverage += kmerCounts.getOrDefault(path.get(i).getFirstKmer(kmerSize), 0);
                nodeCount++;
            }
            
            return nodeCount > 0 ? totalCoverage / nodeCount : 0.0;
        }

        private static boolean compactPaths(DeBruijnGraph graph, int kmerSize) {
            
            AtomicBoolean changed = new AtomicBoolean(false);
            List<String> nodeKeys = new ArrayList<>(graph.nodes.keySet());
            
            for (String key : nodeKeys) {
                
                if (Thread.currentThread().isInterrupted()) {

                    break;

                }
                
                Node node = graph.nodes.get(key);
                
                if (node != null && node.outDegree() == 1) {
                    
                    Node nextNode = node.getOutNodes().get(0);
                    
                    if (nextNode != node && nextNode.inDegree() == 1) {
                        
                        graph.mergeNodes(node, nextNode, kmerSize);
                        changed.set(true);
                        
                    }
                    
                }
                
            }
            
            if (changed.get()) System.out.println("    - Caminos compactados.");
            
            return changed.get();
            
        }

        private static boolean removeTips(DeBruijnGraph graph, int kmerSize) {
            
            AtomicBoolean changed = new AtomicBoolean(false);
            List<Node> tips = graph.nodes.values().stream()
                .filter(n -> n.inDegree() == 0 || n.outDegree() == 0)
                .collect(Collectors.toList());
            
            for (Node tip : tips) {
                
                if (Thread.currentThread().isInterrupted()) {

                    break;

                }
                
                if (graph.nodes.containsKey(tip.getFirstKmer(kmerSize)) && tip.sequence.length() < kmerSize * TIP_LENGTH_THRESHOLD_FACTOR) {
                    
                    graph.removeNode(tip, kmerSize);
                    changed.set(true);
                    
                }
                
            }
            
            if (changed.get()) System.out.println("    - 'Tips' eliminados.");
            
            return changed.get();
            
        }
        
    }

    static class ContigGenerator {
        
        public static List<String> generateContigs(DeBruijnGraph graph) {
            
            return graph.nodes.values().stream()
                    .map(node -> node.sequence)
                    .collect(Collectors.toList());
            
        }
        
    }

    static class DeBruijnGraph {
        
        final Map<String, Node> nodes = new ConcurrentHashMap<>();

        private Node getOrCreateNode(String kmer) {
            
            return nodes.computeIfAbsent(kmer, Node::new);
            
        }

        public synchronized void addEdge(String sourceKmer, String destKmer) {
            
            Node source = getOrCreateNode(sourceKmer);
            Node dest = getOrCreateNode(destKmer);
            source.outNodes.add(dest);
            dest.inNodes.add(source);
            
        }

        public synchronized void mergeNodes(Node nodeA, Node nodeB, int kmerSize) {
            
            nodeA.sequence += nodeB.sequence.substring(kmerSize - 1);
            nodeA.outNodes = new HashSet<>(nodeB.outNodes);
            
            for(Node downstreamNode : nodeA.outNodes) {
                
                if (Thread.currentThread().isInterrupted()) {

                    break;

                }
                
                downstreamNode.inNodes.remove(nodeB);
                downstreamNode.inNodes.add(nodeA);
                
            }
            
            nodes.remove(nodeB.getFirstKmer(kmerSize));
            
        }

        public synchronized void removeNode(Node node, int kmerSize) {
            
            if (nodes.remove(node.getFirstKmer(kmerSize)) == null) return; 
            for (Node prev : node.getInNodes()) prev.outNodes.remove(node);
            for (Node next : node.getOutNodes()) next.inNodes.remove(node);
            
        }
    }

    static class Node {
        
        String sequence;
        Set<Node> inNodes = new HashSet<>();
        Set<Node> outNodes = new HashSet<>();

        public Node(String kmer) { this.sequence = kmer; }

        public String getFirstKmer(int kmerSize) { 
            
            return sequence.substring(0, Math.min(sequence.length(), kmerSize)); 
            
        }
        
        public int inDegree() { return inNodes.size(); }
        public int outDegree() { return outNodes.size(); }
        public List<Node> getOutNodes() { return new ArrayList<>(outNodes); }
        public Set<Node> getInNodes() { return inNodes; }
        
        @Override
        public boolean equals(Object o) {
            
            if (this == o) return true;
            
            if (o == null || getClass() != o.getClass()) return false;
            
            Node node = (Node) o;
            
            return Objects.equals(sequence, node.sequence);
        }

        @Override
        public int hashCode() { return Objects.hash(sequence); }
    }
}