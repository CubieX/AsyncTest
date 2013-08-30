package com.github.CubieX.Plugin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ATEntityListener implements Listener
{
   private AsyncTest plugin = null;

   public ATEntityListener(AsyncTest plugin)
   {        
      this.plugin = plugin;

      plugin.getServer().getPluginManager().registerEvents(this, plugin);
   }

   //================================================================================================    
   @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true) // event has NORMAL priority and will be skipped if it has been cancelled before
   public void onAsyncQueryResultRetrievedEvent(final AsyncQueryResultRetrievedEvent e)
   {
      // Beware! This event is asynchronously called!
      // so make sure to use a sync task if you are accessing any Bukkit API methods
      Bukkit.getServer().getScheduler().runTask(plugin, new Runnable()
      {
         @Override
         public void run()
         {
            if(null != e.getSender())
            {
               if(e.getSender() instanceof Player)
               {
                  Player p = (Player)e.getSender();

                  if(p.isOnline()) // player may have quit while query was running
                  {
                     if(null != e.getInt())
                     {
                        p.sendMessage("Result: " + e.getInt());
                     }
                     else
                     {
                        p.sendMessage(AsyncTest.logPrefix + "Request timed out! (" + AsyncTest.MAX_RETRIEVAL_TIME + " ms)");
                     }
                  }
               }
               else
               {
                  if(null != e.getInt())
                  {
                     e.getSender().sendMessage("Result: " + e.getInt());
                  }
                  else
                  {
                     e.getSender().sendMessage(AsyncTest.logPrefix + "Request timed out! (" + AsyncTest.MAX_RETRIEVAL_TIME + " ms)");
                  }
               }
            }
         }
      });
   } 
}
