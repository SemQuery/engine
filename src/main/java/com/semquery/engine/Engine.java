package com.semquery.engine;

import com.semquery.engine.analyze.Indexer;
import com.semquery.engine.analyze.QueryManager;
import com.semquery.engine.analyze.QueryValidator;

import java.util.List;

public class Engine {


    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Expected >= 2 args");
            System.exit(1);
        }

        String cmd = args[0];
        if (cmd.equals("index")) {
            new Indexer(args[1], args[2]);
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
