package com.semquery.engine.analyze;

import com.mongodb.BasicDBList;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.semquery.engine.element.Element;
import com.semquery.engine.parsers.SemQueryLexer;
import com.semquery.engine.parsers.SemQueryParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.Interval;
import org.bson.Document;
import org.bson.types.BasicBSONList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QueryManager {

    private Element cmp;
    private String repo;
    private MongoDatabase database;

    public QueryManager(String query, String repo) {
        this.repo = repo;

        SemQueryLexer lex = new SemQueryLexer(new ANTLRInputStream(query));
        TokenStream ts = new CommonTokenStream(lex);
        SemQueryParser parser = new SemQueryParser(ts);

        MongoClient client = new MongoClient();
        database = client.getDatabase("semquery");

        cmp = Element.fromParsed(parser.rootElement().element());
    }

    public List<Result> getResults(int page, int perpage) {
        MongoCollection<Document> coll = database.getCollection("files");

        Document query = new Document("repo", repo);

        Document eMatch = new Document();
        eMatch.append("t", cmp.getType());
        for (Map.Entry<String, Object> entry : cmp.getAttributes().entrySet()) {
            if (entry.getValue() instanceof String) {
                String s = (String) entry.getValue();
                //Document regex = new Document("regex", s);
                eMatch.append("a." + entry.getKey(), s);
            }
        }

        BasicBSONList list = new BasicBSONList();
        list.add(eMatch);
        query.put(
                "elements",
                new Document("$elemMatch", new Document(
                        "$and", list
                ))
        );


        System.out.println(coll.count(query));

        FindIterable<Document> find = coll.find(query).limit(perpage).skip(page * perpage);
        List<Result> ret = new ArrayList<>();
        for (Document res : find) {
            tryDoc(res, ret);
        }

        return ret;
    }

    void tryDoc(Document doc, List<Result> results) {
        ArrayList list = (ArrayList) doc.get("elements");
        for (Object o : list) {
            Document item = (Document) o;

            if (cmpDoc(item, list, cmp)) {
                String file = (String) doc.get("file");
                int start = (int) item.get("s");
                int end = (int) item.get("e");
                Interval itv = new Interval(start, end);
                Result res = new Result(file, itv);
                results.add(res);
            }
        }
    }

    boolean cmpDoc(Document doc, ArrayList dList, Element e) {
        String type = (String) doc.get("t");
        if (!type.equals(cmp.getType()))
            return false;

        Document attrs = (Document) doc.get("a");
        for (Map.Entry<String, Object> entry : cmp.getAttributes().entrySet()) {
            if (!(entry.getValue() instanceof String))
                continue;
            if (!attrs.containsKey(entry.getKey()))
                continue;
            String equiv = (String) attrs.get(entry.getKey());
            String val = (String) entry.getValue();

            if (!equiv.contains(val)) {
                return false;
            }
        }

        for (Element child : e.getChildren()) {
            BasicDBList docChildren = (BasicDBList) doc.get("c");
            boolean satisfied = false;
            for (Object o : docChildren) {
                int idx = (int) o;
                Document cDoc = (Document) dList.get(idx);
                if (cmpDoc(cDoc, dList, child)) {
                    satisfied = true;
                    break;
                }
            }
            if (!satisfied)
                return false;
        }

        return true;
    }

    public static class Result {
         public Interval itv;
         public String file;

        public Result(String file, Interval itv) {
            this.file = file;
            this.itv = itv;
        }

        @Override
        public String toString() {
            return file + '[' + itv.a + ".." + itv.b + ']';
        }
    }

}
