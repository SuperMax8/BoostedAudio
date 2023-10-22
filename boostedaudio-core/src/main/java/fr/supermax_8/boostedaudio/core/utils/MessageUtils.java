package fr.supermax_8.boostedaudio.core.utils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.List;
import java.util.function.Consumer;

public class MessageUtils {

    public static StringBuilder colorFormat(StringBuilder text) {
        StringBuilder builder = new StringBuilder(text);
        replaceAll(builder, "&", "§");
        /*if (BoostedAudioHost.bukkitVersion < 1.16) return builder;*/
        int customColorIndex = builder.indexOf("§#");
        while (customColorIndex != -1) {
            String colorText = builder.substring(customColorIndex + 1, customColorIndex + 8);
            builder.replace(customColorIndex, customColorIndex + 8, ChatColor.of(colorText) + "");
            customColorIndex = builder.indexOf("§#");
        }
        return builder;
    }

    public static TextComponent colorFormatToTextComponent(StringBuilder builder) {
        replaceAll(builder, "&", "§");
        //if (BoostedAudioHost.bukkitVersion < 1.16) return new TextComponent(builder.toString());
        TextComponent baseComponent = new TextComponent();
        ChatColor nextColor = null;
        int customColorIndex = builder.indexOf("§#");
        while (customColorIndex != -1) {
            TextComponent beforeNewColorText = new TextComponent(builder.substring(0, customColorIndex));
            if (nextColor != null) {
                beforeNewColorText.setColor(nextColor);
            }
            String colorText = builder.substring(customColorIndex + 1, customColorIndex + 8);
            builder.delete(0, customColorIndex + 8);

            nextColor = ChatColor.of(colorText);

            baseComponent.addExtra(beforeNewColorText);
            customColorIndex = builder.indexOf("§#");
        }
        if (nextColor != null) baseComponent.setColor(nextColor);
        baseComponent.addExtra(builder.toString());
        return baseComponent;
    }

    public static void applyRecursilvlyOnTextComponent(TextComponent component, Consumer<TextComponent> apply) {
        apply.accept(component);
        List<BaseComponent> extra = component.getExtra();
        if (extra == null) return;
        for (BaseComponent component1 : extra) applyRecursilvlyOnTextComponent((TextComponent) component1, apply);
    }

    public static void replaceAll(StringBuilder builder, String toReplace, String replacement) {
        int toReplaceLength = toReplace.length();
        int indexToReplace = builder.indexOf(toReplace);
        while (indexToReplace != -1) {
            builder.replace(indexToReplace, indexToReplace + toReplaceLength, replacement);
            indexToReplace = builder.indexOf(toReplace);
        }
    }

}