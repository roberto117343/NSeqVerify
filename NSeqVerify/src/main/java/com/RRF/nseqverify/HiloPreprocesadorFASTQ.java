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

public class HiloPreprocesadorFASTQ implements Runnable{
    
    public static Thread t_PreprocesarFASTQ;
    
    public String entrada;
    public String salida;
    public int calidadMinima;
    public int corteExtremos;
    public int cantidadLecturas;
    public boolean reversa;
    
    public HiloPreprocesadorFASTQ(String entrada, String salida, int calidadMinima, int corteExtremos, int cantidadLecturas, boolean reversa){
        
        this.entrada = entrada;
        this.salida = salida;
        this.calidadMinima = calidadMinima;
        this.corteExtremos = corteExtremos;
        this.cantidadLecturas = cantidadLecturas;
        this.reversa = reversa;
        
        t_PreprocesarFASTQ = new Thread(this, "Hilo_Preprocesar_FASTQ"); 
        t_PreprocesarFASTQ.start();  
        
    }
    
    @Override
    public void run(){
        
        try {
            
            PreprocesadorFASTQ.cargarPreprocesado(this.entrada, this.salida, this.calidadMinima, this.corteExtremos, this.cantidadLecturas, this.reversa);
            jButton3.setEnabled(true);
            
        } catch (Exception ex) {
            
            Logger.getLogger(HiloPreprocesadorFASTQ.class.getName()).log(Level.SEVERE, null, ex);
            jButton3.setEnabled(true);
            
        }
        
    }
    
}
