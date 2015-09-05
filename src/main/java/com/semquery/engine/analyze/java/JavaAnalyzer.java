package com.semquery.engine.analyze.java;

import com.semquery.engine.analyze.Analyzer;
import com.semquery.engine.analyze.java.JavaAdapter;
import com.semquery.engine.element.Element;
import com.semquery.engine.element.ElementAdapter;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.TokenStream;

import java.util.HashMap;
import java.util.Map;

public class JavaAnalyzer implements Analyzer {

    private static final Map<Class<? extends RuleContext>, ElementAdapter> ADAPTER_MAP;

    static {
        ADAPTER_MAP = new HashMap<>(JavaAdapter.values().length);

        for (JavaAdapter adapter : JavaAdapter.values()) {
            ADAPTER_MAP.put(adapter.getCls(), adapter.getAdapter());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends RuleContext> Element analyze(T ctx, TokenStream ts, Element p) {
        ElementAdapter adapter = ADAPTER_MAP.get(ctx.getClass());
        if (adapter != null) {
            return adapter.convert(ctx, ts, this, p);
        } else {
            System.out.println("No adapter for class: " + ctx.getClass());
            return null;
        }
    }
}
