package com.github.CubieX.AsyncCallback;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/* Test: Async Callback 
 * 
 * Callback heißt, dass ein (asynchroner) Task eine Aufgabe erledigt
 * und sobald das Ergebnis vorliegt, den ServerTask informiert.
 * Vorzugsweise über den Aufruf eines Custom-EventHandlers.
 * Nutzbar z.B. für asynchrone SELECT queries auf MySQL DB, auf die hin
 * man eine Chat-Ausgabe machen will.
 * 
 * Ein Callable Objekt wird benutzt als spezielle Art von thread.
 * Der ExecutorService gibt ein Future-Objekt zurück.
 * Dies kann bei einer DB SELECT query länger dauern.
 * 
 * Im MainTask (bzw. in Bukkit wäre das ein Asynchroner Task) kann mit
 * "future.isDone()" geprüft werden, ob das Ergebnis schon vorliegt.
 * Mit "future.get()" kann der Wert dann abgeholt werden.
 * Da "Future<T> eine parametrierbare Klasse ist, kann damit jegliche Art von Objekten
 * zurückgegeben werden.
 * 
 * "future.get()" wird den MainThread blocken, bis das Ergebnis vorliegt.
 * Deswegen kann "if(futire.isDone())" wie unten gezeigt verwendet werden,
 * um während das Ergebnis zusammengestellt wird die MainTask vom MainThread weiter
 * an etwas arbeiten zu lassen. (optional)
 * Ist das nicht nötig, kann auch mit "future.get()" direkt gewartet werden auf
 * das Ergebnis.
 * 
 * Wenn das Ergebnis vorliegt könnte jetzt ein Custom-Event im ServerTask gefeuert werden,
 * dass das Ergebnis übergeben bekommt und damit die nötigen Aktionen (z.B. Chat-Ausgabe)
 * durchführt.
 * Das ist die bevorzugte Methode das uture-Objekt an die richtige Stelle zu bekommen.
 * 
 * Alternativ könnte auch eine Art Notify-Flag gesetzt werden dass vom ServerTask in einer Checker-Schleife abgefragt wird.
 * Und nach Abholen der Information zurückgesetzt wird.
 * 
 * */

public class Main
{
   public static void main(String[] args)
   {
      ExecutorService executor = Executors.newCachedThreadPool();

      Future<Integer> future = executor.submit(new Callable<Integer>()
            {
         @Override
         public Integer call() throws Exception
         {
            System.out.println("asyncQuery running and waiting for result...");

            try
            {
               Thread.sleep(5000);
            }
            catch (InterruptedException e)
            {        
               e.printStackTrace();
            }

            System.out.println("asyncQuery has retrieved the result. Returning the result now...");
            return 1234;
         }
            });

      while(!future.isDone())
      {
         try
         {
            System.out.println("MainThread working while waiting for future object...");
            Thread.sleep(100);            
         }
         catch (InterruptedException e)
         {            
            e.printStackTrace();
         }
      }

      executor.shutdown(); // shutdown executor after callable has finished and returned its value 

      try
      {
         System.out.println("future returned: " + future.get()); // future.get() will block this thread until the result is ready
      }
      catch (InterruptedException e)
      {         
         e.printStackTrace();
      }
      catch (ExecutionException e)
      {        
         System.out.println(e);
      }
      System.out.println("asyncDBquery returned its value. MainThread has nothing more to do.");
   }
}
