package com.semquery.engine;

import com.semquery.engine.analyze.Java8Analyzer;
import com.semquery.engine.element.Element;
import com.semquery.engine.parsers.Java8Lexer;
import com.semquery.engine.parsers.Java8Parser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;

import java.io.InputStream;

public class Engine {

    public static void main(String[] args) throws Exception {
        InputStream in = Engine.class.getClassLoader().getResourceAsStream("test.java");

        ANTLRInputStream stream = new ANTLRInputStream(in);
        Java8Lexer lexer = new Java8Lexer(stream);

        TokenStream tokens = new CommonTokenStream(lexer);
        Java8Parser parser = new Java8Parser(tokens);

        Java8Analyzer ja = new Java8Analyzer();

        Element elem = ja.analyze(parser.compilationUnit(), tokens);
        elem.prettyPrint();
    }

}
