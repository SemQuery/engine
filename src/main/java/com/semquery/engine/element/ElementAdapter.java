package com.semquery.engine.element;

import com.semquery.engine.analyze.Analyzer;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.TokenStream;

public interface ElementAdapter<T extends RuleContext> {

    Element convert(T ctx, TokenStream ts, Analyzer a, Element parent);

}
