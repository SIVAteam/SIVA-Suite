package org.iviPro.utils;

import java.io.IOException;
import java.io.Reader;

import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

public class Html2TextParser extends HTMLEditorKit.ParserCallback {
 StringBuffer s;

 public Html2TextParser() {}

 public String parse(Reader in) throws IOException {
   s = new StringBuffer();
   ParserDelegator delegator = new ParserDelegator();
   //3rd parameter: TRUE = ignore charset directive
   delegator.parse(in, this, Boolean.TRUE);
   return s.toString();
 }

 public void handleText(char[] text, int pos) {
   s.append(text);
 }
}