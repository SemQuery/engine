package com.semquery.engine.analyze.java;

import com.semquery.engine.analyze.Analyzer;
import com.semquery.engine.element.Element;
import com.semquery.engine.element.ElementAdapter;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ArrayList;
import java.util.List;

import static com.semquery.engine.parsers.JavaParser.*;

public enum JavaAdapter {
        COMPILATION_UNIT(CompilationUnitContext.class, (CompilationUnitContext ctx, TokenStream ts, Analyzer a, Element p) -> {
            Element e = new Element("program");
            return e
                    .withPosition(ctx, ts)
                    .withChild(a.analyze(ctx.packageDeclaration(), ts, e))
                    .withChildren(a.analyze(ctx.importDeclaration(), ts, e))
                    .withChildren(a.analyze(ctx.typeDeclaration(), ts, e));
        }),

        PACKAGE(PackageDeclarationContext.class, (PackageDeclarationContext ctx, TokenStream ts, Analyzer a, Element p) -> {
            return new Element("package")
                    .withAttribute("name", ctx.qualifiedName().getText());
        }),

        IMPORT(ImportDeclarationContext.class, (ImportDeclarationContext ctx, TokenStream ts, Analyzer a, Element p) -> {
            return new Element("import")
                    .withAttribute("name", ctx.qualifiedName().getText());
        }),

        TYPE_DECL(TypeDeclarationContext.class, (TypeDeclarationContext ctx, TokenStream ts, Analyzer a, Element p) -> {
            if (ctx.classDeclaration() != null) {
                return handleClassDecl(ctx.classDeclaration(), ts, a, p);
            } else {
                return null;
            }
        }),

        BLOCK_STMT(BlockStatementContext.class, (BlockStatementContext ctx, TokenStream ts, Analyzer a, Element p) -> {
            if (ctx.localVariableDeclarationStatement() != null) {
                return a.analyze(ctx.localVariableDeclarationStatement(), ts, p);
            } else if (ctx.typeDeclaration() != null) {
                return a.analyze(ctx.typeDeclaration(), ts, p);
            } else if (ctx.statement() != null) {
                return a.analyze(ctx.statement(), ts, p);
            }
            return null;
        }),

        LOCAL_VAR(LocalVariableDeclarationStatementContext.class, (LocalVariableDeclarationStatementContext ctx, TokenStream ts, Analyzer a, Element p) ->  {
            return new Element("local_var")
                    .withAttribute("type", ctx.localVariableDeclaration().type().getText())
                    .withAttribute("name", ctx.localVariableDeclaration().variableDeclarators().getText());
        }),

        STMT(StatementContext.class, (StatementContext ctx, TokenStream ts, Analyzer a, Element p) -> {
            if (ctx.block() != null) {
                return a.analyze(ctx.block(), ts, p);
            } else if (ctx.statementExpression() != null) {
                return a.analyze(ctx.statementExpression().expression(), ts, p);
            }
            String first = ctx.getStart().getText();
            switch (first) {
                case "if":
                    Element ifEle = new Element("if");
                            ifEle.withChild(new Element("then").withChild(
                                    a.analyze(ctx.statement(0), ts, ifEle)
                            ));
                    if (ctx.statement().size() > 1) {
                        ifEle.withChild(new Element("else").withChild(
                                a.analyze(ctx.statement(1), ts, ifEle)
                        ));
                    }
                    return ifEle;
                case ";":
                    return new Element("pass");
                case "return":
                    Element retEle = new Element("return");
                    if (ctx.expression().size() > 0)
                        retEle.withChild(a.analyze(ctx.expression(0), ts, retEle));
                    return retEle;
                case "throw":
                    Element throwEle = new Element("throw");
                    return throwEle.withChild(
                        a   .analyze(ctx.expression(0), ts, throwEle)
                    );
                case "for":
                    Element forEle = new Element("for");
                    return forEle.withChild(
                            a.analyze(ctx.statement(0), ts, forEle)
                    );
                case "while":
                    Element whileEle = new Element("while");
                    return whileEle.withChild(
                            a.analyze(ctx.statement(0), ts, whileEle)
                    );
                case "continue":
                    return new Element("continue");
                case "break":
                    return new Element("break");
            }
            System.out.println(">>> STMT start " + ctx.getStart().getText());
            return null;
        }),

        EXPR(ExpressionContext.class, (ExpressionContext ctx, TokenStream ts, Analyzer a, Element p) -> {
            List<Element> elem = new ArrayList<>();
            findIdentifiers(ctx, elem);

            return new Element("expr").withChildren(elem);
        }),

        BLOCK(BlockContext.class, (BlockContext ctx, TokenStream ts, Analyzer a, Element p) -> {
            for (BlockStatementContext child : ctx.blockStatement())
                p.withChild(a.analyze(child, ts, p));

            return null;
        })
    ;

    private static void findIdentifiers(ParseTree tree, List<Element> addTo) {
        if (tree instanceof PrimaryContext) {
            PrimaryContext primary = (PrimaryContext) tree;
            if (primary.Identifier() != null) {
                addTo.add(
                        new Element("usage").withAttribute("name", primary.Identifier().getText())
                );
            }
        } else {
            for (int i = 0; i < tree.getChildCount(); i++) {
                findIdentifiers(tree.getChild(i), addTo);
            }
        }
    }

    private static Element handleClassDecl(ClassDeclarationContext ctx, TokenStream ts, Analyzer a, Element p) {
        Element e = new Element("class")
                .withAttribute("name", ctx.Identifier().getText());
        if (ctx.type() != null) {
            e.withAttribute("extends", ctx.type().getText());
        }
        if (ctx.typeList() != null) {
            String itfs = ctx.typeList().getText();
            e.withAttribute("implements", itfs);
        }

        for (ClassBodyDeclarationContext bodyDecl : ctx.classBody().classBodyDeclaration()) {
            if (bodyDecl.memberDeclaration() != null) {
                e.withChild(handleClassMember(bodyDecl.memberDeclaration(), ts, a, p));
            }
        }
        return e;
    }

    private static Element handleClassMember(MemberDeclarationContext ctx, TokenStream ts, Analyzer a, Element p) {
        if (ctx.fieldDeclaration() != null) {
            return new Element("field")
                    .withAttribute("name", ctx.fieldDeclaration().variableDeclarators().getText())
                    .withAttribute("type", ctx.fieldDeclaration().type().getText());
        } else if (ctx.methodDeclaration() != null) {
            Element e = new Element("method")
                    .withAttribute("name",
                            ctx.methodDeclaration().Identifier().getText()
                    );
            String type = "void";
            if (ctx.methodDeclaration().type() != null)
                type = ctx.methodDeclaration().type().getText();
            List<BlockStatementContext> blocks = ctx.methodDeclaration().methodBody().block().blockStatement();
            if (blocks != null) {
                e.withChildren(a.analyze(blocks, ts, p));
            }

            return e;
        }

        return null;
    }

    private Class<? extends RuleContext> cls;
    private ElementAdapter<?> adapter;

    JavaAdapter(Class<? extends RuleContext> cls, ElementAdapter<?> adapter) {
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
