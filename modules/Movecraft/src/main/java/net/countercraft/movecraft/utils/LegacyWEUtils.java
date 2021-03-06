package net.countercraft.movecraft.utils;

import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.extent.clipboard.Clipboard;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class LegacyWEUtils {
    private static Method GET_BLOCK;
    static {
        try {
            GET_BLOCK = Clipboard.class.getDeclaredMethod("getBlock", com.sk89q.worldedit.Vector.class);
        } catch (NoSuchMethodException e) {
            GET_BLOCK = null;
            e.printStackTrace();
        }
    }

    public static BaseBlock getBlock(Clipboard clipboard, com.sk89q.worldedit.Vector pos){
        try {
            return GET_BLOCK != null ? (BaseBlock) GET_BLOCK.invoke(clipboard, pos) : null;
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }


}
