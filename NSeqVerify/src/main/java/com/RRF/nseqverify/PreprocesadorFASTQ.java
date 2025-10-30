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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Roberto Reinosa Fernández
 */

public class PreprocesadorFASTQ {
    
    public static void cargarPreprocesado(String entrada, String salidaCadena, int calidadMinima, int corteExtremos, int cantidadLecturas, boolean reversa) {
		
        try{
        
            FileReader f = new FileReader(entrada);
            BufferedReader b = new BufferedReader(f);

            FileWriter salida = new FileWriter(salidaCadena);

            String linea;

            String secuencia;
            String calidad;

            int contadorMaximo = 0;

            while((linea = b.readLine()) != null) {

                if (Thread.currentThread().isInterrupted()) {

                    break;

                }

                secuencia = b.readLine();
                b.readLine();
                calidad = b.readLine();

                if(secuencia.length() > 55) {

                    if(averageQuality(calidad) > calidadMinima) {

                        salida.write(secuencia.substring(corteExtremos, secuencia.length() - corteExtremos) + "\r\n");

                        if(reversa == true){

                            salida.write(reverseComplement(secuencia.substring(corteExtremos, secuencia.length() - corteExtremos)) + "\r\n");

                        }

                        contadorMaximo++;

                    }

                    if(contadorMaximo == cantidadLecturas) {

                        salida.close();
                        break;

                    }

                }


            }

            salida.close();
    
        }catch(IOException e){
            
            Logger.getLogger(PreprocesadorFASTQ.class.getName()).log(Level.SEVERE, null, e);
            jButton3.setEnabled(true);
            CancelarHilos.cancelarHilos();
                      
        }

    }
	
    public static int[] convertQualityString(String qualityString) {

        try{
        
            int[] qualities = new int[qualityString.length()];

            for (int i = 0; i < qualityString.length(); i++) {

                if (Thread.currentThread().isInterrupted()) {

                    break;

                }

                char c = qualityString.charAt(i);
                // Phred+33: restamos 33 al valor ASCII
                qualities[i] = (int) c - 33;

            }

            return qualities;
        
        }catch(Exception e){
        
            Logger.getLogger(PreprocesadorFASTQ.class.getName()).log(Level.SEVERE, null, e);
            jButton3.setEnabled(true);
            CancelarHilos.cancelarHilos();
        
        }
        
        return null;

    }

    // Calcula la media de calidad de toda la secuencia
    public static double averageQuality(String qualityString) {

        try{

            int[] qualities = convertQualityString(qualityString);
            int sum = 0;

            for (int q : qualities) {

                if (Thread.currentThread().isInterrupted()) {

                    break;

                }

                sum += q;

            }

            return (double) sum / qualities.length;
        
        }catch(Exception e){
            
            Logger.getLogger(PreprocesadorFASTQ.class.getName()).log(Level.SEVERE, null, e);
            jButton3.setEnabled(true);
            CancelarHilos.cancelarHilos();
            
        }
        
        return 0;

    }

    public static String reverseComplement(String dnaSequence) {

        try{
        
            if (dnaSequence == null) {

                return "";

            }

            // Usamos StringBuilder para una construcción eficiente de la nueva cadena.
            // Pre-asignamos la capacidad para evitar reasignaciones de memoria.
            StringBuilder complementBuilder = new StringBuilder(dnaSequence.length());

            // Iteramos sobre la secuencia original de DERECHA a IZQUIERDA.
            for (int i = dnaSequence.length() - 1; i >= 0; i--) {

                if (Thread.currentThread().isInterrupted()) {

                    break;

                }

                char base = dnaSequence.charAt(i);
                char complement;

                // Usamos un switch para encontrar el complemento.
                switch (base) {

                    case 'A':

                        complement = 'T';
                        break;

                    case 'a':

                        complement = 't';
                        break;

                    case 'T':

                        complement = 'A';
                        break;

                    case 't':

                        complement = 'a';
                        break;

                    case 'C':

                        complement = 'G';
                        break;

                    case 'c':

                        complement = 'g';
                        break;

                    case 'G':

                        complement = 'C';
                        break;

                    case 'g':

                        complement = 'c';
                        break;

                    default:

                        // Cualquier otro carácter (N, -, R, Y, etc.) se convierte en N.
                         // Se usa 'N' en mayúscula como estándar para bases ambiguas.
                        complement = 'N';

                        break;
                }

                complementBuilder.append(complement);

            }

            return complementBuilder.toString();
        
        }catch(Exception e){
            
            Logger.getLogger(PreprocesadorFASTQ.class.getName()).log(Level.SEVERE, null, e);
            jButton3.setEnabled(true);
            CancelarHilos.cancelarHilos();
            
        }
        
        return null;
        
    }
        
}
