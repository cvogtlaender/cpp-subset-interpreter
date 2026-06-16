package de.cvogtlaender.interpreter;

import java.io.BufferedReader;
import java.io.FileReader;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import de.cvogtlaender.interpreter.ast.Program;
import de.cvogtlaender.interpreter.visitor.ASTBuildVisitor;

public class Main {

  public static void main(String[] args) {

    try (BufferedReader br = new BufferedReader(new FileReader(
        "C:\\Users\\gorpi\\Desktop\\features.cpp"))) {

      StringBuilder sb = new StringBuilder();
      String line = br.readLine();

      while (line != null) {
        sb.append(line);
        sb.append(System.lineSeparator());
        line = br.readLine();
      }

      String everything = sb.toString();
      MiniCppLexer lexer = new MiniCppLexer(CharStreams.fromString(everything));
      CommonTokenStream tokens = new CommonTokenStream(lexer);
      MiniCppParser parser = new MiniCppParser(tokens);

      ASTBuildVisitor astVisitor = new ASTBuildVisitor();
      Program program = (Program) parser.program().accept(astVisitor);

      System.out.println(program.toStringTree());
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }

}
