package com.semquery.engine.analyze.c;

import com.semquery.engine.analyze.Analyzer;
import com.semquery.engine.element.Element;
import com.semquery.engine.element.ElementAdapter;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.TokenStream;

import java.util.HashMap;
import java.util.Map;

public class CAnalyzer implements Analyzer {

    private static final Map<Class<? extends RuleContext>, ElementAdapter> ADAPTER_MAP;

    static {
        ADAPTER_MAP = new HashMap<>(CAdapter.values().length);

        for (CAdapter adapter : CAdapter.values()) {
            ADAPTER_MAP.put(adapter.getCls(), adapter.getAdapter());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends RuleContext> Element analyze(T ctx, TokenStream ts, Element parent) {
        ElementAdapter adapter = ADAPTER_MAP.get(ctx.getClass());
        if (adapter != null) {
            return adapter.convert(ctx, ts, this, parent);
        } else {
            return null;
        }
    }
}
