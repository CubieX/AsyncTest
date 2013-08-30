package com.github.CubieX.Plugin;

import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class AsyncTest extends JavaPlugin
{
   public static final Logger log = Bukkit.getServer().getLogger();
   static final String logPrefix = "[AsyncTest] "; // Prefix to go in front of all log entries
   static final int MAX_RETRIEVAL_TIME = 1000;  // max time in ms to wait for a SELECT query to deliver its result
                                                // This prevents async task jam in case DB is unreachable or connection is very slow

   private ATCommandHandler comHandler = null;
   private ATConfigHandler cHandler = null;
   private ATEntityListener eListener = null;
   //private STSchedulerHandler schedHandler = null;

   // config values
   static boolean debug = false;

   //*************************************************
   static String usedConfigVersion = "1"; // Update this every time the config file version changes, so the plugin knows, if there is a suiting config present
   //*************************************************

   @Override
   public void onEnable()
   {
      cHandler = new ATConfigHandler(this);

      if(!checkConfigFileVersion())
      {
         log.severe(logPrefix + "Outdated or corrupted config file(s). Please delete your config files."); 
         log.severe(logPrefix + "will generate a new config for you.");
         log.severe(logPrefix + "will be disabled now. Config file is outdated or corrupted.");
         getServer().getPluginManager().disablePlugin(this);
         return;
      }

      readConfigValues();

      eListener = new ATEntityListener(this);     
      comHandler = new ATCommandHandler(this, cHandler);      
      getCommand("at").setExecutor(comHandler);

      //schedHandler = new LWSchedulerHandler(this);

      log.info(this.getDescription().getName() + " version " + getDescription().getVersion() + " is enabled!");

      //schedHandler.startPlayerInWaterCheckScheduler_SynchRepeating();
   }   

   private boolean checkConfigFileVersion()
   {      
      boolean configOK = false;     

      if(cHandler.getConfig().isSet("config_version"))
      {
         String configVersion = getConfig().getString("config_version");

         if(configVersion.equals(usedConfigVersion))
         {
            configOK = true;
         }
      }

      return (configOK);
   }  

   public void readConfigValues()
   {
      boolean exceed = false;
      boolean invalid = false;

      if(getConfig().contains("debug")){debug = getConfig().getBoolean("debug");}else{invalid = true;}

      if(exceed)
      {
         log.warning(logPrefix + "One or more config values are exceeding their allowed range. Please check your config file!");
      }

      if(invalid)
      {
         log.warning(logPrefix + "One or more config values are invalid. Please check your config file!");
      }
   }

   @Override
   public void onDisable()
   {      
      this.getServer().getScheduler().cancelTasks(this);
      cHandler = null;
      eListener = null;
      comHandler = null;
      //schedHandler = null; // TODO ACTIVATE THIS AGAIN IF USED!
      log.info(this.getDescription().getName() + " version " + getDescription().getVersion() + " is disabled!");
   }

   // #########################################################
   
   
}


