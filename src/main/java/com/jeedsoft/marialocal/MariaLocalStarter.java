package com.jeedsoft.marialocal;

import java.text.DecimalFormat;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.jeedsoft.marialocal.util.StringUtil;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;

/**
 * Start an MariaDB instance through MariaDB4j
 * 
 * Usage:
 * java -cp "lib/*" com.jeedsoft.marialocal.MariaLocalStarter -p=3306 -d=data -c=db1,db2,db3
 */
public class MariaLocalStarter
{
    private static final int MESSAGE_WIDTH = 48;
    
    public static void main(String[] args) throws ManagedProcessException, ParseException
    {
        long t1 = System.currentTimeMillis();
        Model model = parse(args);
        if (model == null) {
            return;
        }
        System.out.println("port: " + model.port);
        System.out.println("dataDir: " + model.dataDir);
        if (model.createDatabases != null) {
            System.out.println("createDatabases: " + StringUtil.join(model.createDatabases));
        }
        DBConfigurationBuilder config = MariaLocalManager.config(model.port, model.dataDir);
        DB db = DB.newEmbeddedDB(config.build());
        db.start();
        if (model.createDatabases != null) {
            for (String dbName: model.createDatabases) {
                db.createDB(dbName);
            }
        }
        long t2 = System.currentTimeMillis();
        String timeCost = new DecimalFormat("#,##0").format(t2 - t1);
        
        // print startup message
        System.out.println();
        System.out.println("    *" + repeat('-', MESSAGE_WIDTH) + "*");
        System.out.println("    " + pad("MariaDB startup in " + timeCost + " ms (port=" + model.port + ")"));
        System.out.println("    " + pad("Press CTRL+C to shutdown MariaDB."));
        System.out.println("    *" + repeat('-', MESSAGE_WIDTH) + "*");
        System.out.println();
    }
    
    private static String pad(String msg)
    {
        int paddingRight = Math.max(1, MESSAGE_WIDTH - msg.length() - 1);
        return "| " + msg + repeat(' ', paddingRight) + "|";
    }
    
    private static Model parse(String[] args) throws ParseException
    {
        Options options = new Options();
        options.addOption(Option.builder("p")
                .longOpt("port")
                .hasArg()
                .argName("PORT")
                .desc("server port, default '3306'")
                .build());
        options.addOption(Option.builder("d")
                .longOpt("data-dir")
                .hasArg()
                .argName("DATA_DIRECTORY")
                .desc("data directory, default 'data'")
                .build());
        options.addOption(Option.builder("c")
                .longOpt("create-database")
                .hasArg()
                .argName("DATABASE_NAME_TO_CREATE")
                .desc("which database(s) to be created on startup")
                .build());
        
        if (args.length == 1 && "?".equals(args[0])) {
            new HelpFormatter().printHelp("start", options, true);
            return null;
        }
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        Model model = new Model();
        model.port = Integer.parseInt(cmd.getOptionValue("p", "3306"));
        model.dataDir = cmd.getOptionValue("d", "data");
        String database = cmd.getOptionValue("c", "");
        if (!database.isEmpty()) {
            model.createDatabases = database.split(",");
        }
        return model;
    }
    
    private static class Model
    {
        private int port;
        
        private String dataDir;
        
        private String[] createDatabases;
    }
    
    private static String repeat(char c, int count)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; ++i) {
            sb.append(c);
        }
        return sb.toString();
    }
}
