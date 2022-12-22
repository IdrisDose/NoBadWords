package net.idrisdev.mc.nobadwords;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

@Mod(
        modid = NoBadWords.MOD_ID,
        name = NoBadWords.MOD_NAME,
        version = NoBadWords.VERSION
)
public class NoBadWords {

    public static final String MOD_ID = "nobadwords";
    public static final String MOD_NAME = "nobadwords";
    public static final String VERSION = "1.0.1";


    /**
     * This is the instance of your mod as created by Forge. It will never be null.
     */
    @Mod.Instance(MOD_ID)
    public static NoBadWords INSTANCE;
    static ArrayList<String> badWords = new ArrayList<>();
    static Minecraft mc = Minecraft.getMinecraft();
    static ArrayList<String> ignoredWords = new ArrayList<>();

    public static void loadConfigs() {
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            mc.getResourceManager().getResource(
                                    new ResourceLocation("nobadwords", "badwords.txt")
                            ).getInputStream()
                    )
            );

            BufferedReader secondReader = new BufferedReader(
                    new InputStreamReader(
                            mc.getResourceManager().getResource(
                                    new ResourceLocation("nobadwords", "ignoredwords.txt")
                            ).getInputStream()
                    )
            );

            String line;
            while((line = reader.readLine()) != null) {
                badWords.add(line.trim());
            }

            String secondLine;
            while((line = secondReader.readLine()) != null) {
                ignoredWords.add(line.trim());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Iterates over a String input and checks whether a cuss word was found in a list, then checks if the word should be ignored (e.g. bass contains the word *ss).
     * @param input input string can be a word or sentence
     * @return HashSet of bad words found
     */
    public static HashSet<String> badWordsFound(String input) {
        if(input == null) {
            return new HashSet<>();
        }

        // don't forget to remove leetspeak, probably want to move this to its own function and use regex if you want to use this
        input = input.replaceAll("1","i");
        input = input.replaceAll("!","i");
        input = input.replaceAll("3","e");
        input = input.replaceAll("4","a");
        input = input.replaceAll("@","a");
        input = input.replaceAll("5","s");
        input = input.replaceAll("7","t");
        input = input.replaceAll("0","o");
        input = input.replaceAll("9","g");
        input = input.replaceAll("8", "ate");
        input = input.replaceAll("\\+", "t");

        HashSet<String> foundBadWords = new HashSet<>();
        String[] splitString = input.split("\\s+");

        for (String word : splitString){
            word = word.trim().toLowerCase();
            for (String badWord : badWords){
                if(word.contains(badWord) && !ignoredWords.contains(word)){
                    foundBadWords.add(badWord);
                }
            }
        }

        return foundBadWords;
    }

    /**
     * This is the first initialization event. Register tile entities here.
     * The registry events below will have fired prior to entry to this method.
     */
    @Mod.EventHandler
    public void preinit(FMLPreInitializationEvent event) {
        System.out.println("No Bad Words Pre-Init");
        MinecraftForge.EVENT_BUS.register(ChatEventClientOnlyEventHandler.class);
    }

    /**
     * This is the second initialization event. Register custom recipes
     */
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        loadConfigs();
        System.out.println("No Bad Words Init");
    }

    /**
     * This is the final initialization event. Register actions from other mods here
     */
    @Mod.EventHandler
    public void postinit(FMLPostInitializationEvent event) {
        System.out.println("No Bad Words Post Init");
    }

    public static class ChatEventClientOnlyEventHandler {
        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onChat(ClientChatEvent event) {
            String message = event.getMessage();
            StringBuilder newMessage = new StringBuilder();
            HashSet<String> caughtWords = badWordsFound(message);
            boolean caughtBadWord = (long) caughtWords.size() > 0;
            event.setCanceled(caughtBadWord);
            String s = caughtWords.toArray().length > 1 ? "§r, " : "";
            for (String word : caughtWords){
                newMessage.append("§c").append(word).append(s);
            }
            if (caughtBadWord){
                Minecraft.getMinecraft().player.sendMessage(new TextComponentString("§cBAD WORDS, Your message contained:"));
                Minecraft.getMinecraft().player.sendMessage(new TextComponentString(newMessage.toString()));
                Minecraft.getMinecraft().player.sendMessage(new TextComponentString(message));
            }
        }
    }
}
