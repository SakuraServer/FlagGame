package syam.flaggame;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigurationManager {
    // Logger
    public static final Logger log = FlagGame.log;
    private static final String logPrefix = FlagGame.logPrefix;
    private static final String msgPrefix = FlagGame.msgPrefix;

    private JavaPlugin plugin;
    private FileConfiguration conf;

    private static File pluginDir = new File("plugins", "FlagGame");

    // デフォルトの設定定数
    private final String defaultLogPath = "plugins/FlagGame/game.log";
    private final String defaultWorldName = "flag";
    private final List<String> defaultDisableCommands = new ArrayList<String>() {
        {
            add("/spawn");
            add("/home");
            add("/setspawn");
        }
    };
    private final String defaultDetailDirectory = "plugins/FlagGame/detail/";
    private final List<String> defaultPermissions = new ArrayList<String>() {
        {
            add("vault");
            add("pex");
            add("superperms");
            add("ops");
        }
    };

    // 設定項目
    /* Basic Configs */
    private int toolID = new Integer(269);
    private String gameWorld = defaultWorldName;
    private boolean isProtected = new Boolean(true);
    private boolean isDebug = new Boolean(false);
    private boolean useDynmap = new Boolean(false);
    /* Games Configs */
    private int startCountdownInSec = new Integer(10);
    private boolean useFlagEffects = new Boolean(true);
    private boolean deathWhenLogout = new Boolean(true);
    private boolean disableRegainHP = new Boolean(true);
    private boolean disableTeamPVP = new Boolean(true);
    private int godModeTime = new Integer(4);
    private List<String> disableCommands = defaultDisableCommands;
    /* MySQL Configs */
    private String mysqlAddress = "localhost";
    private int mysqlPort = 3306;
    private String mysqlDBName = "DatabaseName";
    private String mysqlUserName = "UserName";
    private String mysqlUserPass = "UserPassword";
    private String mysqlTablePrefix = "flaggame_";
    /* Logging Configs */
    private String detailDirectory = defaultDetailDirectory;
    /* Permissions Configs */
    private List<String> permissions = defaultPermissions;

    /**
     * コンストラクタ
     * 
     * @param plugin
     */
    public ConfigurationManager(final JavaPlugin plugin) {
        this.plugin = plugin;
        pluginDir = this.plugin.getDataFolder();
    }

    /**
     * 設定をファイルから読み込む
     * 
     * @param initialLoad
     *            初回ロードかどうか
     */
    public void loadConfig(boolean initialLoad) throws Exception {
        // ディレクトリ作成
        createDirs();

        // 設定ファイルパス取得
        File file = new File(pluginDir, "config.yml");
        // 無ければデフォルトコピー
        if (!file.exists()) {
            extractResource("/config.yml", pluginDir, false, true);
            log.info(logPrefix + "config.yml is not found! Created default config.yml!");
        }

        plugin.reloadConfig();

        // Check config.yml version
        checkver(plugin.getConfig().getDouble("Version", 0.1D));

        /* Basic Configs */
        toolID = plugin.getConfig().getInt("ToolID", 269);
        gameWorld = plugin.getConfig().getString("WorldName", defaultWorldName);
        isProtected = plugin.getConfig().getBoolean("WorldProtect", true);
        isDebug = plugin.getConfig().getBoolean("Debug", false);
        useDynmap = plugin.getConfig().getBoolean("UseDynmap", false);
        /* Games Configs */
        startCountdownInSec = plugin.getConfig().getInt("StartCountdownInSec", 10);
        useFlagEffects = plugin.getConfig().getBoolean("UseFlagEffects", true);
        deathWhenLogout = plugin.getConfig().getBoolean("DeathWhenLogout", true);
        disableRegainHP = plugin.getConfig().getBoolean("DisableRegainHealth", true);
        disableTeamPVP = plugin.getConfig().getBoolean("DisableTeamPVP", true);
        godModeTime = plugin.getConfig().getInt("RespawnGodModeTime", 4);
        disableCommands = plugin.getConfig().getStringList("DisableCommands");
        /* MySQL Configs */
        mysqlAddress = plugin.getConfig().getString("MySQL.Server.Address", "localhost");
        mysqlPort = plugin.getConfig().getInt("MySQL.Server.Port", 3306);
        mysqlDBName = plugin.getConfig().getString("MySQL.Database.Name", "DatabaseName");
        mysqlUserName = plugin.getConfig().getString("MySQL.Database.User_Name", "Username");
        mysqlUserPass = plugin.getConfig().getString("MySQL.Database.User_Password", "UserPassword");
        mysqlTablePrefix = plugin.getConfig().getString("MySQL.Database.TablePrefix", "flaggame_");
        /* Logging Configs */
        detailDirectory = plugin.getConfig().getString("DetailDirectory", defaultDetailDirectory);
        /* Permissions Configs */
        if (plugin.getConfig().get("Permissions") != null) {
            permissions = plugin.getConfig().getStringList("Permissions");
        } else {
            permissions = defaultPermissions;
        }

        // ワールドチェック 見つからなければプラグイン無効化
        if (Bukkit.getWorld(gameWorld) == null) {
            log.warning(logPrefix + "World " + gameWorld + " is Not Found! Disabling plugin..");
            plugin.getPluginLoader().disablePlugin(plugin);
            return;
        }

        // 詳細ログ用ディレクトリ作成
        createDir(new File(detailDirectory));
    }

    // 設定 getter ここから

    /* Basic Configs */
    public int getToolID() {
        return this.toolID;
    }

    public String getGameWorld() {
        return this.gameWorld;
    }

    public boolean isProtected() {
        return this.isProtected;
    }

    public boolean isDebug() {
        return this.isDebug;
    }

    public boolean getUseDynmap() {
        return this.useDynmap;
    }

    /* Games Configs */
    public int getStartCountdownInSec() {
        return this.startCountdownInSec;
    }

    public boolean getUseFlagEffects() {
        return this.useFlagEffects;
    }

    public boolean getDeathWhenLogout() {
        return this.deathWhenLogout;
    }

    public boolean getDisableRegainHP() {
        return this.disableRegainHP;
    }

    public boolean getDisableTeamPVP() {
        return this.disableTeamPVP;
    }
    
    public int getGodModeTime(){
        return this.godModeTime;
    }

    public List<String> getDisableCommands() {
        return this.disableCommands;
    }

    /* MySQL Configs */
    public String getMySQLaddress() {
        return this.mysqlAddress;
    }

    public int getMySQLport() {
        return this.mysqlPort;
    }

    public String getMySQLdbname() {
        return this.mysqlDBName;
    }

    public String getMySQLusername() {
        return this.mysqlUserName;
    }

    public String getMySQLuserpass() {
        return this.mysqlUserPass;
    }

    public String getMySQLtablePrefix() {
        return this.mysqlTablePrefix;
    }

    /* Logging Configs */
    public String getDetailDirectory() {
        return this.detailDirectory;
    }

    /* Permissions Configs */
    public List<String> getPermissions() {
        return this.permissions;
    }

    // 設定 getter ここまで

    /**
     * 設定ファイルに設定を書き込む (コメントが消えるため使わない)
     * 
     * @throws Exception
     */
    @Deprecated
    public void save() throws Exception {
        plugin.saveConfig();
    }

    /**
     * 必要なディレクトリ群を作成する
     */
    private void createDirs() {
        createDir(plugin.getDataFolder());
    }

    /**
     * 存在しないディレクトリを作成する
     * 
     * @param dir
     *            File 作成するディレクトリ
     */
    private static void createDir(File dir) {
        // 既に存在すれば作らない
        if (dir.isDirectory()) { return; }
        if (!dir.mkdir()) {
            log.warning(logPrefix + "Can't create directory: " + dir.getName());
        }
    }

    /**
     * 設定ファイルのバージョンをチェックする
     * 
     * @param ver
     */
    private void checkver(final double ver) {
        double configVersion = ver; // 設定ファイルのバージョン
        double nowVersion = 0.1D; // プラグインのバージョン

        String versionString = plugin.getDescription().getVersion();
        try {
            // Support maven and Jenkins build number
            int index = versionString.indexOf("-");
            if (index > 0) {
                versionString = versionString.substring(0, index);
            }
            nowVersion = Double.parseDouble(versionString);
        } catch (NumberFormatException ex) {
            log.warning(logPrefix + "Cannot parse version string!");
        }

        // 比較 設定ファイルのバージョンが古ければ config.yml を上書きする
        if (configVersion < nowVersion) {
            // 先に古い設定ファイルをリネームする
            String destName = "oldconfig-v" + configVersion + ".yml";
            String srcPath = new File(plugin.getDataFolder(), "config.yml").getPath();
            String destPath = new File(plugin.getDataFolder(), destName).getPath();
            try {
                copyTransfer(srcPath, destPath);
                log.info(logPrefix + "Copied old config.yml to " + destName + "!");
            } catch (Exception ex) {
                log.warning(logPrefix + "Cannot copy old config.yml!");
            }

            // config.ymlと言語ファイルを強制コピー
            extractResource("/config.yml", plugin.getDataFolder(), true, false);

            log.info(logPrefix + "Deleted existing configuration file and generate a new one!");
        }
    }

    /**
     * リソースファイルをファイルに出力する
     * 
     * @param from
     *            出力元のファイルパス
     * @param to
     *            出力先のファイルパス
     * @param force
     *            jarファイルの更新日時より新しいファイルが既にあっても強制的に上書きするか
     * @param checkenc
     *            出力元のファイルを環境によって適したエンコードにするかどうか
     * @author syam
     */
    static void extractResource(String from, File to, boolean force, boolean checkenc) {
        File of = to;

        // ファイル展開先がディレクトリならファイルに変換、ファイルでなければ返す
        if (to.isDirectory()) {
            String filename = new File(from).getName();
            of = new File(to, filename);
        } else if (!of.isFile()) {
            log.warning(logPrefix + "not a file:" + of);
            return;
        }

        // ファイルが既に存在する場合は、forceフラグがtrueでない限り展開しない
        if (of.exists() && !force) { return; }

        OutputStream out = null;
        InputStream in = null;
        InputStreamReader reader = null;
        OutputStreamWriter writer = null;
        DataInputStream dis = null;
        try {
            // jar内部のリソースファイルを取得
            URL res = FlagGame.class.getResource(from);
            if (res == null) {
                log.warning(logPrefix + "Can't find " + from + " in plugin Jar file");
                return;
            }
            URLConnection resConn = res.openConnection();
            resConn.setUseCaches(false);
            in = resConn.getInputStream();

            if (in == null) {
                log.warning(logPrefix + "Can't get input stream from " + res);
            } else {
                // 出力処理 ファイルによって出力方法を変える
                if (checkenc) {
                    // 環境依存文字を含むファイルはこちら環境

                    reader = new InputStreamReader(in, "UTF-8");
                    writer = new OutputStreamWriter(new FileOutputStream(of)); // 出力ファイルのエンコードは未指定
                                                                               // =
                                                                               // 自動で変わるようにする

                    int text;
                    while ((text = reader.read()) != -1) {
                        writer.write(text);
                    }
                } else {
                    // そのほか

                    out = new FileOutputStream(of);
                    byte[] buf = new byte[1024]; // バッファサイズ
                    int len = 0;
                    while ((len = in.read(buf)) >= 0) {
                        out.write(buf, 0, len);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            // 後処理
            try {
                if (out != null) out.close();
                if (in != null) in.close();
                if (reader != null) reader.close();
                if (writer != null) writer.close();
            } catch (Exception ex) {
            }
        }
    }

    /**
     * コピー元のパス[srcPath]から、コピー先のパス[destPath]へファイルのコピーを行います。
     * コピー処理にはFileChannel#transferToメソッドを利用します。 コピー処理終了後、入力・出力のチャネルをクローズします。
     * 
     * @param srcPath
     *            コピー元のパス
     * @param destPath
     *            コピー先のパス
     * @throws IOException
     *             何らかの入出力処理例外が発生した場合
     */
    public static void copyTransfer(String srcPath, String destPath) throws IOException {
        FileChannel srcChannel = new FileInputStream(srcPath).getChannel();
        FileChannel destChannel = new FileOutputStream(destPath).getChannel();
        try {
            srcChannel.transferTo(0, srcChannel.size(), destChannel);
        } finally {
            srcChannel.close();
            destChannel.close();
        }
    }

    public static File getJarFile() {
        return new File("plugins", "FlagGame.jar");
    }
}
