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
import java.net.URL;
import java.util.*;

@Mod(
        modid = NoBadWords.MOD_ID,
        name = NoBadWords.MOD_NAME,
        version = NoBadWords.VERSION
)
public class NoBadWords {

    public static final String MOD_ID = "nobadwords";
    public static final String MOD_NAME = "nobadwords";
    public static final String VERSION = "1.0-SNAPSHOT";


    /**
     * This is the instance of your mod as created by Forge. It will never be null.
     */
    @Mod.Instance(MOD_ID)
    public static NoBadWords INSTANCE;
    static Map<String, String[]> words = new HashMap<>();
    static int largestWordLength = 0;
    static Minecraft mc = Minecraft.getMinecraft();

    public static void loadConfigs() {
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            mc.getResourceManager().getResource(
                                    new ResourceLocation("nobadwords", "badwords.txt")
                            ).getInputStream()
                    )
            );

            String line = "";
            int counter = 0;
            while((line = reader.readLine()) != null) {
                counter++;
                String[] content = null;
                try {
                    content = line.split(",");
                    if(content.length == 0) {
                        continue;
                    }
                    String word = content[0];
                    String[] ignore_in_combination_with_words = new String[]{};
                    if(content.length > 1) {
                        ignore_in_combination_with_words = content[1].split("_");
                    }

                    if(word.length() > largestWordLength) {
                        largestWordLength = word.length();
                    }
                    words.put(word.replaceAll(" ", ""), ignore_in_combination_with_words);

                } catch(Exception e) {
                    e.printStackTrace();
                }

            }
            System.out.println("Loaded " + counter + " words to filter out");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Iterates over a String input and checks whether a cuss word was found in a list, then checks if the word should be ignored (e.g. bass contains the word *ss).
     * @param input
     * @return
     */

    public static ArrayList<String> badWordsFound(String input) {
        if(input == null) {
            return new ArrayList<>();
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

        ArrayList<String> badWords = new ArrayList<>();
        input = input.toLowerCase().replaceAll("[^a-zA-Z]", "");

        String[] splited = input.split("\\s+");

        for (String word : splited){
            // iterate over each letter in the word
            for(int start = 0; start < word.length(); start++) {
                // from each letter, keep going to find bad words until either the end of the sentence is reached, or the max word length is reached.
                for(int offset = 1; offset < (input.length()+1 - start) && offset < largestWordLength; offset++)  {
                    String wordToCheck = input.substring(start, start + offset);
                    if(words.containsKey(wordToCheck)) {
                        // for example, if you want to say the word bass, that should be possible.
                        String[] ignoreCheck = words.get(wordToCheck);
                        boolean ignore = false;

                        for(int s = 0; s < ignoreCheck.length; s++ ) {
                            if(input.contains(ignoreCheck[s])) {
                                ignore = true;
                                break;
                            }
                        }

                        if(!ignore) {
                            badWords.add(wordToCheck);
                        }
                    }
                }
            }
        }





        for(String s: badWords) {
            System.out.println(s + " qualified as a bad word in a username");
        }
        return badWords;

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
            String[] msgWords = message.split("\\b");

            StringBuilder newMessage = new StringBuilder();
            ArrayList<String> caughtWords = badWordsFound(message);
            boolean caughtBadWord = (long) caughtWords.size() > 0;

            event.setCanceled(caughtBadWord);

            String s = caughtWords.toArray().length > 1 ? "§r, " : "";

            for (String word : caughtWords){
                newMessage.append("§c").append(word).append(s);
            }
            if (caughtBadWord){
                Minecraft.getMinecraft().player.sendMessage(new TextComponentString("§cBAD WORDS, Your message contained:"));
                Minecraft.getMinecraft().player.sendMessage(new TextComponentString(newMessage.toString()));
            }
        }
    }
}
