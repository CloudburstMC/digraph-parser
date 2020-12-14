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
import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.Assert;
import org.junit.Test;

import java.io.FileInputStream;
import java.util.Map;

public class ParserTest {
    public static void log(Object o) {
        System.out.println(o);
    }

    @Test
    public void testGraphParserUsage() {
        log("--- testGraphParserUsage ---");

        try (FileInputStream stream = new FileInputStream("src/test/resources/test1.dg")) {
            DiGraph graph = GraphParser.parse(stream);
            Map<String, DiGraphNode> nodes = graph.getNodes();
            Map<String, DiGraphEdge> edges = graph.getEdges();

            log("--- nodes:");
            for (DiGraphNode node : nodes.values()) {
                log(node.getId() + " " + node.getAttributes());
            }

            log("--- edges:");
            for (DiGraphEdge edge : edges.values()) {
                log(edge.getNode1().getId() + "->" + edge.getNode2().getId() + " " + edge.getAttributes());
            }
        } catch (Exception e) {
            e.printStackTrace();
            log(e);
        }
    }

//    @Test
    public void testGraphParser() {
        log("--- testGraphParser ---");

        String fname = "src/test/resources/test1.dg";
        log("--- " + fname);

        try (FileInputStream stream = new FileInputStream(fname)) {
            DiGraph graph = GraphParser.parse(stream);
            log(graph.getNodes());
            log(graph.getEdges());

            String nodeStr = "{a=Node-a{}, b=Node-b{}, bar=Node-bar{kk=vv, kkk=vvv}, baz=Node-baz{}, foo=Node-foo{}, n1=Node-n1{label=Node 1}, n2=Node-n2{label=Node 2}, n3=Node-n3{}, n4=Node-n4{}, n5=Node-n5{}, xxx=Node-xxx{k=v}, yyy=Node-yyy{k1=v1, k2=v2}}";
            String edgeStr = "{a-b=Edge-a-b{}, foo-bar=Edge-foo-bar{fbk=fbv, ek=ev}, foo-baz=Edge-foo-baz{ek=ev}, n1-n2=Edge-n1-n2{style=dotted, label=A dotted edge}, n1-n4=Edge-n1-n4{}, n2-n3=Edge-n2-n3{}, n3-n5=Edge-n3-n5{}, n4-n5=Edge-n4-n5{}}";

            Assert.assertEquals("testgraph", graph.getId());
            Assert.assertEquals(nodeStr, graph.getNodes().toString());
            Assert.assertEquals(edgeStr, graph.getEdges().toString());
        } catch (Exception e) {
            e.printStackTrace();
            log(e);
        }

        fname = "src/test/resources/test2.dg";
        log("--- " + fname);

        try (FileInputStream stream = new FileInputStream(fname)) {
            DiGraph graph = GraphParser.parse(stream);
            log(graph.getNodes());
            log(graph.getEdges());

            String nodeStr = "{01=Node-01{}, 02=Node-02{}, 03=Node-03{}, 04=Node-04{}, 05=Node-05{}, 06=Node-06{}, 07=Node-07{}, 1=Node-1{}, 11=Node-11{}, 12=Node-12{}, 13=Node-13{}, 14=Node-14{}, 15=Node-15{}, 16=Node-16{}, 17=Node-17{}, 2=Node-2{}, 21=Node-21{}, 22=Node-22{}, 23=Node-23{}, 24=Node-24{}, 25=Node-25{}, 26=Node-26{}, 27=Node-27{}, 3=Node-3{}, 4=Node-4{}, a=Node-a{}, a1=Node-a1{}, a2=Node-a2{}, aa=Node-aa{}, b=Node-b{}, b1=Node-b1{}, b2=Node-b2{}, bb=Node-bb{}, c=Node-c{}, c1=Node-c1{}, c2=Node-c2{}, cc=Node-cc{}, d=Node-d{}, d1=Node-d1{}, d2=Node-d2{}, dd=Node-dd{}, e=Node-e{}, ee=Node-ee{}, f=Node-f{}, ff=Node-ff{}, m1=Node-m1{}, m2=Node-m2{}, n1=Node-n1{}, n2=Node-n2{}}";
            String edgeStr = "{01-02=Edge-01-02{}, 02-03=Edge-02-03{}, 03-04=Edge-03-04{}, 03-05=Edge-03-05{}, 04-06=Edge-04-06{}, 05-06=Edge-05-06{}, 06-07=Edge-06-07{}, 1-2=Edge-1-2{}, 1-3=Edge-1-3{}, 1-4=Edge-1-4{}, 11-12=Edge-11-12{}, 12-13=Edge-12-13{}, 13-14=Edge-13-14{}, 13-15=Edge-13-15{}, 14-15=Edge-14-15{}, 14-16=Edge-14-16{}, 15-16=Edge-15-16{}, 16-17=Edge-16-17{}, 2-3=Edge-2-3{}, 2-4=Edge-2-4{}, 21-22=Edge-21-22{label=test}, 22-23=Edge-22-23{label=test}, 23-24=Edge-23-24{label=test}, 23-25=Edge-23-25{label=test}, 24-25=Edge-24-25{label=xxx}, 24-26=Edge-24-26{label=test}, 25-26=Edge-25-26{label=test}, 26-27=Edge-26-27{label=test}, 3-4=Edge-3-4{}, a-c=Edge-a-c{}, a-d=Edge-a-d{}, a1-b1=Edge-a1-b1{}, a1-c1=Edge-a1-c1{}, a1-m1=Edge-a1-m1{}, a1-n1=Edge-a1-n1{}, a2-b2=Edge-a2-b2{}, a2-c2=Edge-a2-c2{}, a2-m2=Edge-a2-m2{}, a2-n2=Edge-a2-n2{}, aa-cc=Edge-aa-cc{}, aa-dd=Edge-aa-dd{}, aa-ee=Edge-aa-ee{}, aa-ff=Edge-aa-ff{}, b-c=Edge-b-c{}, b-d=Edge-b-d{}, b1-c1=Edge-b1-c1{}, b1-d1=Edge-b1-d1{}, b2-c2=Edge-b2-c2{}, b2-d2=Edge-b2-d2{}, bb-cc=Edge-bb-cc{}, bb-dd=Edge-bb-dd{}, bb-ee=Edge-bb-ee{}, bb-ff=Edge-bb-ff{}, c-e=Edge-c-e{}, c-f=Edge-c-f{}, c1-d1=Edge-c1-d1{}, c1-m1=Edge-c1-m1{}, c1-n1=Edge-c1-n1{}, c2-d2=Edge-c2-d2{}, c2-m2=Edge-c2-m2{}, c2-n2=Edge-c2-n2{}, cc-ee=Edge-cc-ee{}, cc-ff=Edge-cc-ff{}, d-e=Edge-d-e{}, d-f=Edge-d-f{}, dd-ee=Edge-dd-ee{}, dd-ff=Edge-dd-ff{}, m1-d1=Edge-m1-d1{}, m2-d2=Edge-m2-d2{}, m2-n2=Edge-m2-n2{}, n1-d1=Edge-n1-d1{}, n2-d2=Edge-n2-d2{}}";

            Assert.assertEquals(nodeStr, graph.getNodes().toString());
            Assert.assertEquals(edgeStr, graph.getEdges().toString());
        } catch (Exception e) {
            e.printStackTrace();
            log(e);
        }

        fname = "src/test/resources/test3.dg";
        log("--- " + fname);

        try (FileInputStream stream = new FileInputStream(fname)) {
            DiGraph graph = GraphParser.parse(stream);
            log(graph.getNodes());
            log(graph.getEdges());

            String nodeStr = "{01=Node-01{}, 02=Node-02{}, 03=Node-03{}, 04=Node-04{}, 05=Node-05{}, 06=Node-06{}, 07=Node-07{}, 08=Node-08{}, 11=Node-11{}, 12=Node-12{}, 13=Node-13{}, 15=Node-15{}, 16=Node-16{}, 17=Node-17{}, 18=Node-18{}, 21=Node-21{}, 22=Node-22{}, 23=Node-23{}, 24=Node-24{}, 25=Node-25{}, 26=Node-26{}, 27=Node-27{}}";
            String edgeStr = "{01-02=Edge-01-02{}, 01-03=Edge-01-03{}, 01-04=Edge-01-04{}, 01-05=Edge-01-05{}, 01-06=Edge-01-06{}, 01-07=Edge-01-07{}, 02-03=Edge-02-03{}, 02-04=Edge-02-04{}, 02-05=Edge-02-05{}, 02-06=Edge-02-06{}, 02-07=Edge-02-07{}, 03-08=Edge-03-08{}, 04-05=Edge-04-05{}, 04-06=Edge-04-06{}, 04-08=Edge-04-08{}, 05-08=Edge-05-08{}, 06-08=Edge-06-08{}, 07-08=Edge-07-08{}, 11-12=Edge-11-12{}, 11-15=Edge-11-15{}, 11-16=Edge-11-16{}, 11-17=Edge-11-17{}, 11-18=Edge-11-18{}, 12-13=Edge-12-13{}, 12-15=Edge-12-15{}, 12-16=Edge-12-16{}, 12-17=Edge-12-17{}, 12-18=Edge-12-18{}, 13-15=Edge-13-15{}, 13-16=Edge-13-16{}, 13-17=Edge-13-17{}, 13-18=Edge-13-18{}, 15-16=Edge-15-16{}, 21-22=Edge-21-22{}, 21-25=Edge-21-25{}, 21-26=Edge-21-26{}, 21-27=Edge-21-27{}, 22-23=Edge-22-23{}, 22-25=Edge-22-25{}, 22-26=Edge-22-26{}, 22-27=Edge-22-27{}, 23-24=Edge-23-24{}, 23-25=Edge-23-25{}, 23-26=Edge-23-26{}, 23-27=Edge-23-27{}, 24-25=Edge-24-25{}, 24-26=Edge-24-26{}, 24-27=Edge-24-27{}, 25-26=Edge-25-26{}, 26-27=Edge-26-27{}}";

            Assert.assertEquals(nodeStr, graph.getNodes().toString());
            Assert.assertEquals(edgeStr, graph.getEdges().toString());
        } catch (Exception e) {
            e.printStackTrace();
            log(e);
        }
    }

    @Test
    public void testAntlrParser() throws Exception {
        log("--- testAntlrParser ---");

        try {
            String fname = "src/test/resources/test1.dg";
            log("--- " + fname);

            DOTLexer lexer = new DOTLexer(new ANTLRFileStream(fname));
            DOTParser parser = new DOTParser(new CommonTokenStream(lexer));
            parser.setErrorHandler(new BailErrorStrategy());
            ParseTree tree = parser.graph();
            log(tree.toStringTree(parser));
            ParseTreeWalker.DEFAULT.walk(new DOTBaseListener(), tree);
        } catch (Exception e) {
            e.printStackTrace();
            log(e);
        }

        try {
            String fname = "src/test/resources/test2.dg";
            log("--- " + fname);

            DOTLexer lexer = new DOTLexer(new ANTLRFileStream(fname));
            DOTParser parser = new DOTParser(new CommonTokenStream(lexer));
            parser.setErrorHandler(new BailErrorStrategy());
            ParseTree tree = parser.graph();
            log(tree.toStringTree(parser));
            ParseTreeWalker.DEFAULT.walk(new DOTBaseListener(), tree);
        } catch (Exception e) {
            e.printStackTrace();
            log(e);
        }

        try {
            String fname = "src/test/resources/test3.dg";
            log("--- " + fname);

            DOTLexer lexer = new DOTLexer(new ANTLRFileStream(fname));
            DOTParser parser = new DOTParser(new CommonTokenStream(lexer));
            parser.setErrorHandler(new BailErrorStrategy());
            ParseTree tree = parser.graph();
            log(tree.toStringTree(parser));
            ParseTreeWalker.DEFAULT.walk(new DOTBaseListener(), tree);
        } catch (Exception e) {
            e.printStackTrace();
            log(e);
        }
    }
}
