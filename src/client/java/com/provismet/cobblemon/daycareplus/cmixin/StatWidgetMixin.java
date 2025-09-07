package com.provismet.cobblemon.daycareplus.cmixin;

import com.cobblemon.mod.common.api.gui.GuiUtilsKt;
import com.cobblemon.mod.common.client.CobblemonResources;
import com.cobblemon.mod.common.client.gui.summary.widgets.SoundlessWidget;
import com.cobblemon.mod.common.client.gui.summary.widgets.screens.stats.StatWidget;
import com.cobblemon.mod.common.client.render.RenderHelperKt;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.provismet.cobblemon.daycareplus.feature.BreedableProperty;
import com.provismet.cobblemon.daycareplus.util.ClientEggGroup;
import com.provismet.cobblemon.daycareplus.util.DPResources;
import com.provismet.cobblemon.daycareplus.config.ClientOptions;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Locale;

@Mixin(StatWidget.class)
public abstract class StatWidgetMixin extends SoundlessWidget {
    public StatWidgetMixin (int pX, int pY, int pWidth, int pHeight, @NotNull Text component) {
        super(pX, pY, pWidth, pHeight, component);
    }

    @Shadow @Final
    private Pokemon pokemon;

    @Inject(
        method = "renderWidget",
        at = @At(
            value = "INVOKE",
            target = "Lcom/cobblemon/mod/common/client/gui/summary/widgets/screens/stats/StatWidget;drawFullness(IILnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/gui/DrawContext;Lcom/cobblemon/mod/common/pokemon/Pokemon;)V",
            shift = At.Shift.AFTER
        )
    )
    private void drawEggGroups (DrawContext context, int pMouseX, int pMouseY, float pPartialTicks, CallbackInfo info, @Local(name = "drawY") LocalIntRef drawY, @Local(name = "matrices") MatrixStack matrices) {
        if (!ClientOptions.shouldShowEggGroupsFeature()) return;

        int moduleX = this.getX() + 5;
        int moduleY = drawY.get() + 30;

        GuiUtilsKt.blitk(
            matrices,
            DPResources.EGG_GROUP_STAT_BACKGROUND,
            moduleX,
            moduleY,
            28,
            124
        );

        RenderHelperKt.drawScaledText(
            context,
            CobblemonResources.INSTANCE.getDEFAULT_LARGE(),
            Text.translatable("daycareplus.ui.egg_group").styled(style -> style.withBold(true)),
            moduleX + 62,
            moduleY + 2.5f,
            1f,
            1f,
            Integer.MAX_VALUE,
            Colors.WHITE,
            true,
            true,
            pMouseX,
            pMouseY
        );

        MutableText eggGroups;
        if (BreedableProperty.get(this.pokemon)) {
            eggGroups = ClientEggGroup.getGroups(this.pokemon)
                .stream()
                .map(group -> Text.translatable("daycareplus.group." + group.name().toLowerCase(Locale.ROOT)))
                .reduce(Text.empty(), (existingText, groupName) -> {
                    if (existingText.getString().isEmpty()) return groupName;
                    return existingText.append(" - ").append(groupName);
                });
        }
        else {
            eggGroups = Text.translatable("property.daycareplus.unbreedable");
        }

        RenderHelperKt.drawScaledText(
            context,
            CobblemonResources.INSTANCE.getDEFAULT_LARGE(),
            eggGroups,
            moduleX + 62,
            moduleY + 16,
            1f,
            1f,
            Integer.MAX_VALUE,
            Colors.WHITE,
            true,
            true,
            pMouseX,
            pMouseY
        );

        drawY.set(drawY.get() + 30);
    }
}
