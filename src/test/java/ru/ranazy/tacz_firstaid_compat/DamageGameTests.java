package ru.ranazy.tacz_firstaid_compat;

import com.mojang.authlib.GameProfile;
import com.tacz.guns.entity.EntityKineticBullet;
import com.tacz.guns.init.ModDamageTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import ru.ranazy.tacz_firstaid_compat.mixininterface.EntityKineticBulletExtension;
import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.common.util.CommonUtils;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@GameTestHolder(TaCZFirstAidCompat.MOD_ID)
@PrefixGameTestTemplate(false)
public final class DamageGameTests {
    @GameTest(template = "empty")
    public static void explosionUsesBlastCenterAndPreservesDamage(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        AbstractPlayerDamageModel model = CommonUtils.getDamageModel(player);
        helper.assertTrue(model != null, "У игрока должна быть модель First Aid");
        model.forEach(part -> {
            part.setMaxHealth(100);
            part.currentHealth = 100;
        });

        boolean friendly = FirstAidConfig.SERVER.useFriendlyRandomDistribution.get();
        try {
            FirstAidConfig.SERVER.useFriendlyRandomDistribution.set(false);
            Vec3 center = player.position().add(0.3, 1.1, 0);
            Explosion explosion = new Explosion(helper.getLevel(), player,
                    center.x, center.y, center.z, 2, false, Explosion.BlockInteraction.KEEP);
            MinecraftForge.EVENT_BUS.post(new ExplosionEvent.Detonate(helper.getLevel(), explosion,
                    new ArrayList<>(List.of(player))));
            LivingHurtEvent damage = new LivingHurtEvent(player, explosion.getDamageSource(), 20);
            MinecraftForge.EVENT_BUS.post(damage);

            helper.assertTrue(damage.isCanceled(), "First Aid должен обработать урон вместо vanilla");
            assertHealth(helper, model, EnumPlayerPart.LEFT_ARM, 90);
            assertHealth(helper, model, EnumPlayerPart.BODY, 95);
            assertHealth(helper, model, EnumPlayerPart.RIGHT_ARM, 97);
            for (EnumPlayerPart part : List.of(EnumPlayerPart.HEAD, EnumPlayerPart.LEFT_LEG,
                    EnumPlayerPart.RIGHT_LEG, EnumPlayerPart.LEFT_FOOT, EnumPlayerPart.RIGHT_FOOT)) {
                assertHealth(helper, model, part, 99.6f);
            }
            helper.succeed();
        } finally {
            FirstAidConfig.SERVER.useFriendlyRandomDistribution.set(friendly);
        }
    }

    @GameTest(template = "empty")
    public static void explosionSpilloverDoesNotLoseEnergyInFeet(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        AbstractPlayerDamageModel model = CommonUtils.getDamageModel(player);
        model.forEach(part -> {
            part.setMaxHealth(100);
            part.currentHealth = 100;
        });
        model.LEFT_FOOT.currentHealth = 0;
        Vec3 center = player.position().add(0.1, 0.075, 0);
        Explosion explosion = new Explosion(helper.getLevel(), player,
                center.x, center.y, center.z, 2, false, Explosion.BlockInteraction.KEEP);
        MinecraftForge.EVENT_BUS.post(new ExplosionEvent.Detonate(helper.getLevel(), explosion,
                new ArrayList<>(List.of(player))));
        MinecraftForge.EVENT_BUS.post(new LivingHurtEvent(player, explosion.getDamageSource(), 20));
        assertHealth(helper, model, EnumPlayerPart.LEFT_LEG, 87);
        float totalHealth = 0;
        for (var part : model) {
            totalHealth += part.currentHealth;
        }
        helper.assertTrue(Math.abs(totalHealth - 680) < 0.001f, "Взрыв должен передать все 20 HP урона");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void bulletDamageComponentsKeepHitAndSpillover(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        AbstractPlayerDamageModel model = CommonUtils.getDamageModel(player);
        model.forEach(part -> {
            part.setMaxHealth(100);
            part.currentHealth = 100;
        });
        model.LEFT_ARM.currentHealth = 2;
        EntityKineticBullet bullet = new EntityKineticBullet(EntityKineticBullet.TYPE, helper.getLevel());
        ((EntityKineticBulletExtension) bullet).tacZ_firstaid$setLastHitLocation(player.position().add(0.3, 1.1, 0));
        MinecraftForge.EVENT_BUS.post(new LivingHurtEvent(player, bulletSource(helper, bullet, false), 20));
        MinecraftForge.EVENT_BUS.post(new LivingHurtEvent(player, bulletSource(helper, bullet, true), 10));

        assertHealth(helper, model, EnumPlayerPart.LEFT_ARM, 0);
        assertHealth(helper, model, EnumPlayerPart.BODY, 77.6f);
        assertHealth(helper, model, EnumPlayerPart.RIGHT_ARM, 100);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void chestArmorProtectsArmsButNotAgainstPiercing(GameTestHelper helper) {
        ServerPlayer player = createPlayer(helper);
        AbstractPlayerDamageModel model = CommonUtils.getDamageModel(player);
        model.LEFT_ARM.setMaxHealth(100);
        model.LEFT_ARM.currentHealth = 100;
        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.DIAMOND_CHESTPLATE));
        // Атрибуты экипировки в игре обновляются на тике; тест наносит урон сразу.
        player.getAttributes().addTransientAttributeModifiers(
                player.getItemBySlot(EquipmentSlot.CHEST).getAttributeModifiers(EquipmentSlot.CHEST));
        EntityKineticBullet bullet = new EntityKineticBullet(EntityKineticBullet.TYPE, helper.getLevel());
        ((EntityKineticBulletExtension) bullet).tacZ_firstaid$setLastHitLocation(player.position().add(0.3, 1.1, 0));
        MinecraftForge.EVENT_BUS.post(new LivingHurtEvent(player, bulletSource(helper, bullet, false), 10));
        helper.assertTrue(model.LEFT_ARM.currentHealth > 90 && model.LEFT_ARM.currentHealth < 100,
                "Нагрудник должен частично поглотить обычную пулю в руку");
        model.LEFT_ARM.currentHealth = 100;
        MinecraftForge.EVENT_BUS.post(new LivingHurtEvent(player, bulletSource(helper, bullet, true), 10));
        assertHealth(helper, model, EnumPlayerPart.LEFT_ARM, 90);
        helper.succeed();
    }

    private static DamageSource bulletSource(GameTestHelper helper, EntityKineticBullet bullet, boolean piercing) {
        return new DamageSource(helper.getLevel().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(piercing ? ModDamageTypes.BULLET_IGNORE_ARMOR : ModDamageTypes.BULLET), bullet);
    }

    private static ServerPlayer createPlayer(GameTestHelper helper) {
        ServerPlayer player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "CompatTest"));
        // Подменяется только сеть: урон и модель здоровья обрабатывают настоящие моды.
        player.connection = new ServerGamePacketListenerImpl(helper.getLevel().getServer(),
                new Connection(PacketFlow.SERVERBOUND), player) {
            @Override
            public void send(Packet<?> packet) {
            }
        };
        player.setPos(Vec3.atBottomCenterOf(helper.absolutePos(new net.minecraft.core.BlockPos(1, 1, 1))));
        player.setYRot(0);
        return player;
    }

    private static void assertHealth(GameTestHelper helper, AbstractPlayerDamageModel model,
                                     EnumPlayerPart part, float expected) {
        float actual = model.getFromEnum(part).currentHealth;
        helper.assertTrue(Math.abs(actual - expected) < 0.001f,
                part + ": ожидалось " + expected + ", получено " + actual);
    }
}
