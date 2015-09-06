package com.semquery.engine.element;

import com.semquery.engine.parsers.SemQueryParser;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;
import org.bson.BasicBSONObject;
import org.bson.types.BasicBSONList;

import java.util.*;

public class Element {

    private String type;

    // Object is either String or Element
    private Map<String, Object> attributes;
    private List<Element> children;

    private int start, end;

    public Element(String type, Map<String, Object> attributes, List<Element> children, int start, int end) {
        this.type = type;
        this.attributes = attributes;
        this.children = children;
        this.start = start;
        this.end = end;
    }

    public Element(String type) {
        this.type = type;

        this.attributes = new HashMap<>();
        this.children = new ArrayList<>();
    }

    public Element withPosition(int start, int end) {
        this.start = start;
        this.end = end;

        return this;
    }

    public Element withPosition(ParseTree ctx, TokenStream ts) {
        Interval itv = ctx.getSourceInterval();
        this.start = ts.get(itv.a).getStartIndex();
        this.end = ts.get(itv.b).getStopIndex();

        return this;
    }

    public Element withChild(Element child) {
        if (child != null)
            this.children.add(child);
        return this;
    }

    public Element withChildren(List<Element> list) {
        if (list != null)
            this.children.addAll(list);
        return this;
    }

    public Element withAttribute(String key, String val) {
        this.attributes.put(key, val);
        return this;
    }

    public Element withAttribute(String key, Element val) {
        this.attributes.put(key, val);
        return this;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public List<Element> getChildren() {
        return children;
    }

    public int getEnd() {
        return end;
    }

    public int getStart() {
        return start;
    }

    public String getType() {
        return type;
    }

    public void prettyPrint() {
        prettyPrint(0);
    }

    private void prettyPrint(int lvl) {
        String indent = new String(new char[lvl * 4]).replace('\0', ' ');
        System.out.print(indent + type);
        if (attributes.size() > 0) {
            System.out.print('(');
            int count = 0;
            for (Map.Entry<String, Object> entry : attributes.entrySet()) {
                count++;
                System.out.print(entry.getKey() + ": ");
                Object o = entry.getValue();
                if (o instanceof String) {
                    System.out.print(o);
                } else {
                    System.out.print("[complex]");
                }
                if (count != attributes.size())
                    System.out.print(", ");
            }
            System.out.print(')');
        }
        System.out.println();
        for (Element child : children) {
            if (child != null)
                child.prettyPrint(lvl + 1);
        }
    }

    public static BasicBSONObject createBSON(Element e) {
        BasicBSONList list = new BasicBSONList();
        Map<Element, Integer> lookup = new Hashtable<>();

        e.toBSON(list, lookup);

        return new BasicBSONObject("e", list);
    }

    private void toBSON(BasicBSONList list, Map<Element, Integer> lookup) {
        for (Element child : children) {
            if (child != null) {
                child.toBSON(list, lookup);
            }
        }
        for (Object o : attributes.values()) {
            if (o instanceof Element)
                ((Element) o).toBSON(list, lookup);
        }

        BasicBSONObject obj = new BasicBSONObject();
        obj.put("t", type);

        BasicBSONObject attrs = new BasicBSONObject(attributes.size());
        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            if (entry.getValue() instanceof String) {
                attrs.put(entry.getKey(), entry.getValue());
            } else if (entry.getValue() instanceof Element) {
                int index = lookup.get(entry.getValue());
                attrs.put(entry.getKey(), index);
            }
        }
        obj.put("a", attrs);

        BasicBSONList childIndices = new BasicBSONList();
        for (Element child : children) {
            if (child != null) {
                int index = lookup.get(child);
                childIndices.add(index);
            }

        }
        obj.put("c", childIndices);

        int insertIdx = list.size();
        list.add(insertIdx, obj);
        lookup.put(this, insertIdx);
    }

    public static Element fromParsed(SemQueryParser.ElementContext ctx) {
        Element e = new Element(ctx.IDENTIFIER().getText());
        for (SemQueryParser.Attribute_pairContext pair : ctx.attribute_pair()) {
            String ident = pair.IDENTIFIER().getText();
            if (pair.STRING() != null) {
                String str = pair.STRING().getText();
                str = str.substring(1, str.length() - 1);
                e.withAttribute(ident, str);
            } else if (pair.element() != null) {
                e.withAttribute(ident, fromParsed(pair.element()));
            }
        }
        for (SemQueryParser.ElementContext child : ctx.element()) {
            e.withChild(fromParsed(child));
        }

        return e;
    }

}
