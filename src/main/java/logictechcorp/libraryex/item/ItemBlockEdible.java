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

package logictechcorp.libraryex.item;

import logictechcorp.libraryex.item.builder.ItemEdibleProperties;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

public class ItemBlockEdible extends ItemBlockMod
{
    private final int healAmount;
    private final float saturation;
    private final boolean isWolfFood;
    private final boolean alwaysEdible;
    private final PotionEffect potionEffect;
    private final float potionEffectProbability;

    public ItemBlockEdible(Block block, ItemEdibleProperties properties)
    {
        super(block, properties);
        this.healAmount = properties.getHealAmount();
        this.saturation = properties.getSaturation();
        this.isWolfFood = properties.isWolfFood();
        this.alwaysEdible = properties.isAlwaysEdible();
        this.potionEffect = properties.getPotionEffect();
        this.potionEffectProbability = properties.getPotionEffectProbability();
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World world, EntityLivingBase entityLiving)
    {
        if(entityLiving instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer) entityLiving;
            player.getFoodStats().addStats(this.healAmount, this.saturation);
            world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_BURP, SoundCategory.PLAYERS, 0.5F, world.rand.nextFloat() * 0.1F + 0.9F);
            this.onFoodEaten(stack, world, player);
            player.addStat(StatList.getObjectUseStats(this));
        }

        stack.shrink(1);
        return stack;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack)
    {
        return 32;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack)
    {
        return EnumAction.EAT;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
    {
        ItemStack stack = player.getHeldItem(hand);

        if(player.canEat(this.alwaysEdible))
        {
            player.setActiveHand(hand);
            return new ActionResult(EnumActionResult.SUCCESS, stack);
        }
        else
        {
            return new ActionResult(EnumActionResult.FAIL, stack);
        }
    }

    protected void onFoodEaten(ItemStack stack, World world, EntityPlayer player)
    {
        if(!world.isRemote && this.potionEffect != null && world.rand.nextFloat() < this.potionEffectProbability)
        {
            player.addPotionEffect(new PotionEffect(this.potionEffect));
        }
    }

    public int getHealAmount(ItemStack stack)
    {
        return this.healAmount;
    }

    public float getSaturation(ItemStack stack)
    {
        return this.saturation;
    }

    public boolean isWolfsFood()
    {
        return this.isWolfFood;
    }

    public PotionEffect getPotionEffect()
    {
        return this.potionEffect;
    }

    public float getPotionEffectProbability()
    {
        return this.potionEffectProbability;
    }
}
