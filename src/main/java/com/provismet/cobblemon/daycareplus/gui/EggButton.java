package com.provismet.cobblemon.daycareplus.gui;

import ca.landonjw.gooeylibs2.api.button.ButtonAction;
import ca.landonjw.gooeylibs2.api.button.ButtonBase;
import net.minecraft.component.ComponentType;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class EggButton extends ButtonBase {
    private final Consumer<ButtonAction> onClick;

    protected EggButton (@NotNull ItemStack display, Consumer<ButtonAction> onClick) {
        super(display);
        this.onClick = onClick;
    }

    @Override
    public void onClick(@NotNull ButtonAction action) {
        if (onClick != null) onClick.accept(action);
    }

    public static Builder builder () {
        return new Builder();
    }

    public static class Builder {

        protected ItemStack display;
        protected Consumer<ButtonAction> onClick;

        public Builder display (@NotNull ItemStack display) {
            this.display = display;
            return this;
        }

        public <T> Builder with (ComponentType<T> type, T value) {
            this.display.set(type, value);
            return this;
        }

        public Builder onClick (@Nullable Consumer<ButtonAction> behaviour) {
            this.onClick = behaviour;
            return this;
        }

        public Builder onClick (@Nullable Runnable behaviour) {
            this.onClick = (behaviour != null) ? (action) -> behaviour.run() : null;
            return this;
        }

        public EggButton build() {
            this.validate();
            return new EggButton(this.display, onClick);
        }

        protected void validate() {
            if (display == null) throw new IllegalStateException("button display must be defined");
        }

    }
}
