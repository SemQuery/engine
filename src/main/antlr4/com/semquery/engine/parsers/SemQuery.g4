grammar SemQuery;

fragment IDENT_CHAR: 'a'..'z' | 'A'..'Z' | '_';
fragment DIGIT: '0'..'9';
IDENTIFIER: IDENT_CHAR (IDENT_CHAR | DIGIT)*;

fragment ESCAPED_QUOTE: '\\"' | '\\\'';
fragment DOUBLE_QUOTED_STRING: '"' (ESCAPED_QUOTE | ~('\n'|'\r'))*? '"';
fragment SINGLE_QUOTED_STRING: '\'' (ESCAPED_QUOTE | ~('\n'|'\r'))*? '\'';
STRING: DOUBLE_QUOTED_STRING | SINGLE_QUOTED_STRING;

WS: [ \t\r\n]+ -> skip;

rootElement:
    element EOF;

element:
    IDENTIFIER
    ( '(' attribute_pair (',' attribute_pair)* ')' )?
    ( '{' element* '}' )?;

attribute_pair:
    IDENTIFIER ':' (STRING | element);