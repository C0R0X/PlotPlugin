/*
 * Copyright 2022 KCodeYT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ms.kevi.plotplugin.command.other;

import cn.nukkit.Player;
import cn.nukkit.form.element.simple.ButtonImage;
import cn.nukkit.form.element.simple.ElementButton;
import cn.nukkit.form.window.SimpleForm;
import ms.kevi.plotplugin.PlotPlugin;
import ms.kevi.plotplugin.command.PlotCommand;
import ms.kevi.plotplugin.command.SubCommand;
import ms.kevi.plotplugin.lang.TranslationKey;
import ms.kevi.plotplugin.manager.PlotManager;
import ms.kevi.plotplugin.util.BlockEntry;
import ms.kevi.plotplugin.util.Plot;

import java.awt.*;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author Kevims KCodeYT
 * @version 1.0
 */
public class BorderCommand extends SubCommand {

    public BorderCommand(PlotPlugin plugin, PlotCommand parent) {
        super(plugin, parent, "border", "b");
        this.setPermission("plot.command.border");
    }

    @Override
    public boolean execute(Player player, String[] args) {
        final PlotManager plotManager = this.plugin.getPlotManager(player.getLevel());
        final Plot plot;
        if (plotManager == null || (plot = plotManager.getMergedPlot(player.getFloorX(), player.getFloorZ())) == null) {
            player.sendMessage(this.translate(player, TranslationKey.NO_PLOT));
            return false;
        }

        if (!player.hasPermission("plot.command.admin.border") && !plot.isOwner(player.getUniqueId())) {
            player.sendMessage(this.translate(player, TranslationKey.NO_PLOT_OWNER));
            return false;
        }

        final SimpleForm window = new SimpleForm(this.translate(player, TranslationKey.BORDER_FORM_TITLE), "");

        final Map<ElementButton, BlockEntry> buttons = new HashMap<>();
        for (BlockEntry entry : this.plugin.getBorderEntries()) {
            final String text;
            if (entry.isDefault()) {
                text = this.translate(player, TranslationKey.BORDER_RESET_TO_DEFAULT_BUTTON);
            }
            else {
                final boolean hasPerm = entry.getPermission() == null || player.hasPermission(entry.getPermission());
                final String permText = this.translate(player, hasPerm ? TranslationKey.BORDER_BUTTON_HAS_PERM : TranslationKey.BORDER_BUTTON_NO_PERM);

                text = this.translate(player, TranslationKey.BORDER_FORM_BUTTON, entry.getName(), permText);
            }

            final String imageType = entry.getImageType();
            final ButtonImage imageData;
            switch (imageType == null ? "" : imageType.toLowerCase(Locale.ROOT)) {
                case "url" -> imageData = new ButtonImage(ButtonImage.Type.URL, entry.getImageData());
                case "path" -> imageData = new ButtonImage(ButtonImage.Type.PATH, entry.getImageData());
                default -> imageData = null;
            }

            final ElementButton button = new ElementButton(text);
            if (imageData != null) button.image(imageData);

            window.addElement(button);
            buttons.put(button, entry);
        }

        window.onSubmit((ignored, response) -> {
            if (response != null) {
                final ElementButton button = response.button();
                final BlockEntry entry;
                if (button == null || (entry = buttons.get(button)) == null) return;

                if (entry.getPermission() != null && !player.hasPermission(entry.getPermission())) {
                    player.sendMessage(this.translate(player, TranslationKey.BORDER_NO_PERMS, entry.getName()));
                    return;
                }

                if (entry.isDefault()) {
                    for (Plot mergedPlot : plotManager.getConnectedPlots(plot))
                        plotManager.changeBorder(mergedPlot, plotManager.getLevelSettings().getClaimPlotState());
                    player.sendMessage(this.translate(player, TranslationKey.BORDER_RESET_TO_DEFAULT_SUCCESS));
                }
                else {
                    for (Plot mergedPlot : plotManager.getConnectedPlots(plot))
                        plotManager.changeBorder(mergedPlot, entry.getBlockState());
                    player.sendMessage(this.translate(player, TranslationKey.BORDER_SUCCESS, entry.getName()));
                }
            }
        });

        player.sendForm(window);
        return true;
    }

}
