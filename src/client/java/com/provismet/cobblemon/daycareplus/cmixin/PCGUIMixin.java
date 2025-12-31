package com.provismet.cobblemon.daycareplus.cmixin;

import com.cobblemon.mod.common.client.gui.pc.PCGUI;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.provismet.cobblemon.daycareplus.config.ClientOptions;
import com.provismet.cobblemon.daycareplus.gui.EggGroupWidget;
import com.provismet.cobblemon.daycareplus.gui.OriginalTrainerWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PCGUI.class)
public abstract class PCGUIMixin extends Screen {
    protected PCGUIMixin (Text title) {
        super(title);
    }

    @Shadow
    private Pokemon previewPokemon;

    @Unique
    private final EggGroupWidget eggGroupWidget = new EggGroupWidget(0, 0);

    @Unique
    private final OriginalTrainerWidget originalTrainerWidget = new OriginalTrainerWidget(0, 0);

    @Inject(method = "init", at = @At("TAIL"))
    private void addEggWidget (CallbackInfo info) {
        this.addDrawableChild(this.eggGroupWidget);
        this.addDrawableChild(this.originalTrainerWidget);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void renderEggGroups (DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo info) {
        this.eggGroupWidget.setX((super.width - PCGUI.BASE_WIDTH) / 2 - EggGroupWidget.WIDTH + 2);
        this.eggGroupWidget.setY(super.height / 2 + ClientOptions.getPcEggGroupPanelYOffset());
        this.eggGroupWidget.setPokemon(this.previewPokemon);

        this.originalTrainerWidget.setX((super.width - PCGUI.BASE_WIDTH) / 2 + 3);
        this.originalTrainerWidget.setY((super.height + PCGUI.BASE_HEIGHT) / 2 - 4);
        this.originalTrainerWidget.setPokemon(this.previewPokemon);
    }
}
