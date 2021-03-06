package syam.flaggame.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.permissions.Permissible;

import syam.flaggame.FlagGame;

public class Actions {
    // Logger
    public static final Logger log = FlagGame.log;
    private static final String logPrefix = FlagGame.logPrefix;
    private static final String msgPrefix = FlagGame.msgPrefix;

    private final FlagGame plugin;

    public Actions(FlagGame plugin) {
        this.plugin = plugin;
    }

    /****************************************/
    // メッセージ送信系関数
    /****************************************/
    /**
     * メッセージをユニキャスト
     * 
     * @param sender
     *            Sender (null可)
     * @param player
     *            Player (null可)l
     * @param message
     *            メッセージ
     */
    public static void message(CommandSender sender, String message) {
        if (message != null && sender != null) {
            message = message.replaceAll("&([0-9a-fk-or])", "\u00A7$1");
            sender.sendMessage(message);
        }
    }

    public static void message(Player player, String message) {
        if (message != null && player != null) {
            message = message.replaceAll("&([0-9a-fk-or])", "\u00A7$1");
            player.sendMessage(message);
        }
    }

    /**
     * メッセージをブロードキャスト
     * 
     * @param message
     *            メッセージ
     */
    public static void broadcastMessage(String message) {
        if (message != null) {
            message = message.replaceAll("&([0-9a-fk-or])", "\u00A7$1");
            // debug(message);//debug
            Bukkit.broadcastMessage(message);
        }
    }

    /**
     * メッセージをワールドキャスト
     * 
     * @param world
     * @param message
     */
    public static void worldcastMessage(World world, String message) {
        if (world != null && message != null) {
            message = message.replaceAll("&([0-9a-fk-or])", "\u00A7$1");
            for (Player player : world.getPlayers()) {
                player.sendMessage(message);
            }
            log.info("[Worldcast][" + world.getName() + "]: " + message);
        }
    }

    /**
     * メッセージをパーミッションキャスト(指定した権限ユーザにのみ送信)
     * 
     * @param permission
     *            受信するための権限ノード
     * @param message
     *            メッセージ
     */
    public static void permcastMessage(String permission, String message) {
        int i = 0;
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            if (player.hasPermission(permission)) {
                Actions.message(player, message);
                i++;
            }
        }
        log.info("Received " + i + "players: " + message);
    }

    /****************************************/
    // ユーティリティ
    /****************************************/
    /**
     * 文字配列をまとめる
     * 
     * @param s
     *            つなげるString配列
     * @param glue
     *            区切り文字 通常は半角スペース
     * @return
     */
    public static String combine(String[] s, String glue) {
        int k = s.length;
        if (k == 0) { return null; }
        StringBuilder out = new StringBuilder();
        out.append(s[0]);
        for (int x = 1; x < k; x++) {
            out.append(glue).append(s[x]);
        }
        return out.toString();
    }

    /**
     * コマンドをコンソールから実行する
     * 
     * @param command
     */
    public static void executeCommandOnConsole(String command) {
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    /**
     * 文字列の中に全角文字が含まれているか判定
     * 
     * @param s
     *            判定する文字列
     * @return 1文字でも全角文字が含まれていればtrue 含まれていなければfalse
     * @throws UnsupportedEncodingException
     */
    public static boolean containsZen(String s) throws UnsupportedEncodingException {
        for (int i = 0; i < s.length(); i++) {
            String s1 = s.substring(i, i + 1);
            if (URLEncoder.encode(s1, "MS932").length() >= 4) { return true; }
        }
        return false;
    }

    /**
     * 現在の日時を yyyy-MM-dd HH:mm:ss 形式の文字列で返す
     * 
     * @return
     */
    public static String getDatetime() {

        Date date = new Date();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return df.format(date);
    }

    /**
     * 座標データを ワールド名:x, y, z の形式の文字列にして返す
     * 
     * @param loc
     * @return
     */
    public static String getLocationString(Location loc) {
        return loc.getWorld().getName() + ":" + loc.getX() + "," + loc.getY() + "," + loc.getZ();
    }

    public static String getBlockLocationString(Location loc) {
        return loc.getWorld().getName() + ":" + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }

    /**
     * デバッグ用 syamnがオンラインならメッセージを送る
     * 
     * @param msg
     */
    public static void debug(String msg) {
        OfflinePlayer syamn = Bukkit.getServer().getOfflinePlayer("syamn");
        if (syamn.isOnline()) {
            Actions.message((Player) syamn, msg);
        }
    }

    /****************************************/
    // FlagGame
    /****************************************/
    /**
     * 真上の指定したブロックとは違うブロックを返す (ドアの上ブロック取得用)
     * 
     * @param block
     *            指定したブロック
     * @return 真上にある違うブロック
     */
    public static Block getTopBlock(final Block block) {
        // 一つ上のブロック
        Block upBlock = block.getRelative(BlockFace.UP);

        if (upBlock.getY() >= 256) return null; // 無限再帰呼び出しの回避
        // 今のブロックと一つ上のブロックが違えば上のブロックを返す
        // 同じなら再帰呼び出しで違うブロックが出るまで繰り返す
        if (upBlock.getType() != block.getType())
            return upBlock;
        else
            return getTopBlock(upBlock);
    }

    /**
     * 周囲の保護看板ブロックを返す
     * 
     * @param block
     *            対象ブロック
     * @return 保護看板があればそのBlock、無ければnull
     */
    public static Block getProtectSign(Block block) {
        Block sign = null;

        // 全方向を走査
        if (isProtectSign(block.getRelative(BlockFace.NORTH), BlockFace.NORTH))
            sign = block.getRelative(BlockFace.NORTH);
        else if (isProtectSign(block.getRelative(BlockFace.EAST), BlockFace.EAST))
            sign = block.getRelative(BlockFace.EAST);
        else if (isProtectSign(block.getRelative(BlockFace.SOUTH), BlockFace.SOUTH))
            sign = block.getRelative(BlockFace.SOUTH);
        else if (isProtectSign(block.getRelative(BlockFace.WEST), BlockFace.WEST)) sign = block.getRelative(BlockFace.EAST);

        return sign;
    }

    /**
     * そのブロックが保護看板か返す
     * 
     * @param signBlock
     *            対象ブロック
     * @param dir
     *            チェック元のブロックから見た方角(BlockFace)
     * @return 保護看板ならtrue、違えばfalse
     */
    private static boolean isProtectSign(Block signBlock, BlockFace dir) {
        // そもそも壁に付いた看板じゃない
        if (signBlock.getType() != Material.WALL_SIGN) return false;

        // 張り付いた方向をチェック
        Byte face = signBlock.getData();
        switch (dir) {
            case NORTH:
                if (face != 4) return false;
                break;
            case EAST:
                if (face != 2) return false;
                break;
            case SOUTH:
                if (face != 5) return false;
                break;
            case WEST:
                if (face != 3) return false;
                break;
            default:
                return false;
        }

        // 看板の1行目チェック
        Sign sign = (Sign) signBlock.getState();
        String text = sign.getLine(0).replaceAll("(?i)\u00A7[0-F]", "").toLowerCase(); // 色文字は無視

        if (text.equals("[private]") || text.equals("[flag]") || text.equals("[team]"))
            return true;
        else
            return false;
    }

    /**
     * 引数の秒を読みやすい形式の文字列に変換して返す
     * 
     * @param sec
     *            正の秒数
     * @return 変換後の文字列
     */
    public static String getTimeString(int sec) {
        // 負数は許容しない
        if (sec < 0) return "0秒";

        // 60秒以下はそのまま返す
        if (sec < 60) return sec + "秒";

        // 60秒で割り切れれば分だけを返す
        if (sec % 60 == 0) return sec / 60 + "分";

        // 当て嵌まらなければ n分n秒 で返す
        int m = sec / 60; // 小数点以下は切られるのでこれで問題ないはず..
        int s = sec % 60;
        return m + "分" + s + "秒";
    }

    /****************************************/
    // 所持金操作系関数 - Vault
    /****************************************/
    /**
     * 指定したユーザーにお金を加える
     * 
     * @param name
     *            ユーザー名
     * @param amount
     *            金額
     * @return 成功ならtrue、失敗ならfalse
     */
    public static boolean addMoney(String name, double amount) {
        if (amount < 0) return false; // 負数は許容しない
        EconomyResponse r = FlagGame.getInstance().getEconomy().depositPlayer(name, amount);
        if (r.transactionSuccess()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 指定したユーザーからお金を引く
     * 
     * @param name
     *            ユーザー名
     * @param amount
     *            金額
     * @return 成功ならtrue、失敗ならfalse
     */
    public static boolean takeMoney(String name, double amount) {
        if (amount < 0) return false; // 負数は許容しない
        EconomyResponse r = FlagGame.getInstance().getEconomy().withdrawPlayer(name, amount);
        if (r.transactionSuccess()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 指定したユーザーがお金を持っているか
     * 
     * @param name
     *            ユーザー名
     * @param amount
     *            金額
     * @return 持っていればtrue、無ければfalse
     */
    public static boolean checkMoney(String name, double amount) {
        return (FlagGame.getInstance().getEconomy().has(name, amount));
    }

    /****************************************/
    /* ログ操作系 */
    /****************************************/
    /**
     * ログファイルに書き込み
     * 
     * @param file
     *            ログファイル名
     * @param line
     *            ログ内容
     */
    public static void log(String filepath, String line) {
        TextFileHandler r = new TextFileHandler(filepath);
        try {
            r.appendLine("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "] " + line);
        } catch (IOException ex) {
        }
    }

    /****************************************/
    /* その他 */
    /****************************************/
    // プレイヤーがオンラインかチェックしてテレポートさせる
    public static void tpPlayer(Player player, Location loc) {
        if (player == null || loc == null || !player.isOnline()) return;
        player.teleport(loc);
    }

    // プレイヤーのインベントリをその場にドロップさせる
    public static void dropInventoryItems(Player player) {
        if (player == null) return;

        // 頭の羊毛ブロックをドロップさせない
        ItemStack helmet = player.getInventory().getHelmet();
        if (helmet != null && helmet.getType() == Material.WOOL) {
            player.getInventory().setHelmet(null);
        }

        PlayerInventory inv = player.getInventory();
        Location loc = player.getLocation();

        // インベントリアイテム
        for (ItemStack i : inv.getContents()) {
            if (i != null && i.getType() != Material.AIR) {
                inv.remove(i);
                player.getWorld().dropItemNaturally(loc, i);
            }
        }

        // 防具アイテム
        for (ItemStack i : inv.getArmorContents()) {
            if (i != null && i.getType() != Material.AIR) {
                inv.remove(i);
                player.getWorld().dropItemNaturally(loc, i);
            }
        }
    }
}
