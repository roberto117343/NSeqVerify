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

public class HiloClasificadorProteinas implements Runnable{
    
    public static Thread t_ClasificadorProteinas;
    
    public String entrada;
    public String salida;
    
    public HiloClasificadorProteinas(String entrada, String salida){
        
        this.entrada = entrada;
        this.salida = salida;
        
        t_ClasificadorProteinas = new Thread(this, "Hilo_Ensamblador"); 
        t_ClasificadorProteinas.start();
        
    }
    
    @Override
    public void run(){
       
        try {
           
            ClasificadorProteinas clasificador = new ClasificadorProteinas(); 
            clasificador.clasificarSecuencias(this.entrada, this.salida);
            
            jButton3.setEnabled(true);
            
        } catch (Exception e) {
            
            Logger.getLogger(HiloClasificadorProteinas.class.getName()).log(Level.SEVERE, null, e);
            jButton3.setEnabled(true);
            
        }
        
        
    }
    
}
