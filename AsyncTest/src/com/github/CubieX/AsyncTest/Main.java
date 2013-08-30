package com.github.CubieX.AsyncTest;


/* Beispiel von Multithreading. 
 * 
 * Threads sind Ausführungspfade ("Ausführungs-Fäden") eines Tasks.
 * Ein Task ist wie ein "Versprechen" dass eine Aufgabe erledigt wird.
 * Ein Thread ist ein "Arbeiter" der daran arbeitet, dieses Versprechen einzulösen.
 * 
 * Im Einfachsten Fall hat ein Task nur 1 Thread der ihn abarbeitet.
 * Es können aber auch mehrere Threads parallel an einem Task arbeiten.
 * 
 * Threads können über einen ThreadPool wiederverwendet werden, um nach Abarbeitung eines Tasks weitere Tasks zu barbeiten.
 * Siehe "ExecutorService".
 * Denn Threaderstellung ist kostspielig. (-> Wie einen Arbeiter nach erledigter Arbeit rausschmeißen und später neu einstellen...)
 * 
 * Wenn mehrere Threads auf die selbe Methode oder direkt/indirekt auf das selbe Objekt lesend oder schreibend zugreifen,
 * müssen die Methoden die dabei verwendet werden "synchronisiert" werden, um die Abarbeitung einer Operation oder einer ganzen Methode
 * gegen Thread-Interleaving (Umschaltung des aktiven Threads durch das Betriebssystem) zu verhindern,
 * da dies zu inkonsistenten Zuständen der betroffenen Objekte führen kann.
 * Siehe "synchronized" Schlüsselwort für Methoden-deklaration und Code-Blöcke in Methoden.
 * 
 * Bei Bukkit erzeugt ein asynchroner Task automatisch einen neuen Thread, der diesen Task abarbeitet.
 * Innerhalb dieses Tasks können aber auch zusätzliche Threads erzeugt werden um parallel unterschiedliche Teilaufgaben zu erledigen.
 * Siehe
 * Thread worker = new Thread(new Runnable()
 * {
 *    @Override
 *    run()
 *    {
 *      // code zur Ausführung
 *    }
 * });
 * 
 * oder per Klasse, falls mehr Ausführungscode nötig ist, um es übersichtlich zu halten:
 * class WorkerThread extends Thread
   {  
      @Override
      public void run()
      {  
         System.out.println("worker working: " + i + "/10");      
      }
   } 
 * 
 * Im Fall synchroner Tasks die z.B. mit einem Scheduler erzeugt werden,
 * werden diese Tasks vom MainThread abgearbeitet.
 * Dieser kümmert sich also dann um die Programmausführung im ServerTask und auch
 * um die Ausführung des Timer-Tasks.
 * Deswegen blockiert ein synchroner Task den MainThread, während er von ihm abgearbeitet wird.
 * Der "Arbeiter" kümmert sich also dann dann um 2 oder mehr Tasks.
 * Den ServerTask und die zusätzlichen (Timer-)Tasks.
 * Er kann aber immer nur an EINEM davon zu einer Zeit arbeiten und
 * wird den aktuellen Task erst beenden, bevor er zum nächsten übergeht.
 * Heißt: Ein synchroner Timer-Task wird bei Aufruf zuerst komplett abgearbeitet,
 * bevor der ServerTask wieder bedient wird.
 * 
 * Bei asynchronen Tasks (z.B. durch einen Scheduler gestartete async Tasks) gibt es
 * immer auch einen neuen Thread. Also einen eigenen "Arbeiter" der sich um den
 * async Task kümmert und diesen abarbeitet, ohne den ServerTask zu blocken.
 * Es können in einem async Task auch weitere Threads angelegt werden die sich um Teilaufgaben kümmern.
 * 
 * Im ServerTask eigene zusätzliche Threads zu erzeugen, sollte unbedingt unterlassen werden!
 * Denn die Bukkit API-Methoden dürfen nur vom MainThread genutzt werden!
 * (Er alleine darf die Bukkit API benutzen. Es ist sein persönlicher "Werkzeugkasten".)
 * Diese Bukkit API-Methoden sind nicht "Thread-Safe", sind also nicht gegen multiplen Zugriff von mehreren Threads aus gesichert.
 * 
 * 
 * */

class WorkerThread extends Thread
{
   int id = 0;
   
   public WorkerThread(int id)
   {
      this.id = id;
   }
   
   @Override
   public void run()
   {  
      for(int i = 0; i < 10; i++)
      {
         System.out.println("t" + id + " working: " + i + "/10");

         try
         {
            Thread.sleep(500);
         }
         catch (InterruptedException e)
         {              
            e.printStackTrace();
         }

      }
   }     
}

public class Main
{
   /**
    * @param args
    */
   
   static int activeWorkerCounter = 0;
   
   public static void main(String[] args)
   {
      Thread t1 = new WorkerThread(activeWorkerCounter);
      activeWorkerCounter++;
      Thread t2 = new WorkerThread(activeWorkerCounter);

      t1.start();
      t2.start();
      
      while(t1.isAlive() || t2.isAlive())
      {
         try
         {
            System.out.println("MainThread loop...");
            Thread.sleep(100);            
         }
         catch (InterruptedException e)
         {            
            e.printStackTrace();
         }
      }

      System.out.println("t1 and t2 died. MainThread has nothing more to do.");
   }
}
