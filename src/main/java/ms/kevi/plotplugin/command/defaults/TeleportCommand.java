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

package ms.kevi.plotplugin.command.defaults;

import cn.nukkit.Player;
import cn.nukkit.form.element.simple.ElementButton;
import cn.nukkit.form.window.SimpleForm;
import ms.kevi.plotplugin.PlotPlugin;
import ms.kevi.plotplugin.command.PlotCommand;
import ms.kevi.plotplugin.command.SubCommand;
import ms.kevi.plotplugin.lang.TranslationKey;
import ms.kevi.plotplugin.manager.PlotManager;

import java.util.Map;

/**
 * @author Kevims KCodeYT
 * @version 1.0
 */
public class TeleportCommand extends SubCommand {

    public TeleportCommand(PlotPlugin plugin, PlotCommand parent) {
        super(plugin, parent, "teleport", "tp");
        this.setPermission("plot.command.admin.teleport");
    }

    @Override
    public boolean execute(Player player, String[] args) {
        final Map<String, PlotManager> plotManagers = this.plugin.getPlotManagerMap();
        final SimpleForm window = new SimpleForm(this.translate(player, TranslationKey.TELEPORT_FORM_TITLE), "");

        for (String levelName : plotManagers.keySet())
            window.addElement(new ElementButton(levelName));

        window.onSubmit((ignored, response) -> {
            if (response != null) {
                final PlotManager plotManager = plotManagers.get(response.button().text());
                if (plotManager == null) return;

                player.teleport(plotManager.getLevel().getSpawnLocation());
                player.sendMessage(this.translate(player, TranslationKey.TELEPORT_SUCCESS, plotManager.getLevel().getFolderPath()));
            }
        });

        player.sendForm(window);
        return true;
    }

}
