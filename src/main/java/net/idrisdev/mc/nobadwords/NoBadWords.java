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
    static ArrayList<String> ignoredWords = new ArrayList<>();
    static ArrayList<String> ignoredCommands = new ArrayList<>();
    static Minecraft mc = Minecraft.getMinecraft();
    private boolean wordCheckEnabled = true;

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

            BufferedReader thirdReader = new BufferedReader(
                    new InputStreamReader(
                            mc.getResourceManager().getResource(
                                    new ResourceLocation("nobadwords", "ignoredcommands.txt")
                            ).getInputStream()
                    )
            );

            String line;
            while((line = reader.readLine()) != null) {
                badWords.add(line.trim());
            }

            while((line = secondReader.readLine()) != null) {
                ignoredWords.add(line.trim());
                ignoredWords.add(line.trim()+"i");
            }

            while((line = thirdReader.readLine()) != null) {
                ignoredCommands.add(line.trim());
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

        for (String comm : ignoredCommands){
            if (splitString[0].contains(comm)){
                return new HashSet<>();
            }
        }

        for (String word : splitString){
            String parsedWord = word.replaceAll("[^a-zA-Z0-9]", "").trim().toLowerCase();
            for (String badWord : badWords){
                if(parsedWord.contains(badWord) && !ignoredWords.contains(parsedWord)){
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
        INSTANCE = this;
    }

    private static void sendClientMessageWithPrefix(String message){
        String prefix = "§f[§6NoBadWords§f]§r ";
        Minecraft.getMinecraft().player.sendMessage(new TextComponentString(prefix + message));
    }
    private static void sendClientMessage(String message){
        Minecraft.getMinecraft().player.sendMessage(new TextComponentString(message));
    }

    public static class ChatEventClientOnlyEventHandler {
        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onChat(ClientChatEvent event) {
            String message = event.getMessage();

            if(isToggleCommand(message)){
                INSTANCE.wordCheckEnabled = !INSTANCE.wordCheckEnabled;
                sendClientMessageWithPrefix(INSTANCE.wordCheckEnabled ? "NoBadWords Enabled" : "NoBadWords Disabled");
                event.setCanceled(true);
                return;
            }

            if(!INSTANCE.wordCheckEnabled){
                sendClientMessageWithPrefix("§eWarning: NoBadWords is Disabled!");
                return;
            }

            StringBuilder newMessage = new StringBuilder();
            HashSet<String> caughtWords = badWordsFound(message);
            boolean caughtBadWord = (long) caughtWords.size() > 0;
            event.setCanceled(caughtBadWord);
            String s = caughtWords.toArray().length > 1 ? "§r, " : "";
            for (String word : caughtWords){
                newMessage.append("§c").append(word).append(s);
            }
            if (caughtBadWord){
                sendClientMessageWithPrefix("§cBAD WORDS, Your message contained:");
                sendClientMessage(newMessage.toString());
                sendClientMessage(message);
            }
        }

        private static boolean isToggleCommand(String message) {
            String[] splitString = message.split("\\s+");
            return splitString[0].toLowerCase().contains("nbwtoggle");
        }
    }
}
