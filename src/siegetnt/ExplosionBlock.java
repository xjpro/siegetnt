package siegetnt;

import org.bukkit.block.Block;

/**
 *
 * @author Me
 */
public class ExplosionBlock {

    private final Block block;
    private final boolean isCorner;

    public ExplosionBlock(Block block, boolean corner) {
        this.block = block;
        this.isCorner = corner;
    }

    public Block getBlock() {
        return block;
    }

    public boolean isCorner() {
        return isCorner;
    }

}
