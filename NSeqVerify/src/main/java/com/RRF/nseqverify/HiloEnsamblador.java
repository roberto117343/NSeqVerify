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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Roberto Reinosa Fernández
 */

public class HiloEnsamblador implements Runnable{
    
    public static Thread t_Ensamblador;
    
    public String inputFile;
    public String outputFile;
    public String K_MER_SIZES_i;
    public  int MIN_KMER_FREQUENCY_i;
    public int MIN_CONTIG_LENGTH_i;
    public int TIP_LENGTH_THRESHOLD_FACTOR_i;
    public int MAX_BUBBLE_PATH_LENGTH_i;
    public double BUBBLE_COVERAGE_RATIO_i;
    
    public HiloEnsamblador(String inputFile, String outputFile, String K_MER_SIZES_i, int MIN_KMER_FREQUENCY_i, 
            int MIN_CONTIG_LENGTH_i, int TIP_LENGTH_THRESHOLD_FACTOR_i, int MAX_BUBBLE_PATH_LENGTH_i, double BUBBLE_COVERAGE_RATIO_i){
        
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        this.K_MER_SIZES_i = K_MER_SIZES_i;
        this.MIN_KMER_FREQUENCY_i = MIN_KMER_FREQUENCY_i;
        this.MIN_CONTIG_LENGTH_i = MIN_CONTIG_LENGTH_i;
        this.TIP_LENGTH_THRESHOLD_FACTOR_i = TIP_LENGTH_THRESHOLD_FACTOR_i;
        this.MAX_BUBBLE_PATH_LENGTH_i = MAX_BUBBLE_PATH_LENGTH_i;
        this.BUBBLE_COVERAGE_RATIO_i = BUBBLE_COVERAGE_RATIO_i;
      
        t_Ensamblador = new Thread(this, "Hilo_Ensamblador"); 
        t_Ensamblador.start(); 
        
    }
    
    @Override
    public void run(){
        
         try {
             
            Ensamblador.cargarEnsamblador(this.inputFile, this.outputFile, this.K_MER_SIZES_i, this.MIN_KMER_FREQUENCY_i,
                this.MIN_CONTIG_LENGTH_i, this.TIP_LENGTH_THRESHOLD_FACTOR_i, this.MAX_BUBBLE_PATH_LENGTH_i, this.BUBBLE_COVERAGE_RATIO_i);
        
            jButton3.setEnabled(true);
            
        } catch (Exception ex) {
            
            Logger.getLogger(HiloEnsamblador.class.getName()).log(Level.SEVERE, null, ex);
            jButton3.setEnabled(true);
            
        }
            
    }
    
}
