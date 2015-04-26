package me.guichaguri.betterfps;

import cpw.mods.fml.relauncher.IFMLCallHook;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;
import java.io.File;
import java.util.Map;

/**
 * @author Guilherme Chaguri
 */

@TransformerExclusions({"me.guichaguri.betterfps", "me.guichaguri.betterfps.math"})
public class BetterFps implements IFMLLoadingPlugin, IFMLCallHook {

    public static final String MODID = "betterfps";
    public static final String VERSION = "1.0.0";
    public static File LOC;

    @Override
    public String[] getASMTransformerClass() {
        return new String[]{"me.guichaguri.betterfps.BetterFpsTransformer"};
    }
    @Override
    public String getModContainerClass() {
        return "me.guichaguri.betterfps.BetterFpsContainer";
    }
    @Override
    public String getSetupClass() {
        return "me.guichaguri.betterfps.BetterFps";
    }
    @Override
    public void injectData(Map<String, Object> data) {
        LOC = (File)data.get("coremodLocation");
    }
    @Override
    public String getAccessTransformerClass() {
        return null;
    }

    @Override
    public Void call() throws Exception {
        return null;
    }
}