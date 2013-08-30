package com.github.CubieX.Plugin;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ATCommandHandler implements CommandExecutor
{
   private AsyncTest plugin = null;
   private ATConfigHandler cHandler = null;

   public ATCommandHandler(AsyncTest plugin, ATConfigHandler cHandler) 
   {
      this.plugin = plugin;
      this.cHandler = cHandler;
   }

   @Override
   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
   {
      Player player = null;

      if (sender instanceof Player) 
      {
         player = (Player) sender;
      }

      if (cmd.getName().equalsIgnoreCase("at")) // for CLIENT and SERVER
      {
         if (args.length == 0)
         { //no arguments, so help will be displayed
            return false;
         }
         else if(args.length==1)
         {
            if (args[0].equalsIgnoreCase("version"))
            {               
               sender.sendMessage(ChatColor.GREEN + "This server is running " + plugin.getDescription().getName() + " version " + plugin.getDescription().getVersion());

               return true;
            }

            if (args[0].equalsIgnoreCase("reload"))
            {
               if(sender.isOp() || sender.hasPermission("asynctest.admin"))
               {                        
                  cHandler.reloadConfig(sender);
                  return true;
               }
               else
               {
                  sender.sendMessage(ChatColor.RED + "You do not have sufficient permission to reload " + plugin.getDescription().getName() + "!");
               }
            }            

            if (args[0].equalsIgnoreCase("query"))
            {
               if(sender.isOp() || sender.hasPermission("asynctest.admin"))
               {
                  doAsyncSelectQuery(sender);
               }
               else
               {
                  sender.sendMessage(ChatColor.RED + "You do not have sufficient permission to start the client!");
               }

               return true;
            }            
         }         
         else
         {
            sender.sendMessage(ChatColor.YELLOW + "Falsche Parameteranzahl.");
         }                

      }         
      return false; // if false is returned, the help for the command stated in the plugin.yml will be displayed to the player
   }

   // ###################################################

   private void doAsyncSelectQuery(final CommandSender sender)
   {
      Bukkit.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable()
      {
         @Override
         public void run()
         {
            /*ExecutorService executor = Executors.newCachedThreadPool(); // Thread pool that will re-use threads if they are available and create new ones if not.            
            // using "Executors.newFixedThreadPool(3)" would create a thread pool with a limited number of 3 worker threads.
            // This is not needed here because every command will create a new async task with its own thread. But as example...

            Future<Integer> future = executor.submit(new Callable<Integer>()
                  {
               @Override
               public Integer call() throws Exception
               {
                  System.out.println("asyncQuery running and waiting for result...");

                  try
                  {
                     Thread.sleep(5000); // simulates the time needed to aquire a ResultSet from DB
                  }
                  catch (InterruptedException e)
                  {        
                     e.printStackTrace();
                  }

                  System.out.println("asyncQuery has retrieved the result. Returning the result now...");
                  return 1234;
               }});*/


            ExecutorService executor = Executors.newSingleThreadExecutor();

            FutureTask<Integer> future = new FutureTask<Integer>(new Callable<Integer>()
                  {
               @Override
               public Integer call()
               {
                  AsyncTest.log.info(AsyncTest.logPrefix + "doAsyncSelectQuery running its callable and waiting for result...");

                  try
                  {
                     Thread.sleep(500); // simulates the time needed to aquire a ResultSet from DB
                     // if this is taking longer than future.get(MAX_RETRIEVAL_TIME) specifies, the return value will come too late and
                     // no one will get it
                  }
                  catch (InterruptedException e)
                  {        
                     e.printStackTrace();
                  }

                  AsyncTest.log.info(AsyncTest.logPrefix + "doAsyncSelectQuerys callable has retrieved the result. Returning the result now...");
                  return 1234;
               }});

            executor.execute(future); // start the callable task to retrieve the result

            // do stuff while return value is pending... (optional! Doing "future.get()" will also block this tasks main thread until future is ready)
            // BEWARE! This will run forever, and keep the task alive until the result is retrieved.
            // do not use a WHILE loop, if you want to end the task after a given period without a result beeing ready. (see future.get() below!)
            // stuff done in the meantime should be expected to be done BEFORE the future result will be ready.
            // Otherwise this will slow down the retrieval time unnecessarily.

            /*while(!future.isDone()) // do some stuff util the result is ready (optional! Not recommended!)
            {
               try
               {
                  AsyncTest.log.info(AsyncTest.logPrefix + "Async tasks main thread working while waiting for DB ResultSet...");
                  Thread.sleep(100);
               }
               catch (InterruptedException e)
               {
                  e.printStackTrace();
               }
            }*/

            /*for(int i = 0; i < 100; i++) // do some stuff for max. 10 seconds while waiting for the result (optional!)
            {
               if(!future.isDone()) // check if result is still pending and if so, do some stuff in the meanwhile
               {
                  try
                  {
                     AsyncTest.log.info(AsyncTest.logPrefix + "Async tasks main thread doing stuff while waiting for DB ResultSet...");
                     Thread.sleep(100);
                  }
                  catch (InterruptedException e)
                  {
                     e.printStackTrace();
                  }
               }
               else
               {
                  break; // result is ready. So stop doing stuff
               }
            }*/
            
            AsyncTest.log.info(AsyncTest.logPrefix + "Async tasks main thread working while waiting for DB ResultSet...work...work...");

            try
            {
               Integer result = null;

               try
               {
                  result = future.get(AsyncTest.MAX_RETRIEVAL_TIME, TimeUnit.MILLISECONDS); // will wait until result is ready, but will return if MAX_RETRIEVAL_TIME has expired                  
               }
               catch (TimeoutException e)
               {
                  result = null;
                  //e.printStackTrace();
               }
               finally
               {
                  executor.shutdown(); // shutdown executor service after Callable has finished and returned its value (will block current task until all threads in the pool have finished!)
                  // Using this order will make sure, the task will return after given MAX_RETRIEVAL_TIME and not block forever, if the DB connection is dead or slow
               }

               // future.get() will block this thread until the result is ready
               AsyncTest.log.info(AsyncTest.logPrefix + "ResultSet aquired. Now firing QueryResultRetrievedEvent...");

               // fire custom query event ================================================                                
               AsyncQueryResultRetrievedEvent qrreEvent = new AsyncQueryResultRetrievedEvent(sender, result); // Create the event
               plugin.getServer().getPluginManager().callEvent(qrreEvent); // fire Event         
               //==========================================================================                 
            }
            catch (InterruptedException e)
            {
               e.printStackTrace();
            }
            catch (ExecutionException e)
            {        
               AsyncTest.log.severe(AsyncTest.logPrefix + e.getMessage());
            }

            AsyncTest.log.info(AsyncTest.logPrefix + "doAsyncSelectQuery task finished.");
         }
      });
   }
}
