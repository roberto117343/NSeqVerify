package com.RRF.nseqverify;

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

/**
 *
 * @author Roberto Reinosa Fernández
 */

public class SafeThreadKiller {

    public static void interruptAllExceptCurrentAndSystem() {
        
        Thread current = Thread.currentThread();

        for (Thread t : Thread.getAllStackTraces().keySet()) {
            
            if (t == null || t == current) continue;
            if (!t.isAlive()) continue;

            String name = t.getName();
            
            if (name.startsWith("Reference Handler") ||
                name.startsWith("Finalizer") ||
                name.equals("Signal Dispatcher") ||
                name.startsWith("Attach Listener")) {
                
                continue;
                
            }

            try {
                
                t.interrupt();
                
            } catch (Exception ex) {
                
                System.err.println("Error al interrumpir " + name + ": " + ex);
                
            }
        }
    }
}

