package com.semquery.engine.analyze;

import com.semquery.engine.parsers.SemQueryLexer;
import com.semquery.engine.parsers.SemQueryParser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;

import java.util.BitSet;

public class QueryValidator {

    private String query;

    private boolean valid = true;

    private SemQueryParser.RootElementContext rootElem;

    public QueryValidator(String query) {
        this.query = query;
    }

    public boolean validate() {
        SemQueryLexer lex = new SemQueryLexer(new ANTLRInputStream(query));
        TokenStream ts = new CommonTokenStream(lex);
        SemQueryParser parser = new SemQueryParser(ts);

        parser.getErrorListeners().clear();
        parser.addErrorListener(new ANTLRErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object o, int i, int i1, String s, RecognitionException e) {
                setValid(false);
            }

            @Override
            public void reportAmbiguity(Parser parser, DFA dfa, int i, int i1, boolean b, BitSet bitSet, ATNConfigSet atnConfigSet) {
            }

            @Override
            public void reportAttemptingFullContext(Parser parser, DFA dfa, int i, int i1, BitSet bitSet, ATNConfigSet atnConfigSet) {
            }

            @Override
            public void reportContextSensitivity(Parser parser, DFA dfa, int i, int i1, int i2, ATNConfigSet atnConfigSet) {
            }
        });

        rootElem = parser.rootElement();

        return valid;
    }

    void setValid(boolean valid) {
        this.valid = valid;
    }

    public SemQueryParser.RootElementContext getRootElem() {
        return rootElem;
    }
}
