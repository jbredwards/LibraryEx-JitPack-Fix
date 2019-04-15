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

package logictechcorp.libraryex.world.biome;

public enum BiomeBlock implements IBiomeBlock
{
    FLOOR_TOP_BLOCK("floorTopBlock"),
    FLOOR_FILLER_BLOCK("floorFillerBlock"),
    WALL_BLOCK("wallBlock"),
    CEILING_FILLER_BLOCK("ceilingFillerBlock"),
    CEILING_BOTTOM_BLOCK("ceilingBottomBlock"),
    OCEAN_BLOCK("oceanBlock");

    private String identifier;

    BiomeBlock(String identifier)
    {
        this.identifier = identifier;
    }

    @Override
    public String getIdentifier()
    {
        return this.identifier;
    }
}
