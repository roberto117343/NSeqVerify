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

public class HiloClasificador implements Runnable {
    
    public static Thread t_Clasificador;
    
    public String entrada;
    public String salida;
    
    public HiloClasificador(String entrada, String salida) {
        
        this.entrada = entrada;
        this.salida = salida;
        
        t_Clasificador = new Thread(this, "Hilo_Ensamblador"); 
        t_Clasificador.start();  
        
    }
    
    @Override
    public void run(){
        
        try{
        
            Clasificador clasificacion = new Clasificador();
            clasificacion.clasificarSecuencias(this.entrada, this.salida); 
            
            jButton3.setEnabled(true);
        
        }catch(Exception e){
            
            Logger.getLogger(HiloClasificador.class.getName()).log(Level.SEVERE, null, e);
            jButton3.setEnabled(true);
            
        }
        
    }
    
}
