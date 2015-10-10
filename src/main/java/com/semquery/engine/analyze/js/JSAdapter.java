package com.semquery.engine.analyze.js;

import com.semquery.engine.analyze.Analyzer;
import com.semquery.engine.element.Element;
import com.semquery.engine.element.ElementAdapter;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.TokenStream;

import java.util.ArrayList;
import java.util.List;

import static com.semquery.engine.parsers.ECMAScriptParser.*;

public enum JSAdapter {
    PROGRAM(ProgramContext.class, (ProgramContext ctx, TokenStream ts, Analyzer a, Element p) -> {
        Element e = new Element("program");
        ArrayList<Element> children = new ArrayList<>();
        if (ctx.sourceElements() != null) {
            for (SourceElementContext src : ctx.sourceElements().sourceElement()) {
                Element child = a.analyze(src, ts, e);
                if (child != null)
                    children.add(a.analyze(src, ts, e));
            }
        }
        return e
                .withPosition(ctx, ts)
                .withChildren(children);
    }),
    SRC_EL(SourceElementContext.class, (SourceElementContext ctx, TokenStream ts, Analyzer a, Element p) -> {
        FunctionDeclarationContext functionDeclarationContext = ctx.functionDeclaration();
        if (functionDeclarationContext != null) {
            return a.analyze(functionDeclarationContext, ts, p);
        }
        StatementContext statementContext = ctx.statement();
        if (statementContext != null) {
            return a.analyze(statementContext, ts, p);
        }
        return null;
    }),
    FUNC(FunctionDeclarationContext.class, (FunctionDeclarationContext ctx, TokenStream ts, Analyzer a, Element p) -> {
        Element e = new Element("function");
        List<Element> children = new ArrayList<>();
        if (ctx.functionBody().sourceElements() != null) {
            for (SourceElementContext src : ctx.functionBody().sourceElements().sourceElement()) {
                Element child = a.analyze(src, ts, e);
                if (child != null)
                    children.add(child);
            }
        }

        return e
                .withPosition(ctx, ts)
                .withAttribute("name", ctx.Identifier().getText())
                .withChildren(children);
    }),
    STMT(StatementContext.class, (StatementContext ctx, TokenStream ts, Analyzer a, Element p) -> {
        if (ctx.expressionStatement() != null) {
            return a.analyze(ctx.expressionStatement().expressionSequence(), ts, p);
        }
        return null;
    }),
    EXPR(ExpressionSequenceContext.class, (ExpressionSequenceContext ctx, TokenStream ts, Analyzer a, Element p) -> {
        Element e = new Element("expr");
        List<Element> children = new ArrayList<>();
        ctx.singleExpression().forEach((expr) -> {

            if (expr instanceof ArgumentsExpressionContext) {
                ArgumentsExpressionContext arg_ctx = (ArgumentsExpressionContext) expr;

                Element useEl = new Element("usage");

                String[] nodes = arg_ctx.singleExpression().getText().split("\\.");
                children.add(useEl
                        .withPosition(ctx, ts)
                        .withAttribute("name", nodes[nodes.length - 1]));
            }

        });
        return e
                .withPosition(ctx, ts)
                .withChildren(children);
    })
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