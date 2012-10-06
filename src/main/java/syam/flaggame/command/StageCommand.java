/**
 * FlagGame - Package: syam.flaggame.command
 * Created: 2012/09/29 17:41:47
 */
package syam.flaggame.command;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import syam.flaggame.command.queue.Queueable;
import syam.flaggame.enums.config.Configables;
import syam.flaggame.game.Stage;
import syam.flaggame.manager.SetupManager;
import syam.flaggame.manager.StageManager;
import syam.flaggame.permission.Perms;
import syam.flaggame.util.Actions;
import syam.flaggame.util.Util;

/**
 * StageCommand (StageCommand.java)
 * @author syam(syamn)
 */
public class StageCommand extends BaseCommand implements Queueable{
	public StageCommand(){
		bePlayer = true;
		name = "stage";
		argLength = 0;
		usage = "<action> [stage] <- management stages";
	}

	@Override
	public void execute() {
		// サブ引数なし
		if (args.size() <= 0){
			sendAvailableAction();
			return;
		}

		// アクション取得
		stageAction action = null;
		for (stageAction check : stageAction.values()){
			if (check.name().equalsIgnoreCase(args.get(0))){
				action = check;
				break;
			}
		}
		if (action == null){
			sendAvailableAction();
			return;
		}

		// アクションによって処理を分ける
		switch (action){
			case CREATE:
				if (checkPerm(Perms.CREATE)) { create(); }
				return;
			case DELETE:
				if (checkPerm(Perms.DELETE)) { delete(); }
				return;
			case ROLLBACK:
				if (checkPerm(Perms.ROLLBACK)) { rollback(); }
				return;

			// 定義漏れ
			default:
				Actions.message(sender, null, "&アクションが不正です 開発者にご連絡ください");
				log.warning(logPrefix+ "Undefined action: "+action.name()+"! Please report this!");
				break;
		}
	}

	/* ***** ここから各アクション関数 ****************************** */

	private void create(){
		if (args.size() <= 1){
			Actions.message(sender, null, "&cステージ名を指定してください！");
			return;
		}

		// random拒否
		if(args.get(1).equalsIgnoreCase("random") || args.get(1).startsWith("-")){
			Actions.message(sender, null, "&cこのステージ名は使用できません！");
			return;
		}

		Stage stage = StageManager.getStage(args.get(1));
		if (stage != null){
			Actions.message(sender, null, "&cそのステージ名は既に存在します！");
			return;
		}

		// 新規ゲーム登録
		stage = new Stage(plugin, args.get(1));
		stage.setAvailable(false);
		SetupManager.setSelectedStage(player, stage);

		// update dynmap, save stage
		plugin.getDynmap().updateRegions();
		plugin.getFileManager().saveStages();

		Actions.message(sender, null, "&a新規ステージ'"+stage.getName()+"'を登録して選択しました！");
	}

	private void delete(){
		if (args.size() <= 1){
			Actions.message(sender, null, "&cステージ名を入力してください！");
			return;
		}
		Stage stage = StageManager.getStage(args.get(1));
		if (stage == null){
			Actions.message(sender, null, "&cその名前のステージは存在しません！");
			return;
		}

		if (stage.isUsing()){
			Actions.message(sender, null, "&cそのステージは現在受付中または開始中のため削除できません");
			return;
		}

		// confirmキュー追加
		plugin.getQueue().addQueue(sender, this, args, 10);
		Actions.message(sender, null, "&dステージ'&7"+args.get(1)+"&d'を削除しようとしています！");
		Actions.message(sender, null, "&d続行するには &a/flag confirm &dコマンドを入力してください！");
		Actions.message(sender, null, "&a/flag confirm &dコマンドは10秒間のみ有効です。");
	}

	private void rollback(){
		if (args.size() <= 1){
			Actions.message(sender, null, "&cステージ名または -all を指定してください！");
			return;
		}
		boolean all = false;
		if (args.get(1).equalsIgnoreCase("-all")){
			all = true;
		}

		if (!all){
			Stage stage = StageManager.getStage(args.get(1));
			if (stage == null){
				Actions.message(sender, null, "&cその名前のステージは存在しません！");
				return;
			}

			if (stage.isUsing()){
				Actions.message(sender, null, "&cそのステージは現在使用中のためロールバックできません！");
				return;
			}

			// ステージロールバック
			stage.rollbackFlags();
			stage.rollbackChests(sender);

			Actions.message(sender, null, "&aステージ'"+stage.getName()+"'をロールバックしました！");

		}else{
			int i = 0;

			for (Stage stage : StageManager.getStages().values()){
				if (stage.isUsing()) continue;

				// ステージロールバック
				stage.rollbackFlags();
				stage.rollbackChests(sender);
				i++;
			}

			Actions.message(sender, null, "&a全"+i+"ステージをロールバックしました！");
		}
	}


	/* ***** ここまで ********************************************** */
	/**
	 * キュー実行処理
	 */
	@Override
	public void executeQueue(List<String> args) {
		if (stageAction.DELETE.name().equalsIgnoreCase(args.get(0))){
			if (args.size() <= 1){
				Actions.message(sender, null, "&cステージ名が不正です");
				return;
			}
			Stage stage = StageManager.getStage(args.get(1));
			if (stage == null){
				Actions.message(sender, null, "&cその名前のステージは存在しません！");
				return;
			}

			if (stage.isUsing()){
				Actions.message(sender, null, "&cそのステージは現在受付中または開始中のため削除できません");
				return;
			}

			// ステージロールバック
			stage.rollbackFlags();
			stage.rollbackChests();

			// ゲームリストから削除
			StageManager.removeStage(args.get(0));

			// ゲームデータファイルを削除
			String fileDir = plugin.getDataFolder() + System.getProperty("file.separator") + "stageData";
			boolean deleted = false;
			try{
				File file = new File(fileDir + System.getProperty("file.separator") + stage.getFileName());
				if (file.exists()){
					deleted = file.delete();
				}
			}catch (Exception ex){
				deleted = false;
				ex.printStackTrace();
			}

			if (!deleted){
				Actions.message(sender, null, "&cステージ'"+args.get(1)+"'のデータファイル削除中にエラーが発生しました！");
			}else{
				Actions.message(sender, null, "&aステージ'"+args.get(1)+"'を削除しました！");
				plugin.getDynmap().updateRegions();
			}
		}else{
			Actions.message(sender, null, "&c内部エラーが発生しました。開発者までご連絡ください。");
			log.warning(logPrefix+sender.getName() + " send invalid queue! (StageCommand.class)");
		}
	}

	/**
	 * アクションごとの権限をチェックする
	 * @param perm Perms
	 * @return bool
	 */
	private boolean checkPerm(Perms perm){
		if (perm.has(sender)){
			return true;
		}else{
			Actions.message(sender, null, "&cこのアクションを実行する権限がありません！");
			return false;
		}
	}

	/**
	 * 指定可能なステージアクション
	 * stageAction (StageCommand.java)
	 * @author syam(syamn)
	 */
	enum stageAction{
		CREATE,
		DELETE,
		ROLLBACK,
		;
	}

	/**
	 * 指定可能なアクションをsenderに送信する
	 */
	private void sendAvailableAction(){
		List<String> col = new ArrayList<String>();
		for (stageAction action : stageAction.values()){
			col.add(action.name());
		}
		Actions.message(sender, null, "&cそのアクションは存在しません！");
		Actions.message(sender, null, "&6 " + Util.join(col, "/").toLowerCase());
	}

	@Override
	public boolean permission() {
		return (Perms.CREATE.has(sender) ||
				Perms.DELETE.has(sender) ||
				Perms.ROLLBACK.has(sender));
	}
}
