package com.semquery.engine.analyze;

import com.semquery.engine.element.Element;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.TokenStream;

import java.util.ArrayList;
import java.util.List;

public interface Analyzer {

    default <T extends RuleContext> List<Element> analyze(List<T> ctxList, TokenStream ts, Element parent) {
        List<Element> list = new ArrayList<>();
        for (T ctx : ctxList) {
            list.add(analyze(ctx, ts, parent));
        }
        return list;
    }

    <T extends RuleContext> Element analyze(T ctx, TokenStream ts, Element parent);

}
