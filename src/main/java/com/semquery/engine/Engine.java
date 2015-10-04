package com.semquery.engine;

import com.semquery.engine.analyze.Indexer;
import com.semquery.engine.analyze.QueryManager;
import com.semquery.engine.analyze.QueryValidator;
import com.semquery.engine.parsers.ECMAScriptLexer;
import com.semquery.engine.parsers.ECMAScriptParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;

import java.io.InputStream;
import java.util.List;

public class Engine {


    public static void main(String[] args) throws Exception {

        /*
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream in = loader.getResourceAsStream("test.js");
        ANTLRInputStream antlrIn = new ANTLRInputStream(in);
        ECMAScriptLexer lex = new ECMAScriptLexer(antlrIn);
        TokenStream ts = new CommonTokenStream(lex);
        ECMAScriptParser parser = new ECMAScriptParser(ts);

        System.out.println(parser.program().toStringTree(parser));
        */

        /*
        if (args.length < 2) {
            System.err.println("Expected >= 2 args");
            System.exit(1);
        }*/

        String cmd = "index"; //args[0];
        if (cmd.equals("index")) {
            new Indexer("", "");
        } else if (cmd.equals("validate")) {
            String query = args[1];
            QueryValidator qv = new QueryValidator(query);
            System.out.println(qv.validate());
        } else if (cmd.equals("query")) {
            String query = args[1];
            String repo = args[2];

            QueryManager qm = new QueryManager(query, repo);
            List<QueryManager.Result> results = qm.getResults(0, 20);
            int count = 1;
            for (QueryManager.Result res : results) {
                if (count >= 20) break;
                System.out.println(res.file + "," + res.itv.a + "," + res.itv.b);
                System.out.flush();
                count++;
            }
        }
    }
}
