package com.semquery.engine.analyze;

import com.semquery.engine.analyze.java.Java8Adapter;
import com.semquery.engine.element.Element;
import com.semquery.engine.element.ElementAdapter;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.TokenStream;

import java.util.HashMap;
import java.util.Map;

public class Java8Analyzer implements Analyzer {

    private static final Map<Class<? extends RuleContext>, ElementAdapter> ADAPTER_MAP;

    static {
        ADAPTER_MAP = new HashMap<>(Java8Adapter.values().length);

        for (Java8Adapter adapter : Java8Adapter.values()) {
            ADAPTER_MAP.put(adapter.getCls(), adapter.getAdapter());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends RuleContext> Element analyze(T ctx, TokenStream ts) {
        ElementAdapter adapter = ADAPTER_MAP.get(ctx.getClass());
        if (adapter != null) {
            return adapter.convert(ctx, ts, this);
        } else {
            System.out.println("No adapter for class: " + ctx.getClass());
            return null;
        }
    }
}
