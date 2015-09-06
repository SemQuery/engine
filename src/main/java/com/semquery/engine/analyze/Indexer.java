package com.semquery.engine.analyze;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.semquery.engine.element.Element;
import org.bson.Document;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Indexer {

    static String getExt(String f) {
        int idx = f.lastIndexOf('.');
        return f.substring(idx + 1);
    }

    static void syncPrintln(String s) {
        synchronized (System.out) {
            System.out.println(s);
            System.out.flush();
        }
    }

    private volatile int lines = 0;
    private AtomicInteger files = new AtomicInteger();
    private AtomicInteger lastPercent = new AtomicInteger();

    private int fileCount;

    MongoDatabase database;

    private ExecutorService exec;

    private String repo;

    public Indexer(String rootFile, String repo) {
        this.repo = repo;

        exec = Executors.newFixedThreadPool(10);

        MongoClient client = new MongoClient();
        database = client.getDatabase("semquery");

        File f = new File(rootFile);
        fileCount = countFiles(f);
        recur(f);

        exec.shutdown();
        while (!exec.isTerminated());
    }

    int countFiles(File file) {
        if (!file.exists())
            return 0;

        if (file.isDirectory()) {
            int sum = 0;
            for (File f : file.listFiles())
                sum += countFiles(f);
            return sum;
        } else {
            LanguageHandler handler = LanguageHandlers.handlerFor(getExt(file.getName()));
            return handler != null ? 1 : 0;
        }
    }

    void recur(File file) {
        if (!file.exists())
            return;

        if (file.isDirectory()) {
            for (File f : file.listFiles())
                recur(f);
        } else {
            LanguageHandler handler = LanguageHandlers.handlerFor(getExt(file.getName()));
            if (handler != null) {
                handleFile(file, handler);
            }
        }
    }

    void handleFile(File file, LanguageHandler handler) {
        exec.execute(() -> {
            Element ele;
            try {
                InputStream in = new FileInputStream(file);
                InputStream wrapper = new InputStream() {
                    @Override
                    public int read() throws IOException {
                        int val = in.read();
                        if (val == '\n')
                            lines++;
                        return val;
                    }
                };
                ele = handler.createElement(wrapper);
            } catch (IOException e) {
                syncPrintln("err");
                return;
            }

            BasicDBList list = new BasicDBList();
            Map<Element, Integer> lookup = new HashMap<>();
            handleElem(ele, list, lookup);

            Document obj = new Document("file", file.getPath())
                    .append("repo", repo)
                    .append("elements", list);

            database.getCollection("files").insertOne(obj);

            int percent = Math.round(((float) files.incrementAndGet() / fileCount) * 100);
            if (percent > lastPercent.get()) {
                lastPercent.set(percent);
                syncPrintln(percent + "," + files.get() + "," + lines);
            }
        });
    }

    void handleElem(Element e, BasicDBList list, Map<Element, Integer> lookup) {
        if (e == null || lookup.containsKey(e))
            return;

        for (Element child : e.getChildren())
            handleElem(child, list, lookup);
        for (Map.Entry<String, Object> entry : e.getAttributes().entrySet()) {
            Object o = entry.getValue();
            if (o instanceof Element)
                handleElem((Element) o, list, lookup);
        }

        BasicDBObject obj = new BasicDBObject("t", e.getType());
        BasicDBObject attrs = new BasicDBObject();
        for (Map.Entry<String, Object> entry : e.getAttributes().entrySet()) {
            Object o = entry.getValue();
            if (o instanceof Element) {
                attrs.put(entry.getKey(), lookup.get(o));
            } else if (o instanceof String) {
                attrs.put(entry.getKey(), o);
            }
        }

        obj.put("a", attrs);

        BasicDBList children = new BasicDBList();
        for (Element child : e.getChildren()) {
            children.add(lookup.get(child));
        }
        obj.put("c", children);

        obj.append("s", e.getStart()).append("e", e.getEnd());

        int insertIdx = list.size();
        list.add(insertIdx, obj);
        lookup.put(e, insertIdx);
    }

    public AtomicInteger getFiles() {
        return files;
    }
}
