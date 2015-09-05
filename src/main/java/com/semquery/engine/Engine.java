package com.semquery.engine;

import com.semquery.engine.analyze.LanguageHandler;
import com.semquery.engine.analyze.LanguageHandlers;
import com.semquery.engine.element.Element;

import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Engine {

    static ExecutorService exec;

    static AtomicInteger inc = new AtomicInteger();
    static int files = 0;

    public static void main(String[] args) throws Exception {
        exec = Executors.newFixedThreadPool(10);
        long time = System.currentTimeMillis();
        for (String arg : args) {
            recur(new File(arg));
        }
        exec.shutdown();
        while (!exec.isTerminated());

        System.out.println("Finished in " + (System.currentTimeMillis() - time) + "ms");
        System.out.println("" + files + " files, " + inc.get() + " elements");
    }

    public Engine() {
    }

    public static void launchParsing(File f, LanguageHandler handler) {
        exec.execute(() -> {
            try {
                System.out.println("Launching file " + f);

                Element ele = handler.createElement(new FileInputStream(f));

                ele.prettyPrint();

                countElems(ele);

            } catch (Exception e) {
            }
        });
    }

    static void countElems(Element e) {
        inc.incrementAndGet();

        for (Element child : e.getChildren())
            countElems(child);
    }

    public static void recur(File f) {
        if (!f.exists())
            return;

        if (f.isDirectory()) {
            for (File child : f.listFiles())
                recur(child);
        } else {
            String name = f.getName();
            int lastDot = name.lastIndexOf('.');
            String ext  = name.substring(lastDot + 1);

            LanguageHandler handler = LanguageHandlers.handlerFor(ext);
            if (handler != null) {
                launchParsing(f, handler);
                files++;
            }
        }
    }

}
