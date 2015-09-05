package com.semquery.engine.analyze.c;

import com.semquery.engine.analyze.Analyzer;
import com.semquery.engine.element.Element;
import com.semquery.engine.element.ElementAdapter;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.TokenStream;

import static com.semquery.engine.parsers.CParser.*;


public enum CAdapter {
        COMPILATION_UNIT(CompilationUnitContext.class, (CompilationUnitContext ctx, TokenStream ts, Analyzer a, Element p) -> {
            Element cu = new Element("program");
            return cu.withChildren(
                    a.analyze(ctx.translationUnit().externalDeclaration(), ts, cu)
            );
        }),

        EXTERNAL(ExternalDeclarationContext.class, (ExternalDeclarationContext ctx, TokenStream ts, Analyzer a, Element p) -> {
            if (ctx.functionDefinition() != null) {
                return a.analyze(ctx.functionDefinition(), ts, p);
            } else if (ctx.declaration() != null) {
                return a.analyze(ctx.declaration(), ts, p);
            }
            return null;
        }),

        FUNC_DEF(FunctionDefinitionContext.class, (FunctionDefinitionContext ctx, TokenStream ts, Analyzer a, Element p) -> {
            String type = null;
            for (DeclarationSpecifierContext ds : ctx.declarationSpecifiers().declarationSpecifier()) {
                if (ds.typeSpecifier() != null)
                    type = ds.typeSpecifier().getText();
            }

            String name = ctx.declarator().directDeclarator().directDeclarator().getText();

            Element func = new Element("function")
                    .withAttribute("name", name)
                    .withAttribute("type", type);
            return func.withChild(
                    a.analyze(ctx.compoundStatement(), ts, func)
            );
        });

    ;

    private Class<? extends RuleContext> cls;
    private ElementAdapter<?> adapter;

    CAdapter(Class<? extends RuleContext> cls, ElementAdapter<?> adapter) {
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
