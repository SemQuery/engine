package com.semquery.engine.analyze.c;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CInputStreamPreprocessor extends InputStream {

    InputStream src;
    List<CPreprocessorDirective> directives = new ArrayList<>();

    public CInputStreamPreprocessor(InputStream src) {
        this.src = src;
    }

    boolean EOL;
    boolean first = true;

    @Override
    public int read() throws IOException {
        int val = src.read();
        if (val == '\n') {
            EOL = true;
            first = false;
            return val;
        } else {
            if ((EOL || first) && val == '#') {
                StringBuilder directive = new StringBuilder();
                while ((val = src.read()) > 'a' && val <= 'z')
                    directive.append((char) val);

                StringBuilder rest = new StringBuilder();
                boolean cont = val != '\n';
                int last = val;
                while (cont && (val = src.read()) > -1) {
                    if (val == '\n') {
                        cont = last == '\\';
                    }
                    rest.append((char) val);
                    last = val;
                }
                directives.add(new CPreprocessorDirective(directive.toString(), rest.toString()));
            }

            EOL = val == '\n';
            first = false;
            return val;
        }
    }

    public List<CPreprocessorDirective> getDirectives() {
        return directives;
    }

    public static class CPreprocessorDirective {
        String directive;
        String value;

        public CPreprocessorDirective(String directive, String value) {
            this.directive = directive;
            this.value = value;
        }

        public String getDirective() {
            return directive;
        }

        public String getValue() {
            return value;
        }
    }
}
