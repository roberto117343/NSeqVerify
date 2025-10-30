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
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 *
 * @author Roberto Reinosa Fernández
 */

public class Clasificador {

    // Bloque estático para desactivar la validación SSL
    
    static {
        
        try {
            
            TrustManager[] trustAllCerts = new TrustManager[]{ new X509TrustManager() {
                
                @Override
                public X509Certificate[] getAcceptedIssuers() { return null; }
                
                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                
                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                
            }};
            
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HostnameVerifier allHostsValid = (hostname, session) -> true;
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
            System.out.println("ADVERTENCIA: Validación de certificados SSL desactivada para esta sesión.");
            
            
        } catch (KeyManagementException | NoSuchAlgorithmException e) {}
        
    }

    private static final String BLAST_URL = "https://blast.ncbi.nlm.nih.gov/Blast.cgi";

    private static class BlastResult {
        
        String description = "NO_CLASIFICADA";
        String scientificName = "N/A";
        String maxScore = "N/A";
        String totalScore = "N/A";
        String queryCover = "N/A";
        String eValue = "N/A";
        String percentIdentity = "N/A";
        String accessionLength = "N/A";
        String accession = "N/A";

        boolean isClassified() {
            
            return !"NO_CLASIFICADA".equals(description);
            
        }
        
    }

    public void clasificarSecuencias(String archivoFastaEntrada, String archivoResultados) {
        
        try{ 
        
            String archivoSinClasificar = archivoResultados.replaceAll("\\..*$", "_no_identificados.fasta");

            System.out.println("Iniciando proceso de clasificación de nucleótidos...");
            Map<String, String> secuencias = leerFasta(archivoFastaEntrada);
            System.out.println("Se han leído " + secuencias.size() + " secuencias del archivo de entrada.");

            try (BufferedWriter writerResultados = new BufferedWriter(new FileWriter(archivoResultados));

                 BufferedWriter writerSinClasificar = new BufferedWriter(new FileWriter(archivoSinClasificar))) {

                // Cabecera completa con todas las métricas
                writerResultados.write("ID_SECUENCIA\tDESCRIPCION\tSCIENTIFIC_NAME\tACCESSION\tQUERY_COVER\tE_VALUE\tPERCENT_IDENTITY\tMAX_SCORE\tTOTAL_SCORE\tACC_LEN\n");

                for (Map.Entry<String, String> entry : secuencias.entrySet()) {

                    if (Thread.currentThread().isInterrupted()) {

                        break;

                    }

                    String header = entry.getKey();
                    String secuencia = entry.getValue();
                    String idSecuencia = header.startsWith(">") ? header.substring(1).split(" ")[0] : header.split(" ")[0];
                    System.out.println("\nProcesando secuencia: " + idSecuencia);

                    String rid = enviarBlastRequest(secuencia);

                    if (rid == null) {

                        manejarError(idSecuencia, "ERROR_NO_SE_PUDO_EXTRAER_EL_RID", writerResultados, writerSinClasificar, header, secuencia);
                        continue;

                    }

                    System.out.println("  -> Solicitud enviada. RID: " + rid);
                    System.out.print("  -> Esperando finalización del cálculo...");

                    if (!esperarResultados(rid)) {

                        manejarError(idSecuencia, "ERROR_TIMEOUT_O_FALLO_BLAST", writerResultados, writerSinClasificar, header, secuencia);
                        continue;

                    }

                    System.out.println(" ¡Cálculo finalizado!");

                    String respuestaHtml = obtenerResultadosHtml(rid);
                    BlastResult mejorHit = parsearMejorHitDeHtml(respuestaHtml);

                    if (!mejorHit.isClassified()) {

                        System.out.println("  -> Resultado: No se encontraron hits significativos.");
                        String line = String.join("\t", idSecuencia, mejorHit.description, mejorHit.scientificName, mejorHit.accession, mejorHit.queryCover, mejorHit.eValue, mejorHit.percentIdentity, mejorHit.maxScore, mejorHit.totalScore, mejorHit.accessionLength);
                        writerResultados.write(line + "\n");
                        guardarSecuenciaSinClasificar(writerSinClasificar, header, secuencia);

                    } else {

                        // Log de consola más informativo
                        System.out.println("  -> Clasificado como: " + mejorHit.description + 
                                           " (Acc: " + mejorHit.accession + 
                                           " | Identidad: " + mejorHit.percentIdentity + 
                                           " | E-value: " + mejorHit.eValue +
                                           " | Query Cover: " + mejorHit.queryCover + ")");

                        // Línea completa para el archivo de resultados
                        String line = String.join("\t", idSecuencia, mejorHit.description, mejorHit.scientificName, mejorHit.accession, mejorHit.queryCover, mejorHit.eValue, mejorHit.percentIdentity, mejorHit.maxScore, mejorHit.totalScore, mejorHit.accessionLength);
                        writerResultados.write(line + "\n");
                    }

                    Thread.sleep(1000); 
                }
            }

            System.out.println("\nProceso de clasificación finalizado. Resultados en: " + archivoResultados);

        }catch(Exception e){
        
            Logger.getLogger(Clasificador.class.getName()).log(Level.SEVERE, null, e);
            jButton3.setEnabled(true);
            CancelarHilos.cancelarHilos();
            
        }
            
    }
    
    /**
     * <<< MÉTODO DE PARSEO >>>
     * Aísla la primera fila de resultados y extrae cada métrica de su celda
     * específica, identificada por su clase CSS única.
     */
    
    private BlastResult parsearMejorHitDeHtml(String html) {
        
        BlastResult result = new BlastResult();
        
        try{
        
            if (html == null || html.isEmpty()) return result;

            String marcadorFila = "<tr id=\"dtr_";

            int inicioFila = html.indexOf(marcadorFila);
            if (inicioFila == -1) return result;

            int finFila = html.indexOf("</tr>", inicioFila);
            if (finFila == -1) return result;

            String filaHtml = html.substring(inicioFila, finFila);

            result.description = extraerTextoDeCeldaConClase(filaHtml, "ellipsis c2");

            if (!result.isClassified()) return result;

            result.scientificName = extraerTextoDeCeldaConClase(filaHtml, "ellipsis c3");
            result.maxScore = extraerTextoDeCeldaConClase(filaHtml, "c6");
            result.totalScore = extraerTextoDeCeldaConClase(filaHtml, "c7");
            result.queryCover = extraerTextoDeCeldaConClase(filaHtml, "c8");
            result.eValue = extraerTextoDeCeldaConClase(filaHtml, "c9");
            result.percentIdentity = extraerTextoDeCeldaConClase(filaHtml, "c10");
            result.accessionLength = extraerTextoDeCeldaConClase(filaHtml, "c11 acclen");
            result.accession = extraerTextoDeCeldaConClase(filaHtml, "c12 l lim");

            return result;
            
        }catch(Exception e){
            
            Logger.getLogger(Clasificador.class.getName()).log(Level.SEVERE, null, e);
            jButton3.setEnabled(true);
            CancelarHilos.cancelarHilos();
            
        }
        
        return result;
    }

    /**
     * Helper que busca una celda <td> que contenga una clase CSS específica
     * y devuelve su contenido de texto limpio.
     */
    
    private String extraerTextoDeCeldaConClase(String html, String cssClass) {
        
        try{
        
            String marcadorTd = "class=\"" + cssClass + "\"";
        
            int inicioTd = html.indexOf(marcadorTd);
            if (inicioTd == -1) return "N/A";

            int finTd = html.indexOf("</td>", inicioTd);
            if (finTd == -1) return "N/A";

            int inicioContenido = html.indexOf('>', inicioTd) + 1;

            String contenidoHtml = html.substring(inicioContenido, finTd);

            return contenidoHtml.replaceAll("<[^>]*>", "").trim();
        
        }catch(Exception e){
            
            Logger.getLogger(Clasificador.class.getName()).log(Level.SEVERE, null, e);
            jButton3.setEnabled(true);
            CancelarHilos.cancelarHilos();
            
        }
        
        return "";
        
    }
    
    private String obtenerResultadosHtml(String rid) {
        
        try{
        
            String params = "CMD=Get&RID=" + rid;
            return realizarHttpRequest("GET", params);
       
        }catch(Exception e){
            
            Logger.getLogger(Clasificador.class.getName()).log(Level.SEVERE, null, e);
            jButton3.setEnabled(true);
            CancelarHilos.cancelarHilos();
            
        }
        
        return "";
        
    }
    
    private String enviarBlastRequest(String secuencia) {
        
        try{
        
        String params = "CMD=Put&PROGRAM=blastn&DATABASE=nt&QUERY=" + URLEncoder.encode(secuencia, StandardCharsets.UTF_8.name());
        String respuestaCompleta = realizarHttpRequest("POST", params);
        String marcador = "name=\"RID\" value=\"";
      
        int inicio = respuestaCompleta.indexOf(marcador);
        
        if (inicio != -1) {
            
            inicio += marcador.length();
            int fin = respuestaCompleta.indexOf("\"", inicio);
            
            if (fin != -1) return respuestaCompleta.substring(inicio, fin).trim();
        }
        
        System.err.println("  -> ERROR: No se encontró el RID en la respuesta del servidor.");
        
        return null;
        
        }catch(IOException e){
                
            Logger.getLogger(Clasificador.class.getName()).log(Level.SEVERE, null, e);
            jButton3.setEnabled(true);
            CancelarHilos.cancelarHilos();
                
        }
        
        return "";
    }
    
    private boolean esperarResultados(String rid) {
        
        try{
        
            int maxIntentos = 60;

            for (int i = 0; i < maxIntentos; i++) {

                if (Thread.currentThread().isInterrupted()) {

                        break;

                    }

                String params = "CMD=Get&FORMAT_OBJECT=SearchInfo&RID=" + rid;
                String respuesta = realizarHttpRequest("GET", params);

                if (respuesta.contains("Status=READY")) { return true; }

                if (respuesta.contains("Status=FAILED")) { 

                    System.out.println("\n  -> El trabajo de BLAST ha fallado en el servidor de NCBI.");
                    return false; 

                }

                Thread.sleep(5000);

                System.out.print(".");
            }

            System.out.println("\n  -> Tiempo de espera agotado para el RID: " + rid);

            return false;
        
        }catch(Exception e){
            
            Logger.getLogger(Clasificador.class.getName()).log(Level.SEVERE, null, e);
            jButton3.setEnabled(true);
            CancelarHilos.cancelarHilos();
                        
        }
        
        return false;
        
    }

    private String realizarHttpRequest(String metodo, String params) {
        
        try{
        
            String urlString = BLAST_URL;

            if ("GET".equalsIgnoreCase(metodo)) { urlString += "?" + params; }

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(metodo);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; JavaClasificador/1.0)");

            if ("POST".equalsIgnoreCase(metodo)) {

                conn.setDoOutput(true);

                try (OutputStream os = conn.getOutputStream()) { os.write(params.getBytes(StandardCharsets.UTF_8)); }

            }

            StringBuilder response = new StringBuilder();

            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getResponseCode() / 100 == 2 ? conn.getInputStream() : conn.getErrorStream()))) {

                String linea;

                while ((linea = in.readLine()) != null) {

                    if (Thread.currentThread().isInterrupted()) {

                        break;

                    }

                    response.append(linea).append("\n"); 

                }

            } finally {

                conn.disconnect();

            }

            return response.toString();
        
        }catch(Exception e){
            
            Logger.getLogger(Clasificador.class.getName()).log(Level.SEVERE, null, e);
            jButton3.setEnabled(true);
            CancelarHilos.cancelarHilos();
            
        }
        
        return "";
        
    }
    
    private Map<String, String> leerFasta(String filePath) throws IOException {
        
        Map<String, String> s = new LinkedHashMap<>();
        
        try (BufferedReader r = new BufferedReader(new FileReader(filePath))) {
            
            String l, h = null;
            StringBuilder sb = new StringBuilder();
            
            while ((l = r.readLine()) != null) {
                
                if (Thread.currentThread().isInterrupted()) {

                    break;

                }
                
                l = l.trim();
                
                if (l.startsWith(">")) {
                    
                    if (h != null) s.put(h, sb.toString());
                    
                    h = l;
                    sb.setLength(0);
                
                } else if (!l.isEmpty()) { 
                    
                    sb.append(l);
                
                } 
            
            } if (h != null) s.put(h, sb.toString()); 
        
        } 
        
        return s; 
    
    }
    
    
    private void guardarSecuenciaSinClasificar(BufferedWriter w, String h, String s) {
        
        try{
        
        w.write(h + "\n");
        
        for (int i = 0; i < s.length();i += 60) {
            
            if (Thread.currentThread().isInterrupted()) {

                break;

            }
            
            w.write(s.substring(i, Math.min(i + 60, s.length())) + "\n");
        
        } 
        
        }catch(Exception e){
        
            Logger.getLogger(Clasificador.class.getName()).log(Level.SEVERE, null, e);
            jButton3.setEnabled(true);
            CancelarHilos.cancelarHilos();
                
        }
    
    }
   
    private void manejarError(String id, String msg, BufferedWriter wr, BufferedWriter wsc, String h, String s) {
        
        try{
        
            System.out.println("\n  -> Error: " + msg);

            String line = String.join("\t", id, msg, "N/A", "N/A", "N/A", "N/A", "N/A", "N/A", "N/A", "N/A");

            wr.write(line + "\n");

            guardarSecuenciaSinClasificar(wsc, h, s);
        
        }catch(IOException e){
            
            Logger.getLogger(Clasificador.class.getName()).log(Level.SEVERE, null, e);
            jButton3.setEnabled(true);
            CancelarHilos.cancelarHilos();
            
        }
    
    }
    
}