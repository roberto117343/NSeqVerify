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

/**
 *
 * @author Roberto Reinosa Fernández
 */

public class CancelarHilos {
    
    public static void cancelarHilos(){
        
        
        try{
           
            if(HiloPreprocesadorFASTQ.t_PreprocesarFASTQ.isAlive()){
            
                HiloPreprocesadorFASTQ.t_PreprocesarFASTQ.interrupt();
            
            }
            
        }catch(Exception e){}
        
        try{
           
            if(HiloEnsamblador.t_Ensamblador.isAlive()){
            
                HiloEnsamblador.t_Ensamblador.interrupt();
            
            }
            
        }catch(Exception e){}
        
        try{
           
            if(HiloClasificador.t_Clasificador.isAlive()){
            
                HiloClasificador.t_Clasificador.interrupt();
            
            }
            
        }catch(Exception e){}
         
        try{
           
            if(HiloClasificadorProteinas.t_ClasificadorProteinas.isAlive()){
            
                HiloClasificadorProteinas.t_ClasificadorProteinas.interrupt();
            
            }
            
        }catch(Exception e){}
        
    }
    
}
