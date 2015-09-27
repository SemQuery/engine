package com.semquery.engine.analyze;

import com.semquery.engine.analyze.c.CAnalyzer;
import com.semquery.engine.analyze.c.CInputStreamPreprocessor;
import com.semquery.engine.analyze.java.JavaAnalyzer;
import com.semquery.engine.analyze.js.JSAnalyzer;
import com.semquery.engine.element.Element;
import com.semquery.engine.parsers.*;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class LanguageHandlers {

    private static final Map<String, LanguageHandler> HANDLERS;

    static {
        HANDLERS = new HashMap<>();

        HANDLERS.put("java", new JavaHandler());

        CHandler cHandler = new CHandler();
        HANDLERS.put("c", cHandler);
        HANDLERS.put("h", cHandler);

        HANDLERS.put("js", new JSHandler());
    }

    public static LanguageHandler handlerFor(String fileExt) {
        return HANDLERS.get(fileExt);
    }

    private static class JavaHandler implements LanguageHandler {
        JavaAnalyzer analyzer = new JavaAnalyzer();
        @Override
        public Element createElement(InputStream in) throws IOException {
            JavaLexer lex = new JavaLexer(new ANTLRInputStream(in));
            TokenStream ts = new CommonTokenStream(lex);
            JavaParser parser = new JavaParser(ts);

            return analyzer.analyze(parser.compilationUnit(), ts, null);
        }
    }

    private static class CHandler implements LanguageHandler {
        CAnalyzer analyzer = new CAnalyzer();

        @Override
        public Element createElement(InputStream in) throws IOException {
            ANTLRInputStream antlrIn = new ANTLRInputStream(new CInputStreamPreprocessor(in));
            CLexer lex = new CLexer(antlrIn);
            TokenStream ts = new CommonTokenStream(lex);
            CParser parser = new CParser(ts);

            return analyzer.analyze(parser.compilationUnit(), ts, null);
        }
    }

    private static class JSHandler implements LanguageHandler {
        JSAnalyzer analyzer = new JSAnalyzer();

        @Override
        public Element createElement(InputStream in) throws IOException {
            ANTLRInputStream antlrIn = new ANTLRInputStream(in);
            ECMAScriptLexer lex = new ECMAScriptLexer(antlrIn);
            TokenStream ts = new CommonTokenStream(lex);
            ECMAScriptParser parser = new ECMAScriptParser(ts);

            return analyzer.analyze(parser.program(), ts, null);
        }
    }

}
