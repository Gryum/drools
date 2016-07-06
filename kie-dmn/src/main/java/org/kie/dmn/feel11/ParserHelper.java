/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.dmn.feel11;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.kie.dmn.lang.Scope;
import org.kie.dmn.lang.types.LocalScope;
import org.kie.dmn.lang.types.SymbolTable;
import org.kie.dmn.lang.types.VariableSymbol;

import java.util.List;
import java.util.Stack;

public class ParserHelper {

    private static final String GLOBAL = "<global>";
    private static final String LOCAL  = "<local>";

    private SymbolTable   symbols      = new SymbolTable();
    private Scope         currentScope = new LocalScope( GLOBAL, symbols.getBuiltInScope() );
    private Stack<String> currentName  = new Stack<>();

    public ParserHelper() {
        // initial context is loaded
        currentName.push( LOCAL );
    }

    public SymbolTable getSymbolTable() {
        return symbols;
    }

    public void pushScope() {
        currentScope = new LocalScope( currentName.peek(), currentScope );
    }

    public void popScope() {
        currentScope = currentScope.getParentScope();
    }

    public void pushName(ParserRuleContext ctx) {
        this.currentName.push( getOriginalText( ctx ) );
    }

    public void popName() {
        this.currentName.pop();
    }

    public void defineVariable(ParserRuleContext ctx) {
        defineVariable( getOriginalText( ctx ) );
    }

    public void defineVariable( String variable ) {
        VariableSymbol var = new VariableSymbol( variable );
        this.currentScope.define( var );
    }

    public void startVariable(Token t) {
        System.out.println("> start = '"+t.getText()+"'");
        this.currentScope.start( t.getText() );
    }

    public boolean followUp(Token t) {
        boolean follow = this.currentScope.followUp( t.getText() );
        System.out.println( "  + follow = '" + t.getText() + "' -> "+follow );
        return follow;
    }

    private String getOriginalText(ParserRuleContext ctx) {
        int a = ctx.start.getStartIndex();
        int b = ctx.stop.getStopIndex();
        Interval interval = new Interval( a, b );
        return ctx.getStart().getInputStream().getText( interval );
    }

    public static List<Token> getAllTokens(ParseTree ctx, List<Token> tokens) {
        for ( int i = 0; i < ctx.getChildCount(); i++ ) {
            ParseTree child = ctx.getChild( i );
            if ( child instanceof TerminalNode ) {
                tokens.add( ((TerminalNode) child).getSymbol() );
            } else {
                getAllTokens( child, tokens );
            }
        }
        return tokens;
    }

}
