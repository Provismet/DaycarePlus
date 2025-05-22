package com.provismet.cobblemon.daycareplus.imixin;

import ca.landonjw.gooeylibs2.api.button.ButtonBase;
import com.provismet.cobblemon.daycareplus.breeding.PastureContainer;
import com.provismet.cobblemon.daycareplus.breeding.PastureExtension;

public interface IMixinPastureBlockEntity extends PastureContainer {
    PastureExtension getExtension ();
    void setExtension (PastureExtension extension);

    void setShouldBreed (boolean shouldBreed);
    boolean shouldBreed ();

    void setSkipIntroDialogue (boolean skipIntro);
    boolean shouldSkipIntro ();

    void setShouldSkipDaycareGUI (boolean skipGUI);
    boolean shouldSkipDaycareGUI ();

    ButtonBase getEggCounterButton ();
}
