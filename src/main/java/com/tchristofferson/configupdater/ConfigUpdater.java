package com.tchristofferson.configupdater;

import de.laparudi.rudicrates.RudiCrates;
import de.laparudi.rudicrates.language.Language;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ConfigUpdater {

    //Used for separating keys in the keyBuilder inside parseComments method
    private static final char SEPARATOR = '.';
    private static final List<String> IGNORED = Arrays.asList("crates", "items.fill", "items.close");

    private static void sendFeedback(final String configName, final String key, final boolean add) {
        final String action = add ? "added" : "removed";
        
        if (configName.equals("config.yml")) {
            Language.send(Bukkit.getConsoleSender(), "updater.config." + action, "%option%", key);

        } else if (configName.equals("messages.yml")) {
            Language.send(Bukkit.getConsoleSender(), "updater.messages." + action, "%message%", key);
        }
    }
    
    public static void update(final Plugin plugin, final String resourceName, final File toUpdate) {
        try {
            if (!toUpdate.exists()) return;
            final InputStream inputStream = RudiCrates.getPlugin().getResource(resourceName);
            if (inputStream == null) return;
            
            final FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            final FileConfiguration currentConfig = YamlConfiguration.loadConfiguration(toUpdate);
            
            defaultConfig.getKeys(true).forEach(key -> {
                if (StringUtils.startsWithAny(key, IGNORED.toArray(String[]::new))) return;
                if (key.contains(".durability") || currentConfig.getKeys(true).contains(key)) return;
                sendFeedback(resourceName, key, true);
            });
            
            currentConfig.getKeys(true).forEach(key -> {
                if (StringUtils.startsWithAny(key, IGNORED.toArray(String[]::new))) return;
                if (key.contains(".durability") || defaultConfig.getKeys(true).contains(key)) return;
                sendFeedback(resourceName, key, false);
            });
            
            final Map<String, String> comments = parseComments(plugin, resourceName, defaultConfig);
            final Map<String, String> ignoredSectionsValues = parseIgnoredSections(toUpdate, currentConfig, comments);

            // will write updated config file "contents" to a string
            final StringWriter writer = new StringWriter();
            write(defaultConfig, currentConfig, new BufferedWriter(writer), comments, ignoredSectionsValues);
            final String value = writer.toString(); // config contents
            
            final Path toUpdatePath = toUpdate.toPath();
            if (!value.equals(Files.readString(toUpdatePath))) { // if updated contents are not the same as current file contents, update
                Files.write(toUpdatePath, value.getBytes(StandardCharsets.UTF_8));
            }
            
        } catch (final IOException exception) {
            exception.printStackTrace();
        }
    }

    private static void write(final FileConfiguration defaultConfig, final FileConfiguration currentConfig, final BufferedWriter writer,
                              final Map<String, String> comments, final Map<String, String> ignoredValues) throws IOException {
        
        //Used for converting objects to yaml, then cleared
        final FileConfiguration parserConfig = new YamlConfiguration();
        
        keyLoop: for (final String fullKey : defaultConfig.getKeys(true)) {
            final String indents = KeyBuilder.getIndents(fullKey, SEPARATOR);

            if (ignoredValues.isEmpty()) {
                writeCommentIfExists(comments, writer, fullKey, indents);
                
            } else {
                for (final Map.Entry<String, String> entry : ignoredValues.entrySet()) {
                    if (entry.getKey().equals(fullKey)) {
                        writer.write(ignoredValues.get(fullKey) + "\n");
                        continue keyLoop;
                        
                    } else if (KeyBuilder.isSubKeyOf(entry.getKey(), fullKey, SEPARATOR)) {
                        continue keyLoop;
                    }
                }

                writeCommentIfExists(comments, writer, fullKey, indents);
            }

            Object currentValue = currentConfig.get(fullKey);

            if (currentValue == null)
                currentValue = defaultConfig.get(fullKey);

            final String[] splitFullKey = fullKey.split("[" + SEPARATOR + "]");
            final String trailingKey = splitFullKey[splitFullKey.length - 1];

            if (currentValue instanceof ConfigurationSection) {
                writer.write(indents + trailingKey + ":");

                if (!((ConfigurationSection) currentValue).getKeys(false).isEmpty()) {
                    writer.write("\n");
                } else
                    writer.write(" {}\n");

                continue;
            }

            parserConfig.set(trailingKey, currentValue);
            String yaml = parserConfig.saveToString();
            yaml = yaml.substring(0, yaml.length() - 1).replace("\n", "\n" + indents);
            
            final String toWrite = indents + yaml + "\n";
            parserConfig.set(trailingKey, null);
            writer.write(toWrite);
        }

        final String danglingComments = comments.get(null);
        if (danglingComments != null) writer.write(danglingComments);
        writer.close();
    }

    //Returns a map of key comment pairs. If a key doesn't have any comments it won't be included in the map.
    private static Map<String, String> parseComments(final Plugin plugin, final String resourceName, final FileConfiguration defaultConfig) throws IOException {
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(plugin.getResource(resourceName)), StandardCharsets.UTF_8))) {
            final Map<String, String> comments = new LinkedHashMap<>();
            final StringBuilder commentBuilder = new StringBuilder();
            final KeyBuilder keyBuilder = new KeyBuilder(defaultConfig, SEPARATOR);

            String line;
            while ((line = reader.readLine()) != null) {
                final String trimmedLine = line.trim();

                // Only getting comments for keys. A list/array element comment(s) not supported
                if (trimmedLine.startsWith("-")) {
                    continue;
                }

                if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) { // Is blank line or is comment
                    commentBuilder.append(trimmedLine).append("\n");

                } else { //Is a valid yaml key
                    keyBuilder.parseLine(trimmedLine);
                    final String key = keyBuilder.toString();

                    // If there is a comment associated with the key it is added to comments map and the commentBuilder is reset
                    if (commentBuilder.length() > 0) {
                        comments.put(key, commentBuilder.toString());
                        commentBuilder.setLength(0);
                    }

                    // Remove the last key from keyBuilder if current path isn't a config section or if it is empty to prepare for the next key
                    if (!keyBuilder.isConfigSectionWithKeys()) {
                        keyBuilder.removeLastKey();
                    }
                }
            }

            //reader.close();
            if (commentBuilder.length() > 0) comments.put(null, commentBuilder.toString());
            return comments;
        }
    }

    private static Map<String, String> parseIgnoredSections(final File toUpdate, final FileConfiguration currentConfig, final Map<String, String> comments) throws IOException {
        try (final BufferedReader reader = new BufferedReader(new FileReader(toUpdate, StandardCharsets.UTF_8))) {
            final Map<String, String> ignoredSectionsValues = new LinkedHashMap<>(IGNORED.size());
            final KeyBuilder keyBuilder = new KeyBuilder(currentConfig, SEPARATOR);
            final StringBuilder valueBuilder = new StringBuilder();

            String currentIgnoredSection = null;
            String line;
            lineLoop:
            while ((line = reader.readLine()) != null) {
                final String trimmedLine = line.trim();

                if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) continue;

                if (trimmedLine.startsWith("-")) {
                    for (final String ignoredSection : IGNORED) {
                        final boolean isIgnoredParent = ignoredSection.equals(keyBuilder.toString());

                        if (isIgnoredParent || keyBuilder.isSubKeyOf(ignoredSection)) {
                            valueBuilder.append("\n").append(line);
                            continue lineLoop;
                        }
                    }
                }

                keyBuilder.parseLine(trimmedLine);
                final String fullKey = keyBuilder.toString();

                //If building the value for an ignored section and this line is no longer a part of the ignored section,
                //  write the valueBuilder, reset it, and set the current ignored section to null
                if (currentIgnoredSection != null && !KeyBuilder.isSubKeyOf(currentIgnoredSection, fullKey, SEPARATOR)) {
                    ignoredSectionsValues.put(currentIgnoredSection, valueBuilder.toString());
                    valueBuilder.setLength(0);
                    currentIgnoredSection = null;
                }

                for (final String ignoredSection : IGNORED) {
                    boolean isIgnoredParent = ignoredSection.equals(fullKey);

                    if (isIgnoredParent || keyBuilder.isSubKeyOf(ignoredSection)) {
                        if (valueBuilder.length() > 0) valueBuilder.append("\n");

                        final String comment = comments.get(fullKey);

                        if (comment != null) {
                            String indents = KeyBuilder.getIndents(fullKey, SEPARATOR);
                            valueBuilder.append(indents).append(comment.replace("\n", "\n" + indents));//Should end with new line (\n)
                            valueBuilder.setLength(valueBuilder.length() - indents.length());//Get rid of trailing \n and spaces
                        }

                        valueBuilder.append(line);

                        //Set the current ignored section for future iterations of while loop
                        //Don't set currentIgnoredSection to any ignoredSection sub-keys
                        if (isIgnoredParent) currentIgnoredSection = fullKey;

                        break;
                    }
                }
            }

            //reader.close();

            if (valueBuilder.length() > 0) ignoredSectionsValues.put(currentIgnoredSection, valueBuilder.toString());
            return ignoredSectionsValues;
        }
    }

    private static void writeCommentIfExists(final Map<String, String> comments, final BufferedWriter writer, final String fullKey, final String indents) throws IOException {
        final String comment = comments.get(fullKey);

        //Comments always end with new line (\n)
        if (comment != null)
            //Replaces all '\n' with '\n' + indents except for the last one
            writer.write(indents + comment.substring(0, comment.length() - 1).replace("\n", "\n" + indents) + "\n");
    }
}