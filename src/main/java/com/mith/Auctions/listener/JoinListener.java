package com.mith.Auctions.listener;

import com.mith.Auctions.manager.AuctionsPlayerManager;
import com.mith.Auctions.object.AuctionsPlayer;
import com.mith.Auctions.util.RewardUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class JoinListener implements Listener {

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		UUID id = p.getUniqueId();

		AuctionsPlayerManager.getInstance().createPlayer(id);

		AuctionsPlayer ap = AuctionsPlayerManager.getInstance().getPlayer(id);

		if (ap.getOfflineItems().isEmpty())
			return;

		RewardUtil.rewardOffline(ap);
	}
}
