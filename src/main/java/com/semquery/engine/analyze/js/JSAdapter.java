package com.semquery.engine.analyze.js;

import com.semquery.engine.element.ElementAdapter;
import org.antlr.v4.runtime.RuleContext;

public enum JSAdapter {

    ;

    private Class<? extends RuleContext> cls;
    private ElementAdapter<?> adapter;

    JSAdapter(Class<? extends RuleContext> cls, ElementAdapter<?> adapter) {
        this.cls = cls;
        this.adapter = adapter;
    }

    public Class<? extends RuleContext> getCls() {
        return cls;
    }

    public ElementAdapter<?> getAdapter() {
        return adapter;
    }
}
