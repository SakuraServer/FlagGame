package syam.FlagGame.Command;

import syam.FlagGame.Actions;
import syam.FlagGame.Game.Game;

public class SaveCommand extends BaseCommand{
	public SaveCommand(){
		bePlayer = false;
		name = "save";
		argLength = 0;
		usage = "<- save map data";
	}

	@Override
	public boolean execute() {
		// データ保存
		plugin.getManager().saveGames();

		Actions.message(sender, null, "&aGames Saved!");
		return true;
	}

	@Override
	public boolean permission() {
		return sender.hasPermission("flag.admin.save");
	}
}