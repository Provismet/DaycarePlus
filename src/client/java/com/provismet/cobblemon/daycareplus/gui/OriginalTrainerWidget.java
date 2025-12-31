package com.provismet.cobblemon.daycareplus.gui;

import com.cobblemon.mod.common.CobblemonSounds;
import com.cobblemon.mod.common.api.gui.GuiUtilsKt;
import com.cobblemon.mod.common.client.CobblemonResources;
import com.cobblemon.mod.common.client.render.RenderHelperKt;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.provismet.cobblemon.daycareplus.DaycarePlusMain;
import com.provismet.cobblemon.daycareplus.config.ClientOptions;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class OriginalTrainerWidget extends ClickableWidget {
    public static final int WIDTH = 71;
    public static final int HEIGHT = 27;

    public static final Identifier TEXTURE = DaycarePlusMain.identifier("textures/gui/pc/ot_panel.png");
    public static final Identifier TEXTURE_HOVERED = DaycarePlusMain.identifier("textures/gui/pc/ot_panel_hovered.png");
    public static final Identifier TEXTURE_COLLAPSED = DaycarePlusMain.identifier("textures/gui/pc/ot_panel_collapsed.png");
    public static final Identifier TEXTURE_COLLAPSED_HOVERED = DaycarePlusMain.identifier("textures/gui/pc/ot_panel_collapsed_hovered.png");

    private static boolean collapsed = false;

    private String trainerName = "";

    public OriginalTrainerWidget(int x, int y) {
        super(x, y, WIDTH, HEIGHT, Text.translatable("daycareplus.ui.original_trainer"));
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        if (trainerName.isEmpty() || !ClientOptions.shouldShowOriginalTrainerInPC()) return;

        Identifier texture;
        if (this.isHovered()) texture = collapsed ? TEXTURE_COLLAPSED_HOVERED : TEXTURE_HOVERED;
        else texture = collapsed ? TEXTURE_COLLAPSED : TEXTURE;

        // Render background
        GuiUtilsKt.blitk(
            context.getMatrices(),
            texture,
            this.getX(), this.getY(),
            HEIGHT, WIDTH
        );

        if (collapsed) return;

        // Render title
        RenderHelperKt.drawScaledText(
            context,
            CobblemonResources.INSTANCE.getDEFAULT_LARGE(),
            Text.translatable("daycareplus.ui.original_trainer").styled(style -> style.withBold(true)),
            this.getX() + WIDTH / 2, this.getY() + 1,
            0.8f,
            1f,
            Integer.MAX_VALUE,
            Colors.WHITE,
            true,
            true,
            mouseX, mouseY
        );

        // Render OT
        RenderHelperKt.drawScaledText(
            context,
            CobblemonResources.INSTANCE.getDEFAULT_LARGE(),
            Text.literal(this.trainerName),
            this.getX() + WIDTH / 2, this.getY() + 11,
            0.8f,
            1f,
            Integer.MAX_VALUE,
            Colors.WHITE,
            true,
            true,
            mouseX, mouseY
        );
    }

    @Override
    protected void appendClickableNarrations (NarrationMessageBuilder builder) {

    }

    @Override
    public void onClick (double mouseX, double mouseY) {
        collapsed = !collapsed;
    }

    public void setPokemon (@Nullable Pokemon pokemon) {
        if (pokemon == null) this.trainerName = "";
        else this.trainerName = pokemon.getOriginalTrainerName();
    }

    @Override
    public void playDownSound (SoundManager soundManager) {
        soundManager.play(PositionedSoundInstance.master(CobblemonSounds.PC_CLICK, 1f));
    }

    @Override
    public boolean isNarratable () {
        return false;
    }

    @Override
    public boolean isHovered () {
        return super.isHovered() && ClientOptions.shouldShowOriginalTrainerInPC();
    }
}
