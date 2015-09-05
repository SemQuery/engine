package com.semquery.engine.analyze.java;

import com.semquery.engine.analyze.Analyzer;
import com.semquery.engine.element.Element;
import com.semquery.engine.element.ElementAdapter;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;

import static com.semquery.engine.parsers.Java8Parser.*;

public enum Java8Adapter {
        COMPILATION_UNIT(CompilationUnitContext.class, (CompilationUnitContext ctx, TokenStream ts, Analyzer a) -> {
            return new Element("program")
                    .withPosition(ctx, ts)
                    .withChild(a.analyze(ctx.packageDeclaration(), ts))
                    .withChildren(a.analyze(ctx.importDeclaration(), ts))
                    .withChildren(a.analyze(ctx.typeDeclaration(), ts));
        }),

        PACKAGE(PackageDeclarationContext.class, (PackageDeclarationContext ctx, TokenStream ts, Analyzer a) -> {
            List<String> parts = new ArrayList<>(ctx.Identifier().size());
            for (TerminalNode node : ctx.Identifier())
                parts.add(node.getText());

            return new Element("package")
                    .withAttribute("name", String.join(".", parts));
        }),

        IMPORT(ImportDeclarationContext.class, (ImportDeclarationContext ctx, TokenStream ts, Analyzer a) -> {
            if (ctx.singleTypeImportDeclaration() != null) {
                String name = ctx.singleTypeImportDeclaration().typeName().getText();
                return new Element("import")
                        .withAttribute("name", name);
            }
            return null;
        }),

        TYPE_DECL(TypeDeclarationContext.class, (TypeDeclarationContext ctx, TokenStream ts, Analyzer a) -> {
            if (ctx.classDeclaration() != null && ctx.classDeclaration().normalClassDeclaration() != null) {
                return handleClassDecl(ctx.classDeclaration().normalClassDeclaration(), ts, a);
            } else {
                return null;
            }
        }),


    ;

    private static Element handleClassDecl(NormalClassDeclarationContext ctx, TokenStream ts, Analyzer a) {
        Element e = new Element("class")
                .withAttribute("name", ctx.Identifier().getText());
        if (ctx.superclass() != null) {
            String sup = ctx.superclass().classType().getText();
            e.withAttribute("extends", sup);
        }
        if (ctx.superinterfaces() != null) {
            String itfs = ctx.superinterfaces().interfaceTypeList().getText();
            e.withAttribute("implements", itfs);
        }

        for (ClassBodyDeclarationContext bodyDecl : ctx.classBody().classBodyDeclaration()) {
            if (bodyDecl.classMemberDeclaration() != null) {
                e.withChild(handleClassMember(bodyDecl.classMemberDeclaration(), ts, a));
            }
        }
        return e;
    }

    private static Element handleClassMember(ClassMemberDeclarationContext ctx, TokenStream ts, Analyzer a) {
        if (ctx.fieldDeclaration() != null) {
            return new Element("field")
                    .withAttribute("name", ctx.fieldDeclaration().variableDeclaratorList().getText())
                    .withAttribute("type", ctx.fieldDeclaration().unannType().getText());
        } else if (ctx.methodDeclaration() != null) {
            Element e = new Element("method")
                    .withAttribute("name",
                            ctx.methodDeclaration().methodHeader().methodDeclarator().Identifier().getText()
                    );
            BlockStatementsContext blocks = ctx.methodDeclaration().methodBody().block().blockStatements();
            if (blocks != null) {
                e.withChildren(a.analyze(blocks.blockStatement(), ts));
            }

            return e;
        }

        return null;
    }

    private Class<? extends RuleContext> cls;
    private ElementAdapter<?> adapter;

    Java8Adapter(Class<? extends RuleContext> cls, ElementAdapter<?> adapter) {
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
