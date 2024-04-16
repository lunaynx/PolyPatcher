package club.sk1er.patcher.hooks;

import net.minecraft.util.EnumFacing;

import java.util.BitSet;
import java.util.EnumMap;

public class FaceDataHook {

    private final EnumMap<EnumFacing, BitSet> data;
    private final int vMax;

    public FaceDataHook(final int uMax, final int vMax) {
        this.data = new EnumMap<>(EnumFacing.class);
        this.vMax = vMax;
        this.data.put(EnumFacing.WEST, new BitSet(uMax * vMax));
        this.data.put(EnumFacing.EAST, new BitSet(uMax * vMax));
        this.data.put(EnumFacing.UP, new BitSet(uMax * vMax));
        this.data.put(EnumFacing.DOWN, new BitSet(uMax * vMax));
    }

    public void set(final EnumFacing facing, final int u, final int v) {
        this.data.get(facing).set(this.getIndex(u, v));
    }

    public boolean get(final EnumFacing facing, final int u, final int v) {
        return this.data.get(facing).get(this.getIndex(u, v));
    }

    private int getIndex(final int u, final int v) {
        return v * this.vMax + u;
    }

}
