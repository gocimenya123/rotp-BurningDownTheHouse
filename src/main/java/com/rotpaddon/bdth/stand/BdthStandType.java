package com.rotpaddon.bdth.stand;

import com.github.standobyte.jojo.power.impl.stand.stats.StandStats;
import com.github.standobyte.jojo.power.impl.stand.type.NoSummonStandType;

/**
 * Burning Down the House has no body to summon: like Mr. President it is a {@link NoSummonStandType},
 * a Stand that is nothing but its abilities. Everything it does lives in its actions and in the ghost room.
 */
public class BdthStandType extends NoSummonStandType<StandStats> {

    public BdthStandType(NoSummonStandType.Builder<StandStats> builder) {
        super(builder);
    }
}
