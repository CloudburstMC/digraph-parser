/*
[BSD 3-Clause License]

Copyright (c) 2017, PayPal Holdings, Inc.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, this
  list of conditions and the following disclaimer.

* Redistributions in binary form must reproduce the above copyright notice,
  this list of conditions and the following disclaimer in the documentation
  and/or other materials provided with the distribution.

* Neither the name of the copyright holder nor the names of its
  contributors may be used to endorse or promote products derived from
  this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package com.nukkitx.digraph.parser;

import com.nukkitx.digraph.DiGraph;
import com.nukkitx.digraph.DiGraphEdge;
import com.nukkitx.digraph.DiGraphNode;
import com.nukkitx.digraph.parser.antlr.DOTBaseListener;
import com.nukkitx.digraph.parser.antlr.DOTLexer;
import com.nukkitx.digraph.parser.antlr.DOTParser;
import com.nukkitx.digraph.parser.antlr.DOTParser.A_listContext;
import com.nukkitx.digraph.parser.antlr.DOTParser.IdContext;
import org.antlr.v4.runtime.InputMismatchException;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.IntervalSet;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.misc.Nullable;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.io.InputStream;
import java.util.*;

public class GraphParser {

    public static DiGraph parse(InputStream is) throws GraphParserException {
        DOTLexer lexer = null;
        DOTParser parser;

        DiGraph graph = new DiGraph();
        ErrorListener errorListener = new ErrorListener();
        try {
            lexer = new DOTLexer(new ANTLRInputStream(is));
            lexer.addErrorListener(errorListener);

            parser = new DOTParser(new CommonTokenStream(lexer));
            parser.setErrorHandler(new ExceptionErrorStrategy());
            parser.addErrorListener(errorListener);

            ParseTree tree = parser.graph();
            ParseTreeWalker.DEFAULT.walk(new NodeListener(graph), tree);
            ParseTreeWalker.DEFAULT.walk(new EdgeListener(graph), tree);
        } catch (Throwable t) {
            String mErrMsg = errorListener.errorMessage;
            if (mErrMsg != null) throw new GraphParserException(mErrMsg, t);
            if (lexer != null) {
                mErrMsg = "at line " + lexer.getLine() + ":" + lexer.getCharPositionInLine();
                throw new GraphParserException(mErrMsg, t);
            }
            throw new GraphParserException(t);
        }

        String mErrMsg = errorListener.errorMessage;
        if (mErrMsg != null) throw new GraphParserException(mErrMsg);
        return graph;
    }

    private static String trimDoubleQuotes(String text) {
        int textLength = text.length();
        if (textLength >= 2 && text.charAt(0) == '"' && text.charAt(textLength - 1) == '"') {
            return text.substring(1, textLength - 1);
        }
        return text;
    }

    /*
     * populateAttributes helper
     */
    private static void populateAttributes(DOTParser.Attr_listContext ctx, Map<String, Object> attrs) {
        attrs.clear();
        if (ctx == null) return;

        for (A_listContext listCtx : ctx.a_list()) {
            String[] kv = {null, null};
            int i = 0;
            for (IdContext idCtx : listCtx.id()) {
                TerminalNode idObj = idCtx.ID();
                if (idObj == null) idObj = idCtx.NUMBER();
                if (idObj == null) idObj = idCtx.STRING();
                if (idObj == null) idObj = idCtx.HTML_STRING();
                String text = idObj.getText();
                if (idObj.getSymbol().getType() == DOTParser.STRING) {
                    text = trimDoubleQuotes(text);
                    text = text.replace("\\\"", "\"");
                }
                kv[i++] = text;
                if (i > 1) {
                    attrs.put(kv[0], kv[1]);
                    i = 0;
                }
            }
        }
    }

    /*
     * NodeListener
     */
    private static class NodeListener extends DOTBaseListener {
        private final DiGraph graph;
        Map<String, Object> nodeAttrs = new TreeMap<>();

        private NodeListener(DiGraph graph) {
            this.graph = graph;
        }

        @Override
        public void enterGraph(@NotNull DOTParser.GraphContext ctx) {
            if (ctx.id() != null) graph.setId(ctx.id().getText());
        }

        @Override
        public void exitGraph(@NotNull DOTParser.GraphContext ctx) {
            // no-op
        }

        @Override
        public void enterNode_id(@NotNull DOTParser.Node_idContext ctx) {
            String nodeId = ctx.id().getText();
            DiGraphNode node = graph.getNode(nodeId);
            if (node == null) {
                node = new DiGraphNode(nodeId);
                graph.setNode(nodeId, node);
            }
            node.setAttributes(nodeAttrs);
        }

        @Override
        public void exitNode_id(@NotNull DOTParser.Node_idContext ctx) {
            // no-op
        }

        @Override
        public void enterNode_stmt(@NotNull DOTParser.Node_stmtContext ctx) {
            populateAttributes(ctx.attr_list(), nodeAttrs);
        }

        @Override
        public void exitNode_stmt(@NotNull DOTParser.Node_stmtContext ctx) {
            nodeAttrs.clear();
        }
    }

    /*
     * EdgeListener
     */
    private static class EdgeListener extends DOTBaseListener {
        private final DiGraph graph;
        GraphCtx graphCtx = new GraphCtx(null);
        EdgeCtx edgeCtx;

        private EdgeListener(DiGraph graph) {
            this.graph = graph;
        }

        @Override
        public void enterNode_id(@NotNull DOTParser.Node_idContext ctx) {
            String nodeId = ctx.id().getText();
            graphCtx.addNode(nodeId);
        }

        @Override
        public void exitNode_id(@NotNull DOTParser.Node_idContext ctx) {
            // no-op
        }

        @Override
        public void enterSubgraph(@NotNull DOTParser.SubgraphContext ctx) {
            // enter new nested subgraph ctx
            graphCtx = new GraphCtx(graphCtx);
        }

        @Override
        public void exitSubgraph(@NotNull DOTParser.SubgraphContext ctx) {
            // leave nested ctx, pop previous parent ctx
            graphCtx = graphCtx.parent;
        }

        @Override
        public void enterEdge_stmt(@NotNull DOTParser.Edge_stmtContext ctx) {
            // enter new nested edge ctx
            edgeCtx = new EdgeCtx(edgeCtx, new GraphCtx(graphCtx), new GraphCtx(graphCtx), ctx.attr_list());
            graphCtx = edgeCtx.src; // point to src, next node/subgraph populates it
        }

        @Override
        public void exitEdge_stmt(@NotNull DOTParser.Edge_stmtContext ctx) {
            addEdges(edgeCtx.src.graph, edgeCtx.dest.graph, edgeCtx.attrs);

            // leave nested ctx, pop previous parent ctx
            edgeCtx = edgeCtx.parent;
            graphCtx = graphCtx.parent;
        }

        @Override
        public void enterEdgeop(@NotNull DOTParser.EdgeopContext ctx) {
            addEdges(edgeCtx.src.graph, edgeCtx.dest.graph, edgeCtx.attrs);

            if (edgeCtx.srcFlag) {
                // pointing to src, shift to populate dest
                graphCtx = edgeCtx.dest;
                edgeCtx.srcFlag = false;
            } else {
                // pointing to dest already, shift src/dest for next edgeop
                edgeCtx.src = edgeCtx.dest;
                edgeCtx.dest = new GraphCtx(graphCtx.parent);
                graphCtx = edgeCtx.dest;
            }
        }

        @Override
        public void exitEdgeop(@NotNull DOTParser.EdgeopContext ctx) {
            // no-op
        }

        private void addEdges(NodeIdSet srcSet, NodeIdSet destSet, Map<String, Object> attrs) {
            for (String src : srcSet) {
                for (String dest : destSet) {
                    addEdge(src, dest, attrs);
                }
            }
        }

        private void addEdge(String nodeId1, String nodeId2, Map<String, Object> attrs) {
            String edgeId = nodeId1 + "-" + nodeId2;
            DiGraphEdge edge = graph.getEdge(edgeId);
            if (edge == null) {
                DiGraphNode node1 = graph.getNode(nodeId1);
                DiGraphNode node2 = graph.getNode(nodeId2);
                edge = new DiGraphEdge(edgeId, node1, node2);
                graph.setEdge(edgeId, edge);
            }
            edge.setAttributes(attrs);
        }
    }

    /*
     * NodeIdSet
     */
    private static class NodeIdSet extends TreeSet<String> {
        public NodeIdSet() {
            super();
        }

        public NodeIdSet(NodeIdSet other) {
            super(other);
        }
    }

    /*
     * GraphCtx
     */
    private static class GraphCtx {
        GraphCtx parent;
        NodeIdSet graph = new NodeIdSet();

        GraphCtx(GraphCtx parent) {
            this.parent = parent;
        }

        void addNode(String nodeId) {
            graph.add(nodeId);
            if (parent != null) parent.addNode(nodeId);
        }

        public String toString() {
            return graph.toString();
        }
    }

    /*
     * EdgeCtx
     */
    private static class EdgeCtx {
        EdgeCtx parent;
        GraphCtx src;
        GraphCtx dest;
        boolean srcFlag = true;
        Map<String, Object> attrs = new TreeMap<>();

        EdgeCtx(EdgeCtx parent, GraphCtx src, GraphCtx dest, DOTParser.Attr_listContext ctx) {
            this.parent = parent;
            this.src = src;
            this.dest = dest;
            populateAttributes(ctx, attrs);
        }

        public String toString() {
            return src + "->" + dest;
        }
    }

    /*
     * ErrorListener
     */
    private static class ErrorListener extends BaseErrorListener {
        private String errorMessage;

        public void syntaxError(@NotNull Recognizer<?, ?> recognizer,
                                @Nullable Object offendingSymbol, int line,
                                int charPositionInLine, @NotNull String msg,
                                @Nullable RecognitionException e) {
            this.errorMessage = "at line " + line + ":" + charPositionInLine + " " + msg;
            throw e;
        }

        @Override
        public void reportAmbiguity(@NotNull Parser recognizer,
                                    @NotNull DFA dfa,
                                    int startIndex,
                                    int stopIndex,
                                    boolean exact,
                                    @Nullable BitSet ambigAlts,
                                    @NotNull ATNConfigSet configs) {
        }

        @Override
        public void reportAttemptingFullContext(@NotNull Parser recognizer,
                                                @NotNull DFA dfa,
                                                int startIndex,
                                                int stopIndex,
                                                @Nullable BitSet conflictingAlts,
                                                @NotNull ATNConfigSet configs) {
        }

        @Override
        public void reportContextSensitivity(@NotNull Parser recognizer,
                                             @NotNull DFA dfa,
                                             int startIndex,
                                             int stopIndex,
                                             int prediction,
                                             @NotNull ATNConfigSet configs) {
        }
    }

    /*
     * ExceptionErrorStrategy
     */
    private static class ExceptionErrorStrategy extends DefaultErrorStrategy {
        @Override
        public void recover(Parser recognizer, RecognitionException e) {
            throw e;
        }

        @Override
        public void reportInputMismatch(Parser recognizer, InputMismatchException e) throws RecognitionException {
            String msg = "mismatched input " + getTokenErrorDisplay(e.getOffendingToken());
            msg += " expecting one of " + e.getExpectedTokens().toString(recognizer.getTokenNames());
            RecognitionException ex = new RecognitionException(msg, recognizer, recognizer.getInputStream(), recognizer.getContext());
            ex.initCause(e);
            throw ex;
        }

        @Override
        public void reportMissingToken(Parser recognizer) {
            beginErrorCondition(recognizer);
            Token t = recognizer.getCurrentToken();
            IntervalSet expecting = getExpectedTokens(recognizer);
            String msg = "missing " + expecting.toString(recognizer.getTokenNames()) + " at " + getTokenErrorDisplay(t);
            throw new RecognitionException(msg, recognizer, recognizer.getInputStream(), recognizer.getContext());
        }
    }
}
