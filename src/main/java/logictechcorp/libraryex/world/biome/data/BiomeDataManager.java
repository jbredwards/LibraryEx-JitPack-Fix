/*
 * LibraryEx
 * Copyright (c) 2017-2019 by LogicTechCorp
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package logictechcorp.libraryex.world.biome.data;

import com.electronwill.nightconfig.core.file.FileConfig;
import com.electronwill.nightconfig.json.JsonFormat;
import logictechcorp.libraryex.utility.FileHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class BiomeDataManager
{
    protected final Map<ResourceLocation, BiomeData> defaultBiomeData;
    protected final Map<ResourceLocation, BiomeData> currentBiomeData;
    protected final Map<ResourceLocation, BiomeManager.BiomeEntry> currentBiomeEntries;
    protected final Logger logger;

    public BiomeDataManager(String loggerName)
    {
        this.defaultBiomeData = new HashMap<>();
        this.currentBiomeData = new HashMap<>();
        this.currentBiomeEntries = new ConcurrentHashMap<>();
        this.logger = LogManager.getLogger(loggerName);
    }

    public void setup()
    {
        this.defaultBiomeData.putAll(this.currentBiomeData);
        this.currentBiomeData.clear();
        this.currentBiomeEntries.clear();
    }

    public void registerBiomeData(BiomeData biomeData)
    {
        if(biomeData != null && biomeData.getBiome() != null && biomeData != BiomeData.EMPTY)
        {
            Biome biome = biomeData.getBiome();
            ResourceLocation biomeRegistryName = biome.getRegistryName();
            this.currentBiomeData.put(biomeRegistryName, biomeData);

            if(!biomeData.isSubBiome())
            {
                if(biomeData.isEnabled())
                {
                    this.currentBiomeEntries.put(biomeRegistryName, new BiomeManager.BiomeEntry(biome, biomeData.getGenerationWeight()));
                }
                else
                {
                    this.currentBiomeEntries.remove(biomeRegistryName);
                }
            }
        }
    }

    public void unregisterBiomeData(Biome biome)
    {
        if(biome != null)
        {
            ResourceLocation biomeRegistryName = biome.getRegistryName();
            this.currentBiomeData.remove(biomeRegistryName);
            this.currentBiomeEntries.remove(biomeRegistryName);
        }
    }

    public abstract void onWorldLoad(WorldEvent.Load event);

    public abstract void onWorldUnload(WorldEvent.Unload event);

    public void readBiomeDataConfigs(Path biomeConfigDirectoryPath)
    {
        if(Files.exists(biomeConfigDirectoryPath))
        {
            this.logger.info("Reading biome configs.");

            try
            {
                Iterator<Path> pathIter = Files.walk(biomeConfigDirectoryPath).iterator();

                while(pathIter.hasNext())
                {
                    Path configPath = pathIter.next();
                    File configFile = configPath.toFile();

                    if(FileHelper.getFileExtension(configFile).equals("json"))
                    {
                        if(Files.isReadable(configPath))
                        {
                            String fileText = FileUtils.readFileToString(configFile, Charset.defaultCharset()).trim();

                            if(fileText.startsWith("{") && fileText.endsWith("}"))
                            {
                                FileConfig config = FileConfig.builder(configFile, JsonFormat.fancyInstance()).preserveInsertionOrder().build();
                                config.load();

                                Biome biome = ForgeRegistries.BIOMES.getValue(new ResourceLocation(config.get("biome")));

                                if(biome != null)
                                {
                                    BiomeData biomeData;

                                    if(this.hasBiomeData(biome))
                                    {
                                        biomeData = this.getBiomeData(biome);
                                    }
                                    else
                                    {
                                        biomeData = this.createBiomeData(biome.getRegistryName(), 10, true, false);
                                    }

                                    biomeData.readFromConfig(this, config);
                                    this.registerBiomeData(biomeData);
                                }

                                config.save();
                                config.close();
                            }
                            else
                            {
                                this.logger.warn("Skipping biome config at {}. Its contents are invalid.", configPath);
                            }
                        }
                        else
                        {
                            this.logger.warn("Skipping biome config at {}. It is unreadable.", configPath);
                        }
                    }
                    else if(!configFile.isDirectory())
                    {
                        this.logger.warn("Skipping biome config at {}. It is not a json file.", configPath);
                    }
                }
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void createBiomeDataConfigs(Path biomeConfigDirectoryPath)
    {
        if(Files.notExists(biomeConfigDirectoryPath))
        {
            this.logger.info("Creating biome configs.");

            try
            {
                for(BiomeData biomeData : this.defaultBiomeData.values())
                {
                    String biomeRegistryName = biomeData.getBiome().getRegistryName().toString();
                    File configFile = new File(biomeConfigDirectoryPath.toFile(), biomeRegistryName.replace(":", "/") + ".json");
                    Files.createDirectories(configFile.getParentFile().toPath());
                    FileConfig config = FileConfig.builder(configFile, JsonFormat.fancyInstance()).preserveInsertionOrder().build();
                    biomeData.writeToConfig(config);
                    config.save();
                    config.close();
                }
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public BiomeData createBiomeData(ResourceLocation biomeRegistryName, int generationWeight, boolean useDefaultDecorations, boolean isSubBiome)
    {
        return new BiomeData(biomeRegistryName, generationWeight, useDefaultDecorations, isSubBiome);
    }

    public boolean hasBiomeData(Biome biome)
    {
        return this.currentBiomeData.containsKey(biome.getRegistryName());
    }

    public BiomeData getBiomeData(Biome biome)
    {
        return this.currentBiomeData.getOrDefault(biome.getRegistryName(), BiomeData.EMPTY);
    }

    public Map<ResourceLocation, BiomeData> getDefaultBiomeData()
    {
        return Collections.unmodifiableMap(this.defaultBiomeData);
    }

    public Map<ResourceLocation, BiomeData> getCurrentBiomeData()
    {
        return Collections.unmodifiableMap(this.currentBiomeData);
    }

    public Map<ResourceLocation, BiomeManager.BiomeEntry> getCurrentBiomeEntries()
    {
        return Collections.unmodifiableMap(this.currentBiomeEntries);
    }
}
