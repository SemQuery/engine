package com.semquery.engine.analyze;

import com.semquery.engine.element.Element;

import java.io.IOException;
import java.io.InputStream;

public interface LanguageHandler {

    public Element createElement(InputStream in) throws IOException;

}
