package dev.compactmods.machines.datagen;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.function.BiConsumer;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import dev.compactmods.machines.CompactMachines;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

public class LevelBiomeGenerator implements DataProvider {
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
    private final DataGenerator generator;

    private final ResourceLocation COMPACT_BIOME = new ResourceLocation(CompactMachines.MOD_ID, "machine");
    private final ResourceLocation COMPACT_LEVEL = new ResourceLocation(CompactMachines.MOD_ID, "compact_world");

    private final ExistingFileHelper existing;

    LevelBiomeGenerator(DataGenerator generator, ExistingFileHelper files) {
        this.generator = generator;
        this.existing = files;
    }

    @Override
    public void run(@NotNull HashCache cache) {
        Path data = this.generator.getOutputFolder();

        HashMap<ResourceLocation, Biome> biomes = Maps.newHashMap();
        HashMap<ResourceLocation, LevelStem> dims = Maps.newHashMap();
        HashMap<ResourceLocation, DimensionType> dimTypes = Maps.newHashMap();

        var biomeWriter = DataGenUtil.makeWriter(GSON, cache, data, ImmutableSet.of("worldgen", "biome"), Biome.DIRECT_CODEC, biomes);

        var dimTypeWriter = DataGenUtil.makeWriter(GSON, cache, data, ImmutableSet.of("dimension_type"), DimensionType.DIRECT_CODEC, dimTypes);

        var dimWriter = DataGenUtil.makeCustomWriter(GSON, cache, data, ImmutableSet.of("dimension"), this::writeFlatDimension, dims);

        writeBiomes(biomeWriter);
        writeDimensionTypes(dimTypeWriter);
        writeDimensions(biomes, dimTypes, dimWriter);
    }

    private JsonElement writeFlatDimension(LevelStem dimension) {
        JsonObject d = new JsonObject();

        d.addProperty("type", COMPACT_LEVEL.toString());

        var gen = ChunkGenerator.CODEC.encodeStart(JsonOps.INSTANCE, dimension.generator())
                .getOrThrow(false, CompactMachines.LOGGER::error)
                .getAsJsonObject();

        // transform the chunk generator to add the type reference to the flat gen
        var fls = Registry.CHUNK_GENERATOR.getResourceKey(FlatLevelSource.CODEC);
        fls.ifPresent(genType -> gen.addProperty("type", genType.location().toString()));

        // transform the full biome object into a reference
        var settings = gen.get("settings").getAsJsonObject();
        settings.remove("biome");
        settings.addProperty("biome", this.COMPACT_BIOME.toString());

        d.add("generator", gen);

        return d;
    }

    private void writeDimensions(HashMap<ResourceLocation, Biome> biomes, HashMap<ResourceLocation, DimensionType> dimTypes, BiConsumer<LevelStem, ResourceLocation> consumer) {
        var struct = new StructureSettings(false);
        struct.structureConfig().clear();

        var flatSettings = new FlatLevelGeneratorSettings(struct, BuiltinRegistries.BIOME);

        flatSettings.setBiome(() -> biomes.get(COMPACT_BIOME));
        flatSettings.getLayersInfo().add(new FlatLayerInfo(1, Blocks.AIR));
        flatSettings.updateLayers();

        var stem = new LevelStem(() -> dimTypes.get(COMPACT_LEVEL), new FlatLevelSource(flatSettings));
        consumer.accept(stem, COMPACT_LEVEL);
    }

    private void writeDimensionTypes(BiConsumer<DimensionType, ResourceLocation> consumer) {
        final DimensionType dim = new DimensionTypeBuilder()
                .ambientLight(15.0f)
                .bedWorks(false)
                .respawnAnchorWorks(false)
                .fixedTime(18000L)
                .natural(false)
                .raids(false)
                .heightBounds(0, 256)
                .build();

        consumer.accept(dim, COMPACT_LEVEL);
    }

    private void writeBiomes(BiConsumer<Biome, ResourceLocation> biomeWriter) {
        var spawnBuilder = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.plainsSpawns(spawnBuilder);
        var spawns = spawnBuilder.build();

        final Biome compactBiome = new Biome.BiomeBuilder()
                .biomeCategory(Biome.BiomeCategory.NONE)
                .downfall(0)
                .generationSettings(BiomeGenerationSettings.EMPTY)
                .mobSpawnSettings(spawns)
                .precipitation(Biome.Precipitation.NONE)
                .temperature(0.8f)
                .temperatureAdjustment(Biome.TemperatureModifier.NONE)
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .fogColor(12638463)
                        .waterColor(4159204)
                        .waterFogColor(329011)
                        .skyColor(0xFF000000)
                        .build())
                .build();

        compactBiome.setRegistryName(COMPACT_BIOME);
        biomeWriter.accept(compactBiome, COMPACT_BIOME);
    }



    @Override
    public String getName() {
        return CompactMachines.MOD_ID + ":levelgen";
    }
}
