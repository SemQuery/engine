package com.semquery.engine.element;

import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.Interval;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public Element withPosition(RuleContext ctx, TokenStream ts) {
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

}
