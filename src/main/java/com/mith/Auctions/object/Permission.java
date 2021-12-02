package com.mith.Auctions.object;

public enum Permission {

	COMMAND_BASE("auction"),
	COMMAND_BLACKLIST("auction.blacklist"),
	COMMAND_AVERAGE("auction.average"),
	COMMAND_START("auction.start"),
	COMMAND_START_SEALED("auction.start.sealed"),
	COMMAND_CANCEL("auction.cancel"),
	COMMAND_CANCEL_OTHERS("auction.cancel.others"),
	COMMAND_INFO("auction.info"),
	COMMAND_QUEUE("auction.queue"),
	COMMAND_REMOVE("auction.remove"),
	COMMAND_SPAM("auction.spam"),
	COMMAND_IGNORE("auction.ignore"),
	COMMAND_IGNORE_PLAYER("auction.ignore.player"),
	COMMAND_IMPOUND("auction.impound"),
	COMMAND_ENABLE("auction.enable"),
	COMMAND_DISABLE("auction.disable"),
	COMMAND_RELOAD("auction.reload"),
	COMMAND_BID("bid");


	private String permission;

	Permission(String permission) {
		this.permission = "auctions." + permission;
	}

	@Override
	public String toString() {
		return permission;
	}
}
