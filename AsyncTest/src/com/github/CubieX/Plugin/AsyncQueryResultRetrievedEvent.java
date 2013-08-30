package com.github.CubieX.Plugin;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AsyncQueryResultRetrievedEvent extends Event
{
   private static final HandlerList handlers = new HandlerList();
   private Integer myInt = null; // uses Integer object, because it can be null to represent an invalid value (for example if MAX_RETRIEVE_TIME was up)
   private CommandSender sender = null;

   //Constructor
   public AsyncQueryResultRetrievedEvent(CommandSender sender, Integer myInt)
   {
      this.sender = sender;
      this.myInt = myInt;
   }

   public CommandSender getSender()
   {
      return (this.sender);
   }
   
   public Integer getInt()
   {
      return (this.myInt);
   }

   public HandlerList getHandlers()
   {
      return handlers;
   }

   public static HandlerList getHandlerList()
   {
      return handlers;
   }
}
