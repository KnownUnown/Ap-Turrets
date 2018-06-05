package com.snowleopard1863.APTurrets.integration;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultIntegration implements Integration {
	private Economy economy;

	@Override
	public void initialize() throws UnsupportedOperationException {
		RegisteredServiceProvider<Economy> economyService =
				Bukkit.getServicesManager().getRegistration(Economy.class);
		if(economyService == null)
			throw new UnsupportedOperationException("Economy service not found");

		economy = economyService.getProvider();
	}

	public boolean withdrawFunds(Player player, double amount) {
		return economy.withdrawPlayer(player, amount).transactionSuccess();
	}

	@Override
	public String getRequiredPlugin() {
		return "Vault";
	}
}
